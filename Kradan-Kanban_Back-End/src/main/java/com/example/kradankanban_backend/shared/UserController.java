package com.example.kradankanban_backend.shared;

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

//    @GetMapping("")
//    public List<UserEntity> getAllUser(){
//        return service.getAllUser();
//    }

    @PostMapping("")
    public ResponseEntity login(@RequestBody String username, @RequestBody String password) {
        if (service.Authentication(username, password)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
