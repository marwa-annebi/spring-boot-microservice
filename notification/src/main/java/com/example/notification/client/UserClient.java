package com.example.notification.client;


//import com.example.notification.config.FeignConfig;
//import com.example.notification.models.User;
//import org.springframework.cloud.openfeign.FeignClient;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//@FeignClient(name = "user-service", url = "${application.config.users-url}", configuration = FeignConfig.class)
//public interface UserClient {
//    @GetMapping("/api/user/{email}")
//    User findByEmail(@PathVariable("email") String email);
//}
//
import com.example.notification.models.User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserClient {
    private final RestTemplate restTemplate;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public User getUserById(String userId) {
        return restTemplate.getForObject("http://auth/api/user/" + userId, User.class);
    }
}