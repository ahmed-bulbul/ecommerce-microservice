package com.bulbul.accountservice.exception;

import lombok.Data;

@Data
public class CustomException  extends RuntimeException {

    String errorCode;

    public CustomException(String message,String errorCode){
        super(message);
        this.errorCode=errorCode;
    }
}
