package com.example.auth.services;


import com.example.auth.dtos.CreateUserDto;
import com.example.auth.dtos.UserDto;
import com.example.auth.dtos.UserLoginDto;
import com.example.auth.exceptions.AppException;
import com.example.auth.helpers.GenerateUniqueName;
import com.example.auth.models.User;
import com.example.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.CharBuffer;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto login(UserLoginDto credentialsDto) {
        User user = userRepository.findByEmail(credentialsDto.getEmail())
                .orElseThrow(() ->new AppException("Unknown user", HttpStatus.NOT_FOUND));
        if (user==null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User does not exist");
        }

        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.getPassword()), user.getPassword())) {
            return toUserDto(user);
        }
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

    public UserDto register(CreateUserDto userDto) {
        Optional<User> optionalUser = userRepository.findByEmail(userDto.getEmail());

        if (optionalUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Email already exists");
        }

        User user = signUpToUser(userDto);
        user.setPassword(passwordEncoder.encode(CharBuffer.wrap(userDto.getPassword())));
        String userName = GenerateUniqueName.generateUsername();
        user.setUserName(userName);
        User savedUser = userRepository.save(user);

        return toUserDto(savedUser);
    }

    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown user"));
        return user;
    }

    // Convert User to UserDto
    private UserDto toUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setUserName(user.getUserName());
        // Other mappings if needed
        return userDto;
    }

    // Convert CreateUserDto to User
    private User signUpToUser(CreateUserDto createUserDto) {
        User user = new User();
        user.setFirstName(createUserDto.getFirstName());
        user.setLastName(createUserDto.getLastName());
        user.setEmail(createUserDto.getEmail());
        // Other mappings if needed
        return user;
    }

    public User findByEmailUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown user"));
        return user;
    }
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }
}
