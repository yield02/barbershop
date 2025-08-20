package com.yield.barbershop_backend.exception;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.ErrorDetail;

import jakarta.validation.ValidationException;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {
    

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleDataNotFoundException(DataNotFoundException e) {
        ErrorDetail errorDetail = new ErrorDetail("DATA_NOT_FOUND", e.getMessage(), List.of());
        ApiResponse<ErrorDetail> apiResponse = new ApiResponse<>(false, "Data Not Found", errorDetail);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleValidationException(MethodArgumentNotValidException e) {
        ErrorDetail errorDetail = new ErrorDetail("VALIDATION_ERROR", "", e.getAllErrors().stream().map(error -> error.getDefaultMessage()).toList());
        ApiResponse<ErrorDetail> apiResponse = new ApiResponse<>(false, "Validation Error", errorDetail);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    

}
