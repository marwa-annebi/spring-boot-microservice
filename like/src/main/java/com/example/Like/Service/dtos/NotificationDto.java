package com.example.Like.Service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private String postId;
    private String actionUserId; // ID de l'utilisateur qui a liké
    private String type; // Type d'action : "like", "dislike", etc.
}
