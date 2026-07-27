package com.cts.config;

import java.time.LocalTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.cts.dto.FlightDTO;
import com.cts.dto.FlightSeatDTO;
import com.cts.dto.HotelDTO;
import com.cts.dto.HotelRoomDTO;
import com.cts.dto.PartnerDTO;
import com.cts.dto.TransportDTO;
import com.cts.dto.TransportSeatDTO;
import com.cts.dto.TravelPackageDTO;
import com.cts.enums.FlightStatus;
import com.cts.enums.HotelRoomType;
import com.cts.enums.HotelStatus;
import com.cts.enums.PackageStatus;
import com.cts.enums.PartnerStatus;
import com.cts.enums.PartnerType;
import com.cts.enums.SeatType;
import com.cts.enums.TransportClass;
import com.cts.enums.TransportStatus;
import com.cts.enums.TravelPackageCategory;
import com.cts.repository.PartnerRepository;
import com.cts.service.FlightService;
import com.cts.service.HotelService;
import com.cts.service.PartnerService;
import com.cts.service.TransportService;
import com.cts.service.TravelPackageService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seeds a small sample catalog (partners + flights/hotels/transports/packages) at startup.
 * Builds the same create-DTOs the API uses and calls the services, so all validation and
 * partner-type checks apply. Idempotent: skips entirely once any partner exists.
 */
@Component
@AllArgsConstructor
@Slf4j
public class CatalogSeeder implements CommandLineRunner {

    private final PartnerService partnerService;
    private final FlightService flightService;
    private final HotelService hotelService;
    private final TransportService transportService;
    private final TravelPackageService travelPackageService;
    private final PartnerRepository partnerRepo;

    @Override
    public void run(String... args) {
        if (partnerRepo.count() > 0) {
            log.info("Catalog already seeded (partners exist); skipping.");
            return;
        }

        log.info("Seeding sample catalog...");

        Long flightPartnerId = partnerService.createPartner(
                PartnerDTO.builder().name("IndiGo").type(PartnerType.FLIGHT).status(PartnerStatus.ACTIVE).build())
                .getPartnerId();
        Long hotelPartnerId = partnerService.createPartner(
                PartnerDTO.builder().name("Taj Hotels").type(PartnerType.HOTEL).status(PartnerStatus.ACTIVE).build())
                .getPartnerId();
        Long busPartnerId = partnerService.createPartner(
                PartnerDTO.builder().name("VRL Travels").type(PartnerType.BUS).status(PartnerStatus.ACTIVE).build())
                .getPartnerId();
        Long packagePartnerId = partnerService.createPartner(
                PartnerDTO.builder().name("MakeMyTrip").type(PartnerType.PACKAGE).status(PartnerStatus.ACTIVE).build())
                .getPartnerId();

        seedFlights(flightPartnerId);
        seedHotels(hotelPartnerId);
        seedTransports(busPartnerId);
        seedPackages(packagePartnerId);

        log.info("Sample catalog seeding complete.");
    }

    private void seedFlights(Long partnerId) {
        FlightDTO f1 = new FlightDTO();
        f1.setFlightNumber("AI-101");
        f1.setPartnerId(partnerId);
        f1.setSource("Delhi");
        f1.setDestination("Mumbai");
        f1.setDepartureTime(LocalTime.of(9, 30));
        f1.setArrivalTime(LocalTime.of(11, 45));
        f1.setStatus(FlightStatus.SCHEDULED);
        f1.setSeats(List.of(
                FlightSeatDTO.builder().seatType(SeatType.ECONOMY).price(4500.0).totalSeats(120).build(),
                FlightSeatDTO.builder().seatType(SeatType.BUSINESS).price(12000.0).totalSeats(20).build()));
        flightService.addFlight(f1);

        FlightDTO f2 = new FlightDTO();
        f2.setFlightNumber("AI-202");
        f2.setPartnerId(partnerId);
        f2.setSource("Bangalore");
        f2.setDestination("Chennai");
        f2.setDepartureTime(LocalTime.of(14, 0));
        f2.setArrivalTime(LocalTime.of(15, 10));
        f2.setStatus(FlightStatus.SCHEDULED);
        f2.setSeats(List.of(
                FlightSeatDTO.builder().seatType(SeatType.ECONOMY).price(3200.0).totalSeats(100).build()));
        flightService.addFlight(f2);

        log.info("Seeded 2 flights.");
    }

    private void seedHotels(Long partnerId) {
        HotelDTO h1 = new HotelDTO();
        h1.setHotelName("Taj Palace");
        h1.setRatings(5);
        h1.setCity("Mumbai");
        h1.setAddress("Colaba, Mumbai");
        h1.setContactNo("9876543210");
        h1.setEmailId("tajpalace@mail.com");
        h1.setStatus(HotelStatus.AVAILABLE);
        h1.setPartnerId(partnerId);
        h1.setRooms(List.of(
                HotelRoomDTO.builder().roomType(HotelRoomType.STANDARD).price(6000.0).totalRooms(50).build(),
                HotelRoomDTO.builder().roomType(HotelRoomType.SUITE).price(15000.0).totalRooms(10).build()));
        hotelService.addHotel(h1);

        HotelDTO h2 = new HotelDTO();
        h2.setHotelName("Seaside Inn");
        h2.setRatings(4);
        h2.setCity("Goa");
        h2.setAddress("Calangute Beach Road");
        h2.setContactNo("9123456780");
        h2.setEmailId("seaside@mail.com");
        h2.setStatus(HotelStatus.AVAILABLE);
        h2.setPartnerId(partnerId);
        h2.setRooms(List.of(
                HotelRoomDTO.builder().roomType(HotelRoomType.DELUXE).price(8000.0).totalRooms(30).build()));
        hotelService.addHotel(h2);

        log.info("Seeded 2 hotels.");
    }

    private void seedTransports(Long partnerId) {
        TransportDTO t1 = new TransportDTO();
        t1.setTransportNumber(101);
        t1.setSource("Delhi");
        t1.setDestination("Jaipur");
        t1.setTransportType("BUS");
        t1.setDepartureTime(LocalTime.of(22, 0));
        t1.setArrivalTime(LocalTime.of(5, 0));
        t1.setTransportStatus(TransportStatus.AVAILABLE);
        t1.setPartnerId(partnerId);
        t1.setSeats(List.of(
                TransportSeatDTO.builder().transportClass(TransportClass.AC_SLEEPER).price(1200.0).totalSeats(30).build(),
                TransportSeatDTO.builder().transportClass(TransportClass.SEATER).price(600.0).totalSeats(20).build()));
        transportService.addTransport(t1);

        TransportDTO t2 = new TransportDTO();
        t2.setTransportNumber(102);
        t2.setSource("Mumbai");
        t2.setDestination("Pune");
        t2.setTransportType("BUS");
        t2.setDepartureTime(LocalTime.of(7, 30));
        t2.setArrivalTime(LocalTime.of(11, 0));
        t2.setTransportStatus(TransportStatus.AVAILABLE);
        t2.setPartnerId(partnerId);
        t2.setSeats(List.of(
                TransportSeatDTO.builder().transportClass(TransportClass.AC_SEATER).price(450.0).totalSeats(40).build()));
        transportService.addTransport(t2);

        log.info("Seeded 2 transports.");
    }

    private void seedPackages(Long partnerId) {
        TravelPackageDTO p1 = new TravelPackageDTO();
        p1.setPackageName("Goa Getaway");
        p1.setSource("Delhi");
        p1.setDestination("Goa");
        p1.setPrice(25000.0);
        p1.setDurationDays(4);
        p1.setTotalSlots(30);
        p1.setDescription("Beaches, nightlife and a beachfront resort stay.");
        p1.setCategory(TravelPackageCategory.WEEKEND_GETAWAY);
        p1.setStatus(PackageStatus.AVAILABLE);
        p1.setPartnerId(partnerId);
        travelPackageService.addPackage(p1);

        TravelPackageDTO p2 = new TravelPackageDTO();
        p2.setPackageName("Manali Adventure");
        p2.setSource("Delhi");
        p2.setDestination("Manali");
        p2.setPrice(32000.0);
        p2.setDurationDays(6);
        p2.setTotalSlots(20);
        p2.setDescription("Trekking, paragliding and mountain camps.");
        p2.setCategory(TravelPackageCategory.ADVENTURE);
        p2.setStatus(PackageStatus.AVAILABLE);
        p2.setPartnerId(partnerId);
        travelPackageService.addPackage(p2);

        log.info("Seeded 2 travel packages.");
    }
}
