package com.cts.config;

import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
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
 * Seeds a sample catalog (partners + flights/hotels/transports/packages) at startup.
 * Builds the same create-DTOs the API uses and calls the services, so all validation and
 * partner-type checks apply. Idempotent: skips entirely once any partner exists.
 * Runs after {@link DataSeeder} (users) via @Order.
 */
@Component
@Order(2)
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
        // 10 Flights: Delhi to Mumbai
        for (int i = 1; i <= 10; i++) {
            FlightDTO f = new FlightDTO();
            f.setFlightNumber("DLM-" + (100 + i));
            f.setPartnerId(partnerId);
            f.setSource("Delhi");
            f.setDestination("Mumbai");
            f.setDepartureTime(LocalTime.of(8 + (i % 4), 30));
            f.setArrivalTime(LocalTime.of(10 + (i % 4), 45));
            f.setStatus(FlightStatus.SCHEDULED);
            f.setSeats(List.of(
                    FlightSeatDTO.builder().seatType(SeatType.ECONOMY).price(4000.0 + (i * 100)).totalSeats(120).build(),
                    FlightSeatDTO.builder().seatType(SeatType.BUSINESS).price(12000.0 + (i * 200)).totalSeats(20).build()));
            flightService.addFlight(f);
        }

        // 10 More Flights: Various routes
        String[][] routes = {
            {"Bangalore", "Chennai"}, {"Delhi", "Kolkata"}, {"Mumbai", "Goa"}, {"Chennai", "Kolkata"},
            {"Delhi", "Bangalore"}, {"Mumbai", "Delhi"}, {"Kolkata", "Bangalore"}, {"Goa", "Mumbai"},
            {"Delhi", "Chennai"}, {"Bangalore", "Mumbai"}
        };
        for (int i = 0; i < 10; i++) {
            FlightDTO f = new FlightDTO();
            f.setFlightNumber("MIX-" + (200 + i));
            f.setPartnerId(partnerId);
            f.setSource(routes[i][0]);
            f.setDestination(routes[i][1]);
            f.setDepartureTime(LocalTime.of(12 + (i % 6), 0));
            f.setArrivalTime(LocalTime.of(14 + (i % 6), 30));
            f.setStatus(FlightStatus.SCHEDULED);
            f.setSeats(List.of(
                    FlightSeatDTO.builder().seatType(SeatType.ECONOMY).price(3000.0 + (i * 50)).totalSeats(100).build()));
            flightService.addFlight(f);
        }

        log.info("Seeded 20 flights.");
    }

    private void seedHotels(Long partnerId) {
        String[] cities = {"Mumbai", "Goa", "Delhi", "Bangalore", "Chennai", "Kolkata", "Jaipur", "Udaipur", "Shimla", "Manali"};
        for (int i = 1; i <= 20; i++) {
            HotelDTO h = new HotelDTO();
            h.setHotelName("Hotel " + (i == 1 ? "Taj Palace" : "StayCo " + i));
            h.setRatings((i % 3) + 3); // 3 to 5 stars
            h.setCity(cities[i % cities.length]);
            h.setAddress("Street " + i + ", " + cities[i % cities.length]);
            h.setContactNo("98765432" + (i % 10));
            h.setEmailId("hotel" + i + "@mail.com");
            h.setStatus(HotelStatus.AVAILABLE);
            h.setPartnerId(partnerId);
            h.setRooms(List.of(
                    HotelRoomDTO.builder().roomType(HotelRoomType.STANDARD).price(4000.0 + (i * 100)).totalRooms(50).build(),
                    HotelRoomDTO.builder().roomType(HotelRoomType.SUITE).price(12000.0 + (i * 200)).totalRooms(10).build()));
            hotelService.addHotel(h);
        }
        log.info("Seeded 20 hotels.");
    }

    private void seedTransports(Long partnerId) {
        // 10 Transports: Delhi to Jaipur
        for (int i = 1; i <= 10; i++) {
            TransportDTO t = new TransportDTO();
            t.setTransportNumber(100 + i);
            t.setSource("Delhi");
            t.setDestination("Jaipur");
            t.setTransportType("BUS");
            t.setDepartureTime(LocalTime.of(20 + (i % 4), 0));
            t.setArrivalTime(LocalTime.of(2 + (i % 4), 0));
            t.setTransportStatus(TransportStatus.AVAILABLE);
            t.setPartnerId(partnerId);
            t.setSeats(List.of(
                    TransportSeatDTO.builder().transportClass(TransportClass.AC_SLEEPER).price(1000.0 + (i * 50)).totalSeats(30).build(),
                    TransportSeatDTO.builder().transportClass(TransportClass.SEATER).price(500.0 + (i * 20)).totalSeats(20).build()));
            transportService.addTransport(t);
        }

        // 10 More Transports: Various routes
        String[][] routes = {
            {"Mumbai", "Pune"}, {"Bangalore", "Mysore"}, {"Chennai", "Pondicherry"}, {"Delhi", "Agra"},
            {"Kolkata", "Digha"}, {"Hyderabad", "Vijayawada"}, {"Ahmedabad", "Rajkot"}, {"Surat", "Mumbai"},
            {"Jaipur", "Bikaner"}, {"Udaipur", "Jodhpur"}
        };
        for (int i = 0; i < 10; i++) {
            TransportDTO t = new TransportDTO();
            t.setTransportNumber(200 + i);
            t.setSource(routes[i][0]);
            t.setDestination(routes[i][1]);
            t.setTransportType("BUS");
            t.setDepartureTime(LocalTime.of(6 + (i % 6), 30));
            t.setArrivalTime(LocalTime.of(11 + (i % 6), 0));
            t.setTransportStatus(TransportStatus.AVAILABLE);
            t.setPartnerId(partnerId);
            t.setSeats(List.of(
                    TransportSeatDTO.builder().transportClass(TransportClass.AC_SEATER).price(600.0 + (i * 30)).totalSeats(40).build()));
            transportService.addTransport(t);
        }

        log.info("Seeded 20 transports.");
    }

    private void seedPackages(Long partnerId) {
        String[] destinations = {"Goa", "Manali", "Kerala", "Sikkim", "Rajasthan", "Andaman", "Ladakh", "Kashmir", "Thailand", "Bali"};
        TravelPackageCategory[] categories = TravelPackageCategory.values();

        for (int i = 1; i <= 20; i++) {
            TravelPackageDTO p = new TravelPackageDTO();
            String dest = destinations[i % destinations.length];
            p.setPackageName(dest + " Tour " + i);
            p.setSource("Delhi");
            p.setDestination(dest);
            p.setPrice(20000.0 + (i * 1000));
            p.setDurationDays(3 + (i % 7));
            p.setTotalSlots(20 + (i % 10));
            p.setDescription("Wonderful tour to " + dest + " with all amenities.");
            p.setCategory(categories[i % categories.length]);
            p.setStatus(PackageStatus.AVAILABLE);
            p.setPartnerId(partnerId);
            p.setDayWisePlan("[{\"day\":1,\"title\":\"Arrival\",\"activities\":\"Arrival and check-in.\"},{\"day\":2,\"title\":\"Tour\",\"activities\":\"Local sightseeing in " + dest + ".\"}]");
            travelPackageService.addPackage(p);
        }
        log.info("Seeded 20 travel packages.");
    }
}
