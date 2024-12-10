package com.example.post.dtos;

import lombok.Data;

import java.util.List;

@Data
public class CreatePostDto {
    private String title;
    private String description;
    private List<String> images;
    private String createdBy;
}