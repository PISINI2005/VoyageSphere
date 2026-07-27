# 🏛️ High-Level Design (HLD): Travel360 Ecosystem

## 1. System Architecture Overview
Travel360 follows a **Decoupled Client-Server Architecture**. It is designed as a Single Page Application (SPA) interacting with a Stateless RESTful API.

### 1.1 Architectural Layers
*   **Presentation Layer (Frontend):** Built with **Angular**. Responsible for role-based routing, state management via RxJS, and providing a responsive UI for five different user personas.
*   **API Gateway/Security Layer:** Handles authentication and authorization using **JWT (JSON Web Tokens)**. It intercepts every request to verify roles before they reach the business logic.
*   **Business Logic Layer (Backend):** Built with **Spring Boot**. This is where the "heavy lifting" happens—calculating prices, managing itinerary state transitions, and processing payments.
*   **Persistence Layer:** Using **Spring Data JPA** and **Hibernate** to map Java entities to a relational database (MySQL/H2).

---

## 2. Component Decomposition

### 2.1 Frontend Components (`voyagsphere2-ui`)
*   **Core Module:** Contains `AuthGuard` (Role protection), `AuthInterceptor` (JWT injection), and `ThemeService`.
*   **Service Layer:** Modular services (e.g., `BookingService`, `SearchService`, `KpiService`) that handle HTTP communication.
*   **Page Modules:** 
    *   `AdminPages`: Catalog management.
    *   `AgentPages`: Request processing $\rightarrow$ Itinerary building.
    *   `FinancePages`: Invoice $\rightarrow$ KPI tracking.
    *   `CompliancePages`: Complaint resolution $\rightarrow$ Audit logs.
    *   `UserPages`: Search $\rightarrow$ Booking $\rightarrow$ Profile.

### 2.2 Backend Components (`travel360endpoints-3`)
*   **Controllers:** REST endpoints for each domain (e.g., `BookingController`, `InvoiceController`).
*   **Service Layer:** The core engine.
    *   `BookingService`: Manages the logic for reserving seats/rooms.
    *   `ItineraryService`: Handles the complex mapping of a "Request" to a "Plan."
    *   `AuditLogService`: A cross-cutting service that records system activity.
*   **Repository Layer:** Direct database interaction using Spring Data JPA.
*   **Mapper Layer:** Uses DTOs (Data Transfer Objects) to prevent exposing internal database entities to the client.

---

## 3. Detailed Data Flows

### 3.1 The Self-Service (DIY) Flow
**User $\rightarrow$ Search $\rightarrow$ Book $\rightarrow$ Pay**
1.  **Search:** `SearchController` $\rightarrow$ `SearchService` $\rightarrow$ Database (Filters for Flights/Hotels).
2.  **Booking:** `BookingController` $\rightarrow$ `BookingService` $\rightarrow$ Creates `Booking` entity $\rightarrow$ Sets status to `PENDING_PAYMENT`.
3.  **Invoicing:** `InvoiceService` $\rightarrow$ Calculates total $\rightarrow$ Generates `Invoice` entity.
4.  **Payment:** `PaymentController` $\rightarrow$ `PaymentService` $\rightarrow$ Updates `PaymentStatus` $\rightarrow$ Updates `BookingStatus` to `CONFIRMED`.

### 3.2 The Agent-Assisted (Concierge) Flow
**User $\rightarrow$ Request $\rightarrow$ Curate $\rightarrow$ Book $\rightarrow$ Pay**
1.  **Request:** `BookingRequestController` $\rightarrow$ Creates `BookingRequest` entity (User preferences).
2.  **Curation:** `Agent` $\rightarrow$ `ItineraryService` $\rightarrow$ Selects specific Flights/Hotels $\rightarrow$ Creates `Itinerary`.
3.  **Finalization:** `BookingService` $\rightarrow$ Converts `Itinerary` $\rightarrow$ `Booking`.
4.  **Payment:** (Same as Self-Service flow).

---

## 4. Database Design (Entity Relationship)
*The system relies on a normalized relational schema to ensure data integrity.*

*   **User $\leftrightarrow$ Role:** Many-to-Many (A user can have multiple roles).
*   **BookingRequest $\leftrightarrow$ Itinerary:** One-to-One (A request results in one curated plan).
*   **Booking $\leftrightarrow$ Passenger:** One-to-Many (One booking can have multiple passengers).
*   **Booking $\rightarrow$ Invoice $\rightarrow$ Payment:** Linear chain (Booking triggers Invoice $\rightarrow$ Invoice triggers Payment).
*   **Complaint $\rightarrow$ Booking:** Many-to-One (A user can file multiple complaints against one booking).

---

## 5. Security & Cross-Cutting Concerns

### 5.1 Security Model
*   **Authentication:** Stateless JWT. The client sends a token in the `Authorization: Bearer <token>` header.
*   **Authorization:** 
    *   **Backend:** `@PreAuthorize("hasRole('ROLE_ADMIN')")` on controller methods.
    *   **Frontend:** `RoleGuard` prevents unauthorized users from accessing specific URL paths.

### 5.2 The Audit System (AOP)
To avoid writing logging code in every single method, the system uses **Aspect-Oriented Programming (AOP)**:
*   **Custom Annotation:** `@Audit(action = "CREATE_BOOKING")`
*   **Aspect:** The `AuditAspect` intercepts methods marked with this annotation, captures the current user and timestamp, and writes to the `AuditLog` table automatically.

---

## 6. Technical Trade-offs & Design Decisions

### 6.1 Architectural Choices
*   **Modular Monolith vs. Microservices:** 
    *   *Decision:* Chose a Modular Monolith.
    *   *Reasoning:* For the current scale, it reduces operational complexity and network latency. However, clear service boundaries are maintained to allow for an easy transition to microservices if needed.
*   **Stateless JWT vs. Session Cookies:**
    *   *Decision:* Stateless JWT.
    *   *Reasoning:* Facilitates horizontal scaling without requiring a shared session store (like Redis).
*   **DTO Pattern:**
    *   *Decision:* Strict separation of Entities and DTOs.
    *   *Reasoning:* Prevents "Over-posting" attacks and decouples the database schema from the external API contract.

### 6.2 Concurrency & Data Integrity
*   **Double Booking Prevention:** To prevent race conditions during the final seat reservation, the system employs:
    *   **Pessimistic Locking:** Locks the specific service row in the database during the payment finalization phase.
    *   **Optimistic Locking:** Uses a `@Version` column to detect concurrent modifications during the agent curation process.

---

## 7. Error Handling & Resilience

### 7.1 Fault Tolerance
*   **Global Exception Handling:** Implemented `@ControllerAdvice` to map internal exceptions (e.g., `ResourceNotFoundException`) to standard HTTP status codes.
*   **Transaction Management:** Utilizes `@Transactional` to ensure **Atomicity**. If a booking payment fails, the entire transaction is rolled back, preventing orphaned invoice records.

---

## 8. Scalability Roadmap (Future State)

### 8.1 Performance Optimization
*   **Caching Layer:** Implementation of **Redis** for frequently searched travel destinations to reduce database read load.
*   **Async Processing:** Moving email notifications and PDF invoice generation to an asynchronous message queue (e.g., **RabbitMQ**).
*   **Database Scaling:** Implementation of a **Read-Replica** strategy to separate heavy search queries from critical booking writes.
