package com.yield.barbershop_backend.exception;


import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.ErrorDetail;

import io.jsonwebtoken.JwtException;


@RestControllerAdvice
public class GlobalControllerExceptionHandler {
    

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleDataNotFoundException(DataNotFoundException e) {
        ErrorDetail errorDetail = new ErrorDetail("DATA_NOT_FOUND", e.getMessage(), e.getDetails());
        ApiResponse<ErrorDetail> apiResponse = new ApiResponse<>(false, "Data Not Found", errorDetail);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleValidationException(MethodArgumentNotValidException e) {
        ErrorDetail errorDetail = new ErrorDetail("VALIDATION_ERROR", "", e.getAllErrors().stream().map(error -> error.getDefaultMessage()).toList());
        ApiResponse<ErrorDetail> apiResponse = new ApiResponse<>(false, "Validation Error", errorDetail);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataConflictException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleDataConflictException(DataConflictException e) {
        ErrorDetail errorDetail = new ErrorDetail("DATA_CONFLICT", e.getMessage(), e.getDetails());
        ApiResponse<ErrorDetail> apiResponse = new ApiResponse<>(false, "Data Conflict", errorDetail);
        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleUsernameNotFoundException(UsernameNotFoundException e) {
        ErrorDetail errorDetail = new ErrorDetail("UNAUTHORIZED", e.getMessage(), List.of());
        ApiResponse<ErrorDetail> apiResponse = new ApiResponse<>(false, e.getMessage(), errorDetail);
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleUnauthorizedException(JwtException e) {
        ErrorDetail errorDetail  = new ErrorDetail("UNAUTHORIZED", e.getMessage(), List.of());
        ApiResponse<ErrorDetail> apiResponse = new ApiResponse<>(false, e.getMessage(), errorDetail);
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(AccessDeniedException.class) 
    public ResponseEntity<ApiResponse<ErrorDetail>> handleIllegalArgumentException(AccessDeniedException e) {
        ErrorDetail errorDetail  = new ErrorDetail("FORBIDDEN", "You are not authorized", List.of());
        ApiResponse<ErrorDetail> apiResponse = new ApiResponse<>(false, "You are not authorized", errorDetail);
        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleIllegalArgumentException(BadRequestException e) {
        ErrorDetail errorDetail  = new ErrorDetail("BAD_REQUEST", e.getMessage(), List.of());
        ApiResponse<ErrorDetail> apiResponse = new ApiResponse<>(false, e.getMessage(), errorDetail);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }
}
