package com.example.Comment.Service.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authhhhhh"+authentication);
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // Extract email from the token's subject
        }
        throw new RuntimeException("Unauthorized: No authenticated user found");
    }
}
