package com.smartmetrix.backend.notification;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{userId}")
    public List<Notification> getUserNotifications(
            @PathVariable Long userId
    ) {
        return notificationService.getUserNotifications(userId);
    }

    @PostMapping
    public Notification createNotification(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam String type
    ) {
        return notificationService.createNotification(
                userId,
                title,
                message,
                type
        );
    }

    @PutMapping("/{id}/read")
    public String markAsRead(
            @PathVariable Long id
    ) {
        notificationService.markAsRead(id);
        return "Notification marked as read";
    }
}