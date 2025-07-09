package com.hanyang.dataportal.core.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(final String message) {
        super(message);
    }
}
