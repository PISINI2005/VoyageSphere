# 📖 Travel360: The Ultimate Technical Encyclopedia & Developer's Handbook

This document is the definitive, exhaustive technical reference for the **Travel360 Monolithic Project**. It is designed to take a developer from zero knowledge to a complete understanding of the system's internal mechanics, architectural decisions, and business logic.

---

## 📑 Table of Contents
1. [The Big Picture: Architectural Philosophy](#-1-the-big-picture-architectural-philosophy)
2. [Layered Architecture: The Deep Dive](#-2-layered-architecture-the-deep-dive)
3. [Identity & Security: The JWT Ecosystem](#-3-identity--security-the-jwt-ecosystem)
4. [Data Integrity: The Validation Engine](#-4-data-integrity-the-validation-engine)
5. [Database Mastery: Persistence & Concurrency](#-5-database-mastery-persistence--concurrency)
6. [The Booking Engine: Step-by-Step Algorithms](#-6-the-booking-engine-step-by-step-algorithms)
7. [The Cancellation & Refund State Machine](#-7-the-cancellation--refund-state-machine)
8. [Performance Optimization: The KPI Report Logic](#-8-performance-optimization-the-kpi-report-logic)
9. [Resilient Error Handling: The Global Handler](#-9-resilient-error-handling-the-global-handler)
10. [The Invisible Eye: AOP & Audit Logging](#-10-the-invisible-eye-aop--audit-logging)
11. [Comprehensive Entity & DTO Mapping](#-11-comprehensive-entity--dto-mapping)
12. [Troubleshooting, FAQ & Developer Notes](#-12-troubleshooting-faq--developer-notes)

---

## 🏗️ 1. The Big Picture: Architectural Philosophy

### 1.1 Why a Monolith?
The Travel360 project is implemented as a **Monolith**. In modern software, you often hear about "Microservices," but for this specific domain, a monolith was chosen for several key reasons:
- **Transactional Integrity:** Booking a trip often requires updating a `Booking` record, creating an `Invoice`, and updating `FlightSeat` availability. In a monolith, this is one single database transaction (`@Transactional`). If any part fails, everything rolls back. In microservices, you would need complex "Saga Patterns" or "Two-Phase Commits."
- **Reduced Latency:** There are no network calls between services. Everything happens in the same JVM memory.
- **Simplified Deployment:** One artifact (JAR file), one pipeline, one database.

### 1.2 Business Domain Overview
The system is designed to manage the "Travel Lifecycle":
`Search` $\rightarrow$ `Booking` $\rightarrow$ `Payment` $\rightarrow$ `Notification` $\rightarrow$ `Travel` $\rightarrow$ `Audit`.
It handles four primary travel modes: **Flights, Hotels, Transport (Bus), and Holiday Packages**.

---

## 🍰 2. Layered Architecture: The Deep Dive

The project uses a strict **N-Tier Architecture**. This ensures that a change in the database schema doesn't break the API, and a change in the API doesn't require rewriting the database logic.

### 2.1 The Presentation Layer (Controllers)
**Package:** `com.cts.controller`
Controllers are the "API Surface." They are annotated with `@RestController`.
- **Responsibility:** They handle HTTP mapping (`@PostMapping`, `@GetMapping`), trigger the `@Valid` check on DTOs, and call the service layer.
- **Rule of Thumb:** Controllers should be "thin." They should contain **zero** business logic. If you see an `if` statement checking a business rule in a controller, it's a bug; that logic belongs in the Service.

### 2.2 The Business Layer (Services)
**Package:** `com.cts.service` (Interfaces) and `com.cts.serviceimpl` (Implementations).
This is the "Brain" of the application. 
- **Interface-Based Design:** We use interfaces (e.g., `BookingService`) and implementations (`BookingServiceImpl`). This allows for "Dependency Inversion," making it easy to swap implementations or create mocks for testing.
- **Transaction Management:** Most methods are marked `@Transactional`. This tells Spring: "Start a database transaction here. If the method finishes successfully, commit all changes. If an exception is thrown, undo everything (rollback)."

### 2.3 The Data Access Layer (Repositories)
**Package:** `com.cts.repository`
Repositories use **Spring Data JPA**.
- **Abstraction:** Instead of writing long SQL strings, we extend `JpaRepository<Entity, ID>`. This gives us `save()`, `delete()`, and `findById()` for free.
- **Custom Queries:** For complex logic (like KPI stats), we use `@Query` with JPQL to perform aggregations (SUM, COUNT) directly on the database server for maximum speed.

### 2.4 The Data Model (Entities vs. DTOs)
- **Entities (`com.cts.entity`):** The "Source of Truth." These map 1:1 to MySQL tables. They use JPA annotations like `@Entity`, `@Table`, `@Id`, and `@ManyToOne`.
- **DTOs (`com.cts.dto`):** "Data Transfer Objects." These are used to communicate with the client. 
    - **Why?** To avoid "Over-posting" (where a user sends a `role=ADMIN` field in a request to make themselves an admin) and "Information Leakage" (where the server accidentally sends the user's hashed password in a response).

---

## 🔐 3. Identity & Security: The JWT Ecosystem

### 3.1 The Stateless Identity Model
Travel360 does not use `HttpSession`. It is **stateless**. The server does not "remember" who you are. Instead, the client must prove their identity with every single request using a **JWT (JSON Web Token)**.

### 3.2 Detailed Token Anatomy
A JWT is composed of three parts separated by dots: `Header.Payload.Signature`.
- **Header:** Contains the algorithm (e.g., HS256).
- **Payload (The Claims):** This is the most important part. It contains:
    - `sub` (Subject): The user's email.
    - `role`: The user's authority (e.g., `ADMIN`).
    - `userId`: The database primary key of the user.
    - `exp`: Expiration timestamp.
- **Signature:** A hash of the header and payload using a **secret key** known only to the server. If a user changes the `role` from `USER` to `ADMIN`, the signature will no longer match, and the server will reject the token.

### 3.3 The Request Interception Flow (`JWTFilter`)
Every request goes through the `JWTFilter` before it ever reaches a Controller:
1. **Extraction:** It looks for the `Authorization` header and strips the `"Bearer "` prefix.
2. **Verification:** `JWTUtil.validateToken()` checks the signature and the expiration date.
3. **Contextualization:** If valid, it creates a `UsernamePasswordAuthenticationToken`.
4. **SecurityContextHolder:** This token is placed in the `SecurityContextHolder`. This is a `ThreadLocal` storage, meaning the user's identity is attached to the specific thread processing that request.

### 3.4 The `AuthenticatedUserProvider` Optimization
A common mistake in Spring Boot apps is to query the database to get the user object on every request. 
**Travel360's Optimized Approach:**
The `AuthenticatedUserProvider` reads the `userId` and `role` **directly from the JWT claims** inside the token.
- **Scenario:** User requests `/api/my-bookings`.
- **Standard way:** Token $\rightarrow$ Email $\rightarrow$ `SELECT * FROM users WHERE email = ?` $\rightarrow$ User ID.
- **Travel360 way:** Token $\rightarrow$ User ID.
- **Performance Gain:** Zero database hits for identity resolution.

---

## 🛡️ 4. Data Integrity: The Validation Engine

Validation is the first line of defense against bad data and security vulnerabilities.

### 4.1 The Syntactic Layer (DTO Annotations)
We use **Jakarta Bean Validation**. These are "declarative" rules.

| Annotation | Technical Effect | Business Reason |
| :--- | :--- | :--- |
| `@NotNull` | Throws exception if field is null. | Mandatory fields (e.g., `flightId`). |
| `@NotBlank` | Checks `length > 0` and not just spaces. | Ensures names/emails are real. |
| `@Future` | Validates `date > now`. | No bookings for past dates. |
| `@Min(1)` | Validates `value >= 1`. | No zero or negative seat bookings. |
| `@Max(10)` | Validates `value <= 10`. | Prevents mass-booking bots. |
| `@Size(max=50)`| Limits string length. | Prevents database overflow/buffer attacks. |
| `@NotEmpty` | Checks that a List is not null and not empty. | At least one passenger per booking. |

### 4.2 The Semantic Layer (Service Validation)
Annotations can't check the database. That's where **Service Validation** comes in.

**Example: The Passenger Validation Algorithm**
When `createFlightBooking` is called:
1. **Fetch Profiles:** The system takes the `List<Long> passengerProfileIds` and queries the database.
2. **Verify Ownership:** For each profile found, it checks: `profile.getUser().getUserId() == tokenUserId`. If a user tries to add a passenger profile belonging to someone else, it's a security breach.
3. **Capacity Check:** It ensures that `passengerProfileIds.size() == dto.getUnits()`. You can't book 5 seats but only provide 2 passengers.
4. **Status Check:** It verifies that the passenger profiles are in an `ACTIVE` status.

---

## 🗄️ 5. Database Mastery: Persistence & Concurrency

### 5.1 Entity Relationship Mapping (The Glue)
The project uses complex JPA relationships:
- **ManyToOne:** (e.g., `Booking` $\rightarrow$ `User`). Many bookings can belong to one user.
- **OneToMany:** (e.g., `Booking` $\rightarrow$ `Passenger`). One booking has multiple passengers.
- **Cascading:** `cascade = CascadeType.ALL` on the passengers list in `Booking`. This means when we save a `Booking`, all its `Passenger` records are automatically saved too.

### 5.2 Solving the "Race Condition" with Pessimistic Locking
In high-traffic travel apps, "Overbooking" is the biggest risk.
**The Problem:**
1. User A reads: "1 seat left."
2. User B reads: "1 seat left."
3. User A saves booking.
4. User B saves booking.
5. **Result:** 2 people have the same seat.

**The Travel360 Solution:**
We use **Pessimistic Write Locking** in `FlightRepository`:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT f FROM Flight f WHERE f.flightId = :id")
Optional<Flight> findByIdForUpdate(Long id);
```
**How it works at the SQL level:**
This generates a `SELECT ... FOR UPDATE` query. 
- When User A calls this, MySQL **locks the row**.
- When User B calls this, MySQL makes User B **wait** until User A's transaction is either committed or rolled back.
- This guarantees that the seat count is always accurate.

---

## ✈️ 6. The Booking Engine: Step-by-Step Algorithms

### 6.1 Flight Booking Algorithm
The logic in `FlightBookingServiceImpl.createFlightBooking()` follows these exact steps:
1. **Auth Check:** `authUser.assertCanActAs(userId)` ensures the user is booking for themselves.
2. **Flight Fetch:** Fetch the flight using the **Pessimistic Lock**.
3. **Status Validation:** Ensure flight status is `SCHEDULED`.
4. **Seat Type Resolution:** Find the `FlightSeat` entity that matches the requested `SeatType` (Economy, Business, etc.).
5. **Availability Check:** 
   - `totalSeats` (from `FlightSeat` entity).
   - `bookedSeats` (from `bookingRepo.getBookedSeats(...)` query).
   - `available = totalSeats - bookedSeats`.
   - If `available < requestedUnits`, throw `InsufficientAvailabilityException`.
6. **Entity Creation:** 
   - Build the `Booking` entity.
   - Link it to the `User` and `Flight`.
7. **Passenger Linking:** Use `PassengerResolver` to create `Passenger` entities based on the profiles.
8. **Financials:** Generate an `Invoice` record with `PaymentStatus.PENDING`.
9. **External Triggers:** Call `NotificationService` to send a confirmation.
10. **Audit:** Call `AuditLogService` to record `CREATE_BOOKING`.

---

## 🔄 7. The Cancellation & Refund State Machine

Cancellations are not just "deleting" a record; they are a state transition.

### 7.1 The State Transition Map
- **PENDING $\rightarrow$ CANCELLED:** Simple. No money involved.
- **CONFIRMED $\rightarrow$ CANCELLED:** Complex. Requires financial refund.

### 7.2 Full Cancellation Logic (`deleteBooking`)
1. **Eligibility:** Check if `travelDate` is in the past. If yes, block cancellation.
2. **The Refund Calculation:**
   - The `BookingHelper` class uses a "Time-to-Travel" formula.
   - *Example:* If `daysUntilTravel > 30` $\rightarrow$ 100% refund. If `daysUntilTravel < 7` $\rightarrow$ 10% refund.
3. **The Ledger Entry:**
   - We **never** just delete a payment. We create a new `Payment` record with `status = REFUNDED`. This provides a full audit trail for accountants.
4. **Invoice Update:** The original `Invoice` status is updated to `REFUNDED`.

### 7.3 Partial Passenger Cancellation Logic (`cancelPassenger`)
This is a a surgical operation on a booking.
1. **Eligibility:** Only for `FLIGHT` and `TRANSPORT`.
2. **The la- la- la Logic (The "Last Man" Rule):**
   - If the booking has 5 passengers and you remove 1, the `Booking` entity's `units` is reduced to 4, and the `amount` is reduced by 1 seat price.
   - If the booking has 1 passenger and you remove them, the system **automatically triggers a full `deleteBooking()`**.
3. **Partial Refund:**
   - Calculate refund for 1 seat only.
   - Update `Invoice` to `PARTIALLY_REFUNDED`.
   - Create a `Payment` record for the partial refund amount.

---

## 📈 8. Performance Optimization: The KPI Report Logic

KPI reports are the most resource-intensive part of the system because they aggregate thousands of rows.

### 8.1 The "N+1" Performance Trap
In a naive system, generating a yearly report (12 months) would look like this:
- 12 calls to `getMonthlyRevenue()`.
- 12 calls to `getMonthlyBookingCount()`.
- **Total:** 24 database round-trips. This is slow and causes "database chatter."

### 8.2 The "Bulk Aggregation" Strategy
`KpiReportServiceImpl` uses a high-performance bulk strategy:
1. **The Revenue Bulk Query:** `paymentRepo.getMonthlyMoneyStats(year)`.
   - This executes a single SQL query with a `GROUP BY month` clause.
   - It returns a list of 12 `MonthlyKpiStatsDTO` objects in one go.
2. **The Count Bulk Query:** `bookingRepo.getMonthlyCounts(year)`.
   - Similar `GROUP BY month` query for booking totals.
3. **In-Memory Merge:** The service iterates from 1 to 12 and matches the revenue stats with the count stats using Java Streams.
4. **Computational Result:** 
   - **Round-trips reduced from 24 $\rightarrow$ 2.**
   - **Database load reduced by ~90%.**

---

## 🚨 9. Resilient Error Handling: The Global Handler

The `GlobalExceptionHandler` is the "Safety Net" of the application.

### 9.1 The a-ha! Moment: Enum Reflection
A major technical achievement in this project is how it handles invalid enums.

**The Technical Problem:** 
When a user sends `roomType: "GOLDEN_SUITE"`, Jackson (the JSON parser) throws an `InvalidFormatException`. By default, this is a messy technical error.

**The Elegant Solution:**
1. **Catch the Exception:** The handler catches `InvalidFormatException`.
2. **Type Reflection:** It uses `ife.getTargetType()` to find out which Enum was expected (e.g., `HotelRoomType.class`).
3. **Constant Extraction:** It calls `targetType.getEnumConstants()`. This is a Java Reflection API call that returns an array of all possible values defined in that Enum.
4. **Dynamic Message:** It constructs a message: `"Invalid value 'GOLDEN_SUITE'. Accepted values are: [STANDARD, DELUXE, SUITE]."`

### 9.2 Error Mapping Table
| Exception | Trigger | HTTP Status | User-Facing Message |
| :--- | :--- | :--- | :--- |
| `MethodArgumentNotValidException`| `@NotNull` failed | 400 | "Validation failed: [field] is required" |
| `ResourceNotFoundException` | `findById().orElseThrow()` | 404 | "Booking not found" |
| `InsufficientAvailabilityException`| `availableSeats < units` | 409 | "Not enough seats available" |
| `AccessDeniedException` | `assertCanActAs()` failed | 403 | "You can only access your own resources" |
| `InvalidFormatException` | Wrong Enum Value | 400 | "Invalid value... Accepted values: [...]" |

---

## 👁️ 10. The Invisible Eye: AOP & Audit Logging

Audit logging is critical for financial systems. However, adding `auditLogService.log(...)` to every single method in the project would make the code unreadable (this is called "Cross-Cutting Concern Pollution").

### 10.1 Aspect-Oriented Programming (AOP)
We use **AOP** to separate the logging logic from the business logic.

**The "Aspect" Concept:**
An Aspect is like a "wrapper" that sits around your methods. It doesn't live *inside* the method; it lives *around* it.

**The Execution Flow:**
1. **Pointcut:** We define a "Pointcut" that tells Spring: "Watch every method inside the `com.cts.serviceimpl` package."
2. **Around Advice:** When a method in that package is called, the `LoggingAspect` triggers.
3. **Pre-Processing:** The aspect captures the method name and the arguments the user sent.
4. **The Execution:** The actual business method runs.
5. **Post-Processing:** 
   - If it succeeds $\rightarrow$ The aspect captures the return value.
   - If it fails $\rightarrow$ The aspect captures the exception.
6. **Persistence:** The `AuditLogService` saves this data into the `audit_log` table.

**Result:** The `FlightBookingServiceImpl` doesn't need 100 lines of logging code. The logging happens "in the background" automatically.

---

## 🗺️ 11. Comprehensive Entity & DTO Mapping

To prevent database leakage, we use a strict mapping pattern.

### 11.1 The Data Transformation Path
`Request (JSON)` $\rightarrow$ `DTO` $\rightarrow$ `Entity` $\rightarrow$ `Repository` $\rightarrow$ `Database`

**Example: Flight Booking**
1. **`BookingFlightDTO`:** Contains `flightId` (Long).
2. **Mapping:** The service uses `flightRepo.findById(dto.getFlightId())` to turn that ID into a `Flight` **Entity**.
3. **`Booking` Entity:** The `Booking` object is built using the `Flight` entity as a relationship.
4. **`BookingFlightResponseDTO`:** Before returning to the user, the `BookingMapper` converts the `Booking` entity back into a DTO, removing sensitive internal IDs and formatting the price.

---

## 🛠️ 12. Troubleshooting, FAQ & Developer Notes

### 12.1 Common Errors & Solutions
**Error:** `LazyInitializationException`
- **Cause:** You tried to access a list (like `booking.getPassengers()`) outside of a `@Transactional` method.
- **Fix:** Add `@Transactional` to the service method or use `JOIN FETCH` in the repository.

**Error:** `AccessDeniedException`
- **Cause:** The `userId` in the JWT doesn't match the `userId` of the resource you are trying to edit.
- **Fix:** Check if you are using the correct token or if the user has the `ADMIN` role.

**Error:** `DataIntegrityViolationException`
- **Cause:** You tried to create a user with an email that already exists (Unique constraint).
- **Fix:** The `GlobalExceptionHandler` catches this and returns a `409 Conflict` with the message "Email already exists."

### 12.2 Developer Tips for Extension
- **Adding a New Travel Mode:** 
    1. Create a new Entity (e.g., `Cruise`).
    2. Create a corresponding `BookingType` enum.
    3. Implement a new `CruiseBookingService`.
    4. Add the logic to `BookingServiceImpl` to route requests.
- **Changing Refund Rules:** 
    - Do not touch the `BookingServiceImpl`. Go directly to `BookingHelper.java` and modify the percentage logic.

### 12.3 Final Complexity Summary
- **Time Complexity:** Most operations are $O(1)$ or $O(\log N)$ due to Primary Key indexing in MySQL.
- **Space Complexity:** Minimal, as we use DTOs to keep the memory footprint of each request small.
- **Concurrency:** Guaranteed by Pessimistic Locking on inventory rows.
