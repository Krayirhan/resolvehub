package com.resolvehub.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ResolveHubException {
    public NotFoundException(String message) {
        super("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}
