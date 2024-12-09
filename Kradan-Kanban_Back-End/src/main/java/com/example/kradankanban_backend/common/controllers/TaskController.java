package com.example.kradankanban_backend.common.controllers;

import com.example.kradankanban_backend.common.dtos.DetailTaskDTO;
import com.example.kradankanban_backend.common.dtos.SimpleTaskDTO;
import com.example.kradankanban_backend.common.entities.TaskEntity;
import com.example.kradankanban_backend.common.services.TaskService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {"http://localhost:5174" , "http://localhost:5173"})
//@CrossOrigin(origins = "http://ip23kp2.sit.kmutt.ac.th:80")
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private TaskService service;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("") //DTO
    public ResponseEntity<Object> getAllTasks(@RequestParam(required = false) String sortBy, @RequestParam(required = false) List<String> filterStatuses) {
        List<TaskEntity> tasks = service.findTasks(sortBy, filterStatuses);
        List<SimpleTaskDTO> simpleTaskDTOS = tasks.stream().map(p -> modelMapper.map(p, SimpleTaskDTO.class)).collect(Collectors.toList());
        return ResponseEntity.ok(simpleTaskDTOS);
    }
}
