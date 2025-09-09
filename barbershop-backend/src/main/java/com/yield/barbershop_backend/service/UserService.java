package com.yield.barbershop_backend.service;

import java.util.List;

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
import com.yield.barbershop_backend.repository.UserRepo;
import com.yield.barbershop_backend.specification.UserSpecification;

import jakarta.transaction.Transactional;

@Service
public class UserService {


    @Autowired
    private UserRepo userRepo;


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



}
