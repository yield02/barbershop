package com.yield.barbershop_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yield.barbershop_backend.model.User;
import com.yield.barbershop_backend.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    

    @Autowired
    private UserService userService;


    @GetMapping("")
    public List<User> getUserByUsername(@RequestParam String username) {
        return userService.getUserByUsername(username);
    }
    

}