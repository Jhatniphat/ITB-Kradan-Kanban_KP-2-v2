package com.example.kradankanban_backend.common.services;

import com.example.kradankanban_backend.common.dtos.CollabRequestDTO;
import com.example.kradankanban_backend.common.entities.BoardEntity;
import com.example.kradankanban_backend.common.entities.CollabEntity;
import com.example.kradankanban_backend.common.entities.CollabId;
import com.example.kradankanban_backend.common.repositories.BoardRepository;
import com.example.kradankanban_backend.common.repositories.CollabRepository;
import com.example.kradankanban_backend.exceptions.AuthenticationFailedException;
import com.example.kradankanban_backend.exceptions.BadRequestException;
import com.example.kradankanban_backend.exceptions.ItemNotFoundException;
import com.example.kradankanban_backend.shared.Entities.UserEntity;
import com.example.kradankanban_backend.shared.UserRepository;
import com.example.kradankanban_backend.shared.services.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        if (board.getUserId().equals(user.getOid())) {
            throw new BadRequestException("Board owner cannot be added as a collaborator");
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
        collab.setAccessRight(newCollabEntity.getAccessRight());
        collabRepository.save(collab);
        return collab;
    }

    public CollabEntity deleteCollaborator(String boardId, String userId) {
        BoardEntity board = boardRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("No Board found"));
        CollabEntity collab = collabRepository.findByBoardIdAndUserId(boardId, userId).orElseThrow(() -> new ItemNotFoundException("No Collaborator found"));
        UserEntity user = userRepository.findByUsername(JwtUserDetailsService.getCurrentUser().getUsername());
        if (user.getOid().equals(userId)) {
            throw new BadRequestException("No Permission");
        }
        collabRepository.delete(collab);
        return collab;
    }
}
