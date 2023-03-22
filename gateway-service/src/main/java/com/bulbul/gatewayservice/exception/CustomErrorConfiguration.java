package com.bulbul.gatewayservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class CustomErrorConfiguration {

    @Bean
    @Order(-2)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ObjectMapper objectMapper) {
        return new CustomErrorWebExceptionHandler(objectMapper);
    }

    @Component
    private static class CustomErrorWebExceptionHandler implements ErrorWebExceptionHandler {

        private final ObjectMapper objectMapper;

        @Autowired
        public CustomErrorWebExceptionHandler(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
            HttpStatus httpStatus;
            String errorMessage;

            if (ex instanceof ResponseStatusException responseStatusException) {
                httpStatus = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
                errorMessage = responseStatusException.getReason();
            } else {
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                errorMessage = "An unexpected error occurred";
            }

            ErrorResponse errorResponse = new ErrorResponse(httpStatus, errorMessage);

            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(httpStatus);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            try {
                String errorResponseJson = objectMapper.writeValueAsString(errorResponse);
                return response.writeWith(Mono.just(response.bufferFactory().wrap(errorResponseJson.getBytes())));
            } catch (Exception e) {
                return Mono.error(e);
            }
        }

        @Data
        @AllArgsConstructor
        private static class ErrorResponse {
            private HttpStatus status;
            private String message;
        }
    }
}
