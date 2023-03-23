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
import com.bulbul.orderservice.kafka.OrderProducer;
import com.bulbul.orderservice.model.OrderRequest;
import com.bulbul.orderservice.model.OrderResponse;
import com.bulbul.orderservice.external.response.ProductResponse;
import com.bulbul.orderservice.respository.OrderRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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


    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, ProductService productService, PaymentService paymentService,
                            RestTemplate restTemplate, OrderProducer orderProducer, AccountService accountService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.paymentService = paymentService;
        this.restTemplate = restTemplate;
        this.orderProducer = orderProducer;
        this.accountService = accountService;
    }


    @Override
    public long placeOrder(OrderRequest orderRequest) {

        log.info("Placing Order request: {}", orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Creating Order with Status CREATED");

        Order order = Order.builder()
                .productId(orderRequest.getProductId())
                .userId(orderRequest.getUserId())
                .amount(orderRequest.getTotalAmount())
                .quantity(orderRequest.getQuantity())
                .orderStatus("CREATED")
                .orderDate(Instant.now())
                .build();

        order = orderRepository.save(order);

        log.info("Calling Payment Service to complete the payment");

        PaymentRequest paymentRequest
                =PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus =null;
        try{
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully.Changing the order status to PLACED");
            orderStatus="PLACED";
        }catch (Exception e){
            log.error("Error occurred in payment.Changing the order status to PAYMENT_FAILED");
            orderStatus="PAYMENT_FAILED";
        }

        log.info("Invoking Account Service to Deduct User Balance");

        accountService.deductUserBalance(orderRequest.getUserId(),orderRequest.getTotalAmount());



        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        log.info("Order places successfully with orderId: {}", order.getId());


        //send to kafka
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setStatus("PENDING");
        orderEvent.setMessage("Order status is in pending state");
        orderEvent.setOrderStatus(order.getOrderStatus());
        orderEvent.setOrderId(order.getId());
        orderEvent.setEmail(orderRequest.getEmail());

        orderProducer.sendMessage(orderEvent);

        return order.getId();
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
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();
    }
}
