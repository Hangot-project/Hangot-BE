package com.hanyang.api.core.exception;

public class JwtTokenException extends RuntimeException{
    public JwtTokenException(String message) {
        super(message);
    }
}
