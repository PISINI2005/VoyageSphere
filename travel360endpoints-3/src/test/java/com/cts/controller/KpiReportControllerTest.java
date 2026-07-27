//package com.cts.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import com.cts.config.JWTUtil;
//import com.cts.dto.KpiReportResponseDTO;
//import com.cts.exception.GlobalExceptionHandler;
//import com.cts.service.KpiReportService;
//
//@WebMvcTest(KpiReportController.class)
//@AutoConfigureMockMvc(addFilters = false)
//@Import(GlobalExceptionHandler.class) // Ensures MockMvc passes through your exact application exception filters
//class KpiReportControllerTest {
//
//    @MockitoBean
//    private JWTUtil jwtUtil;
//
//    @MockitoBean
//    private KpiReportService kpiService;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    private KpiReportResponseDTO mockDTO() {
//        KpiReportResponseDTO dto = new KpiReportResponseDTO();
//        dto.setReportId(1L);
//        dto.setTotalRevenue(1000.0);
//        dto.setTotalBookings(10L);
//        return dto;
//    }
//
//    // =========================================================================
//    // SUCCESS PATHS
//    // =========================================================================
//
//    @Test
//    void testGenerateMonthly_Success() throws Exception {
//        when(kpiService.generateMonthlyReport(eq(5), eq(2025)))
//                .thenReturn(mockDTO());
//
//        mockMvc.perform(post("/api/v1/reports/kpi/generate")
//                .param("month", "5")
//                .param("year", "2025"))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.reportId").value(1L));
//    }
//
//    @Test
//    void testGenerateCustom_Success() throws Exception {
//        when(kpiService.generateCustomReport(any(), any()))
//                .thenReturn(mockDTO());
//
//        mockMvc.perform(post("/api/v1/reports/kpi/generate-custom")
//                .param("startDate", "2025-01-01T00:00:00")
//                .param("endDate", "2025-01-05T00:00:00"))
//                .andExpect(status().isOk())   // computed-only, not created
//                .andExpect(jsonPath("$.reportId").value(1L));
//    }
//
//    @Test
//    void testGetYearly_Success() throws Exception {
//        when(kpiService.generateYearlyReport(2025))
//                .thenReturn(List.of(mockDTO()));
//
//        mockMvc.perform(get("/api/v1/reports/kpi/yearly/2025"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].reportId").value(1L));
//    }
//
//    @Test
//    void testGetAll_Success() throws Exception {
//        when(kpiService.getAllReports())
//                .thenReturn(List.of(mockDTO()));
//
//        mockMvc.perform(get("/api/v1/reports/kpi"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(1));
//    }
//
//    @Test
//    void testGetById_Success() throws Exception {
//        when(kpiService.getReportById(1L))
//                .thenReturn(mockDTO());
//
//        mockMvc.perform(get("/api/v1/reports/kpi/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.reportId").value(1L));
//    }
//
//    // =========================================================================
//    // EDGE CASES & VALIDATION PATHS
//    // =========================================================================
//
//    @Test
//    void testGetAll_EmptyList() throws Exception {
//        when(kpiService.getAllReports())
//                .thenReturn(List.of());
//
//        mockMvc.perform(get("/api/v1/reports/kpi"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(0));
//    }
//
//    @Test
//    void testGenerateMonthly_BadRequest_MissingYear() throws Exception {
//        // Missing required 'year' parameter goes to your generic Exception handler -> 500 Internal Server Error
//        mockMvc.perform(post("/api/v1/reports/kpi/generate")
//                .param("month", "5")) 
//                .andExpect(status().isInternalServerError()); 
//    }
//
//    @Test
//    void testGenerateCustom_BadRequest_MissingEndDate() throws Exception {
//        // Missing required 'endDate' parameter goes to your generic Exception handler -> 500 Internal Server Error
//        mockMvc.perform(post("/api/v1/reports/kpi/generate-custom")
//                .param("startDate", "2025-01-01T00:00:00")) 
//                .andExpect(status().isInternalServerError());
//    }
//}