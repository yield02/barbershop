package com.yield.barbershop_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.PagedResponse;
import com.yield.barbershop_backend.dto.UserDTO;
import com.yield.barbershop_backend.dto.UserFilterDTO;
import com.yield.barbershop_backend.model.User;
import com.yield.barbershop_backend.service.UserService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/users")
public class UserController {
    
 
    @Autowired
    private UserService userService;


    @GetMapping("")
    public ResponseEntity<ApiResponse<PagedResponse<User>>> getUsersByFilter(UserFilterDTO filter) {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "",
            new PagedResponse<User>(userService.getUsersByFilter(filter))
        ));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "User fetched successfully",
            userService.getUserById(userId)
        ));
    }
    
    @PutMapping("/{userId}") 
    public ResponseEntity<ApiResponse<String>> updateUser(@PathVariable Long userId, @RequestBody @Validated UserDTO user) {
        userService.updateUser(userId, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    

}