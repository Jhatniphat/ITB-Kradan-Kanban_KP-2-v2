package com.example.kradankanban_backend.controllers;

import com.example.kradankanban_backend.dtos.DetailTaskDTO;
import com.example.kradankanban_backend.dtos.SimpleTaskDTO;
import com.example.kradankanban_backend.entities.TaskEntity;
import com.example.kradankanban_backend.services.TaskService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    @GetMapping("/{id}") //Get Task By ID
    public TaskEntity getTaskById(@PathVariable int id) {
        return service.findById(id);
    }

    @PostMapping("")
    public ResponseEntity<Object> addTask(@Valid @RequestBody TaskEntity task) {
        TaskEntity createdTask = service.addTask(task);
        DetailTaskDTO createdTaskDTO = modelMapper.map(createdTask, DetailTaskDTO.class);
        return new ResponseEntity<>(createdTaskDTO, HttpStatus.CREATED);
    }
//    @PostMapping("")
//    public NewTaskDTO addTask(@RequestBody NewTaskDTO task) {
//        return service.addTask(task);
//    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> editTask(@PathVariable int id,@Valid @RequestBody TaskEntity task) {
        TaskEntity updatedTask = service.editTask(id, task);
        DetailTaskDTO updatedTaskDTO = modelMapper.map(updatedTask, DetailTaskDTO.class);
        return new ResponseEntity<>(updatedTaskDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SimpleTaskDTO> deleteTaskById(@PathVariable int id) {
        SimpleTaskDTO simpleTaskDTO = modelMapper.map(service.deleteTask(id), SimpleTaskDTO.class);
        return ResponseEntity.ok().body(simpleTaskDTO);
    }

}
