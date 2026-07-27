package com.cts.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.cts.dto.TravelPackageDTO;
import com.cts.dto.TravelPackageResponseDTO;
import com.cts.entity.TravelPackage;
import com.cts.enums.PackageStatus;
import com.cts.enums.TravelPackageCategory;

public interface TravelPackageService {

	TravelPackage addPackage(TravelPackageDTO dto);

	TravelPackage updatePackage(Long id, TravelPackageDTO dto);

	TravelPackage updatePackageStatus(Long id, PackageStatus status);

	TravelPackageResponseDTO getTravelPackageById(Long id, LocalDate travelDate);

	Page<TravelPackageResponseDTO> getAllPackages(int page, int size, Double min, Double max);

	Page<TravelPackageResponseDTO> searchByCategory(TravelPackageCategory category, int page, int size, Double min, Double max);

	Page<TravelPackageResponseDTO> searchByCategoryWithAvailability(
	    TravelPackageCategory category, LocalDate travelDate, int page, int size, Double min, Double max
	);

	Page<TravelPackageResponseDTO> getAllPackagesWithAvailability(
	    LocalDate travelDate, int page, int size, Double min, Double max
	);

	List<TravelPackageResponseDTO> getPackagesByRoute(String source, String destination, LocalDate travelDate, TravelPackageCategory category, Double min, Double max);

	void deletePackage(Long id);
}
