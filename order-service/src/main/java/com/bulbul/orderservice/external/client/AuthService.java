package com.bulbul.orderservice.external.client;


import com.bulbul.orderservice.exception.CustomException;
import com.bulbul.orderservice.external.response.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@CircuitBreaker(name = "external", fallbackMethod = "fallback")
@FeignClient(name = "AUTH-SERVICE/auth")
public interface AuthService {

    @PutMapping("/user/deductBal/{id}")
    String deductUserBalance(@PathVariable Long id, @RequestParam Long amount);

    @GetMapping("/user/{id}")
    UserResponse getUser(@PathVariable Long id);

    @GetMapping("/getLoggedInUserId")
    Long getLoggedInUserId();

    default ResponseEntity<Long> fallback(Exception e) {
        throw new CustomException("Auth Service is not available",
                "UNAVAILABLE",
                500);
    }
}
