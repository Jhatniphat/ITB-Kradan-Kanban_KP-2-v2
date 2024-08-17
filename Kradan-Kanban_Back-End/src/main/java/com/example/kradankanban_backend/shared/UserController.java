package com.example.kradankanban_backend.shared;

import com.example.kradankanban_backend.shared.dtos.UserDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@CrossOrigin(origins = {"http://localhost:5174" , "http://localhost:5173"})
//@CrossOrigin(origins = "http://ip23kp2.sit.kmutt.ac.th:80")
@RestController
@RequestMapping("/login")
public class UserController {
    @Autowired
    UserService service;

    @PostMapping("")
    public ResponseEntity login(@RequestBody UserDataDTO userData) {
        if (service.Authentication(userData.getUserName(), userData.getPassWord())) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
