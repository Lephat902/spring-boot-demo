package com.phatle.demo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.phatle.demo.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findOneByUsername(String username);
}