package com.bulbul.notificationservice.kafka;


import com.bulbul.commonservice.event.OrderEvent;
import com.bulbul.notificationservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumer.class);

    private final EmailService emailService;

    public OrderConsumer(EmailService emailService) {
        this.emailService = emailService;
    }


    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(OrderEvent event) {
        LOGGER.info(String.format("Order event received in email service => %s",event.toString()));


        //save an email to the customer
        emailService.sendSimpleMail(event.getEmail(),"Order Confirmation",
                "Your order has been confirmed. Order number is : "+event.getOrderId());



    }
}