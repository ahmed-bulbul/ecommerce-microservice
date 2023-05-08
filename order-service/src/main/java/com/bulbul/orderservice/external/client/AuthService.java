package com.bulbul.orderservice.external.client;


import com.bulbul.orderservice.config.FeignConfig;
import com.bulbul.orderservice.exception.CustomException;
import com.bulbul.orderservice.external.response.UserResponse;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@CircuitBreaker(name = "external", fallbackMethod = "fallback")
@FeignClient(name = "AUTH-SERVICE/auth",configuration  = FeignConfig.class)
public interface AuthService {

    @GetMapping("/user/{id}")
    UserResponse getUser(@PathVariable Long id);

    @GetMapping("/isValidUser/{userId}")
    boolean isValidUser(@PathVariable Long userId);

    @GetMapping("/{username}")
    UserResponse getUserByUsername(@PathVariable String username);


    default boolean fallback(Long userId, Throwable ex) {
        if (ex instanceof FeignException) {
            Throwable cause = ex.getCause();
            throw new CustomException("User is not valid", "USER_NOT_VALID", 403);
        }  // handle other exceptions here

        return false;
    }

    default UserResponse fallback(String  username, Throwable ex) {
        throw new CustomException("Auth Service is not available", "SERVICE_UNAVAILABLE", 503);
    }

    default ResponseEntity<Long> fallback(Exception e) {
        throw new CustomException("User Service is not available",
                "UNAVAILABLE",
                500);
    }
}
