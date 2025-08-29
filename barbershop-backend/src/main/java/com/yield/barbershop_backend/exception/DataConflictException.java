package com.yield.barbershop_backend.exception;

import java.util.List;

import lombok.Data;


@Data
public class DataConflictException extends RuntimeException {
    
    private List<?> details;

    public DataConflictException(String message) {
        super(message);
    }

    public DataConflictException(String message, List<?> details) {
        super(message);
        this.details = details;
    }

}
