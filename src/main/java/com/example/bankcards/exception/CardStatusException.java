package com.example.bankcards.exception;

public class CardStatusException extends ApiException {
    public CardStatusException(String message, String errorCode, int httpStatus) {
        super(message, errorCode,  httpStatus);
    }
}
