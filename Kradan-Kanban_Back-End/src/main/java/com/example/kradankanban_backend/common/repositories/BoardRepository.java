package com.example.kradankanban_backend.common.repositories;

import com.example.kradankanban_backend.common.entities.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity , String> {
    BoardEntity findByUserId(String userId);

    Optional<BoardEntity> findByBoardId(String boardId);
    List<BoardEntity> findAllByUserIdAndVisibility(String userId, BoardEntity.Visibility visibility);
    List<BoardEntity> findAllByVisibility(BoardEntity.Visibility visibility);
}
