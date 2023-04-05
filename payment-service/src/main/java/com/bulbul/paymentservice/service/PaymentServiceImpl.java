package com.bulbul.paymentservice.service;


import com.bulbul.paymentservice.entity.TransactionDetails;
import com.bulbul.paymentservice.model.PaymentMode;
import com.bulbul.paymentservice.model.PaymentRequest;
import com.bulbul.paymentservice.model.PaymentResponse;
import com.bulbul.paymentservice.model.PaymentStatus;
import com.bulbul.paymentservice.repository.TransactionDetailsRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class PaymentServiceImpl implements PaymentService {


    private final TransactionDetailsRepository transactionDetailsRepository;


    @Autowired
    public PaymentServiceImpl(TransactionDetailsRepository transactionDetailsRepository) {
        this.transactionDetailsRepository = transactionDetailsRepository;
    }

    @Override
    public long doPayment(PaymentRequest paymentRequest) {
        log.info("Recording Payment Details: {}", paymentRequest);

        TransactionDetails transactionDetails
                = TransactionDetails.builder()
                .paymentDate(Instant.now())
                .paymentMode(paymentRequest.getPaymentMode().name())
                .paymentStatus(PaymentStatus.SUCCESS.name())
                .orderId(paymentRequest.getOrderId())
                .referenceNumber(paymentRequest.getReferenceNumber())
                .amount(paymentRequest.getAmount())
                .build();

        transactionDetailsRepository.save(transactionDetails);

        log.info("Transaction Completed with Id: {}", transactionDetails.getId());

        return transactionDetails.getId();
    }

    @Override
    public PaymentResponse getPaymentDetailsByOrderId(String orderId) {
        log.info("Getting payment details for the Order Id: {}", orderId);

        TransactionDetails transactionDetails
                = transactionDetailsRepository.findByOrderId(Long.parseLong(orderId));

        return PaymentResponse.builder()
                .paymentId(transactionDetails.getId())
                .paymentMode(PaymentMode.valueOf(transactionDetails.getPaymentMode()))
                .paymentDate(transactionDetails.getPaymentDate())
                .orderId(transactionDetails.getOrderId())
                .status(transactionDetails.getPaymentStatus())
                .amount(transactionDetails.getAmount())
                .build();
    }

    @Override
    public long failedPayment(Long id) {
        TransactionDetails transactionDetails = transactionDetailsRepository.findById(id).orElseThrow(()-> new RuntimeException("Not found!"));
        transactionDetails.setPaymentStatus("FAILED");
        transactionDetailsRepository.save(transactionDetails);
        return transactionDetails.getId();
    }
}
