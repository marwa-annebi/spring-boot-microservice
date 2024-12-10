package com.example.Comment.Service.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtAuthFilterConfig {

    private final UserAuthenticationProvider userAuthenticationProvider;

    public JwtAuthFilterConfig(UserAuthenticationProvider userAuthenticationProvider) {
        this.userAuthenticationProvider = userAuthenticationProvider;
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(userAuthenticationProvider);
    }
}
