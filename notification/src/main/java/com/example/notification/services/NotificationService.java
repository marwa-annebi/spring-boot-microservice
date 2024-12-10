package com.example.notification.services;

import com.example.notification.client.PostClient;
import com.example.notification.client.UserClient;
import com.example.notification.dtos.NotificationDto;
import com.example.notification.models.Notification;
import com.example.notification.models.Post;
import com.example.notification.models.User;
import com.example.notification.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
//    private final NotificationRepository notificationRepository;
//    private final UserClient userClient;
//
//    public Notification createNotification(User userId, Post postId, String actionUserId, String type) {
//        Notification notification = Notification.builder()
//                .userId(userId)
//                .postId(postId)
//                .actionUserId(actionUserId)
//                .type(type)
//                .isRead(false)
//                .build();
//        return notificationRepository.save(notification);
//    }
//
//    public List<Notification> getUserNotifications(String userId) {
//        return notificationRepository.findByUserId(userId);
//    }
//
//    public void markNotificationAsRead(String notificationId) {
//        Notification notification = notificationRepository.findById(notificationId)
//                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
//        notification.setRead(true);
//        notificationRepository.save(notification);
//    }
//}
private final NotificationRepository notificationRepository;
    private final UserClient userClient; // Pour récupérer les infos utilisateur
    private final PostClient postClient; // Pour récupérer les infos du post

    @Autowired
    // Full constructor
    public NotificationService(NotificationRepository notificationRepository, UserClient userClient, PostClient postClient) {
        this.notificationRepository = notificationRepository;
        this.userClient = userClient;
        this.postClient = postClient;
    }

    // Constructor with only NotificationRepository
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        this.userClient = null; // Or assign a default value
        this.postClient = null; // Or assign a default value
    }
    public void createNotification(NotificationDto notificationDto) {
        // Récupérer les informations du post
        Post post = postClient.getPostById(notificationDto.getPostId());

        // Vérifier que le propriétaire du post n'est pas l'actionneur
        if (post.getPostedBy().equals(notificationDto.getActionUserId())) {
            return; // Pas de notification si l'utilisateur aime son propre post
        }

        // Créer une notification
        Notification notification = Notification.builder()
                .userId(post.getPostedBy()) // Le propriétaire du post
                .postId(notificationDto.getPostId())
                .actionUserId(notificationDto.getActionUserId())
                .type(notificationDto.getType())
                .isRead(false)
                .createdAt(new Date())
                .build();

        // Sauvegarder dans la base de données
        notificationRepository.save(notification);
    }
}