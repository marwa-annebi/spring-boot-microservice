package com.example.auth.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserDto {

    @NotEmpty(message = "First name cannot be empty")
    @Size(min = 3, message = "First name must be at least 3 characters long")
    private String firstName;

    @NotEmpty(message = "Last name cannot be empty")
    @Size(min = 3, message = "Last name must be at least 3 characters long")
    private String lastName;

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
}
