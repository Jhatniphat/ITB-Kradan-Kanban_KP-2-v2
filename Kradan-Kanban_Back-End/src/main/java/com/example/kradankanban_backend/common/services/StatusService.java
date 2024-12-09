package com.example.kradankanban_backend.common.services;

import com.example.kradankanban_backend.common.entities.BoardEntity;
import com.example.kradankanban_backend.common.entities.CollabEntity;
import com.example.kradankanban_backend.common.entities.StatusEntity;
import com.example.kradankanban_backend.common.repositories.BoardRepository;
import com.example.kradankanban_backend.common.repositories.CollabRepository;
import com.example.kradankanban_backend.common.repositories.StatusRepository;
import com.example.kradankanban_backend.common.repositories.TaskRepository;
import com.example.kradankanban_backend.exceptions.BadRequestException;
import com.example.kradankanban_backend.exceptions.ForbiddenException;
import com.example.kradankanban_backend.exceptions.ItemNotFoundException;
import com.example.kradankanban_backend.exceptions.WrongBoardException;
import com.example.kradankanban_backend.shared.services.JwtUserDetailsService;
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

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    CollabRepository collabRepository;

    public List<StatusEntity> getAll(String boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new WrongBoardException("No board found with id: " + boardId);
        }
        return repository.findAllByStBoard(boardId);
    }

    public StatusEntity getById(String boardId,int statusId) {
        if (!boardRepository.existsById(boardId)) {
            throw new WrongBoardException("No board found with id: " + boardId);
        }
        return repository.findByStBoardAndId(boardId, statusId).orElseThrow( () -> new ItemNotFoundException("No status found with id: " + statusId) );
    }

    // * addStatus
    @Transactional
    public StatusEntity addStatus(String boardId, StatusEntity status) {
        if (!boardRepository.existsById(boardId)) {
            throw new WrongBoardException("No board found with id: " + boardId);
        }
        try {
            status.setStBoard(boardId);
            return repository.save(status);
        } catch (Exception e) {
            throw new ItemNotFoundException("Database Exception");
        }
    }

    // * editStatus
    @Transactional
    public StatusEntity editStatus(String boardId ,int id, StatusEntity status) {
        StatusEntity oldStatus = repository.findById(id).orElseThrow( () -> new ItemNotFoundException("No status found with id: " + id) );
        if (!boardRepository.existsById(boardId) || !repository.existsByIdAndStBoard(id, boardId)) {
            throw new WrongBoardException("No board found with id: " + boardId);
        }
        if (oldStatus.getName().equals("No Status") || oldStatus.getName().equals("Done")){
            throw new BadRequestException( "'" + oldStatus.getName() +"'"+ " cannot be edited !!!");
        }
        if (!oldStatus.getName().equals(status.getName()) && repository.existsByName(status.getName())) {
            throw new BadRequestException("Name must be unique");
        }
        try {
            status.setStBoard(boardId);
            status.setId(id);
            return repository.save(status);
        } catch (Exception e) {
            throw new BadRequestException("Database Exception");
        }
    }

    // * deleteStatus
    public StatusEntity deleteStatus(String userId, String boardId ,int id) {
//        checkAccessRight(boardId);
        if (!boardRepository.existsById(boardId) || !repository.existsByIdAndStBoard(id, boardId)) {
            throw new WrongBoardException("No board found with id: " + boardId);
        }
        StatusEntity status = repository.findById(id).orElseThrow( () -> new ItemNotFoundException("No status found with id: " + id) );
        if (status.getName().equals("No Status") || status.getName().equals("Done")){
            throw new BadRequestException( "'" + status.getName() +"'"+ " cannot be delete !!!");
        }
        if (taskRepository.existsByStatus(status.getName())) {
            throw new BadRequestException("Have Task On This Status");
        }
        BoardEntity board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
        if (status.getName().equals("No Status")) {
            throw new BadRequestException("Cannot delete 'No Status'!!!");
        }
        try {
            status.setStBoard(boardId);
            repository.delete(status);
            return status;
        }catch (Exception e){
            throw new ItemNotFoundException(e.toString());
        }
    }

    // * transferStatus
    public StatusEntity transferStatus (String boardId, int oldId, int newId){
        if (oldId == newId) {
            throw new BadRequestException("destination status for task transfer must be different from current status");
        }
        if (!repository.existsById(oldId)) {
            throw new ItemNotFoundException("No status found with id: " + oldId);
        }
        if (!repository.existsById(newId)) {
            throw new ItemNotFoundException("the specified status for task transfer does not exist");
        }
        if (!boardRepository.existsById(boardId) || !repository.existsByIdAndStBoard(oldId, boardId) || !repository.existsByIdAndStBoard(newId, boardId)) {
            throw new WrongBoardException("No board found with id: " + boardId);
        }
        StatusEntity oldStatus = repository.findByStBoardAndId(boardId, oldId).orElseThrow();
        String newgetName = repository.findByStBoardAndId(boardId, newId).orElseThrow().getName();
        validateStatusLimitToDeleteTransfer(boardId, newgetName);
        try {

            taskRepository.updateTaskStatus(oldStatus.getName() , newgetName);
            repository.delete(oldStatus);

            return oldStatus;
        }catch (Exception message){
            throw new ItemNotFoundException(message.toString());
        }
    }
    @Transactional
    public void toggleIsEnable(String boardId) {
        Boolean currentIsEnable = repository.findIsEnable(boardId);
        repository.updateIsEnable(!currentIsEnable);
    }

    public Object getLimitData(String boardId) {
        return repository.findIsEnable(boardId);
    }

    public void validateStatusLimitToDeleteTransfer(String boardId, String statusName) {
        if (repository.findIsEnable(boardId)) {
            long count = taskRepository.countByStatus(statusName);
            if (count >= repository.findLimit()) {
                throw new BadRequestException("the destination status cannot be over the limit after transfer");
            }
        }
    }

    public void validateStatusLimitToAddEdit(String boardId, String statusName) {
        if (repository.findIsEnable(boardId)) {
            long count = taskRepository.countByStatus(statusName);
            if (count >= repository.findLimit()) {
                throw new BadRequestException("the status has reached the limit");
            }
        }
    }
}
