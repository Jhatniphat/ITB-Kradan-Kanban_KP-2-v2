package com.example.kradankanban_backend.common.controllers;


import com.example.kradankanban_backend.common.dtos.*;
import com.example.kradankanban_backend.common.entities.BoardEntity;
import com.example.kradankanban_backend.common.entities.CollabEntity;
import com.example.kradankanban_backend.common.entities.StatusEntity;
import com.example.kradankanban_backend.common.entities.TaskEntity;
import com.example.kradankanban_backend.common.services.*;
import com.example.kradankanban_backend.shared.Entities.UserEntity;
import com.example.kradankanban_backend.shared.services.JwtUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "${cors.allowed-origins}")
@RestController
@RequestMapping("/v3/boards")
public class BoardController {
    @Autowired
    private BoardService service;
    @Autowired
    private TaskService taskService;
    @Autowired
    private CollabService collabService;
    @Autowired
    private FileService fileService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private StatusService statusService;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    private String getUserId(HttpServletRequest request) {
        String userId = null;
        String jwtToken = request.getHeader("Authorization");;
        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            userId = extractUserIdFromToken(jwtToken.substring(7));
        }
        return userId;
    }

    //  ! ============================================== BOARD ==============================================
    @GetMapping("/{boardId}")
    public DetailBoardDTO getBoardById(@PathVariable String boardId, HttpServletRequest request) {
        return service.getBoardById(boardId);
    }

    @GetMapping("")
    public ResponseEntity<List<DetailBoardDTO>> getBoardByUserId(@RequestHeader("Authorization") String requestTokenHeader) {
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

    @PatchMapping("/{boardId}")
    public ResponseEntity<VisibilityDTO> updateBoardVisibility(@PathVariable String boardId, @RequestBody VisibilityDTO visibility, HttpServletRequest request) {
        VisibilityDTO editVisibility = service.editVisibility(boardId, visibility);
        return ResponseEntity.ok(editVisibility);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<BoardEntity> deleteBoard(@PathVariable String boardId) {
        BoardEntity deletedBoard = service.deleteBoard(boardId);
        return ResponseEntity.ok(deletedBoard);
    }

    //  ! ============================================== TASK ==============================================

    // "/boards/tasks"

    @GetMapping("/{boardId}/tasks")
    public ResponseEntity<Object> getTasksByBoardId(@PathVariable String boardId, HttpServletRequest request) {
        String userId = getUserId(request);
        DetailBoardDTO board = service.getBoardById(boardId);
        if (board == null) {
            return new ResponseEntity<>("Board not found", HttpStatus.NOT_FOUND);
        }
        List<SimpleTaskDTO> simpleTaskDTOS = taskService.findAllTasksByBoardId(userId, boardId);
        return new ResponseEntity<>(simpleTaskDTOS, HttpStatus.OK);
    }

    @PostMapping("/{boardId}/tasks")
    public ResponseEntity<DetailTaskDTO> addTaskForBoard(@PathVariable String boardId, @Valid @RequestBody AddEditTaskDTO addEditTaskDTO, HttpServletRequest request) {
        String userId = getUserId(request);
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
    public ResponseEntity<DetailTaskWithTimeOnDTO> getTaskByBoardIdAndTaskId(@PathVariable String boardId, @PathVariable int taskId,HttpServletRequest request) {
        DetailTaskWithTimeOnDTO task = taskService.findTaskByBoardIdAndTaskId(boardId, taskId);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<DetailTaskDTO> editTaskForBoard(@PathVariable String boardId, @PathVariable int taskId, @Valid @RequestBody AddEditTaskDTO addEditTaskDTO, HttpServletRequest request) {
        String userId = getUserId(request);
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
    public ResponseEntity<SimpleTaskDTO> deleteTask(@PathVariable String boardId, @PathVariable int taskId, HttpServletRequest request) {
        String userId = getUserId(request);
        SimpleTaskDTO deletedTask = taskService.deleteTaskByBoardIdAndTaskId(userId, boardId, taskId);
        return ResponseEntity.ok(deletedTask);
    }



    //  ! ============================================== STATUS ==============================================

    // /board/status
    @GetMapping("/{boardId}/statuses")
    public ResponseEntity<Object> getAll(@PathVariable String boardId,HttpServletRequest request) {
        List<StatusEntity> statuses = statusService.getAll(boardId);
        List<SimpleStatusDTO> simpleStatuses = statuses.stream().map(p -> modelMapper.map(p, SimpleStatusDTO.class)).collect(Collectors.toList());
        return ResponseEntity.ok(simpleStatuses);
    }

    @GetMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<Object> getById(@PathVariable String boardId, @PathVariable int statusId,HttpServletRequest request) {
        return ResponseEntity.ok(statusService.getById(boardId, statusId));
    }

    @PostMapping("/{boardId}/statuses")
    public ResponseEntity<Object> addStatus(@PathVariable String boardId,@Valid @RequestBody StatusEntity status, HttpServletRequest request) {
        StatusEntity createdStatus = statusService.addStatus(boardId, status);
        SimpleStatusDTO simpleStatusDTO = modelMapper.map(createdStatus, SimpleStatusDTO.class);
        return new ResponseEntity<> (simpleStatusDTO , HttpStatus.CREATED);
    }

    @PutMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<Object> updateStatus(@PathVariable String boardId,@PathVariable int statusId, @Valid @RequestBody StatusEntity status, HttpServletRequest request) {
        return new ResponseEntity<>(statusService.editStatus(boardId, statusId, status) , HttpStatus.OK);
    }

    @DeleteMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<Object> deleteStatus(@PathVariable String boardId,@PathVariable int statusId, HttpServletRequest request) {
        String userId = getUserId(request);
        return new ResponseEntity<>(statusService.deleteStatus(userId, boardId, statusId) , HttpStatus.OK);
    }

    @DeleteMapping("/{boardId}/statuses/{oldId}/{newId}")
    public ResponseEntity<Object> transferStatus(@PathVariable String boardId, @PathVariable int oldId, @PathVariable int newId, HttpServletRequest request) {
        return new ResponseEntity<>(statusService.transferStatus(boardId, oldId, newId) , HttpStatus.OK);
    }

    @GetMapping("/{boardId}/statuses/maximum-task")
    public ResponseEntity<Object> getAllLimitSettings(@PathVariable String boardId, HttpServletRequest request) {
        return new ResponseEntity<>(statusService.getLimit(boardId) , HttpStatus.OK);
    }

    @PatchMapping("/{boardId}/statuses/maximum-task")
    public ResponseEntity<Object> updateLimitAndToggle (@PathVariable String boardId, @RequestBody LimitDTO limitDTO) {
        service.updateLimitAndToggle(boardId, limitDTO);
        return ResponseEntity.ok(limitDTO);
    }

    //  ! ============================================== COLLAB ==============================================

    @GetMapping("/{boardId}/collabs")
    public ResponseEntity<List<CollabDTO>> getAllCollaborators(@PathVariable String boardId, HttpServletRequest request) {
        List<CollabEntity> collabEntities = collabService.getAllCollaborators(boardId);
        List<CollabDTO> collabDTOs = collabEntities.stream().map(collabEntity -> {
            UserEntity user = jwtUserDetailsService.getUserById(collabEntity.getUserId());
            CollabDTO collabDTO = modelMapper.map(collabEntity, CollabDTO.class);
            modelMapper.map(user, collabDTO);
            return collabDTO;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(collabDTOs);
    }

    @GetMapping("{boardId}/collabs/{collabId}")
    public ResponseEntity<CollabDTO> getCollaborator(@PathVariable String boardId, @PathVariable String collabId) {
        CollabEntity collabEntity = collabService.getCollaborators(boardId, collabId);
        UserEntity user = jwtUserDetailsService.getUserById(collabEntity.getUserId());
        CollabDTO collabDTO = modelMapper.map(collabEntity, CollabDTO.class);
        modelMapper.map(user, collabDTO);
        return ResponseEntity.ok(collabDTO);

    }

    @PostMapping("{boardId}/collabs")
    public ResponseEntity<CollabDTO> addCollaborator(@PathVariable String boardId, @RequestBody CollabRequestDTO collabRequestDTO) {
        CollabEntity collabEntity = collabService.addCollaborator(boardId, collabRequestDTO);
        CollabDTO collabDTO = modelMapper.map(collabEntity, CollabDTO.class);
        UserEntity user = jwtUserDetailsService.getUserById(collabEntity.getUserId());
        modelMapper.map(user, collabDTO);
        return new ResponseEntity<>(collabDTO, HttpStatus.CREATED);
    }

    @PatchMapping("{boardId}/collabs/{collabId}")
    public ResponseEntity<CollabEntity> updateAccessCollaborator(@PathVariable String boardId, @PathVariable String collabId, @RequestBody CollabEntity newCollabEntity) {
        CollabEntity collabEntity = collabService.updateAccessCollaborator(boardId, collabId, newCollabEntity);
        return ResponseEntity.ok(collabEntity);
    }

    @PatchMapping("{boardId}/collabs/invitations")
    public ResponseEntity<CollabEntity> updateStatusCollaborator(@PathVariable String boardId, @RequestBody CollabEntity newCollabEntity) {
        CollabEntity collabEntity = collabService.updateStatusCollaborator(boardId, newCollabEntity);
        return ResponseEntity.ok(collabEntity);
    }

    @DeleteMapping("{boardId}/collabs/{collabId}")
    public ResponseEntity<CollabEntity> deleteCollaborator(@PathVariable String boardId, @PathVariable String collabId) {
        CollabEntity collabEntity = collabService.deleteCollaborator(boardId, collabId);
        return ResponseEntity.ok(collabEntity);
    }

    //  ! ============================================== ATTACHMENT ==============================================
    @GetMapping("/{boardId}/tasks/{taskId}/attachments")
    public ResponseEntity<List<AttachmentDTO>> getAllAttachments(@PathVariable String boardId, @PathVariable Integer taskId) {
        List<AttachmentDTO> attachments = fileService.getAllAttachments(taskId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/{boardId}/tasks/{taskId}/attachments/download/{attachmentId}")
    public ResponseEntity<Object> downloadAttachment(@PathVariable String boardId, @PathVariable Integer taskId, @PathVariable Integer attachmentId) {
        Resource resource = fileService.downloadAttachment(attachmentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/{boardId}/tasks/{taskId}/attachments")
    public ResponseEntity<List<AttachmentResponseDTO>> uploadAttachments(
            @PathVariable String boardId,
            @PathVariable Integer taskId,
            @RequestParam("files") List<MultipartFile> files) {
        List<AttachmentResponseDTO> responseDTOs = fileService.uploadAttachments(taskId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTOs);
    }

    @DeleteMapping("/{boardId}/tasks/{taskId}/attachments/{attachmentId}")
    public ResponseEntity<AttachmentDTO> deleteAttachment(@PathVariable String boardId, @PathVariable Integer taskId, @PathVariable Integer attachmentId) {
        AttachmentDTO deletedAttachment = fileService.deleteAttachment(attachmentId);
        return ResponseEntity.ok(deletedAttachment);
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
