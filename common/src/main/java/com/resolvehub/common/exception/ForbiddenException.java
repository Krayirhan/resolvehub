package com.resolvehub.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ResolveHubException {
    public ForbiddenException(String message) {
        super("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }
}
