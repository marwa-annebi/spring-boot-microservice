package com.example.post.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, String> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Invalid request");
        response.put("message", ex.getMessage());
        return response;
    }

    @ExceptionHandler(IOException.class)
    public Map<String, String> handleIOException(IOException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "File upload error");
        response.put("message", ex.getMessage());
        return response;
    }
}
