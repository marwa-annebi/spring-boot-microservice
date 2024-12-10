package com.example.notification.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.Date;
import java.util.List;

@Document(collection = "posts")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Post {
    @Id
    private String id;

    private String description;
    private List<String> images;

    @DocumentReference
    private User postedBy;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;
    // Constructor for ID only
    public Post(String id) {
        this.id = id;
    }
}
