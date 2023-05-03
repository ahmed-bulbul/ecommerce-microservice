package com.bulbul.authservice.service;

import com.bulbul.authservice.dto.TokenRefreshRequest;
import com.bulbul.authservice.dto.TokenRefreshResponse;
import com.bulbul.authservice.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {

    Optional<RefreshToken> findByToken(String token);

    RefreshToken createRefreshToken(Long userId);

    RefreshToken verifyExpiration(RefreshToken token);

    int deleteByUserId(Long userId);

    TokenRefreshResponse refreshToken(TokenRefreshRequest request);
}
