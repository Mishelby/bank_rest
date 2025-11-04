package com.example.bankcards.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    protected final String errorCode;
    protected final int httpStatus;

    public ApiException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
