package com.example.kradankanban_backend.shared;

import com.example.kradankanban_backend.shared.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    UserEntity findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
}
