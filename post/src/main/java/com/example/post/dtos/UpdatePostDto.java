package com.example.post.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdatePostDto {
    private String title;
    @NotBlank(message = "Description cannot be blank")
    private String description;
    private List<String> images;
}