package com.cts.entity;

import com.cts.enums.HotelRoomType;
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
@Table(name = "hotel_room",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hotel_id", "room_type"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hotelRoomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type")
    private HotelRoomType roomType;

    // Per-night price for this room type on this hotel
    private double price;

    // Inventory available for this room type on this hotel
    private int totalRooms;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Hotel hotel;
}
