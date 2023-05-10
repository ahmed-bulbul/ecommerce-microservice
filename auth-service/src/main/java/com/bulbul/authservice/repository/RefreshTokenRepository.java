package com.bulbul.authservice.repository;

import com.bulbul.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

    Optional<RefreshToken> findById(Long id);

    Optional<RefreshToken> findByToken(String token);

    Integer deleteByUserId(Long userId);

    Optional<RefreshToken> findByUserId(Long userId);
}
