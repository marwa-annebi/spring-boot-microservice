// src/main/java/com/example/post/dtos/CreateDto.java

package com.example.post.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDto {

    @NotBlank(message = "Description is mandatory")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;


}


//package com.example.post.dtos;
//
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.Min;
//import jakarta.validation.constraints.NotEmpty;
//import jakarta.validation.constraints.Size;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class CreateDto {
//    @NotEmpty()
//    @Min(3)
//    private String description;
//
//}
