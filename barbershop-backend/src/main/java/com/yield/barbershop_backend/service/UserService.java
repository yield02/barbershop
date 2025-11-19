package com.yield.barbershop_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.dto.user.UserDTO;
import com.yield.barbershop_backend.dto.user.UserFilterDTO;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.AccountPrincipal;
import com.yield.barbershop_backend.model.User;
import com.yield.barbershop_backend.model.UserVerification;
import com.yield.barbershop_backend.repository.UserRepo;
import com.yield.barbershop_backend.repository.UserVerificationRepo;
import com.yield.barbershop_backend.specification.UserSpecification;
import com.yield.barbershop_backend.util.EmailUltil;

import jakarta.transaction.Transactional;

@Service
public class UserService {


    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserVerificationRepo userVerificationRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailUltil emailUltil;

    public Page<User> getUsersByFilter(UserFilterDTO filter) {
        Pageable page = PageRequest.of(filter.getPage(), filter.getPageSize());
        return userRepo.findAll(UserSpecification.filter(filter), page);
    }

    public User getUserById(Long userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new DataNotFoundException("User not found"));
        // user.setEmail(user.getMaskedEmail());
        // user.setPhoneNumber(user.getMaskedPhoneNumber());
        return user;
    }

    @Transactional
    public void updateUser(Long userId, UserDTO user) {
        int result = userRepo.updateUserById(userId, user.getFullName(), user.getRole(), user.getEmail(), user.getPhoneNumber(), user.getIsActive());
        if(result == 0) {
            throw new DataNotFoundException("User not found");
        }
    }

    public void deleteUser(Long userId) {
        userRepo.deleteById(userId);
    }

    public UserDetails loadUserByEmail(String email) {

        User user = userRepo.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Email not found"));
        
        return new AccountPrincipal<User>(user);
    }



    @Transactional
    public void sendUserEmailVerification(Long userId) {
        
        User user = userRepo.findById(userId)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if(user.getEmail() == null) {
            throw new DataNotFoundException("Email not found");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15);

        UserVerification userVerification = userVerificationRepo.findByUserIdAndType(user.getId(), UserVerification.VerificationType.EMAIL);

        if(userVerification != null) {
            Boolean isVerified = userVerification.getVerified();
            if(isVerified) {
                throw new DataNotFoundException("Email already verified");
            }
            else if(userVerification.getExpiry_at().isAfter(LocalDateTime.now())) {
                throw new DataNotFoundException("Verification email already sent");
            }
        }

        else {
            userVerification = new UserVerification();
            userVerification.setUserId(user.getId());
            userVerification.setType(UserVerification.VerificationType.EMAIL);
            userVerification.setVerified(false);
            userVerification.setVerified_at(null);
        }
        
        userVerification.setToken_hash(token);
        userVerification.setExpiry_at(expiryTime);
        userVerificationRepo.save(userVerification);

        String verificationLink = emailUltil.getAppBaseUrl() + "/auth/staff/verify-email?token=" + token + "&userId=" + user.getId();
        String text = emailUltil.getStaffVerificationTextHtml(user.getFullName(), verificationLink, expiryTime);
        emailService.sendEmail(user.getEmail(), "[Barbershop] Staff Email Verification", text);

    }

    @Transactional
    public void verifyUserEmail(Long userId, String token) {

        UserVerification userVerification = userVerificationRepo.findByUserIdAndType(userId, UserVerification.VerificationType.EMAIL);
        
        if(userVerification.getExpiry_at().isBefore(LocalDateTime.now())) {
            userVerificationRepo.delete(userVerification);
            throw new DataNotFoundException("Verification token expired");
        }

        if(userVerification.getToken_hash().equals(token)) {
            userVerification.setVerified(true);
            userVerification.setVerified_at(LocalDateTime.now());
            userVerificationRepo.save(userVerification);
        }
        else {
            userVerificationRepo.delete(userVerification);
            throw new DataNotFoundException("Verification token is incorrect");
        }

    }

    public List<User> getUserByIds(Set<Long> userIds) {
        return userRepo.findAllById(userIds);
    }

}
