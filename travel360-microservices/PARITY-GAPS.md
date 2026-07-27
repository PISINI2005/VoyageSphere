# Microservices ↔ Monolith Parity Gaps

The microservices workspace (`travel360-microservices/`) was built on an **older** version of the
monolith (`travel360endpoints-3/`). This is a checklist of every functional divergence found, so the
monolith can be mirrored item-by-item.

**Source of truth:** monolith `travel360endpoints-3`.
**Out of scope (intentional, NOT gaps):** `@ManyToOne` → scalar-ID + Feign conversion, package splits,
the inter-service write endpoints (`POST /notifications`, `POST /auditLogs`), and Feign-facing
`getById` additions.

Severity: 🔴 security/data-integrity · 🟠 missing endpoint/feature · 🟡 business logic · ⚪ contract/cosmetic

---

## user-service

- [x] 🔴 **Self-registration privilege escalation.** FIXED — `register()` now hard-codes `Role.CUSTOMER` +
  `UserStatus.ACTIVE`; registration `UserDTO` carries only email/password/phoneNo (role/status fields removed).
  - Monolith: `UserServiceImpl.java:47`, `UserMapper.java:26-35`, registration `UserDTO` (email/password/phone only)
  - Micro: `serviceimpl/UserServiceImpl.java`, `mapper/UserMapper.java`, `dto/UserDTO.java`
- [x] 🟠 **`POST /api/v1/users`** ADDED (ADMIN creates privileged user from `CreateUserDTO`, default password `Welcome@123` via local constant — micro has no DataSeeder).
- [x] 🟠 **`PATCH /api/v1/users/{id}/status`** ADDED (ADMIN, `@Transactional`, from `UserStatusUpdateDTO`).
- [x] ⚪ **`UserStatus` enum** ADDED (`ACTIVE/INACTIVE/SUSPENDED/BLOCKED`); `User.status` + `UserResponseDTO.status` retyped to the enum.
- [x] ⚪ **`UserRepository.existsByEmail`** ADDED.

---

## catalog-service

- [x] 🟠 **5 `PATCH /{id}/status` endpoints** ADDED + their `*StatusUpdateDTO` + service/serviceimpl methods
  (Flight/Hotel/Transport/Package/Partner), mirroring monolith mappings, `@PreAuthorize`, `@Transactional`, audit. The
  Partner endpoint carries the inventory-deactivation cascade (FLIGHT→CANCELLED, HOTEL/PACKAGE→INACTIVE, BUS→OUT_OF_SERVICE).
- [x] 🟡 **Default status on create** RESTORED in serviceimpls — Flight `SCHEDULED` (+`@Transactional` on addFlight),
  Hotel/Transport/Package `AVAILABLE`, Partner `ACTIVE` (+`@Transactional` on createPartner).
- [x] ⚪ **`PartnerDTO`** `@NotNull` on `type` and `status` RESTORED.
- [x] ⚪ **`PartnerController.updatePartner`** `@Valid` on the body RESTORED.
- [x] ⚪ Dead micro-only `TransportSeatType` enum REMOVED.

---

## booking-service

### PassengerProfile feature — entirely absent (the largest gap)
The monolith models travelers as reusable, soft-deletable **PassengerProfile** records; bookings send
`passengerProfileIds` and `PassengerResolver` turns them into `Passenger` rows. The micro still uses the
**old inline model** (each `Passenger` stores its own identity fields; bookings carry a `PassengerDTO` list).

- [x] 🟠 **`PassengerProfile` entity** ADDED (scalar `userId` instead of monolith's `@ManyToOne User`, per the Feign design).
- [x] 🟠 **PassengerProfile CRUD** ADDED — `/api/v1/passengers/profiles` POST, GET `/me`, GET `/{id}`,
  PUT `/{id}`, DELETE `/{id}` (soft-delete), all `hasRole('CUSTOMER')` + service/serviceimpl/mapper/repository/request+response DTOs.
- [x] 🟠 **`PassengerResolver`** ADDED — resolves profile IDs, rejects duplicate IDs in one booking,
  enforces per-profile ownership (adapted to scalar `userId`). Replaces the old `PassengerMapper.toEntities`.
- [x] 🟡 **Identity-document validation** ADDED — FOREIGN→PASSPORT rule + per-type regex via `enums/IdentificationType`.
  `InvalidPassengerException` + `GlobalExceptionHandler` mapping added.
- [x] ⚪ **`Passenger` entity shape** switched to `@ManyToOne PassengerProfile profile`; booking create DTOs now carry
  `passengerProfileIds` (Flight/Transport). `PassengerResponseDTO` gains `passengerProfileId`/`identificationType`/`Nationality`.
- [x] ⚪ Supporting enums ADDED: `PassengerProfileStatus`, `Nationality`, `IdentificationType`;
  `AuditActions.CREATE/UPDATE/DELETE_PASSENGER_PROFILE`; `AuditEntity.PASSENGER_PROFILE`.

### Security / data integrity
- [x] 🔴 **`identificationNumber` masking** ADDED — `PassengerMapper.toResponse` masks via `util/MaskUtil.maskId` (keeps last 4).
- [x] 🔴 **`InvoiceServiceImpl.createInvoice`** RESTORED `@Transactional`, ownership check (`assertCanActAs(booking.getUserId())`),
  billable-state check (rejects CANCELLED/FAILED), and duplicate-invoice idempotency guard.
- [x] 🔴 **Authorization narrowed** — `CUSTOMER` removed from staff-only `/user/{userId}` routes:
  Invoices → `FINANCE_OFFICER,ADMIN`; Itineraries → `TRAVEL_AGENT,ADMIN`. (Booking `/user/{userId}` left as-is; not listed as a gap.)

### Refund / cancellation logic
- [x] 🟡 **1-day refund rule** — now `daysBetween <= 1` → refund 0 (cancel allowed), matching monolith (no longer throws).
- [x] 🟡 **Travel-date-passed guard** ADDED to `deleteBooking` + `cancelPassenger` (`bookingDate.isBefore(today)`).
- [x] 🟡 **Refund accounting** — `recordRefund` now flips the existing SUCCESS invoice to `REFUNDED`/`PARTIALLY_REFUNDED`
  and records a `REFUNDED` `Payment` (one invoice per booking) instead of creating a new REFUNDED invoice.
- [x] 🟡 **Full-refund classification** — uses drift-tolerant `BookingHelper.refundStatus` (`refund >= full`).
- [x] ⚪ **`PaymentStatus.PARTIALLY_REFUNDED`** enum value ADDED.

### KPI computation
- [x] 🟡 **Money source** — now computed from the **Payment ledger** (net of refunds) via `PaymentRepository.getMoneyStats`/
  `getMonthlyMoneyStats`; counts from `BookingRepository.countBookingsInPeriod`/`countCancellationsInPeriod`/`getMonthlyCounts`.
  `cancelledRevenue` renamed to `refundedAmount` across entity/DTOs/mapper; `KpiStatsDTO`/`MonthlyKpiStatsDTO` retyped + `MonthlyKpiCountsDTO` added.
- [x] 🟡 **Custom report transient** — `generateCustomReport` is now unsaved (`@Transactional(readOnly)`), controller returns 200 OK.
- [x] ⚪ **Month boundary** — now half-open `[start, +1 month)`.

### Booking create / endpoints
- [x] 🟡 **`userId` optional** — booking DTOs no longer `@NotNull` it; serviceimpls auto-inject from JWT when null (all 4 types).
- [x] ⚪ **Notification text** — "Payment yet to be made." (Flight/Hotel/Transport) / "Payment Pending." (Package) clause restored.
- [x] 🟠 **`/me` endpoints** ADDED: `GET /bookings/me`, `GET /invoices/me`, `GET /itineraries/me`.
- [ ] ⚪ **Extra `userId` query params** on `cancelPassenger` and itinerary get/delete — **LEFT AS-IS** (confirm-intent item; see summary).
- [ ] ⚪ **`email` always null** in invoice + itinerary responses — **DEFERRED** (see summary): the monolith reads it off the
  `@ManyToOne User` association, which the micro intentionally dropped for scalar IDs. Populating it faithfully would require a
  per-row Feign call inside a stateless, list-context mapper (N+1). Left null pending a decision.

---

## notification-service

- [x] 🟠 **`GET /api/v1/notifications/me`** ADDED.
- [x] 🟠 **`PATCH /notifications/{id}/read`** ADDED (`markAsRead`, incl. IDOR ownership check via `assertCanActAs`).
- [x] 🟠 **`PATCH /notifications/read-all`** ADDED (`markAllAsRead`) + repo `findByUserIdAndStatus`
  (adapted to the micro's scalar `userId` Notification shape).
- [x] 🔴 **`@PreAuthorize`** ADDED on `getUserNotifications` (`TRAVEL_AGENT,ADMIN`) and on all audit-log read
  endpoints (`COMPLIANCE_OFFICER,ADMIN`).
- [x] ⚪ **`NotificationCategory.COMPLAINT`** ADDED.
- [x] ⚪ **`AuditEntity`** `PASSENGER_PROFILE`, `COMPLAINT` ADDED.
- [x] ⚪ **`AuditActions`** ADDED: `DELETE_FLIGHT/HOTEL/TRANSPORT/PACKAGE`, `UPDATE_PACKAGE_ITINERARY`,
  `CREATE_COMPLAINT`, `UPDATE_COMPLAINT_STATUS`, `UPDATE_USER_STATUS`, `CREATE/UPDATE/DELETE_PASSENGER_PROFILE`.
- [ ] ⚪ **Audit query API shape** (path-based `/all`, `/entity/{}/{}`, etc. vs monolith's consolidated `GET /auditLogs`)
  — **LEFT AS-IS** (confirm-intent item; only the missing `@PreAuthorize` roles were added). See summary.

---

## Complaint module — entirely absent (whole feature)

The monolith has a full Complaint feature; the microservices has **none** of it. Needs a home service
(logically booking- or a new complaints-service) since it spans customers + compliance officers.

- [x] 🟠 **`/api/v1/complaints`** endpoints ADDED in `booking-service` (`ComplaintController.java`):
  `POST` create (CUSTOMER), `GET /me`, `GET` list+status filter (COMPLIANCE_OFFICER/ADMIN),
  `GET /{id}` (COMPLIANCE_OFFICER/ADMIN), `PATCH /{id}/status` (COMPLIANCE_OFFICER/ADMIN).
- [x] 🟠 Supporting types ADDED: `entity/Complaint` (scalar `userId` instead of `@ManyToOne User`), `ComplaintRequestDTO`,
  `ComplaintResponseDTO`, `ComplaintStatusUpdateDTO`, `enums/ComplaintStatus`, `enums/ComplaintTargetType`,
  `InvalidComplaintException` (+ handler), `ComplaintMapper`, `ComplaintRepository`, `ComplaintService(+Impl)`.
  Cross-domain user existence check is a Feign `UserClient` call; notifications via the `Long userId` notification client.
- [x] ⚪ `COMPLAINT` category/entity + `CREATE_COMPLAINT`/`UPDATE_COMPLAINT_STATUS` actions wired in.

---

## Suggested order (when you decide to act)
1. 🔴 Security/integrity (registration escalation, invoice guards, ID masking, missing `@PreAuthorize`).
2. 🟠 Missing endpoints (catalog status PATCHes, user admin endpoints, `/me` endpoints, notification read).
3. PassengerProfile feature + Complaint module (the two large feature ports).
4. 🟡 Business logic (refunds, KPI ledger).
5. ⚪ Enum/contract cleanup + confirm the "intentional?" items.
