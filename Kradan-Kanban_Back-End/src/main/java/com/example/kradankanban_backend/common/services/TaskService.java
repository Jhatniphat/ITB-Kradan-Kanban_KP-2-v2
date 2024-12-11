package com.example.kradankanban_backend.common.services;

import com.example.kradankanban_backend.common.dtos.DetailBoardDTO;
import com.example.kradankanban_backend.common.dtos.DetailTaskWithTimeOnDTO;
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
import java.util.stream.Collectors;

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

    public List<SimpleTaskDTO> findAllTasksByBoardId(String userId, String boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new WrongBoardException("Board not found");
        }
        BoardEntity board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
        List<TaskEntity> tasks = repository.findByTkBoard_BoardId(boardId);
        return tasks.stream().map(task -> {
            SimpleTaskDTO dto = modelMapper.map(task, SimpleTaskDTO.class);
            dto.setTotal_attachment(task.getAttachments().size());
            return dto;
        }).collect(Collectors.toList());
    }


    public DetailTaskWithTimeOnDTO findTaskByBoardIdAndTaskId(String boardId, int taskId) {
        if (!repository.existsByIdAndTkBoard(taskId, boardRepository.findByBoardId(boardId).orElseThrow(() -> new WrongBoardException("Board not found")))) {
            throw new WrongBoardException("Board not found");
        }
        BoardEntity board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
        TaskEntity tasks = repository.findByTkBoardAndId(board, taskId).orElseThrow(() -> new ItemNotFoundException("Task ID " + taskId + " not found in this board!!"));
        DetailTaskWithTimeOnDTO dto = modelMapper.map(tasks, DetailTaskWithTimeOnDTO.class);
        dto.setTotal_attachment(tasks.getAttachments().size());
        return dto;
    }

    public TaskEntity addTaskForBoard(String userId, String boardId, TaskEntity task) {
        if (!boardRepository.existsById(boardId)) {
            throw new WrongBoardException("Board not found");
        }
        BoardEntity board = boardRepository.findByBoardId(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
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
            throw new ItemNotFoundException("No Task Found");
        }
        BoardEntity board = boardRepository.findByBoardId(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
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
        if (!repository.existsByIdAndTkBoard(taskId, boardRepository.findByBoardId(boardId).orElseThrow(() -> new WrongBoardException("Board not found")))) {
            throw new WrongBoardException("Board not found");
        }
        BoardEntity board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));


        TaskEntity task = repository.findByTkBoard_BoardIdAndId(boardId, taskId).orElseThrow(() -> new ItemNotFoundException("Task ID " + taskId + " not found in this board!!"));
        SimpleTaskDTO simpleTaskDTO = modelMapper.map(task, SimpleTaskDTO.class);
        repository.delete(task);
        return simpleTaskDTO;
    }

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

}
