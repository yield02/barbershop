package com.yield.barbershop_backend.util;

import org.springframework.stereotype.Component;

@Component
public class NumberUtil {
    

    public static Double round(Double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


}
