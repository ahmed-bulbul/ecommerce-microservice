package com.bulbul.orderservice.external.client;


import com.bulbul.orderservice.exception.CustomException;
import com.bulbul.orderservice.external.decoder.CustomErrorDecoder;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@CircuitBreaker(name = "external", fallbackMethod = "fallback")
@FeignClient(name = "ACCOUNT-SERVICE/account", configuration = CustomErrorDecoder.class)
public interface AccountService {

    @PutMapping("/user/deductBal/{id}")
    String deductUserBalance(@PathVariable Long id, @RequestParam double amount);

    default String fallback(Long id, double amount, Throwable t) {
        if (t instanceof FeignException && ((FeignException) t).status() == 503) {
            // handle Service Unavailable error
            return "Service Unavailable: Account Service is currently unavailable.";
        } else {
            throw new CustomException("User balance can not be deducted",
                    "UNAVAILABLE",
                    500);
        }
    }

}
