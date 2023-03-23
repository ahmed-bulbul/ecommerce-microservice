package com.bulbul.accountservice.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountResponse {

    private Long id;
    private Long userId;
    private Double balance;
}
