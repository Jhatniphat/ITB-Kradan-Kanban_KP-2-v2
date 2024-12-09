package com.example.kradankanban_backend.common.controllers;

import com.example.kradankanban_backend.common.dtos.SimpleStatusDTO;
import com.example.kradankanban_backend.common.entities.StatusEntity;
import com.example.kradankanban_backend.common.services.StatusService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:5174" , "http://localhost:5173"})
//@CrossOrigin(origins = "http://ip23kp2.sit.kmutt.ac.th:80")
@RestController
@RequestMapping("/statuses")
public class StatusController {
}
