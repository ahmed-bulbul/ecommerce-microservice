package com.bulbul.authservice.controller;

import com.bulbul.authservice.config.UserDetailsImpl;
import com.bulbul.authservice.dto.*;
import com.bulbul.authservice.entity.User;
import com.bulbul.authservice.exception.CustomException;
import com.bulbul.authservice.service.AuthService;
import com.bulbul.authservice.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService service;
    private final AuthenticationManager authenticationManager;

    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthController(AuthService service, AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService) {
        this.service = service;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
    }

    @GetMapping("/user/{id}")
    public UserResponse getUser(@PathVariable Long id){
        return service.getUser(id);
    }




    @PostMapping("/register")
    public String addNewUser(@Valid @RequestBody  User user) {
        return service.saveUser(user);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> getToken(@RequestBody UserRequest authRequest) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        if (authenticate.isAuthenticated()) {
            String jwt =  service.generateToken(authRequest.getUsername());
            UserDetailsImpl userDetails =(UserDetailsImpl) authenticate.getPrincipal();

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles));
        } else {
            throw new CustomException("invalid access","INVALID_ACCESS");
        }
    }

    @GetMapping("/validate")
    public String validateToken(@RequestParam("token") String token) {
       // service.validateToken(token);
        return "Token is valid";
    }

    @GetMapping("/{username}")
    public UserResponse getUserByUsername(@PathVariable String username){
        return service.getUserByUsername(username);
    }


    @GetMapping("/isValidUser/{userId}")
    @ResponseBody
    public ResponseEntity<Boolean>  isValidUser(@PathVariable Long userId){
        log.info("isValidUser function called.....");
         boolean isValid = service.isValidUser(userId);
         log.info("isValid user : {}",isValid);
         return new ResponseEntity<>(isValid,HttpStatus.OK);
    }

    @PostMapping("/refresh/token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return new ResponseEntity<>(refreshTokenService.refreshToken(request), HttpStatus.OK);
    }
}
