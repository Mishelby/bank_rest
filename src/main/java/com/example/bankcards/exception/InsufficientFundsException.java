package com.example.bankcards.exception;

public class InsufficientFundsException extends ApiException {
    public InsufficientFundsException(String message, String errorCode, int httpStatus) {
        super(message, errorCode,  httpStatus);
    }
}
