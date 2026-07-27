package com.cts.service;

import com.cts.enums.TravelPackageCategory;
import java.time.LocalDate;

public interface SearchService {
	Object search(String type,
            String source,
            String destination,
            String city,
            Double min,
            Double max,
            Integer ratings,
            TravelPackageCategory category,
            LocalDate date,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int page,
            int size);
}