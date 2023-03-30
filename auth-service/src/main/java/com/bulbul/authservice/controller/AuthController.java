package com.bulbul.authservice.controller;

import com.bulbul.authservice.config.UserDetailsImpl;
import com.bulbul.authservice.dto.JwtResponse;
import com.bulbul.authservice.dto.UserRequest;
import com.bulbul.authservice.dto.UserResponse;
import com.bulbul.authservice.entity.User;
import com.bulbul.authservice.exception.CustomException;
import com.bulbul.authservice.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService service;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(AuthService service, AuthenticationManager authenticationManager) {
        this.service = service;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/user/{id}")
    public UserResponse getUser(@PathVariable Long id){
        return service.getUser(id);
    }




    @PostMapping("/register")
    public String addNewUser(@RequestBody User user) {
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


    @GetMapping("/isValidUser/{userId}")
    public boolean isValidUser(@PathVariable Long userId){
        return service.isValidUser(userId);
    }
}
