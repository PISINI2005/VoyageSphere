package com.cts.enums;

/**
 * The kind of customer-owned record a complaint can be filed against. Each value
 * traces to a user (BOOKING → User, INVOICE → Booking → User, PAYMENT → Invoice →
 * Booking → User), so the referenced target's ownership can be verified against the
 * complainant. Catalog entities (flight/hotel/transport/package) are intentionally
 * excluded — a customer complains about their booking of one, not the catalog item.
 */
public enum ComplaintTargetType {
    BOOKING,
    INVOICE,
    PAYMENT
}
