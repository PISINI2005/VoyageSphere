package com.cts.controller;

import com.cts.dto.KpiReportResponseDTO;
import com.cts.service.KpiReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/kpi-reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KpiReportController {

    private final KpiReportService kpiReportService;

    @GetMapping
    public ResponseEntity<?> getReport(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
            
       
        if (year != null && month == null) {
            return ResponseEntity.ok(kpiReportService.generateMonthlyTrendReport(year));
        }
        
        // Otherwise, return the standard object snapshot
        return ResponseEntity.ok(kpiReportService.generateReport(year, month));
    }
}