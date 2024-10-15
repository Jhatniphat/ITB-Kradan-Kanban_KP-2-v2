package com.example.kradankanban_backend.common.services;

import com.example.kradankanban_backend.common.dtos.DetailBoardDTO;
import com.example.kradankanban_backend.common.dtos.SimpleTaskDTO;
import com.example.kradankanban_backend.common.entities.BoardEntity;
import com.example.kradankanban_backend.common.entities.CollabEntity;
import com.example.kradankanban_backend.common.entities.TaskEntity;
import com.example.kradankanban_backend.common.repositories.BoardRepository;
import com.example.kradankanban_backend.common.repositories.CollabRepository;
import com.example.kradankanban_backend.exceptions.*;
import com.example.kradankanban_backend.common.repositories.StatusRepository;
import com.example.kradankanban_backend.common.repositories.TaskRepository;
import com.example.kradankanban_backend.common.services.StatusService;
import com.example.kradankanban_backend.shared.services.JwtUserDetailsService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository repository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private StatusService statusService;
    @Autowired
    private CollabRepository collabRepository;

    //New TaskService With Personal Board
    public List<TaskEntity> findAllTasksByBoardId(String userId, String boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new WrongBoardException("Board not found");
        }
        BoardEntity board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
//        if (!board.getUserId().equals(userId)) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this board.");
//        }
        return repository.findByTkBoard_BoardId(boardId);
    }


    public TaskEntity findTaskByBoardIdAndTaskId(String userId, String boardId, int taskId) {
        if (!repository.existsByIdAndTkBoard(taskId, boardRepository.findByBoardId(boardId).orElseThrow(() -> new WrongBoardException("Board not found")))) {
            throw new WrongBoardException("Board not found");
        }
        BoardEntity board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
//        if (!board.getUserId().equals(userId)) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this board.");
//        }
        return repository.findByTkBoardAndId(board, taskId).orElseThrow(() -> new ItemNotFoundException("Task ID " + taskId + " does not exist !!!"));
    }

    public TaskEntity addTaskForBoard(String userId, String boardId, TaskEntity task) {
        if (!boardRepository.existsById(boardId)) {
            throw new WrongBoardException("Board not found");
        }
        BoardEntity board = boardRepository.findByBoardId(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
        if (!board.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this board.");
        }
        task.setTkBoard(board);
        String newTaskStatus = task.getStatus();
        if (isNumeric(newTaskStatus)) {
            newTaskStatus = statusRepository.findById(Integer.valueOf(newTaskStatus)).orElseThrow(() -> new ItemNotFoundException(task.getStatus() + "does not exist'")).getName();
            task.setStatus(newTaskStatus);
        }
        statusService.validateStatusLimitToAddEdit(boardId, task.getStatus());
        try {
            return repository.save(task);
        } catch (Exception e) {
            throw new ItemNotFoundException("Database Exception");
        }
    }

    @Transactional
    public TaskEntity editTaskForBoard(String userId, String boardId, int taskId, TaskEntity newTask) {
        if (!repository.existsByIdAndTkBoard(taskId, boardRepository.findByBoardId(boardId).orElseThrow(() -> new WrongBoardException("Board not found")))) {
            throw new WrongBoardException("Board not found");
        }
        BoardEntity board = boardRepository.findByBoardId(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));

        if (!board.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this board.");
        }

        TaskEntity existingTask = repository.findByTkBoardAndId(board, taskId).orElseThrow(() -> new ItemNotFoundException("Task ID " + taskId + " not found in this board!!"));

        String newTaskStatus = newTask.getStatus();

        if (isNumeric(newTaskStatus)) {
            newTaskStatus = statusRepository.findById(Integer.valueOf(newTaskStatus))
                    .orElseThrow(() -> new ItemNotFoundException("Status: " + newTask.getStatus() + " does not exist"))
                    .getName();
        }
        if (newTask.getTitle() == null || newTask.getTitle().isEmpty()) {
            throw new ItemNotFoundException("Title must not be empty");
        } else {
            existingTask.setTitle(newTask.getTitle());
            existingTask.setDescription(newTask.getDescription());
            existingTask.setAssignees(newTask.getAssignees());
            existingTask.setStatus(newTaskStatus);
            if (!statusRepository.existsByName(existingTask.getStatus())) {
                throw new ItemNotFoundException("Task status does not exist");
            }
            statusService.validateStatusLimitToAddEdit(boardId, existingTask.getStatus());

            return repository.save(existingTask);
        }
    }

    @Transactional
    public SimpleTaskDTO deleteTaskByBoardIdAndTaskId(String userId, String boardId, int taskId) {
        checkAccessRight(boardId);
        if (!repository.existsByIdAndTkBoard(taskId, boardRepository.findByBoardId(boardId).orElseThrow(() -> new WrongBoardException("Board not found")))) {
            throw new WrongBoardException("Board not found");
        }
        BoardEntity board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
        if (!board.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have access this board.");
        }

        TaskEntity task = repository.findByTkBoard_BoardIdAndId(boardId, taskId).orElseThrow(() -> new ItemNotFoundException("Task ID " + taskId + " not found in this board!!"));
        SimpleTaskDTO simpleTaskDTO = modelMapper.map(task, SimpleTaskDTO.class);
        repository.delete(task);
        return simpleTaskDTO;
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Old TaskService Without Personal Board

//    public List<TaskEntity> findAll() {
//        return repository.findAll();
//    }
//
//    public TaskEntity findById(int id) {
//        return repository.findById(id).orElseThrow(() -> new TaskIdNotFound("Task ID "+ id +" does not exist !!!"){
//        });
//    }
//
//    @Transactional
//    public TaskEntity addTask(TaskEntity task) {
//        String newTaskStatus = task.getStatus();
//        if(isNumeric(newTaskStatus)){
//            newTaskStatus = statusRepository.findById(Integer.valueOf(newTaskStatus)).orElseThrow(() -> new ItemNotFoundException(task.getStatus() + "does not exist'")).getName();
//            task.setStatus(newTaskStatus);
//        }
//        //        if (task.getTitle() == null || task.getTitle().isEmpty()) {
////            throw new BadRequestException("Task title is null !!!");
////        }
////        if (task.getTitle().length() > 100) {
////            throw new BadRequestException("Task title length should be less than 100 !!!");
////        }
////        if (task.getDescription() != null && task.getDescription().length() > 500) {
////            throw new BadRequestException("Task description length should be less than 500 !!!");
////        }
////        if (task.getAssignees() != null && task.getAssignees().length() > 30) {
////            throw new BadRequestException("Task assignees length should be less than 30 !!!");
////        }
////        if (!statusRepository.existsByName(task.getStatus()) ){
////            throw new ItemNotFoundException("Task status not exist !!!");
////        }
//        statusService.validateStatusLimitToAddEdit(task.getStatus());
//        try {
//            return repository.save(task);
//        } catch (Exception e) {
//            throw new ItemNotFoundException("Database Exception");
//        }
//    }
//
//    @Transactional
//    public SimpleTaskDTO deleteTask(int id) {
//        TaskEntity task = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
//        SimpleTaskDTO simpleTaskDTO = modelMapper.map(task, SimpleTaskDTO.class);
//        repository.delete(task);
//        return simpleTaskDTO;
//    }
//
//    @Transactional
//    public TaskEntity editTask(int id, TaskEntity newTask) {
//        String newTaskStatus = newTask.getStatus();
//        if(isNumeric(newTaskStatus)){
//            newTaskStatus = statusRepository.findById(Integer.valueOf(newTaskStatus)).orElseThrow( () -> new ItemNotFoundException("status : " + newTask.getStatus() + "does not exist")).getName();
//        }
////        try {
////            double d = Double.parseDouble(newTaskStatus);
////        } catch (NumberFormatException nfe) {
//////            return false;
////        } finally {
////            newTaskStatus = statusRepository.findById(Integer.valueOf(newTaskStatus)).orElseThrow().getName();
////        }
//        TaskEntity task = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
//        if (newTask.getTitle() == null || newTask.getTitle().isEmpty()) {
//            throw new ItemNotFoundException("NOT FOUND");
//        } else {
//            newTask.setId(id);
//            task.setTitle(newTask.getTitle());
//            task.setDescription(newTask.getDescription());
//            task.setAssignees(newTask.getAssignees());
//            task.setStatus(newTaskStatus);
//            if (!statusRepository.existsByName(task.getStatus())){
////                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,"Task status not exist !!!");
//                throw new ItemNotFoundException("Task status not exist !!!");
//            }
//            statusService.validateStatusLimitToAddEdit(task.getStatus());
//            return repository.save(newTask);
//        }
//    }

    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList("status.id", "status.name", "id", "title", "assignees");

    public List<TaskEntity> findTasks(String sortBy, List<String> filterStatuses) {
        if (sortBy != null && !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Invalid sort field: " + sortBy);
        }
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "id"; // Default sort by id if sortBy is not provided
        }
        return repository.findTasksWithSortingAndFiltering(sortBy, filterStatuses);
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public void checkAccessRight (String boardId) {
        CollabEntity collab = collabRepository.findByBoardIdAndUserId(boardId, JwtUserDetailsService.getCurrentUser().getOid()).orElseThrow(() -> new ForbiddenException("You do not have access this board."));
        if (collab.getAccessRight().equals(CollabEntity.AccessRight.READ)) {
            throw new ForbiddenException("You do not have permission to perform this action.");
        }
    }
}
