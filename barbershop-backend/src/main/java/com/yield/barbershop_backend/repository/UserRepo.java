package com.yield.barbershop_backend.repository;

import org.springframework.stereotype.Repository;

import com.yield.barbershop_backend.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

@Repository

public interface UserRepo extends 
JpaRepository<User, Long>,
PagingAndSortingRepository<User, Long>,
JpaSpecificationExecutor<User>

{
    List<User> findByUsernameLike(String username);

    @Modifying
    @Query("UPDATE users u SET u.fullName = :fullName, u.role = :role, u.email = :email, u.phoneNumber = :phoneNumber, u.isActive = :isActive WHERE u.id = :userId")
    int updateUserById(@Param("userId") Long userId, @Param("fullName") String fullName, @Param("role") String role, @Param("email") String email, @Param("phoneNumber") String phoneNumber, @Param("isActive") Byte isActive);
}

