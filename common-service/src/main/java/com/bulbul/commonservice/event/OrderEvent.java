package com.bulbul.commonservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {

    private String message;
    private String status;
    private long orderId;
    private String email;
    private String orderStatus;
}