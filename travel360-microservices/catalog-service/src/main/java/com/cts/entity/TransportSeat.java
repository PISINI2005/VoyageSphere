package com.cts.entity;

import com.cts.enums.TransportClass;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "transport_seat",
        uniqueConstraints = @UniqueConstraint(columnNames = {"transport_id", "transport_class"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transportSeatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_class")
    private TransportClass transportClass;

    // Absolute price for this class on this transport
    private double price;

    // Inventory available for this class on this transport
    private int totalSeats;

    @ManyToOne
    @JoinColumn(name = "transport_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Transport transport;
}
