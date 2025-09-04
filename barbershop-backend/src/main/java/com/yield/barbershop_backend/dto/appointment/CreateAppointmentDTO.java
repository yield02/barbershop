package com.yield.barbershop_backend.dto.appointment;

import java.lang.reflect.Array;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentDTO {

    @NotNull(message = "User ID is required")
    private Long userId;
    @NotBlank(message = "Start time is required")
    private LocalDateTime startTime;
    @NotEmpty(message = "Service IDs are required")
    private ArrayList<Long> serviceIds;
    @NotNull(message = "Notes are required")
    private String notes;
    @Null
    private Date createdAt = new Date(System.currentTimeMillis());
    @Null
    private Date updatedAt = new Date(System.currentTimeMillis());

}
