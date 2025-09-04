package com.yield.barbershop_backend.dto.appointment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePaymentConfirmedAppointmentDTO {
    
    @NotNull(message = "Payment confirmed status must not be null")
    private Boolean paymentConfirmed;
    
}
