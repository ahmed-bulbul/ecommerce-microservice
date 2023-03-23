package com.bulbul.accountservice.repository;

import com.bulbul.accountservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findByUserId(Long userId);
}
