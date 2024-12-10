//package com.example.Comment.Service.config;
//
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//public class Webconfig {
//
//
//        @Bean
//        public WebMvcConfigurer corsConfigurer() {
//            return new WebMvcConfigurer() {
//                @Override
//                public void addCorsMappings(CorsRegistry registry) {
//                    registry.addMapping("/**")
//                            .allowedOrigins("http://localhost:3000") // Frontend URL
//                            .allowedMethods("GET", "POST", "PUT", "DELETE")
//                            .allowedHeaders("*")
//                            .allowCredentials(true);
//                }
//            };
//        }
//    }
//
//
