package com.bulbul.orderservice.external.client;

import com.bulbul.orderservice.exception.CustomException;
import com.bulbul.orderservice.external.request.PaymentRequest;
import com.bulbul.orderservice.external.response.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CircuitBreaker(name = "external", fallbackMethod = "fallback")
@FeignClient(name = "PAYMENT-SERVICE/payment")
public interface PaymentService {

    @PostMapping
    ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);

    @PutMapping("/{id}")
    ResponseEntity<Long> failedPayment(@PathVariable Long id);

    default long fallback(Long  id, Throwable ex) {
        throw new CustomException("Payment not updated", "NOT_UPDATED", 403);
    }


    default ResponseEntity<Long> fallback(Exception e) {
        throw new CustomException("Payment Service is not available",
                "UNAVAILABLE",
                500);
    }
}
