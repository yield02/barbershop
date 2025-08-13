package com.yield.barbershop_backend.repository;

import org.springframework.stereotype.Repository;

import com.yield.barbershop_backend.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository

public interface UserRepo extends JpaRepository<User, Long> {
    List<User> findByUsernameLike(String username);
}

