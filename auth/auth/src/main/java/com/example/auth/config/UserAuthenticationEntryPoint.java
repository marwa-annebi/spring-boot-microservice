package com.example.auth.config;

import com.example.auth.dtos.ErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        // Log the exception message for debugging
        System.out.println("AuthenticationException: " + authException.getMessage());

        // Set the response status to UNAUTHORIZED (401)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // Create a meaningful error message
        ErrorDto errorResponse = new ErrorDto("Unauthorized: " + authException.getMessage());

        // Write the error response as JSON
        OBJECT_MAPPER.writeValue(response.getOutputStream(), errorResponse);
    }
}
