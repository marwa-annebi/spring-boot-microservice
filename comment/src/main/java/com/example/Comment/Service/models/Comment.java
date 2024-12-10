package com.example.Comment.Service.models;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "comments")
public class Comment {

    @Id
    private String id;

    @NotBlank(message = "Comment text is required.")
    private String text;

    @DocumentReference
    private Post postId;


    @DocumentReference
    private User userId;

    private String parentCommentId;

    private List<String> replies = new ArrayList<>(); // Stores IDs of replies

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;

    // Additional helper methods (if required)
    public boolean isReply() {
        return parentCommentId != null && !parentCommentId.isEmpty();
    }


}


//package com.example.Comment.Service.models;
//
//import jakarta.validation.constraints.NotBlank;
//import lombok.*;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//@Data
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@Document(collection = "comments")
//public class Comment {
//    @Id
//    private String id;
//
//    @NotBlank
//    private String text;
//
//    @NotBlank
//    private String postId;
//
//    @NotBlank
//    private String userId;
//
//    private String parentCommentId;
//
//    private List<String> replies = new ArrayList<>();
//
//    @CreatedDate
//    private Date createdAt;
//
//    @LastModifiedDate
//    private Date updatedAt;
//}


//@Document(collection = "comments")
//public class Comment {
//    @Id
//    private String id;
//
//    private String text;
//
//    private String postId;
//
//    private String userId;
//
//    @DocumentReference
//    private Comment parentComment;
//
//    @DocumentReference
//    private List<Comment> replies = new ArrayList<>();
//
//    @CreatedDate
//    private Date createdAt;
//
//    @LastModifiedDate
//    private Date updatedAt;
//}
