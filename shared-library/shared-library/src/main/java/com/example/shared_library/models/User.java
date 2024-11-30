package com.example.shared_library.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Document(collection = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class User {

    @Id
    private String id;

    private String userName;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;

}
