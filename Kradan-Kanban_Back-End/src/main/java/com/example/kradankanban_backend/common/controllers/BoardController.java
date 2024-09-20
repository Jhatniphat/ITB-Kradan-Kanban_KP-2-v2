package com.example.kradankanban_backend.common.controllers;


import com.example.kradankanban_backend.common.dtos.*;
import com.example.kradankanban_backend.common.entities.BoardEntity;
import com.example.kradankanban_backend.common.entities.StatusEntity;
import com.example.kradankanban_backend.common.entities.TaskEntity;
import com.example.kradankanban_backend.common.services.BoardService;
import com.example.kradankanban_backend.common.services.StatusService;
import com.example.kradankanban_backend.common.services.TaskService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

//@CrossOrigin(origins = {"http://localhost:5174" , "http://localhost:5173"})
//@CrossOrigin(origins = "http://ip23kp2.sit.kmutt.ac.th:80")
@CrossOrigin(origins = "${cors.allowed-origins}")
@RestController
@RequestMapping("/v3/boards")
public class BoardController {
    @Autowired
    private BoardService service;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private StatusService statusService;


    //  ! ============================================== BOARD ==============================================
    @GetMapping("/{boardId}")
    public DetailBoardDTO getBoardById(@PathVariable String boardId) {
        return service.getBoardById(boardId);
    }

    @GetMapping("")
    public ResponseEntity<DetailBoardDTO> getBoardByUserId(@RequestHeader("Authorization") String requestTokenHeader) {
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            String userId = extractUserIdFromToken(jwtToken);
            return ResponseEntity.ok(service.getBoardByUserId(userId));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authorization header must start with Bearer");
        }
    }

    @PostMapping("")
    public ResponseEntity<DetailBoardDTO> AddNewBoard(@RequestHeader("Authorization") String requestTokenHeader ,@Valid @RequestBody(required = false) BoardNameDTO boardNameDTO) {
        if (boardNameDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is missing");
        }
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            String userId = extractUserIdFromToken(jwtToken);
//            return service.AddBoard(userId, boardNameDTO.getName());
            return new ResponseEntity<>(service.AddBoard(userId, boardNameDTO.getName()), HttpStatus.CREATED);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authorization header must start with Bearer");
        }
    }

    //  ! ============================================== TASK ==============================================

    // "/boards/tasks"

    @GetMapping("/{boardId}/tasks")
    public ResponseEntity<Object> getTasksByBoardId(@RequestHeader("Authorization") String requestTokenHeader ,@PathVariable String boardId) {
        String userId = extractUserIdFromToken(requestTokenHeader.substring(7));
        DetailBoardDTO board = service.getBoardById(boardId);
        if (board == null) {
            return new ResponseEntity<>("Board not found", HttpStatus.NOT_FOUND);
        }
        List<TaskEntity> tasks = taskService.findAllTasksByBoardId(userId,boardId);
        List<SimpleTaskDTO> simpleTaskDTOS = tasks.stream().map(task -> modelMapper.map(task, SimpleTaskDTO.class)).collect(Collectors.toList());

        return new ResponseEntity<>(simpleTaskDTOS, HttpStatus.OK);
    }

    @PostMapping("/{boardId}/tasks")
    public ResponseEntity<DetailTaskDTO> addTaskForBoard(@RequestHeader("Authorization") String requestTokenHeader ,@PathVariable String boardId, @Valid @RequestBody AddEditTaskDTO addEditTaskDTO) {
        String userId = extractUserIdFromToken(requestTokenHeader.substring(7));
        TaskEntity task = new TaskEntity();
        task.setTitle(addEditTaskDTO.getTitle());
        task.setDescription(addEditTaskDTO.getDescription());
        task.setAssignees(addEditTaskDTO.getAssignees());
        task.setStatus(addEditTaskDTO.getStatus());

        TaskEntity createdTask = taskService.addTaskForBoard(userId,boardId, task);
        DetailTaskDTO createdTaskDTO = modelMapper.map(createdTask, DetailTaskDTO.class);
        return new ResponseEntity<>(createdTaskDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<DetailTaskWithTimeOnDTO> getTaskByBoardIdAndTaskId(@RequestHeader("Authorization") String requestTokenHeader ,@PathVariable String boardId, @PathVariable int taskId) {
        String userId = extractUserIdFromToken(requestTokenHeader.substring(7));
        TaskEntity task = taskService.findTaskByBoardIdAndTaskId(userId,boardId, taskId);
        DetailTaskWithTimeOnDTO DetailTask = modelMapper.map(task, DetailTaskWithTimeOnDTO.class);
        return ResponseEntity.ok(DetailTask);
    }

    @PutMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<DetailTaskDTO> editTaskForBoard(@RequestHeader("Authorization") String requestTokenHeader ,@PathVariable String boardId, @PathVariable int taskId, @Valid @RequestBody AddEditTaskDTO addEditTaskDTO) {
        String userId = extractUserIdFromToken(requestTokenHeader.substring(7));
        TaskEntity task = new TaskEntity();
        task.setTitle(addEditTaskDTO.getTitle());
        task.setDescription(addEditTaskDTO.getDescription());
        task.setAssignees(addEditTaskDTO.getAssignees());
        task.setStatus(addEditTaskDTO.getStatus());

        TaskEntity updatedTask = taskService.editTaskForBoard(userId,boardId, taskId, task);
        DetailTaskDTO updatedTaskDTO = modelMapper.map(updatedTask, DetailTaskDTO.class);
        return new ResponseEntity<>(updatedTaskDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<SimpleTaskDTO> deleteTask(@RequestHeader("Authorization") String requestTokenHeader, @PathVariable String boardId, @PathVariable int taskId) {
        String userId = extractUserIdFromToken(requestTokenHeader.substring(7));
        SimpleTaskDTO deletedTask = taskService.deleteTaskByBoardIdAndTaskId(userId, boardId, taskId);
        return ResponseEntity.ok(deletedTask);
    }



    //  ! ============================================== STATUS ==============================================

    // /board/status
    @GetMapping("/{boardId}/statuses")
    public ResponseEntity<Object> getAll(@PathVariable String boardId) {
        List<StatusEntity> statuses = statusService.getAll(boardId);
        List<SimpleStatusDTO> simpleStatuses = statuses.stream().map(p -> modelMapper.map(p, SimpleStatusDTO.class)).collect(Collectors.toList());
        return ResponseEntity.ok(simpleStatuses);
    }

    @GetMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<Object> getById(@PathVariable String boardId, @PathVariable int statusId) {
        return ResponseEntity.ok(statusService.getById(boardId, statusId));
    }

    @PostMapping("/{boardId}/statuses")
    public ResponseEntity<Object> addStatus(@PathVariable String boardId,@Valid @RequestBody StatusEntity status) {
        StatusEntity createdStatus = statusService.addStatus(boardId, status);
        SimpleStatusDTO simpleStatusDTO = modelMapper.map(createdStatus, SimpleStatusDTO.class);
        return new ResponseEntity<> (simpleStatusDTO , HttpStatus.CREATED);
    }

    @PutMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<Object> updateStatus(@PathVariable String boardId,@PathVariable int statusId, @Valid @RequestBody StatusEntity status) {
        return new ResponseEntity<>(statusService.editStatus(boardId, statusId, status) , HttpStatus.OK);
    }

    @DeleteMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<Object> deleteStatus(@PathVariable String boardId,@PathVariable int statusId) {
        return new ResponseEntity<>(statusService.deleteStatus(boardId, statusId) , HttpStatus.OK);
    }

    @DeleteMapping("/{boardId}/statuses/{oldId}/{newId}")
    public ResponseEntity<Object> transferStatus(@PathVariable String boardId, @PathVariable int oldId, @PathVariable int newId) {
        return new ResponseEntity<>(statusService.transferStatus(boardId, oldId, newId) , HttpStatus.OK);
    }

    @GetMapping("/{boardId}/statuses/maximum-task")
    public ResponseEntity<Object> getAllLimitSettings(@PathVariable String boardId) {
        return new ResponseEntity<>(statusService.getLimitData(boardId) , HttpStatus.OK);
    }

    @PatchMapping("/{boardId}/statuses/maximum-task")
    public ResponseEntity<Void> toggleMaximumTask(@PathVariable String boardId) {
        statusService.toggleIsEnable(boardId);
        return ResponseEntity.noContent().build();
    }

    //  ! ============================================== PRIVATE METHOD ==============================================

    // Add this method to extract userId from JWT token
    private String extractUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey("N7KgseMPtJ26AEved0ahUKEwj4563eioyFAxUyUGwGHbTODx0Q4dUDCBA") // Use your secret key here
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("oid" , String.class); // Assuming userId is stored in the subject
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }
}
