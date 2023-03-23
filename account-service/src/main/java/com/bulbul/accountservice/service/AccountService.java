package com.bulbul.accountservice.service;

import com.bulbul.accountservice.model.AccountRequest;
import com.bulbul.accountservice.model.AccountResponse;

public interface AccountService {

    Long create(AccountRequest accountRequest);

    AccountResponse getAccountByUserId(Long userId);

    String deductUserBalance(Long userId, double amount);
}
