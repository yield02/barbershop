package com.yield.barbershop_backend.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class ApiResponse<T> {

    private Boolean success;
    private String message;
    private T data;
    private String timestamp;
    private ErrorDetail errorDetail;

    public ApiResponse() {
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public ApiResponse(Boolean success, String message, T data) {
        this();
        this.success = success;
        this.message = message.isEmpty() ?  "Thành công" :  message;
        this.data = data;
    }

    public ApiResponse(Boolean success, String message, T data, ErrorDetail errorDetail) {
        this(success, message, data);
        this.errorDetail = errorDetail;
    }
    
}
