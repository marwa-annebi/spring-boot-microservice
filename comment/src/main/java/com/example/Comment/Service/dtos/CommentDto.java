package com.example.Comment.Service.dtos;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@Builder
public class CommentDto {
    @JsonProperty("_id") // Map MongoDB `_id` to this field
    private String id;   // Use `id` instead of `_id` for consistency
    private String text;
    private String userId;
    private String postId;
    private Date createdAt;
    private List<CommentDto> replies;
}
