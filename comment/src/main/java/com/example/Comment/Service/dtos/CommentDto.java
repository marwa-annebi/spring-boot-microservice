package com.example.Comment.Service.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class CommentDto {
    private String id;
    private String text;
    private String userName;
    private Date createdAt;
    private List<CommentDto> replies; // Nested replies
}
//    private String id;
//    private String text;
//    private String userName;
//    private Date createdAt;
