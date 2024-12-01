// src/main/java/com/example/post/dtos/WithLikesCount.java

package com.example.post.dtos;

import lombok.Data;

@Data
public class WithLikesCount {
    private String id;
    private String description;
    private java.util.List<String> images;
    private String postedBy;
    private java.util.List<?> comments; // Replace '?' with appropriate type
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private Integer likesCount;
    private Boolean liked;
    private boolean disliked;
}


//package com.example.post.dtos;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.Date;
//import java.util.List;
//
//@Data
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//public class WithLikesCount {
//    private String id;
//    private String description;
//    private List<String> images;
//    private String postedBy;
//    private List<String> comments;
//    private Date createdAt;
//    private Date updatedAt;
//    private int likesCount;
//    private boolean liked;
//    private boolean disliked;
//}
