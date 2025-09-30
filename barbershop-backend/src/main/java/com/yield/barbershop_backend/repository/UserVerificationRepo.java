package com.yield.barbershop_backend.repository;

import org.springframework.data.repository.CrudRepository;

import com.yield.barbershop_backend.model.UserVerification;
import com.yield.barbershop_backend.model.UserVerification.VerificationType;

public interface UserVerificationRepo extends CrudRepository<UserVerification, Long> {
    
    

    UserVerification findByUserIdAndType(Long userId, VerificationType type);

} 
