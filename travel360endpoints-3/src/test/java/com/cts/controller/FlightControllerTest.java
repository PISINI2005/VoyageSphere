//package com.cts.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//import org.springframework.http.MediaType;
//
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import com.cts.config.AuthenticatedUserProvider;
//import com.cts.config.JWTUtil;
//import com.cts.dto.FlightResponseDTO;
//import com.cts.entity.Flight;
//import com.cts.entity.FlightSeat;
//import com.cts.enums.SeatType;
//import com.cts.service.AuditLogService;
//import com.cts.service.FlightService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@WebMvcTest(FlightController.class)
//@AutoConfigureMockMvc(addFilters = false)
//public class FlightControllerTest {
//
//    @MockitoBean
//    private JWTUtil jwtUtil;
//
//    @MockBean
//    private FlightService service;
//
//    @MockBean
//    private AuditLogService auditLogService;
//
//    @MockBean
//    private AuthenticatedUserProvider authUser;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper mapper;
//
//    // ✅ ADD FLIGHT
//    @Test
//    public void testAddFlight() throws Exception {
//
//        Flight flight = new Flight();
//        when(service.addFlight(any())).thenReturn(flight);
//
//        String body = """
//        {
//          "flightNumber":"AA-123",
//          "partnerId":1,
//          "source":"Chennai",
//          "destination":"Delhi",
//          "status":"SCHEDULED",
//          "arrivalTime":"10:00:00",
//          "departureTime":"08:00:00",
//          "seats":[{"seatType":"ECONOMY","price":5000,"totalSeats":100}]
//        }
//        """;
//
//        mockMvc.perform(post("/api/v1/flights")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//                .andExpect(status().isCreated());
//    }
//
//    // ✅ Regression: returning a flight WITH seat classes must serialize without
//    // infinite recursion through the FlightSeat -> flight back-reference.
//    @Test
//    public void testAddFlight_withSeats_serializesWithoutRecursion() throws Exception {
//
//        Flight flight = Flight.builder().flightId(1L).flightNumber("AA-123").build();
//        flight.setSeats(List.of(
//                FlightSeat.builder()
//                        .flightSeatId(1L)
//                        .seatType(SeatType.ECONOMY)
//                        .price(5000)
//                        .totalSeats(100)
//                        .flight(flight)
//                        .build()));
//
//        when(service.addFlight(any())).thenReturn(flight);
//
//        String body = """
//        {
//          "flightNumber":"AA-123",
//          "partnerId":1,
//          "source":"Chennai",
//          "destination":"Delhi",
//          "status":"SCHEDULED",
//          "arrivalTime":"10:00:00",
//          "departureTime":"08:00:00",
//          "seats":[{"seatType":"ECONOMY","price":5000,"totalSeats":100}]
//        }
//        """;
//
//        mockMvc.perform(post("/api/v1/flights")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.seats[0].seatType").value("ECONOMY"))
//                .andExpect(jsonPath("$.seats[0].flight").doesNotExist());
//    }
//
//    // ✅ UPDATE FLIGHT
//    @Test
//    public void testUpdateFlight() throws Exception {
//
//        Flight flight = new Flight();
//        when(service.updateFlight(any(), any())).thenReturn(flight);
//
//        String body = """
//        {
//          "flightNumber":"AA-123",
//          "partnerId":1,
//          "source":"Chennai",
//          "destination":"Delhi",
//          "status":"SCHEDULED",
//          "arrivalTime":"10:00:00",
//          "departureTime":"08:00:00",
//          "seats":[{"seatType":"ECONOMY","price":5000,"totalSeats":100}]
//        }
//        """;
//
//        mockMvc.perform(put("/api/v1/flights/1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ UPDATE FLIGHT STATUS
//    @Test
//    public void testUpdateFlightStatus() throws Exception {
//
//        Flight flight = new Flight();
//        when(service.updateFlightStatus(eq(1L), any())).thenReturn(flight);
//
//        String body = """
//        {
//          "status":"CANCELLED"
//        }
//        """;
//
//        mockMvc.perform(patch("/api/v1/flights/1/status")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ GET BY ID
//    @Test
//    public void testGetById() throws Exception {
//
//        FlightResponseDTO flight = FlightResponseDTO.builder().build();
//        when(service.getFlightById(1L)).thenReturn(flight);
//
//        mockMvc.perform(get("/api/v1/flights/1"))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ GET ALL
//    @Test
//    public void testGetAllFlights() throws Exception {
//
//        when(service.getAllFlights(0, 5))
//                .thenReturn(List.of(FlightResponseDTO.builder().build()));
//
//        mockMvc.perform(get("/api/v1/flights"))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ SEARCH (no price filter)
//    @Test
//    public void testSearchFlights() throws Exception {
//
//        when(service.filterFlights("Chennai", "Delhi", null, null, 0, 5))
//                .thenReturn(List.of(FlightResponseDTO.builder().build()));
//
//        mockMvc.perform(get("/api/v1/flights/search")
//                .param("source", "Chennai")
//                .param("destination", "Delhi"))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ SEARCH WITH PRICE FILTER
//    @Test
//    public void testFilterFlights() throws Exception {
//
//        when(service.filterFlights("Chennai", "Delhi", 1000.0, 5000.0, 0, 5))
//                .thenReturn(List.of(FlightResponseDTO.builder().build()));
//
//        mockMvc.perform(get("/api/v1/flights/search")
//                .param("source", "Chennai")
//                .param("destination", "Delhi")
//                .param("min", "1000")
//                .param("max", "5000"))
//                .andExpect(status().isOk());
//    }
//}