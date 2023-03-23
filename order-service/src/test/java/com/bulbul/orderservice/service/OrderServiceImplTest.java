package com.bulbul.orderservice.service;

import com.bulbul.orderservice.entity.Order;
import com.bulbul.orderservice.external.client.AccountService;
import com.bulbul.orderservice.external.client.PaymentService;
import com.bulbul.orderservice.external.client.ProductService;
import com.bulbul.orderservice.external.response.PaymentResponse;
import com.bulbul.orderservice.external.response.ProductResponse;
import com.bulbul.orderservice.kafka.OrderProducer;
import com.bulbul.orderservice.model.OrderResponse;
import com.bulbul.orderservice.model.PaymentMode;
import com.bulbul.orderservice.respository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;


@SpringBootTest
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductService productService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private RestTemplate restTemplate;


    @InjectMocks
    OrderServiceImpl orderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

 /*   @DisplayName("Get Order - Success Scenario")
    @Test
    void test_When_Order_Success() {
        //Mocking
        Order order = getMockOrder();
        Mockito.when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.of(order));

        Mockito.when(restTemplate.getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(),
                ProductResponse.class
        )).thenReturn(getMockProductResponse());

        Mockito.when(restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/"+order.getId(),
                PaymentResponse.class
        )).thenReturn(getMockPaymentResponse());

        //Actual
        OrderResponse orderResponse = orderService.getOrderDetails(1);

        //Verification
        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());
        Mockito.verify(restTemplate.getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(),
                ProductResponse.class));
        Mockito.verify(restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/"+order.getId(),
                PaymentResponse.class));


        //Assert
        assertNotNull(orderResponse);
        assertEquals(order.getId(), orderResponse.getOrderId());
    }*/

    private PaymentResponse getMockPaymentResponse() {
        return PaymentResponse.builder()
                .paymentId(1)
                .paymentDate(Instant.now())
                .paymentMode(PaymentMode.CASH)
                .amount(200)
                .orderId(1)
                .status("ACCEPTED")
                .build();
    }

    private ProductResponse getMockProductResponse() {
        return ProductResponse.builder()
                .productId(2)
                .productName("iphone")
                .quantity(200)
                .build();
    }

    private Order getMockOrder() {
        return Order.builder()
                .orderStatus("PLACED")
                .orderDate(Instant.now())
                .id(1)
                .amount(200)
                .productId(2)
                .build();

    }


}