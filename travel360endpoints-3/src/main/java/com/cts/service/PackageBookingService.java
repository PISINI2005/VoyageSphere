package com.cts.service;

import com.cts.dto.BookingPackageDTO;
import com.cts.dto.BookingPackageResponseDTO;

public interface PackageBookingService {
    BookingPackageResponseDTO createPackageBooking(BookingPackageDTO dto);
}
