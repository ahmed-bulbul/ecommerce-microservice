package com.bulbul.accountservice.controller;


import com.bulbul.accountservice.model.AccountRequest;
import com.bulbul.accountservice.model.AccountResponse;
import com.bulbul.accountservice.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody AccountRequest accountRequest) {
        return new ResponseEntity<>(
                accountService.create(accountRequest),
                HttpStatus.OK
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<AccountResponse> getAccountByUserId(@PathVariable Long userId) {
        return new ResponseEntity<>(
                accountService.getAccountByUserId(userId),
                HttpStatus.OK
        );
    }

    @PutMapping("/user/deductBal/{id}")
    public String deductUserBalance(@PathVariable Long id,@RequestParam double amount){
        return accountService.deductUserBalance(id,amount);
    }
}
