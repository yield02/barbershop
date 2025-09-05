package com.yield.barbershop_backend.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerRegisterDTO {
    

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+84|84|0)[35789]\\d{8}$", message = "Invalid phone number format")
    private String phoneNumber;

}
