package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.dto.ComplaintRequestDTO;
import com.cts.dto.ComplaintResponseDTO;
import com.cts.dto.ComplaintStatusUpdateDTO;
import com.cts.entity.Booking;
import com.cts.entity.Complaint;
import com.cts.entity.Invoice;
import com.cts.entity.User;
import com.cts.enums.ComplaintStatus;
import com.cts.enums.ComplaintTargetType;
import com.cts.exception.InvalidComplaintException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.exception.UserNotFoundException;
import com.cts.mapper.ComplaintMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.ComplaintRepository;
import com.cts.repository.InvoiceRepository;
import com.cts.repository.PaymentRepository;
import com.cts.repository.UserRepository;
import com.cts.service.NotificationService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceImplTest {

    @Mock
    private ComplaintRepository complaintRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private AuthenticatedUserProvider authUser;

    @Mock
    private NotificationService notificationService;

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private InvoiceRepository invoiceRepo;

    @Mock
    private PaymentRepository paymentRepo;

    @Spy
    private ComplaintMapper complaintMapper = new ComplaintMapper();

    @InjectMocks
    private ComplaintServiceImpl service;

    private User customer() {
        User u = new User();
        u.setUserId(1L);
        return u;
    }

    // ✅ CREATE COMPLAINT
    @Test
    void createComplaint() {

        when(authUser.current()).thenReturn(customer());
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer()));

        Complaint saved = Complaint.builder()
                .complaintId(5L)
                .subject("Late refund")
                .description("My refund is delayed")
                .status(ComplaintStatus.PENDING)
                .user(customer())
                .build();
        when(complaintRepo.save(any())).thenReturn(saved);

        ComplaintRequestDTO dto = ComplaintRequestDTO.builder()
                .subject("Late refund")
                .description("My refund is delayed")
                .build();

        ComplaintResponseDTO result = service.createComplaint(dto);

        assertEquals(5L, result.getComplaintId());
        assertEquals(ComplaintStatus.PENDING, result.getStatus());
        verify(complaintRepo).save(any());
    }

    // ✅ CREATE COMPLAINT - USER MISSING
    @Test
    void createComplaint_userNotFound() {

        when(authUser.current()).thenReturn(customer());
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        ComplaintRequestDTO dto = ComplaintRequestDTO.builder()
                .subject("x").description("y").build();

        assertThrows(UserNotFoundException.class, () -> service.createComplaint(dto));
        verify(complaintRepo, never()).save(any());
    }

    // ✅ GET MY COMPLAINTS
    @Test
    void getMyComplaints() {

        when(authUser.current()).thenReturn(customer());

        Complaint c = Complaint.builder()
                .complaintId(1L).subject("s").description("d")
                .status(ComplaintStatus.PENDING).user(customer())
                .createdDate(LocalDateTime.now()).build();
        when(complaintRepo.findByUserUserId(1L)).thenReturn(List.of(c));

        List<ComplaintResponseDTO> result = service.getMyComplaints();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getComplaintId());
    }

    // ✅ GET ALL (NO FILTER)
    @Test
    void getComplaints_noFilter() {

        when(complaintRepo.findAll()).thenReturn(List.of());

        assertTrue(service.getComplaints(null).isEmpty());
        verify(complaintRepo).findAll();
        verify(complaintRepo, never()).findByStatus(any());
    }

    // ✅ GET BY STATUS
    @Test
    void getComplaints_byStatus() {

        Complaint c = Complaint.builder().complaintId(1L).status(ComplaintStatus.PENDING).build();
        when(complaintRepo.findByStatus(ComplaintStatus.PENDING)).thenReturn(List.of(c));

        List<ComplaintResponseDTO> result = service.getComplaints(ComplaintStatus.PENDING);

        assertEquals(1, result.size());
        verify(complaintRepo).findByStatus(ComplaintStatus.PENDING);
    }

    // ✅ GET BY ID - NOT FOUND
    @Test
    void getComplaintById_notFound() {

        when(complaintRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getComplaintById(99L));
    }

    // ✅ UPDATE STATUS -> RESOLVED stamps resolvedDate
    @Test
    void updateStatus_resolved() {

        Complaint existing = Complaint.builder()
                .complaintId(1L).subject("s").description("d")
                .status(ComplaintStatus.PENDING).user(customer()).build();
        when(complaintRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(complaintRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ComplaintStatusUpdateDTO dto = ComplaintStatusUpdateDTO.builder()
                .status(ComplaintStatus.RESOLVED)
                .resolutionNote("Refund processed")
                .build();

        ComplaintResponseDTO result = service.updateStatus(1L, dto);

        assertEquals(ComplaintStatus.RESOLVED, result.getStatus());
        assertEquals("Refund processed", result.getResolutionNote());
        assertNotNull(result.getResolvedDate());
        // Customer is notified when the complaint reaches a terminal outcome.
        verify(notificationService).sendNotification(any(), anyString(), any());
    }

    // ✅ UPDATE STATUS - ILLEGAL TRANSITION (terminal state is locked)
    @Test
    void updateStatus_illegalTransition() {

        Complaint existing = Complaint.builder()
                .complaintId(1L).status(ComplaintStatus.RESOLVED).user(customer()).build();
        when(complaintRepo.findById(1L)).thenReturn(Optional.of(existing));

        ComplaintStatusUpdateDTO dto = ComplaintStatusUpdateDTO.builder()
                .status(ComplaintStatus.PENDING).build();

        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(1L, dto));
        verify(complaintRepo, never()).save(any());
        verify(notificationService, never()).sendNotification(any(), anyString(), any());
    }

    // ✅ UPDATE STATUS -> IN_PROGRESS leaves resolvedDate null
    @Test
    void updateStatus_inProgress() {

        Complaint existing = Complaint.builder()
                .complaintId(1L).status(ComplaintStatus.PENDING).user(customer()).build();
        when(complaintRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(complaintRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ComplaintStatusUpdateDTO dto = ComplaintStatusUpdateDTO.builder()
                .status(ComplaintStatus.IN_PROGRESS).build();

        ComplaintResponseDTO result = service.updateStatus(1L, dto);

        assertEquals(ComplaintStatus.IN_PROGRESS, result.getStatus());
        assertNull(result.getResolvedDate());
    }

    // ✅ UPDATE STATUS - NOT FOUND
    @Test
    void updateStatus_notFound() {

        when(complaintRepo.findById(99L)).thenReturn(Optional.empty());

        ComplaintStatusUpdateDTO dto = ComplaintStatusUpdateDTO.builder()
                .status(ComplaintStatus.RESOLVED).build();

        assertThrows(ResourceNotFoundException.class, () -> service.updateStatus(99L, dto));
        verify(complaintRepo, never()).save(any());
    }

    // ✅ CREATE COMPLAINT - about the customer's OWN booking succeeds and carries the target
    @Test
    void createComplaint_withOwnedBooking_succeeds() {

        when(authUser.current()).thenReturn(customer());
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer()));
        when(bookingRepo.findById(50L))
                .thenReturn(Optional.of(Booking.builder().bookingId(50L).user(customer()).build()));
        when(complaintRepo.save(any())).thenAnswer(inv -> {
            Complaint c = inv.getArgument(0);
            c.setComplaintId(9L);
            return c;
        });

        ComplaintRequestDTO dto = ComplaintRequestDTO.builder()
                .subject("Wrong seat").description("Got the wrong seat")
                .targetType(ComplaintTargetType.BOOKING).targetId(50L)
                .build();

        ComplaintResponseDTO result = service.createComplaint(dto);

        assertEquals(ComplaintTargetType.BOOKING, result.getTargetType());
        assertEquals(50L, result.getTargetId());
        verify(authUser).assertCanActAs(1L);
        verify(complaintRepo).save(any());
    }

    // ✅ CREATE COMPLAINT - target type without id is rejected (400)
    @Test
    void createComplaint_targetTypeWithoutId_throwsInvalidComplaint() {

        when(authUser.current()).thenReturn(customer());
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer()));

        ComplaintRequestDTO dto = ComplaintRequestDTO.builder()
                .subject("s").description("d")
                .targetType(ComplaintTargetType.BOOKING) // no targetId
                .build();

        assertThrows(InvalidComplaintException.class, () -> service.createComplaint(dto));
        verify(complaintRepo, never()).save(any());
    }

    // ✅ CREATE COMPLAINT - referencing a non-existent target is rejected (404)
    @Test
    void createComplaint_targetNotFound_throwsResourceNotFound() {

        when(authUser.current()).thenReturn(customer());
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer()));
        when(invoiceRepo.findById(60L)).thenReturn(Optional.empty());

        ComplaintRequestDTO dto = ComplaintRequestDTO.builder()
                .subject("s").description("d")
                .targetType(ComplaintTargetType.INVOICE).targetId(60L)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> service.createComplaint(dto));
        verify(complaintRepo, never()).save(any());
    }

    // ✅ CREATE COMPLAINT - referencing someone else's record is denied (403)
    @Test
    void createComplaint_othersBooking_throwsAccessDenied() {

        User other = new User();
        other.setUserId(2L);

        when(authUser.current()).thenReturn(customer());
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer()));
        when(invoiceRepo.findById(70L)).thenReturn(Optional.of(
                Invoice.builder().invoiceId(70L)
                        .booking(Booking.builder().bookingId(7L).user(other).build())
                        .build()));
        doThrow(new AccessDeniedException("You can only access your own resources"))
                .when(authUser).assertCanActAs(2L);

        ComplaintRequestDTO dto = ComplaintRequestDTO.builder()
                .subject("s").description("d")
                .targetType(ComplaintTargetType.INVOICE).targetId(70L)
                .build();

        assertThrows(AccessDeniedException.class, () -> service.createComplaint(dto));
        verify(complaintRepo, never()).save(any());
    }
}
