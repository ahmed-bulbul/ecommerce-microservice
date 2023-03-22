package com.bulbul.orderservice.service;

import com.bulbul.orderservice.model.OrderRequest;
import com.bulbul.orderservice.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
