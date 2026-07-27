package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUser;
import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.InvoiceDTO;
import com.cts.dto.InvoiceResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Invoice;
import com.cts.enums.AuditEntity;
import com.cts.enums.BookingStatus;
import com.cts.enums.LogType;
import com.cts.exception.InvalidBookingException;
import com.cts.exception.InvoiceNotFoundException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.InvoiceMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.InvoiceRepository;
import com.cts.service.AuditLogService;
import com.cts.service.InvoiceService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final BookingRepository bookingRepo;
    private final AuthenticatedUserProvider authUser;
    private final AuditLogService auditLogService;
    private final InvoiceMapper invoiceMapper;

    @Override
    @Transactional
    public InvoiceResponseDTO createInvoice(InvoiceDTO dto) {

        log.info("Creating invoice for bookingId: {}", dto.getBookingId());

        Booking booking = bookingRepo.findById(dto.getBookingId())
                .orElseThrow(() -> {
                    log.error("Booking not found with ID: {}", dto.getBookingId());
                    return new ResourceNotFoundException("Booking not found");
                });

        // Security: only the booking owner (or staff) can raise its invoice.
        authUser.assertCanActAs(booking.getUserId());

        // A cancelled/failed booking is not billable.
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.FAILED) {
            log.error("Cannot invoice booking {} in status {}", booking.getBookingId(), booking.getStatus());
            throw new InvalidBookingException("Cannot invoice a " + booking.getStatus() + " booking");
        }

        // Idempotency: at most one invoice per booking.
        if (!invoiceRepo.findByBookingBookingId(booking.getBookingId()).isEmpty()) {
            log.error("Invoice already exists for booking {}", booking.getBookingId());
            throw new InvalidBookingException("Invoice already exists for booking " + booking.getBookingId());
        }

        Invoice invoice = invoiceMapper.toEntity(booking);

        invoice = invoiceRepo.save(invoice);
        auditLogService.logAction(AuditActions.CREATE_INVOICE, AuditEntity.INVOICE, invoice.getInvoiceId(), currentUserId(), LogType.INFO);

        log.info("Invoice created successfully with ID: {}", invoice.getInvoiceId());

        return invoiceMapper.toResponse(invoice);
    }

    @Override
    public List<InvoiceResponseDTO> getInvoicesByBooking(Long bookingId) {

        log.info("Fetching invoices for bookingId: {}", bookingId);

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found with ID: {}", bookingId);
                    return new ResourceNotFoundException("Booking not found");
                });

        authUser.assertCanActAs(booking.getUserId());

        List<Invoice> invoices = invoiceRepo.findByBookingBookingId(bookingId);

        log.info("Found {} invoices for bookingId: {}", invoices.size(), bookingId);

        return invoices.stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    @Override
    public List<InvoiceResponseDTO> getInvoicesByUser(Long userId) {

        log.info("Fetching invoices for userId: {}", userId);

        // Security: Users can only view their own invoices
        authUser.assertCanActAs(userId);

        // Get all bookings for this user, then get invoices for those bookings
        List<Booking> userBookings = bookingRepo.findByUserId(userId);

        List<Invoice> invoices = userBookings.stream()
                .flatMap(booking -> invoiceRepo.findByBookingBookingId(booking.getBookingId()).stream())
                .toList();

        log.info("Found {} invoices for userId: {}", invoices.size(), userId);

        return invoices.stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    @Override
    public List<InvoiceResponseDTO> getMyInvoices() {
        return getInvoicesByUser(authUser.current().getUserId());
    }

    @Override
    public List<InvoiceResponseDTO> getAllInvoices() {

        log.info("Fetching all invoices");

        List<Invoice> invoices = invoiceRepo.findAll();

        log.info("Total invoices fetched: {}", invoices.size());

        return invoices.stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    @Override
    public InvoiceResponseDTO getInvoiceById(Long id) {

        log.info("Fetching invoice with ID: {}", id);

        Invoice invoice = invoiceRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Invoice not found with ID: {}", id);
                    return new InvoiceNotFoundException("Invoice not found");
                });

        authUser.assertCanActAs(invoice.getBooking().getUserId());

        log.info("Invoice fetched successfully with ID: {}", id);

        return invoiceMapper.toResponse(invoice);
    }

    private Long currentUserId() {
        AuthenticatedUser caller = authUser.currentOrNull();
        return caller != null ? caller.getUserId() : null;
    }
}
