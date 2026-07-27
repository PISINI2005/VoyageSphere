package com.cts.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.config.JWTUtil;
import com.cts.dto.ComplaintResponseDTO;
import com.cts.enums.ComplaintStatus;
import com.cts.service.AuditLogService;
import com.cts.service.ComplaintService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ComplaintController.class)
@AutoConfigureMockMvc(addFilters = false)
class ComplaintControllerTest {

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockBean
    private ComplaintService service;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private AuthenticatedUserProvider authUser;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    // ✅ CREATE
    @Test
    void testCreateComplaint() throws Exception {

        when(service.createComplaint(any()))
                .thenReturn(ComplaintResponseDTO.builder().complaintId(1L).build());

        String body = """
        {
          "subject":"Late refund",
          "description":"My refund is delayed"
        }
        """;

        mockMvc.perform(post("/api/v1/complaints")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());
    }

    // ✅ CREATE - VALIDATION FAIL
    @Test
    void testCreateValidationFail() throws Exception {

        mockMvc.perform(post("/api/v1/complaints")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    // ✅ GET MINE
    @Test
    void testGetMyComplaints() throws Exception {

        when(service.getMyComplaints())
                .thenReturn(List.of(ComplaintResponseDTO.builder().complaintId(1L).build()));

        mockMvc.perform(get("/api/v1/complaints/me"))
                .andExpect(status().isOk());
    }

    // ✅ GET ALL (REVIEW QUEUE)
    @Test
    void testGetComplaints() throws Exception {

        when(service.getComplaints(eq(ComplaintStatus.PENDING)))
                .thenReturn(List.of(ComplaintResponseDTO.builder().complaintId(1L).build()));

        mockMvc.perform(get("/api/v1/complaints").param("status", "PENDING"))
                .andExpect(status().isOk());
    }

    // ✅ GET BY ID
    @Test
    void testGetById() throws Exception {

        when(service.getComplaintById(1L))
                .thenReturn(ComplaintResponseDTO.builder().complaintId(1L).build());

        mockMvc.perform(get("/api/v1/complaints/1"))
                .andExpect(status().isOk());
    }

    // ✅ UPDATE STATUS
    @Test
    void testUpdateStatus() throws Exception {

        when(service.updateStatus(eq(1L), any()))
                .thenReturn(ComplaintResponseDTO.builder()
                        .complaintId(1L).status(ComplaintStatus.RESOLVED).build());

        String body = """
        {
          "status":"RESOLVED",
          "resolutionNote":"Handled"
        }
        """;

        mockMvc.perform(patch("/api/v1/complaints/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk());
    }

    // ✅ UPDATE STATUS - VALIDATION FAIL
    @Test
    void testUpdateStatusValidationFail() throws Exception {

        mockMvc.perform(patch("/api/v1/complaints/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().is4xxClientError());
    }
}
