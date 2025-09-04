package com.yield.barbershop_backend.dto.appointment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateStatusAppointmentDTO {
    
    @NotBlank
    @Pattern(regexp = "^(Pending|Confirmed|Completed|Cancelled)$", message = "Status must be one of the following values: Pending, Confirmed, Completed, Cancelled")
    private String status;

}
