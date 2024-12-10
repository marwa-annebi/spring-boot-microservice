package com.example.notification.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEventDto {
    private String userId; // User qui reçoit la notification
    private String postId; // Post concerné
    private String actionUserId; // User qui a interagi
    private String type; // Type d'action (like, dislike, comment)
}
