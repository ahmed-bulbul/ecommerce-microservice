package com.bulbul.authservice.service.impl;


import com.bulbul.authservice.dto.TokenRefreshRequest;
import com.bulbul.authservice.dto.TokenRefreshResponse;
import com.bulbul.authservice.entity.RefreshToken;
import com.bulbul.authservice.repository.RefreshTokenRepository;
import com.bulbul.authservice.service.AuthService;
import com.bulbul.authservice.service.JwtService;
import com.bulbul.authservice.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${ms.app.refresh.jwtExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthService userService;
    private final JwtService jwtUtils;

    /**
     * Parameterized constructor
     *  @param refreshTokenRepository {@link RefreshTokenRepository}
     * @param userService {@link AuthService}
     * @param jwtUtils        {@link JwtService}
     */
    @Autowired
    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, AuthService userService, JwtService jwtUtils) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * find by login or throw error
     *
     * @param token {@link String}
     * @return {@link Optional <RefreshToken>}
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * create refresh token
     *
     * @param userId {@link String}
     * @return {@link RefreshToken}
     */
    public RefreshToken createRefreshToken(Long userId) {

        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUserId(userId);
        RefreshToken refreshToken;
        refreshToken = optionalRefreshToken.orElseGet(RefreshToken::new);
        refreshToken.setUser(userService.findUserById(userId));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    /**
     * verify refresh token expiration
     *
     * @param token {@link String}
     * @return {@link RefreshToken}
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Exception: ");
        }
        return token;
    }

    /**
     * delete refresh token by userId
     *
     * @param userId {@link String}
     * @return {@link RefreshToken}
     */
    @Transactional
    @Override
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * create refresh token
     *
     * @param request {@link com.bulbul.authservice.dto.TokenRefreshRequest}
     * @return {@link com.bulbul.authservice.dto.TokenRefreshResponse}
     */
    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        return findByToken(requestRefreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateToken(user.getUsername());
                    return new TokenRefreshResponse(token, requestRefreshToken);
                })
                .orElseThrow(() -> new RuntimeException("Exception occurred......"));
    }

}