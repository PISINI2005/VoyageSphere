package com.cts.serviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.dto.NotificationRequestDTO;
import com.cts.dto.NotificationResponseDTO;
import com.cts.entity.Notification;
import com.cts.enums.NotificationCategory;
import com.cts.enums.NotificationStatus;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.NotificationMapper;
import com.cts.repository.NotificationRepository;
import com.cts.service.NotificationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo;
    private final NotificationMapper notificationMapper;
    private final AuthenticatedUserProvider authUser;


    @Override
    public void sendNotification(Long userId, String message, NotificationCategory category) {

        log.info("Sending notification to userId: {} with category: {}", userId, category);

        Notification notification = notificationMapper.toEntity(userId, message, category);

        Notification savedNotification = notificationRepo.save(notification);

        log.info("Notification created successfully with ID: {}", savedNotification.getNotificationId());
    }

    @Override
    public NotificationResponseDTO create(NotificationRequestDTO dto) {

        log.info("Sending notification to userId: {} with category: {}", dto.getUserId(), dto.getCategory());

        Notification notification = notificationMapper.toEntity(dto);

        Notification savedNotification = notificationRepo.save(notification);

        log.info("Notification created successfully with ID: {}", savedNotification.getNotificationId());

        return notificationMapper.toResponse(savedNotification);
    }


    @Override
    public List<NotificationResponseDTO> getUserNotifications(Long userId) {

        log.info("Fetching notifications for userId: {}", userId);

        // Security: Users can only view their own notifications
        authUser.assertCanActAs(userId);

        List<NotificationResponseDTO> notifications = notificationRepo.findByUserId(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();

        log.info("Total notifications fetched for userId {}: {}", userId, notifications.size());

        return notifications;
    }

    @Override
    public List<NotificationResponseDTO> getMyNotifications() {
        return getUserNotifications(authUser.current().getUserId());
    }

    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(Long notificationId) {

        log.info("Marking notification as read: {}", notificationId);

        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> {
                    log.error("Notification not found with id {}", notificationId);
                    return new ResourceNotFoundException("Notification not found");
                });

        // Security: users can only mark their own notifications as read.
        authUser.assertCanActAs(notification.getUserId());

        notification.setStatus(NotificationStatus.READ);
        Notification saved = notificationRepo.save(notification);

        log.info("Notification {} marked as read", notificationId);

        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public int markAllAsRead() {

        Long userId = authUser.current().getUserId();
        log.info("Marking all notifications as read for userId: {}", userId);

        List<Notification> unread = notificationRepo.findByUserIdAndStatus(userId, NotificationStatus.UNREAD);
        unread.forEach(n -> n.setStatus(NotificationStatus.READ));
        notificationRepo.saveAll(unread);

        log.info("Marked {} notifications as read for userId: {}", unread.size(), userId);

        return unread.size();
    }
}
