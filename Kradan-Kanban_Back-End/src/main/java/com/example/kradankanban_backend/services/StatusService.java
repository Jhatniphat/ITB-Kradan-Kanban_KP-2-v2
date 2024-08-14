package com.example.kradankanban_backend.services;

import com.example.kradankanban_backend.entities.LimitSettings;
import com.example.kradankanban_backend.entities.StatusEntity;
import com.example.kradankanban_backend.exceptions.BadRequestException;
import com.example.kradankanban_backend.exceptions.ItemNotFoundException;
import com.example.kradankanban_backend.repositories.StatusRepository;
import com.example.kradankanban_backend.repositories.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusService {
    @Autowired
    StatusRepository repository;

    @Autowired
    TaskRepository taskRepository;

    public List<StatusEntity> getAll() {
        return repository.findAll();
    }

    public StatusEntity getById(int id) {
        return repository.findById(id).orElseThrow( () -> new ItemNotFoundException("No status found with id: " + id) );
    }

    // * addStatus
    @Transactional
    public StatusEntity addStatus(StatusEntity status) {
//        if (status.getName().trim().isEmpty() || status.getName() == null) {
//            throw new BadRequestException( "Status Name is null !!!");
//        }
//        if (status.getName().trim().length() > 50) {
//            throw new BadRequestException("Status Name length should be less than 20 !!!");
//        }
//        if (status.getDescription() != null && status.getDescription().trim().length() > 200) {
//            throw new BadRequestException("Status Description length should be less than 200 !!!");
//        }
//        // ? เช็คว่ามี Name นั้นหรือยัง
//        if (repository.existsByName(status.getName())) {
//            throw new BadRequestException("Status Name already exists !!!");
//        }
        try {
            return repository.save(status);
        } catch (Exception e) {
            throw new ItemNotFoundException("Database Exception");
        }
    }

    // * editStatus
    @Transactional
    public StatusEntity editStatus(int id, StatusEntity status) {
        StatusEntity oldStatus = repository.findById(id).orElseThrow( () -> new ItemNotFoundException("No status found with id: " + id) );
        if (oldStatus.getName().equals("No Status") || oldStatus.getName().equals("Done")){
            throw new BadRequestException( "'" + oldStatus.getName() +"'"+ " cannot be edited !!!");
        }
        if (!oldStatus.getName().equals(status.getName()) && repository.existsByName(status.getName())) {
            throw new BadRequestException("Name must be unique");
        }
//        if (status.getName().equals("Done")) {
//            throw new BadRequestException("The status name 'Done' will not changed");
//        }
//        if (status.getName().trim().isEmpty() || status.getName() == null) {
//            throw new BadRequestException("Status Name is null !!!");
//        }
//        if (status.getName().trim().length() > 50) {
//            throw new BadRequestException("Status Name length should be less than 20 !!!");
//        }
//        if (status.getDescription() != null && status.getDescription().trim().length() > 200) {
//            throw new BadRequestException("Status Description length should be less than 200 !!!");
//        }
        try {
            status.setId(id);
            return repository.save(status);
        } catch (Exception e) {
            throw new BadRequestException("Database Exception");
        }
    }

    // * deleteStatus
    public StatusEntity deleteStatus(int id) {
        StatusEntity status = repository.findById(id).orElseThrow( () -> new ItemNotFoundException("No status found with id: " + id) );
        if (status.getName().equals("No Status") || status.getName().equals("Done")){
            throw new BadRequestException( "'" + status.getName() +"'"+ " cannot be delete !!!");
        }
        if (taskRepository.existsByStatus(status.getName())) {
            throw new BadRequestException("Have Task On This Status");
        }
        if (status.getName().equals("No Status")) {
            throw new BadRequestException("Cannot delete 'No Status'!!!");
        }
        try {
            repository.delete(status);
            return status;
        }catch (Exception e){
            throw new ItemNotFoundException(e.toString());
        }
    }

    // * transferStatus
    public StatusEntity transferStatus (int oldId, int newId){
        if (oldId == newId) {
            throw new BadRequestException("destination status for task transfer must be different from current status");
        }
        if (!repository.existsById(oldId)) {
            throw new ItemNotFoundException("No status found with id: " + oldId);
        }
        if (!repository.existsById(newId)) {
            throw new ItemNotFoundException("the specified status for task transfer does not exist");
        }
        StatusEntity oldStatus = repository.findById(oldId).orElseThrow();
        String newgetName = repository.findById(newId).orElseThrow().getName();
        validateStatusLimitToDeleteTransfer(newgetName);
        try {
            taskRepository.updateTaskStatus(oldStatus.getName() , newgetName);
            repository.delete(oldStatus);

            return oldStatus;
        }catch (Exception message){
            throw new ItemNotFoundException(message.toString());
        }
    }
    @Transactional
    public void toggleIsEnable() {
        Boolean currentIsEnable = repository.findIsEnable();
        repository.updateIsEnable(!currentIsEnable);
    }

    public Object getLimitData(){
        return repository.findIsEnable();
    }

    public void validateStatusLimitToDeleteTransfer(String statusName) {
        if (repository.findIsEnable()) {
            long count = taskRepository.countByStatus(statusName);
            if (count >= repository.findLimit()) {
                throw new BadRequestException("the destination status cannot be over the limit after transfer");
            }
        }
    }

    public void validateStatusLimitToAddEdit(String statusName) {
        if (repository.findIsEnable()) {
            long count = taskRepository.countByStatus(statusName);
            if (count >= repository.findLimit()) {
                throw new BadRequestException("the status has reached the limit");
            }
        }
    }
}
