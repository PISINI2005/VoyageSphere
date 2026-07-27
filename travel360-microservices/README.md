# Travel360 — Microservices Architecture

This workspace is the microservices conversion of the `travel360endpoints-3` monolith.
The monolith (16 controllers, 15 services, 17 entities, JWT security, AOP audit/logging,
centralized exception handling) has been decomposed into **4 business microservices** plus
**3 infrastructure services**, wired together with Spring Cloud (Config Server, Eureka, Gateway, OpenFeign).

Business logic, naming, layering, validation, Swagger annotations and security rules are preserved.
The only structural changes are the ones a database-per-service split forces: JPA cross-aggregate
associations became scalar foreign keys + Feign lookups, and the cross-cutting audit/notification
writes became Feign calls.

---

## 1. High-Level Architecture

### Infrastructure services
| Service | Port | Responsibility |
|---|---|---|
| **discovery-server** | 8761 | Netflix Eureka registry. Every service registers here; Feign + Gateway resolve `lb://NAME`. |
| **config-server** | 8888 | Spring Cloud Config Server (native backend). Serves externalized config to all services from `config-server/src/main/resources/config/`. |
| **api-gateway** | 8080 | Spring Cloud Gateway. The single public entry point; routes each path prefix to the owning service by Eureka name. Routes are config-driven. |

### Business microservices
| Service | Port | Database | Owns | Talks to (Feign) |
|---|---|---|---|---|
| **user-service** | 8081 | `travel360_user_db` | `User`, authentication, **JWT minting** | notification-service (audit) |
| **catalog-service** | 8082 | `travel360_catalog_db` | `Partner, Flight, FlightSeat, Hotel, HotelRoom, Transport, TransportSeat, TravelPackage, PackageItinerary`, **Search** | notification-service (audit) |
| **booking-service** | 8083 | `travel360_booking_db` | `Booking, Passenger, Itinerary, KpiReport, Invoice, Payment` | user, catalog, notification |
| **notification-service** | 8085 | `travel360_notification_db` | `Notification, AuditLog` (central sink) | — (provider only) |

### Why these boundaries
- **Invoice & Payment live inside booking-service** (not a separate payment-service): the monolith
  creates and refunds invoices/payments *inside* `BookingServiceImpl`'s `@Transactional` methods.
  Keeping them co-located preserves that atomic refund logic instead of splitting one transaction
  across a distributed boundary.
- **Catalog is one service**: `Partner` is the parent of `Flight/Hotel/Transport/Package`, and
  `Search` composes all of them. Splitting these would create heavy cross-service chatter for
  every availability check, so they form a single "inventory" bounded context. `Search` stays an
  in-process aggregator (no Feign).
- **Notification + AuditLog together**: both are cross-cutting write sinks every service feeds.

### Communication flow
```
Client ──HTTP──> api-gateway (8080)
                     │  (path predicate → lb://SERVICE via Eureka)
       ┌─────────────┼─────────────────────────────┐
       ▼             ▼                               ▼
 user-service   catalog-service                booking-service
   (8081)          (8082)                          (8083)
       │             │                               │  Feign:
       │             │                               ├─ UserClient        → user-service     (validate user)
       │             │                               ├─ CatalogClient     → catalog-service  (flight/hotel/transport/package + seats/price/status)
       └─── audit ───┴──────── audit ────────────────┼─ NotificationClient→ notification-service (send notification)
                                                      └─ AuditClient       → notification-service (record audit)
                                          notification-service (8085)
```
All services register with **Eureka (8761)** and load config from **Config Server (8888)**.
The JWT is minted by user-service and propagated end-to-end: gateway → service → (Feign interceptor) → downstream service.

---

## 2. Project Structure
```
travel360-microservices/
├── pom.xml                         # aggregator: Spring Boot 3.5.14 parent + Spring Cloud 2025.0.2 BOM
├── config-server/                  # @EnableConfigServer (8888)
│   └── src/main/resources/config/  # CENTRALIZED CONFIG (served to all services)
│       ├── application.yml         #   shared: eureka, jpa, SECRET_KEY, feign timeouts
│       ├── user-service.yml        #   port 8081 + datasource
│       ├── catalog-service.yml     #   port 8082 + datasource
│       ├── booking-service.yml     #   port 8083 + datasource
│       ├── notification-service.yml#   port 8085 + datasource
│       └── api-gateway.yml         #   port 8080 + ALL ROUTES
├── discovery-server/               # @EnableEurekaServer (8761)
├── api-gateway/                    # Spring Cloud Gateway (8080), routes from config-server
├── user-service/                   # @SpringBootApplication @EnableFeignClients
│   └── src/main/java/com/cts/
│       ├── controller/  service/  serviceimpl/  repository/
│       ├── entity/  dto/  mapper/  enums/  exception/
│       ├── config/      # JWTUtil, JWTFilter, SecurityConfig, AuthenticatedUserProvider, FeignClientConfig, Swagger
│       ├── client/      # AuditClient (Feign → notification-service)
│       ├── aspect/  constants/  util/
│       └── UserServiceApplication.java
├── catalog-service/                # same layered structure (+ SearchController, 8 entities)
├── booking-service/                # same + client/ {UserClient, CatalogClient, NotificationClient, AuditClient}
└── notification-service/           # Notification + AuditLog, exposes write endpoints for inter-service calls
```
Every business service has the full **controller → service → serviceimpl → repository → entity** stack
plus **mapper**, **dto**, **enums**, **exception**, and the ported **config/aspect** cross-cutting layer.

---

## 3. Where to find each required piece of code
| Concern | File |
|---|---|
| Sample controller | `user-service/.../controller/UserController.java`, `booking-service/.../controller/BookingController.java` |
| Service layer | `*/serviceimpl/*ServiceImpl.java` (interfaces in `*/service/`) |
| Feign client examples | `booking-service/.../client/{UserClient,CatalogClient,NotificationClient,AuditClient}.java` |
| Feign auth propagation | `*/config/FeignClientConfig.java` (RequestInterceptor forwards `Authorization`) |
| Config Server setup | `config-server/.../ConfigServerApplication.java` + `config-server/src/main/resources/application.yml` |
| Config Client setup | every service's `src/main/resources/application.yml` (`spring.config.import: configserver:...`) |
| Centralized config | `config-server/src/main/resources/config/*.yml` |
| Gateway routing | `config-server/src/main/resources/config/api-gateway.yml` |
| Eureka registry | `discovery-server/.../DiscoveryServerApplication.java` |

---

## 4. Key Transformations (monolith → microservices)

### a. Cross-aggregate JPA associations → scalar IDs + Feign
`Booking` in the monolith held `@ManyToOne User/Flight/Hotel/Transport/TravelPackage`. Those entities
now live in other databases, so the associations became scalar foreign keys:
```java
// before (monolith)            // after (booking-service)
@ManyToOne private User user;        private Long userId;
@ManyToOne private Flight flight;    private Long flightId;   // @Column(name="flight_id")
@ManyToOne private Hotel hotel;      private Long hotelId;
@ManyToOne private Transport ...;    private Long transportId;
@ManyToOne private TravelPackage ..; private Long packageId;
// kept local: @ManyToOne Itinerary, @OneToMany List<Passenger>
```

### b. Internal repository/service calls → Feign calls
```java
// BookingServiceImpl — before                    // after
userRepo.findById(dto.getUserId())                userClient.getUser(dto.getUserId())
flightRepo.findById(dto.getFlightId())            catalogClient.getFlight(dto.getFlightId())
hotelrepo.findById(dto.getHotelId())              catalogClient.getHotel(dto.getHotelId())
transportRepo.findById(dto.getTransportId())      catalogClient.getTransport(dto.getTransportId())
packageRepo.findById(dto.getPackageId())          catalogClient.getPackage(dto.getPackageId())
notificationService.sendNotification(user,...)    notificationClient.send(NotificationRequestDTO)
auditLogService.logAction(..., user, ...)         auditClient.record(AuditLogRequestDTO)   // best-effort
```
`Invoice`/`Payment` saves stay **local** (same DB as Booking → refund transaction stays atomic).

### c. Availability queries stay local; catalog data comes over Feign
Seat/room/slot counts query the *booking* table, so they stay in booking-service — only the JPQL
field paths changed from associations to scalars:
```sql
-- before: WHERE b.flight.flightId = :flightId       -- after: WHERE b.flightId = :flightId
-- before: WHERE b.hotel.hotelId   = :hotelId         -- after: WHERE b.hotelId   = :hotelId
-- before: WHERE b.travelPackage.packageId = :pkgId   -- after: WHERE b.packageId = :pkgId
-- before: WHERE b.transport.transportId = :tId       -- after: WHERE b.transportId = :tId
```
`totalSeats`, `price` and `status` (previously read off the JPA entity) now come from the Catalog
Feign response DTOs (`FlightResponseDTO.getSeats()`, etc.).

### d. Cross-cutting AuditLog/Notification → notification-service
`Notification` and `AuditLog` dropped their `@ManyToOne User` for a scalar `userId` (notification-service
has no User table). Each consumer keeps the original `AuditLogService.logAction(...)` / `sendNotification(...)`
call-site but the implementation now POSTs to notification-service via Feign, wrapped in try/catch so a
logging/notification failure never breaks the business flow (preserves the monolith's `@Async` best-effort intent).

### e. New internal endpoints added to support the split
- `GET /api/v1/users/{userId}` (user-service) — booking-service fetches the user it used to `findById`.
- `GET /api/v1/transports/{id}` (catalog-service) — the monolith had no transport-by-id; booking needs it.
- `POST /api/v1/notifications` and `POST /api/v1/auditLogs` (notification-service) — write endpoints for inter-service calls.

---

## 5. Build & Run

### Build
```bash
mvn clean compile          # all 8 modules
```
> Verified: the full reactor compiles cleanly (Spring Boot 3.5.14, Spring Cloud 2025.0.2, Java 17).
> In a network with TLS interception, add:
> `-Dmaven.resolver.transport=wagon -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true`

### Run (in order)
1. `discovery-server`  (8761) — start first
2. `config-server`     (8888)
3. `api-gateway`       (8080)
4. `user-service` (8081), `catalog-service` (8082), `notification-service` (8085), `booking-service` (8083) — any order
```bash
cd <module> && mvn spring-boot:run
```
Each needs MySQL on `localhost:3306` (DBs auto-create via `createDatabaseIfNotExist=true`).
Override `DB_USERNAME`/`DB_PASSWORD`/`SECRET_KEY` via environment. All external calls go through
`http://localhost:8080` (the gateway); Swagger UI is available per-service at `/swagger-ui.html`.

---

## 6. Known behavioral notes
- **booking list responses**: convenience fields that used to be read off the in-graph entity
  (`flightNumber`, `hotelName`, `email` on *list* endpoints) are left null to avoid an N+1 Feign
  fan-out the monolith never incurred. The owning IDs (`flightId`, `hotelId`, `userId`, …) are always
  present, and the per-booking **create** responses still populate them (the create flow already fetches
  the Catalog/User DTO). This is the one place the database-per-service split trades a denormalized field
  for performance; revisit with a batch/aggregation endpoint if those names are needed on list views.
