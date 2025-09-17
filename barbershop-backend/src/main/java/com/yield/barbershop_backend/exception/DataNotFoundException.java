package com.yield.barbershop_backend.exception;

import java.util.List;

import lombok.Data;


@Data
public class DataNotFoundException extends RuntimeException {


    List<?> details;

    public DataNotFoundException(String message) {
        super(message);
    }

    public DataNotFoundException(String message, List<?> details) {
        super(message);
        this.details = details;
    }
}
