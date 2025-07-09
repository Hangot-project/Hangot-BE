package com.hanyang.dataportal.core.advice;

import com.hanyang.dataportal.core.exception.*;
import com.hanyang.dataportal.core.response.ApiResponse;
import com.hanyang.dataportal.core.response.ResponseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerController {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(ResourceExistException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityExistsException(ResourceExistException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(FileException.class)
    public ResponseEntity<ApiResponse<?>> handleFileException(FileException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(UnAuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleUnAuthenticationException(UnAuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(e.getMessage()));
    }

    //API 타입 에러
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
    }

    //valid 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        String message = objectError.getDefaultMessage();
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(message));
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse<?>> handleTokenExpiredException(TokenExpiredException e) {
        log.warn(e.getMessage());
        if (e.getMessage().equals(ResponseMessage.REFRESH_EXPIRED)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(ResponseMessage.REFRESH_EXPIRED));
        }
        // access token 만료는 filter에서 처리
//        if (e.getMessage().equals(EXPIRED_ACCESS_TOKEN.getMessage())) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(EXPIRED_ACCESS_TOKEN.getCode()));
//        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(JwtTokenException.class)
    public ResponseEntity<ApiResponse<?>> handleJwtTokenException(JwtTokenException e) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
    }
  
    @ExceptionHandler(NullException.class)
    public ResponseEntity<ApiResponse<?>> handleNullException(NullException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(IllegalProviderException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalProviderException(IllegalProviderException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
    }
}
