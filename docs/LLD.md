# 🛠️ Exhaustive Low-Level Design (LLD): Travel360 Ecosystem

This document provides a comprehensive technical specification for the Travel360 system, covering every architectural detail from database constraints to class-level method signatures.

---

## 1. Detailed Database Schema (Data Dictionary)

### 1.1 User & Access Management
| Table | Column | Data Type | Constraint | Description |
| :--- | :--- | :--- | :--- | :--- |
| `users` | `id` | `BIGINT` | PK, Auto-Inc | Unique User Identifier |
| | `username` | `VARCHAR(50)` | Unique, Not Null | Login identifier |
| | `password` | `VARCHAR(255)` | Not Null | BCrypt hashed password |
| | `email` | `VARCHAR(100)` | Unique, Not Null | Communication email |
| | `status` | `VARCHAR(20)` | Default 'ACTIVE' | User state (ACTIVE, INACTIVE, LOCKED) |
| `roles` | `id` | `BIGINT` | PK, Auto-Inc | Role Identifier |
| | `role_name` | `VARCHAR(20)` | Unique, Not Null | ROLE_ADMIN, ROLE_AGENT, etc. |
| `user_roles` | `user_id` | `BIGINT` | FK $\rightarrow$ `users.id` | Mapping of users to roles |
| | `role_id` | `BIGINT` | FK $\rightarrow$ `roles.id` | Mapping of users to roles |

### 1.2 Travel Catalog (The Inventory)
| Table | Column | Data Type | Constraint | Description |
| :--- | :--- | :--- | :--- | :--- |
| `flights` | `id` | `BIGINT` | PK | Flight Unique ID |
| | `flight_number`| `VARCHAR(10)` | Not Null | IATA Flight Code |
| | `origin` | `VARCHAR(50)` | Not Null | Departure city |
| | `destination` | `VARCHAR(50)` | Not Null | Arrival city |
| | `departure_time`| `TIMESTAMP` | Not Null | Scheduled departure |
| | `base_price` | `DECIMAL(10,2)`| Not Null | Standard fare |
| `flight_seats`| `id` | `BIGINT` | PK | Seat Identifier |
| | `flight_id` | `BIGINT` | FK $\rightarrow$ `flights.id` | Reference to flight |
| | `seat_number` | `VARCHAR(5)` | Not Null | e.g., 12A, 14C |
| | `is_available` | `BOOLEAN` | Default True | Availability flag |

### 1.3 Booking Lifecycle
| Table | Column | Data Type | Constraint | Description |
| :--- | :--- | :--- | :--- | :--- |
| `booking_requests`| `id` | `BIGINT` | PK | Request ID |
| | `user_id` | `BIGINT` | FK $\rightarrow$ `users.id` | The requesting customer |
| | `budget` | `DECIMAL(10,2)`| Not Null | Maximum spend |
| | `status` | `VARCHAR(20)` | Enum | PENDING, PROCESSING, COMPLETED, REJECTED |
| `itineraries` | `id` | `BIGINT` | PK | Curated Plan ID |
| | `request_id` | `BIGINT` | FK $\rightarrow$ `booking_requests.id` | Original request link |
| | `agent_id` | `BIGINT` | FK $\rightarrow$ `users.id` | The agent who curated it |
| | `total_cost` | `DECIMAL(10,2)`| Not Null | Sum of all selected services |
| `bookings` | `id` | `BIGINT` | PK | Confirmed Booking ID |
| | `booking_type` | `VARCHAR(20)` | Enum | FLIGHT, HOTEL, TRANSPORT, PACKAGE |
| | `status` | `VARCHAR(20)` | Enum | PENDING_PAYMENT, CONFIRMED, CANCELLED |
| | `created_at` | `TIMESTAMP` | Not Null | Booking timestamp |

---

## 2. Logic & State Machine Transitions

### 2.1 Booking Lifecycle State Machine
The system enforces a strict state transition to prevent illegal operations (e.g., paying for a cancelled booking).

**State Transition Table:**
| Current State | Event | Target State | Condition |
| :--- | :--- | :--- | :--- |
| `NULL` | `CREATE_BOOKING` | `PENDING_PAYMENT` | Valid availability check passed |
| `PENDING_PAYMENT`| `PAYMENT_SUCCESS` | `CONFIRMED` | Payment gateway returns SUCCESS |
| `PENDING_PAYMENT`| `PAYMENT_FAIL` | `PENDING_PAYMENT`| Retry allowed |
| `CONFIRMED` | `USER_CANCEL` | `CANCELLED` | Date check $\rightarrow$ Calc Refund |
| `CONFIRMED` | `ADMIN_CANCEL` | `CANCELLED` | Forced cancellation (Force Majeure) |

### 2.2 Tiered Refund Logic (Algorithm)
When `BookingService.deleteBooking()` is called, the system applies the following logic:
```java
public double calculateRefund(Booking booking) {
    long daysRemaining = calculateDaysUntilDeparture(booking.getDate());
    double basePrice = booking.getTotalAmount();
    
    if (daysRemaining > 30) return basePrice * 1.00; // 100% refund
    if (daysRemaining > 15) return basePrice * 0.75; // 75% refund
    if (daysRemaining > 7)  return basePrice * 0.50; // 50% refund
    if (daysRemaining > 0)  return basePrice * 0.25; // 25% refund
    return 0.0; // No refund for last-minute cancellations
}
```

---

## 3. Backend Class Detailed Design

### 3.1 Service Interface Signatures
The business logic is encapsulated in interfaces to allow for different implementations.

#### `BookingService.java`
```java
public interface BookingService {
    // Direct Bookings
    BookingFlightResponseDTO createFlightBooking(@Valid BookingFlightDTO dto);
    BookingHotelResponseDTO createHotelBooking(@Valid BookingHotelDTO dto);
    BookingPackageResponseDTO createPackageBooking(@Valid BookingPackageDTO dto);
    
    // Management
    BookingCancelResponseDTO deleteBooking(BookingCancelDTO dto);
    PassengerCancelResponseDTO cancelPassenger(Long bookingId, Long passengerId);
    
    // Query
    Page<BookingResponseDTO> getBookings(Long userId, Pageable pageable);
    BookingResponseDTO getBookingById(Long bookingId);
}
```

#### `AuditLogService.java`
```java
public interface AuditLogService {
    void logAction(String action, AuditEntity entity, Long entityId, User user, LogType type);
    Page<AuditLogResponseDTO> getAuditLogs(Pageable pageable);
}
```

### 3.2 AOP Audit Implementation
The `AuditAspect` uses a **Pointcut** to intercept any method annotated with `@Audit`.
*   **Aspect Logic:**
    1.  Capture the method name and arguments.
    2.  Retrieve current user from `SecurityContextHolder`.
    3.  Create an `AuditLog` entity.
    4.  Save to `audit_logs` table.

---

## 4. Comprehensive API Specification

### 4.1 Authentication Endpoints
| Method | Endpoint | Params | Role | Logic |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | `LoginDTO` | Public | Validate $\rightarrow$ Generate JWT $\rightarrow$ Return `AuthResponseDTO` |
| `POST` | `/api/v1/auth/register` | `CreateUserDTO` | Public | Validate unique email $\rightarrow$ Hash password $\rightarrow$ Save |

### 4.2 Finance & KPI Endpoints
| Method | Endpoint | Params | Role | Logic |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/kpi/report` | None | FINANCE | Aggregate `Booking` totals $\rightarrow$ return `KpiReportResponseDTO` |
| `POST` | `/api/v1/payments` | `PaymentDTO` | CUSTOMER | Validate `Invoice` $\rightarrow$ process payment $\rightarrow$ update status |

---

## 5. Frontend Technical Architecture (Angular)

### 5.1 Reactive Data Flow
The frontend uses a **Unidirectional Data Flow** pattern:
`Component` $\rightarrow$ `Service` $\rightarrow$ `HTTP Call` $\rightarrow$ `Backend` $\rightarrow$ `Observable Result` $\rightarrow$ `UI Update`.

### 5.2 Component Logic Detail: `RoleGuard`
```typescript
canActivate(route: ActivatedRouteSnapshot): boolean {
  const requiredRole = route.data['expectedRole'];
  const userRole = this.authService.getCurrentUserRole(); // Decoded from JWT
  
  if (userRole === requiredRole) {
    return true;
  } else {
    this.router.navigate(['/unauthorized']);
    return false;
  }
}
```

### 5.3 `AuthInterceptor` Implementation
*   **Purpose:** To avoid manually adding the token to every single service call.
*   **Logic:** 
    1.  Intercepts outgoing `HttpRequest`.
    2.  Reads `token` from `localStorage`.
    3.  Uses `req.clone({ setHeaders: { Authorization: 'Bearer ' + token } })`.
    4.  Passes the cloned request to the next handler in the chain.

---

## 6. Error Handling Matrix

| Error Scenario | Backend Exception | HTTP Code | Frontend Response |
| :--- | :--- | :--- | :--- |
| User not found | `UserNotFoundException` | `404` | "User profile not found. Please check ID." |
| Seat already taken | `InsufficientAvailabilityException`| `409` | "Sorry, this seat was just booked by another user." |
| Invalid Token | `JWTException` | `401` | Redirect to `/login` with "Session Expired" toast. |
| Unauthorized Role | `AccessDeniedException` | `403` | Redirect to `/unauthorized` page. |
| Validation Fail | `MethodArgumentNotValidException`| `400` | Display field-level error messages on the form. |
