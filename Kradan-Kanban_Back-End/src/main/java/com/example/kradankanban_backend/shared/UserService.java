package com.example.kradankanban_backend.shared;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    UserRepository repository;

    public List<UserEntity> getAllUser(){
        return repository.findAll();
    }
}
