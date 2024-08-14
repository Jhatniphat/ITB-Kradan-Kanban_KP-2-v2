package com.example.kradankanban_backend.services;

import com.example.kradankanban_backend.dtos.SimpleTaskDTO;
import com.example.kradankanban_backend.entities.TaskEntity;
import com.example.kradankanban_backend.exceptions.BadRequestException;
import com.example.kradankanban_backend.exceptions.ItemNotFoundException;
import com.example.kradankanban_backend.exceptions.TaskIdNotFound;
import com.example.kradankanban_backend.repositories.StatusRepository;
import com.example.kradankanban_backend.repositories.TaskRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository repository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private StatusService statusService;

    public List<TaskEntity> findAll() {
        return repository.findAll();
    }

    public TaskEntity findById(int id) {
        return repository.findById(id).orElseThrow(() -> new TaskIdNotFound("Task ID "+ id +" does not exist !!!"){
        });
    }

    @Transactional
    public TaskEntity addTask(TaskEntity task) {
        String newTaskStatus = task.getStatus();
        if(isNumeric(newTaskStatus)){
            newTaskStatus = statusRepository.findById(Integer.valueOf(newTaskStatus)).orElseThrow(() -> new ItemNotFoundException(task.getStatus() + "does not exist'")).getName();
            task.setStatus(newTaskStatus);
        }
        //        if (task.getTitle() == null || task.getTitle().isEmpty()) {
//            throw new BadRequestException("Task title is null !!!");
//        }
//        if (task.getTitle().length() > 100) {
//            throw new BadRequestException("Task title length should be less than 100 !!!");
//        }
//        if (task.getDescription() != null && task.getDescription().length() > 500) {
//            throw new BadRequestException("Task description length should be less than 500 !!!");
//        }
//        if (task.getAssignees() != null && task.getAssignees().length() > 30) {
//            throw new BadRequestException("Task assignees length should be less than 30 !!!");
//        }
//        if (!statusRepository.existsByName(task.getStatus()) ){
//            throw new ItemNotFoundException("Task status not exist !!!");
//        }
        statusService.validateStatusLimitToAddEdit(task.getStatus());
        try {
            return repository.save(task);
        } catch (Exception e) {
            throw new ItemNotFoundException("Database Exception");
        }
    }

    @Transactional
    public SimpleTaskDTO deleteTask(int id) {
        TaskEntity task = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
        SimpleTaskDTO simpleTaskDTO = modelMapper.map(task, SimpleTaskDTO.class);
        repository.delete(task);
        return simpleTaskDTO;
    }

    @Transactional
    public TaskEntity editTask(int id, TaskEntity newTask) {
        String newTaskStatus = newTask.getStatus();
        if(isNumeric(newTaskStatus)){
            newTaskStatus = statusRepository.findById(Integer.valueOf(newTaskStatus)).orElseThrow( () -> new ItemNotFoundException("status : " + newTask.getStatus() + "does not exist")).getName();
        }
//        try {
//            double d = Double.parseDouble(newTaskStatus);
//        } catch (NumberFormatException nfe) {
////            return false;
//        } finally {
//            newTaskStatus = statusRepository.findById(Integer.valueOf(newTaskStatus)).orElseThrow().getName();
//        }
        TaskEntity task = repository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
        if (newTask.getTitle() == null || newTask.getTitle().isEmpty()) {
            throw new ItemNotFoundException("NOT FOUND");
        } else {
            newTask.setId(id);
            task.setTitle(newTask.getTitle());
            task.setDescription(newTask.getDescription());
            task.setAssignees(newTask.getAssignees());
            task.setStatus(newTaskStatus);
            if (!statusRepository.existsByName(task.getStatus())){
//                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,"Task status not exist !!!");
                throw new ItemNotFoundException("Task status not exist !!!");
            }
            statusService.validateStatusLimitToAddEdit(task.getStatus());
            return repository.save(newTask);
        }
    }

    private static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList("status.id", "status.name", "id", "title","assignees");

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
