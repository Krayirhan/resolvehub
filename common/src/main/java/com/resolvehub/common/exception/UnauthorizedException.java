package com.resolvehub.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ResolveHubException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }
}
