package com.campusmarket.backend.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String errorCode) {
        super(HttpStatus.FORBIDDEN, "You don't have permission to do this", errorCode);
    }
}