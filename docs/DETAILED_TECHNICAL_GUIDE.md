# 🎓 Travel360: Ultimate Technical Implementation Guide

This document provides an exhaustive technical deep-dive into the **Travel360** monolithic architecture. It is intended for developers and architects to understand the exact "how" and "why" of the system's design.

---

## 🔐 1. Security & Identity Management

### 1.1 JWT Token Architecture
The system implements a stateless identity model. Instead of storing session IDs in a database (which would be slow and hard to scale), all user identity is embedded within a signed **JSON Web Token (JWT)**.

#### The Token Journey:
1. **Client Request:** The client attaches the token to every request via the `Authorization` header as a `Bearer` token.
2. **The Filter (`JWTFilter`):** 
   - Extends `OncePerRequestFilter` to ensure it executes exactly once per request.
   - Calls `JWTUtil` to parse the token.
3. **Claims Extraction:** The token contains "claims" (pieces of information). The system extracts:
   - `email`: Used as the primary identifier.
   - `role`: (e.g., `ADMIN`, `USER`) used for authorization.
   - `userId`: Used to identify the user in the database.
4. **Security Context:** If the token is valid, the filter creates an `Authentication` object and stores it in the `SecurityContextHolder`. This makes the user's identity available globally throughout the current thread of execution.

### 1.2 Reducing Database Overhead with `AuthenticatedUserProvider`
To optimize performance, the system avoids querying the `User` table for every single request just to get the `userId`.

- **The Optimization:** `AuthenticatedUserProvider.current()` extracts the `userId` and `role` **directly from the JWT claims**.
- **The Result:** This reduces the number of database calls by one per request, significantly lowering latency for authenticated endpoints.
- **Ownership Enforcement:** `assertCanActAs(Long ownerUserId)` compares the `userId` from the token with the resource's owner ID. If they don't match (and the user isn't an Admin), an `AccessDeniedException` is thrown.

---

## 🛠️ 2. Validation Strategy & Annotations

The project employs a two-tier validation strategy to ensure data integrity.

### 2.1 Tier 1: Syntactic Validation (DTO Level)
This happens at the very edge of the application. We use **Jakarta Bean Validation** annotations in the DTOs (e.g., `BookingFlightDTO.java`).

| Annotation | Purpose | Why we use it |
| :--- | :--- | :--- |
| `@NotNull` | Ensures a field is not null. | Prevents `NullPointerException` in the service layer. |
| `@NotBlank` | Ensures a String is not null and not just whitespace. | Validates that names/emails are actually provided. |
| `@Size(min=X, max=Y)` | Limits the length of a string or size of a list. | Prevents "Buffer Overflow" style attacks or absurdly large requests. |
| `@Future` | Ensures a date is in the future. | Prevents users from booking flights that have already departed. |
| `@Min` / `@Max` | Restricts numeric values. | Ensures `units` are at least 1 and not more than 10 (prevents bulk-buying bots). |
| `@NotEmpty` | Ensures a collection has at least one element. | Ensures at least one passenger is provided for a booking. |

### 2.2 Tier 2: Semantic Validation (Service Level)
Once the DTO is syntactically correct, the `ServiceImpl` classes perform "Business Logic" validation.

**Example: Passenger Validation Logic**
In `FlightBookingServiceImpl`, passengers are validated as follows:
1. **ID Resolution:** The system receives a list of `passengerProfileIds`.
2. **Existence Check:** The `PassengerResolver` (used via `passengerResolver.resolve(...)`) fetches these profiles from the database.
3. **Ownership Check:** The system ensures that the `PassengerProfile` being used actually belongs to the `userId` extracted from the token.
4. **Integrity:** If a profile ID is invalid or doesn't belong to the user, a `ResourceNotFoundException` or `InvalidBookingException` is thrown.

---

## 🏎️ 3. Performance & Concurrency

### 3.1 Database Locking (Preventing Overbooking)
In a travel system, two users might try to book the last remaining seat at the exact same millisecond. A standard `findById` would allow both, leading to overbooking.

**The Solution: Pessimistic Locking**
In `FlightBookingServiceImpl`, we use:
```java
Flight flight = flightRepo.findByIdForUpdate(dto.getFlightId()).orElseThrow(...);
```
- **How it works:** This executes a `SELECT ... FOR UPDATE` SQL query.
- **Database Behavior:** The database places a **write-lock** on that specific flight row.
- **Concurrency Result:** If User B tries to book the same flight while User A's transaction is still open, User B's request will **wait (block)** until User A's transaction commits or rolls back.
- **Guaranteed Integrity:** This ensures the "Check Availability $\rightarrow$ Create Booking" sequence is atomic and thread-safe.

### 3.2 KPI Report Optimization (Reducing DB Operations)
The `KpiReportServiceImpl` is designed to avoid the "N+1 Query Problem" when generating yearly reports.

**The "Naive" Approach (Slow):**
Querying the database 12 times (once for each month) to get revenue and bookings.

**The Optimized Approach (Fast):**
1. **Bulk Fetching:** The system makes only **two** primary queries for the entire year:
   - `paymentRepo.getMonthlyMoneyStats(year)`: Fetches a summary of revenue for all 12 months in one shot.
   - `bookingRepo.getMonthlyCounts(year)`: Fetches total bookings/cancellations for all 12 months in one shot.
2. **In-Memory Merging:** The service then uses Java Streams to filter and merge these two lists in memory.
3. **Outcome:** This reduces database round-trips from $12 \times 2$ to just **2**, drastically improving report generation speed.

---

## 🔄 4. Complex Business Logic: Cancellations & Refunds

Cancellations are the most complex part of the system because they affect Bookings, Passengers, Invoices, and Payments.

### 4.1 Full Booking Cancellation Flow
**Logic Path:** `BookingServiceImpl.deleteBooking()`
- **Condition 1 (Date):** If `travelDate` < `today`, cancellation is blocked.
- **Condition 2 (Status):**
    - **If PENDING:** No money was paid $\rightarrow$ Mark `Booking` and `Invoice` as `CANCELLED`.
    - **If CONFIRMED:** Money was paid $\rightarrow$ Use `BookingHelper` to calculate the refund based on the time until travel.
- **Financial Record:** A new `Payment` entity is created with status `REFUNDED` to maintain a permanent ledger of money moving back to the user.

### 4.2 Partial Passenger Cancellation Flow
**Logic Path:** `BookingServiceImpl.cancelPassenger()`
- **Restriction:** Only allowed for `FLIGHT` and `TRANSPORT` (Hotels are usually booked as a room, not per person).
- **The "Last Passenger" Trigger:** If `activePassengers == 1`, the system automatically redirects the flow to `deleteBooking()` because a booking cannot exist without passengers.
- **Financial Adjustment:**
    - The `amount` and `units` of the `Booking` are decremented.
    - The `Invoice` is updated to `PARTIALLY_REFUNDED`.
    - A partial refund payment is recorded.

---

## 📋 5. Summary of Design Patterns Used

| Pattern | Implementation | Why? |
| :--- | :--- | :--- |
| **DTO Pattern** | `BookingFlightDTO`, etc. | To isolate the API contract from the Database schema. |
| **Repository Pattern** | `BookingRepository` | To abstract SQL queries and provide a clean interface for services. |
| **Strategy Pattern** | `BookingService` $\rightarrow$ `FlightBookingService`, etc. | To handle different booking types (Flight vs Hotel) with specific logic. |
| **AOP (Aspect Oriented Programming)** | `LoggingAspect` | To implement audit logging across the whole app without writing `log.info` in every method. |
| **Global Exception Handling** | `@RestControllerAdvice` | To ensure the client always receives a consistent `ErrorResponseDTO`. |
