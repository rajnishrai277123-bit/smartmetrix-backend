package com.smartmetrix.backend.notification;

import com.smartmetrix.backend.user.User;
import com.smartmetrix.backend.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public Notification createNotification(
            Long userId,
            String title,
            String message,
            String type
    ) {

        Notification notification = new Notification();

        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    // ==========================================
    // NOTIFICATION FOR ALL USERS OF A ROLE
    // ==========================================

    public void createNotificationForRole(
            String role,
            String title,
            String message,
            String type
    ) {

        List<User> users =
                userRepository.findByRoleAndStatus(
                        role,
                        "ACTIVE"
                );

        for (User user : users) {

            createNotification(
                    user.getId(),
                    title,
                    message,
                    type
            );
        }
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                )
                        );

        notification.setRead(true);

        notificationRepository.save(notification);
    }
}