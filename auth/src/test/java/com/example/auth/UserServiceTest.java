package com.example.auth;

import com.example.auth.dtos.CreateUserDto;
import com.example.auth.dtos.UserDto;
import com.example.auth.dtos.UserLoginDto;
import com.example.auth.exceptions.AppException;
import com.example.auth.models.User;
import com.example.auth.repositories.UserRepository;
import com.example.auth.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.CharBuffer;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;



    @Test
    public void testLogin_UserNotFound() {
        // Arrange
        String email = "unknown@example.com";
        when(userRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () ->
                userService.login(new UserLoginDto(email, "password"))
        );

        assertEquals("Unknown user", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(userRepository, times(1)).findByEmail(email);
        verifyNoInteractions(passwordEncoder);
    }
    @Test
    public void testRegister_Success() {
        // Arrange
        String email = "newuser@example.com";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        CreateUserDto createUserDto = CreateUserDto.builder()
                .firstName("New")
                .lastName("User")
                .email(email)
                .password(rawPassword)
                .build();

        User savedUser = User.builder()
                .id("1")
                .firstName("New")
                .lastName("User")
                .email(email)
                .userName("New-Serve-123")
                .password(encodedPassword)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn(encodedPassword); // Use flexible matcher
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserDto userDto = userService.register(createUserDto);

        // Assert
        assertNotNull(userDto);
        assertEquals("New", userDto.getFirstName());
        assertEquals(email, userDto.getEmail());
        assertNotNull(userDto.getId());

        verify(userRepository, times(1)).findByEmail(email);
        // Verify that the passwordEncoder.encode() was called with a CharSequence containing "password"
        verify(passwordEncoder, times(1)).encode(argThat(argument ->
                argument.toString().equals(rawPassword)
        ));
        verify(userRepository, times(1)).save(any(User.class));
    }
    @Test
    public void testLogin_Success() {
        // Arrange
        String email = "user@example.com";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        UserLoginDto loginDto = UserLoginDto.builder()
                .email(email)
                .password(rawPassword)
                .build();

        User user = User.builder()
                .id("1")
                .email(email)
                .password(encodedPassword)
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CharBuffer.wrap(rawPassword), encodedPassword)).thenReturn(true);

        // Act
        UserDto userDto = userService.login(loginDto);

        // Assert
        assertNotNull(userDto);
        assertEquals("John", userDto.getFirstName());
        assertEquals(email, userDto.getEmail());
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(eq(CharBuffer.wrap(rawPassword)), eq(encodedPassword));
    }


    @Test
    public void testLogin_InvalidPassword() {
        // Arrange
        String email = "user@example.com";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        UserLoginDto loginDto = UserLoginDto.builder()
                .email(email)
                .password(rawPassword)
                .build();

        User user = User.builder()
                .id("1")
                .email(email)
                .password(encodedPassword)
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CharBuffer.wrap(rawPassword), encodedPassword)).thenReturn(false);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> userService.login(loginDto));
        assertEquals("Invalid password", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(eq(CharBuffer.wrap(rawPassword)), eq(encodedPassword));
    }


}