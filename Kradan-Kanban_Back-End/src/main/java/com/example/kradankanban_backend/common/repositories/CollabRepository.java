package com.example.kradankanban_backend.common.repositories;

import com.example.kradankanban_backend.common.entities.CollabEntity;
import com.example.kradankanban_backend.common.entities.CollabId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollabRepository extends JpaRepository<CollabEntity, CollabId> {
    List<CollabEntity> findByBoardId(String boardId);
    Optional<CollabEntity> findByBoardIdAndUserId(String boardId, String userId);
    void deleteAllByBoardId(String boardId);
}
