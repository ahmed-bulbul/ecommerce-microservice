package com.bulbul.orderservice.controller;


import com.bulbul.orderservice.contant.ApplicationConstant;
import com.bulbul.orderservice.model.OrderRequest;
import com.bulbul.orderservice.model.OrderResponse;
import com.bulbul.orderservice.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    private final OrderService orderService;


    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @PostMapping
    public ResponseEntity<Long> placeOrder(@RequestBody OrderRequest orderRequest,@RequestHeader(ApplicationConstant.LoggedInUser) String username){
        log.info("Logged in user {}",username);
        long orderId = orderService.placeOrder(orderRequest,username);
        log.info("Order id: {}", orderId);
        return new ResponseEntity<>(orderId, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable long orderId){
        OrderResponse orderResponse = orderService.getOrderDetails(orderId);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }
}
