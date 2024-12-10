package com.example.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto {

    private String id;
    private String description;
    private List<String> images;

    // User details
    private String postedById;
    private String postedByUserName;
    private String postedByFirstName;
    private String postedByLastName;

    private Date createdAt;
    private Date updatedAt;

    // Like details
    private boolean likedByCurrentUser;
    private int nbLike;
}
