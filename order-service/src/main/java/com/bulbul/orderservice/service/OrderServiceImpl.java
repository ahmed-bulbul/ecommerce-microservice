package com.bulbul.orderservice.service;


import com.bulbul.commonservice.event.OrderEvent;
import com.bulbul.orderservice.entity.Order;
import com.bulbul.orderservice.exception.CustomException;
import com.bulbul.orderservice.external.client.AccountService;
import com.bulbul.orderservice.external.client.AuthService;
import com.bulbul.orderservice.external.client.PaymentService;
import com.bulbul.orderservice.external.client.ProductService;
import com.bulbul.orderservice.external.request.PaymentRequest;
import com.bulbul.orderservice.external.response.PaymentResponse;
import com.bulbul.orderservice.external.response.UserResponse;
import com.bulbul.orderservice.kafka.OrderProducer;
import com.bulbul.orderservice.model.OrderRequest;
import com.bulbul.orderservice.model.OrderResponse;
import com.bulbul.orderservice.external.response.ProductResponse;
import com.bulbul.orderservice.respository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@Slf4j
public class OrderServiceImpl  implements OrderService{

    private final OrderRepository orderRepository;

    private final ProductService productService;

    private final PaymentService paymentService;

    private final RestTemplate restTemplate;

    private final OrderProducer orderProducer;

    private final AccountService accountService;

    private final AuthService authService;



    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, ProductService productService, PaymentService paymentService,
                            RestTemplate restTemplate, OrderProducer orderProducer, AccountService accountService, AuthService authService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.paymentService = paymentService;
        this.restTemplate = restTemplate;
        this.orderProducer = orderProducer;
        this.accountService = accountService;
        this.authService = authService;
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public long placeOrder(OrderRequest orderRequest,String username) {
        log.info("Placing Order request: {}", orderRequest);
        validateUser(orderRequest.getUserId(),username);
        log.info("Creating Order with Status CREATED");
        // 1. Create order
        Order order = Order.builder()
                .productId(orderRequest.getProductId())
                .userId(orderRequest.getUserId())
                .amount(orderRequest.getTotalAmount())
                .quantity(orderRequest.getQuantity())
                .orderStatus("CREATED")
                .orderDate(Instant.now())
                .isActive(Boolean.TRUE)
                .build();
        order = orderRepository.save(order);

        // 2. Try to reduce product quantity
        try {
            productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());
        } catch (Exception e) {
            order.setIsActive(Boolean.FALSE);
            throw new CustomException("Failed to reduce quantity", "FAILED_REDUCE_QUANTITY", 404);
        }
        log.info("Calling Payment Service to complete the payment");
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus = null;
        Long paymentId=null;
        // 3. Try to payment
        try {
            paymentId = paymentService.doPayment(paymentRequest).getBody();
            log.info("Payment done Successfully. Changing the order status to PLACED");
            orderStatus = "PLACED";
        } catch (Exception e) {
            order.setIsActive(Boolean.FALSE);
            productService.revertQuantity(orderRequest.getProductId(), orderRequest.getQuantity());
            log.error("Error occurred in payment. Changing the order status to PAYMENT_FAILED");
            throw new CustomException("Failed to payment","PAYMENT_FAILED",404);
        }

        log.info("Invoking Account Service to Deduct User Balance");
        // 4. Try to deduct user balance
        try {
            accountService.deductUserBalance(orderRequest.getUserId(), orderRequest.getTotalAmount());
        } catch (Exception e) {
            order.setIsActive(Boolean.FALSE);
            productService.revertQuantity(orderRequest.getProductId(), orderRequest.getQuantity());
            paymentService.failedPayment(paymentId);
            log.error("Failed to deduct user balance. Changing the order status to ACCOUNT_SERVICE_FAILED");
            throw new CustomException("Failed to deduct user balance", "FAILED_DEDUCT_USER_BALANCE", 404);
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        // send to kafka
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setStatus("PENDING");
        orderEvent.setMessage("Order status is in pending state");
        orderEvent.setOrderStatus(order.getOrderStatus());
        orderEvent.setOrderId(order.getId());
        orderEvent.setEmail(orderRequest.getEmail());
        // orderProducer.sendMessage(orderEvent);
        return order.getId();
    }

    private void validateUser(long userId, String username) {
        try {
            UserResponse user = authService.getUserByUsername(username);
            if (userId != user.getId()) {
                log.error("User not valid {}",username);
                throw new CustomException("User not valid", "USER_NOT_VALID", 403);
            }
            log.info("current logged in user: {}", user.getUsername());
        } catch (Exception e) {
            throw new CustomException("User not valid", "USER_NOT_VALID", 403);
        }
    }


    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get the order details for orderId: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found", "NOT_FOUND",404));

        log.info("Invoking Product service to fetch the product for id: {}", order.getProductId());
        ProductResponse productResponse
                = restTemplate.getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(),
                ProductResponse.class);

        PaymentResponse paymentResponse
                = restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/"+order.getId(),
        PaymentResponse.class);

        OrderResponse.ProductDetails productDetails;

        if(productResponse != null){
            productDetails = OrderResponse.ProductDetails
                    .builder()
                    .productName(productResponse.getProductName())
                    .productId(productResponse.getProductId())
                    .build();
        }else{
            productDetails = null;
        }

        OrderResponse.PaymentDetails paymentDetails;

        if(paymentResponse != null){
            paymentDetails = OrderResponse.PaymentDetails
                    .builder()
                    .paymentId(paymentResponse.getPaymentId())
                    .paymentStatus(paymentResponse.getStatus())
                    .paymentMode(paymentResponse.getPaymentMode())
                    .paymentDate(paymentResponse.getPaymentDate())
                    .build();
        }else{
            paymentDetails = null;
        }

        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .amount(order.getAmount())
                .quantity(order.getQuantity())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();
    }
}
