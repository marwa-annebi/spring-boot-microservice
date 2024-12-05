package com.example.Like.Service.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "likes")
public class Like {
    @Id
    private String id;

    @DocumentReference
    private User likedBy;

    @DocumentReference
    private Post post;
    private boolean top;
    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;


}