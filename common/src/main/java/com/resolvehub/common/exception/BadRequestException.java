package com.resolvehub.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ResolveHubException {
    public BadRequestException(String message) {
        super("BAD_REQUEST", message, HttpStatus.BAD_REQUEST);
    }
}
