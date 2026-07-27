//package com.cts.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import com.cts.config.AuthenticatedUserProvider;
//import com.cts.config.JWTUtil;
//import com.cts.dto.TravelPackageResponseDTO;
//import com.cts.entity.TravelPackage;
//import com.cts.enums.TravelPackageCategory;
//import com.cts.service.AuditLogService;
//import com.cts.service.TravelPackageService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@WebMvcTest(TravelPackageController.class)
//@AutoConfigureMockMvc(addFilters = false)
//class TravelPackageControllerTest {
//
//    @MockitoBean
//    private JWTUtil jwtUtil;
//
//    @MockitoBean
//    private TravelPackageService service;
//
//    @MockitoBean
//    private AuditLogService auditLogService;
//
//    @MockitoBean
//    private AuthenticatedUserProvider authUser;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper mapper;
//
//    // ✅ ADD
//    @Test
//    void testAddPackage() throws Exception {
//
//        when(service.addPackage(any()))
//                .thenReturn(new TravelPackage());
//
//        String body = """
//        {
//          "packageName":"Goa",
//          "source":"Chennai",
//          "destination":"Goa",
//          "price":10000,
//          "durationDays":5,
//          "totalSlots":20,
//          "description":"Trip",
//          "category":"FAMILY",
//          "status":"AVAILABLE",
//          "partnerId":1
//        }
//        """;
//
//        mockMvc.perform(post("/api/v1/packages")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//                .andExpect(status().isCreated());
//    }
//
//    // ✅ UPDATE
//    @Test
//    void testUpdatePackage() throws Exception {
//
//        when(service.updatePackage(eq(1L),any()))
//                .thenReturn(new TravelPackage());
//
//        String body = """
//        {
//          "packageName":"Goa",
//          "source":"Chennai",
//          "destination":"Goa",
//          "price":10000,
//          "durationDays":5,
//          "totalSlots":20,
//          "description":"Trip",
//          "category":"FAMILY",
//          "status":"AVAILABLE",
//          "partnerId":1
//        }
//        """;
//
//        mockMvc.perform(put("/api/v1/packages/1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ UPDATE STATUS
//    @Test
//    void testUpdatePackageStatus() throws Exception {
//
//        when(service.updatePackageStatus(eq(1L), any()))
//                .thenReturn(new TravelPackage());
//
//        String body = """
//        {
//          "status":"INACTIVE"
//        }
//        """;
//
//        mockMvc.perform(patch("/api/v1/packages/1/status")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ GET ALL
//    @Test
//    void testGetAll() throws Exception {
//
//        when(service.getAllPackages(0, 5))
//                .thenReturn(List.of(new TravelPackageResponseDTO()));
//
//        mockMvc.perform(get("/api/v1/packages"))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ FILTER BY CATEGORY
//    @Test
//    void testGetByCategory() throws Exception {
//
//        when(service.searchByCategory(TravelPackageCategory.FAMILY, 0, 5))
//                .thenReturn(List.of(new TravelPackageResponseDTO()));
//
//        mockMvc.perform(get("/api/v1/packages")
//                .param("category", "FAMILY"))
//                .andExpect(status().isOk());
//    }
//
//    // ✅ VALIDATION FAIL
//    @Test
//    void testValidationFail() throws Exception {
//
//        String body = "{}";
//
//        mockMvc.perform(post("/api/v1/packages")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(body))
//                .andExpect(status().is4xxClientError());
//    }
//}
