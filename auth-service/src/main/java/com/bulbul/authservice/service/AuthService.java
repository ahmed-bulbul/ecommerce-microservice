package com.bulbul.authservice.service;

import com.bulbul.authservice.dto.UserResponse;
import com.bulbul.authservice.entity.User;
import com.bulbul.authservice.exception.CustomException;
import com.bulbul.authservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String saveUser(User credential) {
        credential.setPassword(passwordEncoder.encode(credential.getPassword()));
        repository.save(credential);
        return "user added to the system";
    }

    public String generateToken(String username) {
        return jwtService.generateToken(username);
    }

    public void validateToken(String token) {
        jwtService.validateToken(token);
    }


    public UserResponse getUser(Long id) {
        User user = repository.findById(id).orElseThrow( ()-> new CustomException("User not found with this is","USER_NOT_FOUND"));
        return convertToResponseDto(user);
    }



    private void validateUser(Long id) {
        User user = findById(id);
        //TODO : NEED TO TOKEN VALIDATION
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
           log.info(authentication.getName());
        } else {
            log.info("null name");
        }

    }

    public User findById(Long id){
        return repository.findById(id).orElseThrow(()->new CustomException("User not found","NOT_FOUND"));
    }




    private UserResponse convertToResponseDto(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public static String getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        } else {
            return null;
        }
    }
}
