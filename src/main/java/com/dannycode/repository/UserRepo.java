package com.dannycode.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dannycode.model.User;

public interface UserRepo  extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

}
