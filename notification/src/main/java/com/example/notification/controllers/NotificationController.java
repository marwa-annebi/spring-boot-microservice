package com.example.notification.controllers;

import com.example.notification.dtos.NotificationDto;
import com.example.notification.dtos.NotificationEventDto;
import com.example.notification.models.Notification;
import com.example.notification.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    //    private final NotificationService notificationService;
//
//    @PostMapping
//    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
//        Notification savedNotification = notificationService.createNotification(
//                notification.getUserId(),
//                notification.getPostId(),
//                notification.getActionUserId(),
//                notification.getType());
//        return ResponseEntity.ok(savedNotification);
//    }
//
//    @GetMapping("/{userId}")
//    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable String userId) {
//        List<Notification> notifications = notificationService.getUserNotifications(userId);
//        return ResponseEntity.ok(notifications);
//    }
//
//    @PatchMapping("/{notificationId}/read")
//    public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
//        notificationService.markNotificationAsRead(notificationId);
//        return ResponseEntity.noContent().build();
//    }
//}
    private final NotificationService notificationService;
    private NotificationDto notificationDto;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Void> createNotification(@RequestBody NotificationDto notificationDto) {
        this.notificationDto = notificationDto;
        notificationService.createNotification(notificationDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
//    private final NotificationService notificationService;
//
//    @PostMapping("/create")
//    public ResponseEntity<Notification> createNotification(
//            @RequestParam String userId,
//            @RequestParam String postId,
//            @RequestParam String actionUserId,
//            @RequestParam String type) {
//        Notification notification = notificationService.createNotification(userId, postId, actionUserId, type);
//        return ResponseEntity.ok(notification);
//    }
//
//    @GetMapping("/{userId}")
//    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable String userId) {
//        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
//        return ResponseEntity.ok(notifications);
//    }
//
//    @PatchMapping("/{notificationId}/read")
//    public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
//        notificationService.markNotificationAsRead(notificationId);
//        return ResponseEntity.ok().build();
//    }
//    @PostMapping("/event")
//    public ResponseEntity<Notification> handleEvent(@RequestBody NotificationEventDto notificationEventDto) {
//        Notification notification = notificationService.createNotification(
//                notificationEventDto.getUserId(),
//                notificationEventDto.getPostId(),
//                notificationEventDto.getActionUserId(),
//                notificationEventDto.getType()
//        );
//        return ResponseEntity.ok(notification);
//    }
//    @GetMapping("/{userId}")
//    public ResponseEntity<List<Notification>> getNotifications(@PathVariable String userId) {
//        List<Notification> notifications = notificationService.getNotificationsForUser(userId);
//        return ResponseEntity.ok(notifications);
//    }
////
////    @PatchMapping("/{notificationId}/read")
////    public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
////        notificationService.markNotificationAsRead(notificationId);
////        return ResponseEntity.noContent().build();
////    }
//
//    @PostMapping("/like")
//    public ResponseEntity<Void> handleLikeNotification(
//            @RequestParam String likerId, @RequestParam String postId) {
//        notificationService.generateLikeNotification(likerId, postId);
//        return ResponseEntity.noContent().build();
//    }
//
//    @PostMapping("/comment")
//    public ResponseEntity<Void> handleCommentNotification(
//            @RequestParam String commenterId, @RequestParam String postId) {
//        notificationService.generateCommentNotification(commenterId, postId);
//        return ResponseEntity.noContent().build();
//    }
//}
