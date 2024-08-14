package com.example.kradankanban_backend.controllers;

import com.example.kradankanban_backend.dtos.SimpleStatusDTO;
import com.example.kradankanban_backend.entities.LimitSettings;
import com.example.kradankanban_backend.entities.StatusEntity;
import com.example.kradankanban_backend.services.StatusService;
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
    @Autowired
    private StatusService service;
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("")
    public ResponseEntity<Object> getAll() {
        List<StatusEntity> statuses = service.getAll();
        List<SimpleStatusDTO> simpleStatuses = statuses.stream().map(p -> modelMapper.map(p, SimpleStatusDTO.class)).collect(Collectors.toList());
        return ResponseEntity.ok(simpleStatuses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("")
    public ResponseEntity<Object> addStatus(@Valid @RequestBody StatusEntity status) {
        StatusEntity createdStatus = service.addStatus(status);
        SimpleStatusDTO simpleStatusDTO = modelMapper.map(createdStatus, SimpleStatusDTO.class);
        return new ResponseEntity<> (simpleStatusDTO , HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @Valid @RequestBody StatusEntity status) {
        return new ResponseEntity<>(service.editStatus(id, status) , HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteStatus(@PathVariable int id) {
        return new ResponseEntity<>(service.deleteStatus(id) , HttpStatus.OK);
    }

    @DeleteMapping("/{id}/{newId}")
    public ResponseEntity<Object> transferStatus(@PathVariable int id, @PathVariable int newId) {
        return new ResponseEntity<>(service.transferStatus(id, newId) , HttpStatus.OK);
    }

    @GetMapping("/maximum-task")
    public ResponseEntity<Object> getAllLimitSettings() {
        return new ResponseEntity<>(service.getLimitData() , HttpStatus.OK);
    }

    @PatchMapping("/maximum-task")
    public ResponseEntity<Void> toggleMaximumTask() {
        service.toggleIsEnable();
        return ResponseEntity.noContent().build();
    }
}
