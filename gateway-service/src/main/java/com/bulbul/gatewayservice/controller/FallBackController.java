package com.bulbul.gatewayservice.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallBackController {


    @GetMapping("/orderServiceFallBack")
    public String orderServiceFallBack(){
        return "Order Service is down! Please wait and try again later.";
    }
    @GetMapping("/productServiceFallBack")
    public String productServiceFallBack(){
        return "Product Service is down! Please wait and try again later.";
    }
    @GetMapping("/paymentServiceFallBack")
    public String paymentServiceFallBack(){
        return "Payment Service is down! Please wait and try again later.";
    }

    @GetMapping("/authServiceFallBack")
    public String authServiceFallBack(){
        return "Auth Service is down! Please wait and try again later.";
    }

}
