package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.dto.NotificationResponseDTO;
import com.cts.entity.Notification;
import com.cts.entity.User;
import com.cts.enums.NotificationCategory;
import com.cts.enums.NotificationStatus;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.NotificationMapper;
import com.cts.repository.NotificationRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository repo;

    @Mock
    private AuthenticatedUserProvider authUser;

    @Spy
    private NotificationMapper notificationMapper = new NotificationMapper();

    @InjectMocks
    private NotificationServiceImpl service;

    // ✅ SEND NOTIFICATION
    @Test
    void sendNotification() {

        User user = new User();
        user.setUserId(1L);

        Notification saved = new Notification();
        saved.setNotificationId(1L);

        when(repo.save(any())).thenReturn(saved);

        service.sendNotification(user, "Test Message", NotificationCategory.PAYMENT);

        verify(repo).save(any());
    }

    // ✅ GET USER NOTIFICATIONS (NON-EMPTY)
    @Test
    void getUserNotifications_nonEmpty() {

        Notification n = new Notification();
        n.setNotificationId(1L);
        n.setMessage("Test");
        n.setCategory(NotificationCategory.PAYMENT);
        n.setStatus(NotificationStatus.UNREAD);
        n.setCreatedDate(LocalDateTime.now());

        // Mock security check
        doNothing().when(authUser).assertCanActAs(1L);

        when(repo.findByUserUserIdOrderByCreatedDateDesc(1L))
                .thenReturn(List.of(n));

        List<NotificationResponseDTO> result = service.getUserNotifications(1L);

        assertFalse(result.isEmpty());
        assertEquals("Test", result.get(0).getMessage());
        verify(authUser).assertCanActAs(1L);
    }

    // ✅ GET USER NOTIFICATIONS (EMPTY LIST)
    @Test
    void getUserNotifications_empty() {

        // Mock security check
        doNothing().when(authUser).assertCanActAs(1L);

        when(repo.findByUserUserIdOrderByCreatedDateDesc(1L))
                .thenReturn(List.of());

        List<NotificationResponseDTO> result = service.getUserNotifications(1L);

        assertTrue(result.isEmpty());
        verify(authUser).assertCanActAs(1L);
    }

    // ✅ MARK AS READ
    @Test
    void markAsRead() {

        User user = new User();
        user.setUserId(1L);

        Notification n = new Notification();
        n.setNotificationId(1L);
        n.setUser(user);
        n.setStatus(NotificationStatus.UNREAD);

        when(repo.findById(1L)).thenReturn(Optional.of(n));
        doNothing().when(authUser).assertCanActAs(1L);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponseDTO result = service.markAsRead(1L);

        assertEquals(NotificationStatus.READ, result.getStatus());
        verify(authUser).assertCanActAs(1L);
    }

    // ✅ MARK AS READ - NOT FOUND
    @Test
    void markAsRead_notFound() {

        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.markAsRead(99L));
        verify(repo, never()).save(any());
    }

    // ✅ MARK ALL AS READ
    @Test
    void markAllAsRead() {

        User user = new User();
        user.setUserId(1L);
        when(authUser.current()).thenReturn(user);

        Notification n1 = new Notification();
        n1.setStatus(NotificationStatus.UNREAD);
        Notification n2 = new Notification();
        n2.setStatus(NotificationStatus.UNREAD);

        when(repo.findByUserUserIdAndStatus(1L, NotificationStatus.UNREAD))
                .thenReturn(List.of(n1, n2));

        int count = service.markAllAsRead();

        assertEquals(2, count);
        assertEquals(NotificationStatus.READ, n1.getStatus());
        verify(repo).saveAll(anyList());
    }
}