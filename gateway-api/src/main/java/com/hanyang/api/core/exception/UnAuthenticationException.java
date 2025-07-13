package com.hanyang.api.core.exception;

public class UnAuthenticationException extends RuntimeException{
    public UnAuthenticationException(String message) {
        super(message);
    }
}
