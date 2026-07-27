package com.cts.entity;

import com.cts.enums.BookingRequestCustomerStatus;
import com.cts.enums.BookingRequestStatus;
import com.cts.enums.BookingRequestType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingRequestId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private User agent;

    private Double budget;

    @Column(columnDefinition = "TEXT")
    private String requestDetails;

    @Column(columnDefinition = "TEXT")
    private String agentRemarks;

    @Column(columnDefinition = "TEXT")
    private String modificationDetails;

    @Enumerated(EnumType.STRING)
    private BookingRequestCustomerStatus customerStatus;

    @Enumerated(EnumType.STRING)
    private BookingRequestType type;

    @Enumerated(EnumType.STRING)
    private BookingRequestStatus status;

    @ElementCollection
    @CollectionTable(name = "booking_request_bookings", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "booking_id")
    @Builder.Default
    private List<Long> linkedBookingIds = new ArrayList<>();

    @Version
    private Long version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
