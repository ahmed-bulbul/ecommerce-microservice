package com.bulbul.accountservice.service;

import com.bulbul.accountservice.entity.Account;
import com.bulbul.accountservice.exception.CustomException;
import com.bulbul.accountservice.model.AccountRequest;
import com.bulbul.accountservice.model.AccountResponse;
import com.bulbul.accountservice.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService{

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Long create(AccountRequest accountRequest) {
        Account account = findByUserId(accountRequest.getUserId());
        if(Objects.isNull(account)){
            account = convertToEntity(accountRequest);
            accountRepository.save(account);
        }else{
            update(account,accountRequest);
        }
        log.info("Account Balance affected for user id : {} ",accountRequest.getUserId());
        return account.getId();
    }

    public void update(Account account, AccountRequest accountRequest){
        account.setBalance(accountRequest.getBalance());
        accountRepository.save(account);
    }

    @Override
    public AccountResponse getAccountByUserId(Long userId) {
        Account account = findByUserId(userId);
        return convertToResponse(account);
    }

    @Override
    public String deductUserBalance(Long userId, double amount) {
        validate(userId,amount);
        Account account = findByUserId(userId);
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
        log.info("User account balance deducted successfully");
        return "User account balance deducted successfully";
    }

    public Account findByUserId(Long userId){
        return  accountRepository.findByUserId(userId).orElseThrow(()->
                new CustomException("Account not found with user id :"+userId, "NOT_FOUND"));
    }

    private AccountResponse convertToResponse(Account account) {
        return AccountResponse.builder()
                .userId(account.getUserId())
                .balance(account.getBalance())
                .build();
    }

    private Account convertToEntity(AccountRequest accountRequest) {
            return Account.builder()
                    .userId(accountRequest.getUserId())
                    .balance(accountRequest.getBalance())
                    .build();
    }

    public void validate(Long userId, double amount){
        Account account = findByUserId(userId);
        if(account.getBalance() < amount){
            throw new CustomException("Insufficient User Balance","INSUFFICIENT_BALANCE");
        }
    }


}
