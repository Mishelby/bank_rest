package com.example.bankcards.exception;

public class AuthException extends ApiException {
    public AuthException(String message, String errorCode, int httpStatus) {
        super(message, errorCode, httpStatus);
    }
}
