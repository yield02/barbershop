package com.yield.barbershop_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.model.User;
import com.yield.barbershop_backend.repository.UserRepo;

@Service
public class UserService {


    @Autowired
    private UserRepo userRepo;

    public List<User> getUserByUsername(String username) {

        return userRepo.findByUsernameLike(username);
    }
}
