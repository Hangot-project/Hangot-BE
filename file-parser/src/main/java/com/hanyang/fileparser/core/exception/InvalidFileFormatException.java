package com.hanyang.fileparser.core.exception;

public class InvalidFileFormatException extends RuntimeException {
    public InvalidFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
