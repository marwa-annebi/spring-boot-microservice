//package com.example.notification.models;
//
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.AllArgsConstructor;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.time.LocalDateTime;
//import java.util.Date;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Document(collection = "notifications")
//public class Notification {
//
//    @Id
//    private String id;
//
//    private String userId;
//
//    private String targetUserId;
//
//    private String message;
//
//
//    @CreatedDate
//    private Date createdAt;
//
//    @LastModifiedDate
//    private Date updatedAt;
//}
package com.example.notification.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "notifications")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    private String id;

    private User userId; // L'utilisateur recevant la notification
    private Post postId; // Le post concerné
    private String actionUserId; // L'utilisateur qui a aimé/disliké
    private String type; // Type de notification ("like", "dislike")

    @CreatedDate
    private Date createdAt;
    private boolean isRead; // Marquer la notification comme lue ou non
}
