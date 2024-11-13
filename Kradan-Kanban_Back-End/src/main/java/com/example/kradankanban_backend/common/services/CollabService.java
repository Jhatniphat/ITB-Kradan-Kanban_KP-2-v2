package com.example.kradankanban_backend.common.services;

import com.example.kradankanban_backend.common.dtos.CollabRequestDTO;
import com.example.kradankanban_backend.common.entities.BoardEntity;
import com.example.kradankanban_backend.common.entities.CollabEntity;
import com.example.kradankanban_backend.common.repositories.BoardRepository;
import com.example.kradankanban_backend.common.repositories.CollabRepository;
import com.example.kradankanban_backend.exceptions.BadRequestException;
import com.example.kradankanban_backend.exceptions.ConfilctException;
import com.example.kradankanban_backend.exceptions.ItemNotFoundException;
import com.example.kradankanban_backend.shared.Entities.UserEntity;
import com.example.kradankanban_backend.shared.UserRepository;
import com.example.kradankanban_backend.shared.services.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CollabService {

    @Autowired
    CollabRepository collabRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;

    public List<CollabEntity> getAllCollaborators(String boardId) {
        return collabRepository.findByBoardId(boardId);
    }

    public CollabEntity getCollaborators(String boardId, String userId) {
        return collabRepository.findByBoardIdAndUserId(boardId, userId).orElseThrow(() -> new ItemNotFoundException("No Collaborator found"));
    }

    public CollabEntity addCollaborator(String boardId, CollabRequestDTO collabRequestDTO) {
        BoardEntity board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("No Board found"));
        UserEntity user = userRepository.findByEmail(collabRequestDTO.getEmail()).orElseThrow(() -> new ItemNotFoundException("No Collaborator found"));
        if (collabRequestDTO.getAccessRight() == null) {
            throw new BadRequestException("Access right is required");
        }
        if (user.getOid().equals(JwtUserDetailsService.getCurrentUser().getOid())) {
            throw new ConfilctException("Board owner cannot be added as a collaborator");
        }
        if (collabRepository.findByBoardIdAndUserId(boardId, user.getOid()).isPresent()) {
            throw new ConfilctException("Collaborator already exists");
        }
        CollabEntity collabEntity = new CollabEntity();
        collabEntity.setBoardId(boardId);
        collabEntity.setUserId(user.getOid());
        collabEntity.setAccessRight(collabRequestDTO.getAccessRight());
        collabRepository.save(collabEntity);

        return collabEntity;
    }

    public CollabEntity updateAccessCollaborator(String boardId, String userId, CollabEntity newCollabEntity) {
        CollabEntity collab = collabRepository.findByBoardIdAndUserId(boardId, userId).orElseThrow(() -> new ItemNotFoundException("No Collaborator found"));
        if (newCollabEntity.getAccessRight() == null) {
            throw new BadRequestException("Access right is required");
        }
        collab.setAccessRight(newCollabEntity.getAccessRight());
        collabRepository.save(collab);
        return collab;
    }

    // New Service to Change Status of Collaborator

    public CollabEntity deleteCollaborator(String boardId, String userId) {
        BoardEntity board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("No Board found"));
        CollabEntity collab = collabRepository.findByBoardIdAndUserId(boardId, userId).orElseThrow(() -> new ItemNotFoundException("No Collaborator found"));
        UserEntity user = userRepository.findByUsername(JwtUserDetailsService.getCurrentUser().getUsername());
        collabRepository.delete(collab);
        return collab;
    }

    public boolean isCollaborator(String boardId, String userId) {
        Optional<CollabEntity> collab = collabRepository.findByBoardIdAndUserId(boardId, userId);
        return collab.isPresent();
    }

    public boolean isWriteAccess(String boardId, String userId) {
        return collabRepository.existsByBoardIdAndUserIdAndAccessRight(boardId, userId, CollabEntity.AccessRight.WRITE);
    }

//    public boolean isStatusAccepted(String boardId, String userId) {
//        return collabRepository.existsByBoardIdAndUserIdAndStatus(boardId, userId, CollabEntity.Status.ACCEPTED);
//    }

}
