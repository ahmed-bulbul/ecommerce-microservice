package com.bulbul.paymentservice.service;


import com.bulbul.paymentservice.model.PaymentRequest;
import com.bulbul.paymentservice.model.PaymentResponse;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(String orderId);
}
