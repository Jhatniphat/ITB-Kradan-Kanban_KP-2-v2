package com.example.kradankanban_backend.common.repositories;

import com.example.kradankanban_backend.common.entities.CollabEntity;
import com.example.kradankanban_backend.common.entities.CollabId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollabRepository extends JpaRepository<CollabEntity, CollabId> {
    List<CollabEntity> findByBoardId(String boardId);
    Optional<CollabEntity> findByBoardIdAndUserId(String boardId, String userId);
    boolean existsByBoardIdAndUserIdAndAccessRight(String boardId, String userId, CollabEntity.AccessRight accessRight);


    void deleteAllByBoardId(String boardId);
}
