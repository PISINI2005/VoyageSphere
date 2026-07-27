package com.cts.repository;

import com.cts.entity.Booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

	Page<Booking> findByUserUserId(Long userId, Pageable pageable);

	List<Booking> findByItineraryItineraryId(Long itineraryId);

	@Query("SELECT COALESCE(SUM(b.units), 0) FROM Booking b WHERE b.flight.flightId = :flightId AND b.seatType = :seatType AND b.bookingDate = :bookingDate AND b.status <> com.cts.enums.BookingStatus.CANCELLED")
	int getBookedSeats(@Param("flightId") long flightId, @Param("seatType") com.cts.enums.SeatType seatType,
			@Param("bookingDate") LocalDate bookingDate);

	@Query("SELECT COALESCE(SUM(b.units),0) FROM Booking b "
			+ "WHERE b.hotel.hotelId = :hotelId AND b.roomType = :roomType "
			+ "AND b.status <> com.cts.enums.BookingStatus.CANCELLED "
			+ "AND b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate")
	int getBookedRooms(@Param("hotelId") Long hotelId, @Param("roomType") com.cts.enums.HotelRoomType roomType,
			@Param("checkInDate") LocalDate checkInDate, @Param("checkOutDate") LocalDate checkOutDate);

	@Query("SELECT COALESCE(SUM(b.units),0) FROM Booking b "
			+ "WHERE b.travelPackage.packageId = :packageId AND b.bookingDate = :travelDate "
			+ "AND b.status <> com.cts.enums.BookingStatus.CANCELLED")
	int getBookedSlots(@Param("packageId") Long packageId, @Param("travelDate") LocalDate travelDate);

	@Query("SELECT COALESCE(SUM(b.units),0) FROM Booking b "
			+ "WHERE b.transport.transportId = :transportId AND b.transportClass = :transportClass "
			+ "AND b.bookingDate = :bookingDate " + "AND b.status <> com.cts.enums.BookingStatus.CANCELLED")
	int getBookedTransportSeats(@Param("transportId") Long transportId,
			@Param("transportClass") com.cts.enums.TransportClass transportClass,
			@Param("bookingDate") LocalDate bookingDate);

	// KPI counts (booking events). Money lives on the Payment ledger; see
	// PaymentRepository.
	// Bookings are counted by createdAt; revenue is counted by cash date, so the
	// two queries stay separate.

	@Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt >= :startDate AND b.createdAt < :endDate")
	long countBookingsInPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	@Query("SELECT COUNT(b) FROM Booking b WHERE b.status = com.cts.enums.BookingStatus.CANCELLED AND b.createdAt >= :startDate AND b.createdAt < :endDate")
	long countCancellationsInPeriod(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	@Query("SELECT MONTH(b.createdAt), COUNT(b), SUM(CASE WHEN b.status = com.cts.enums.BookingStatus.CANCELLED THEN 1 ELSE 0 END) "
			+ "FROM Booking b WHERE YEAR(b.createdAt) = :year "
			+ "GROUP BY MONTH(b.createdAt) ORDER BY MONTH(b.createdAt)")
	List<Object[]> getMonthlyBookingCountsByYear(@Param("year") int year);
}
