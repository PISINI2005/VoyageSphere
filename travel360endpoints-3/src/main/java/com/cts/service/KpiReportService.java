package com.cts.service;

import java.time.LocalDateTime;
import java.util.List;

import com.cts.dto.KpiReportResponseDTO;

public interface KpiReportService {
	public KpiReportResponseDTO generateReport(Integer year, Integer month);
	public List<KpiReportResponseDTO> generateMonthlyTrendReport(int year);
}
