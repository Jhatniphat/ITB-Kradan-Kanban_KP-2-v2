package com.example.kradankanban_backend.shared;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
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

    public boolean Authentication(String username, String password){
        UserEntity userEntity = repository.findByUsername(username);
        if(userEntity == null){
            return false;
        } else {
            Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 16,32);
            char[] passwordArray = password.toCharArray();
            return argon2.verify(userEntity.getEmail(), passwordArray);
        }
    }
}
