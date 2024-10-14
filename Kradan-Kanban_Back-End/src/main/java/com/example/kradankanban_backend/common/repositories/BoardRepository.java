package com.example.kradankanban_backend.common.repositories;

import com.example.kradankanban_backend.common.dtos.CollabDTO;
import com.example.kradankanban_backend.common.entities.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity , String> {
    BoardEntity findByUserId(String userId);

    Optional<BoardEntity> findByBoardId(String boardId);
    List<BoardEntity> findAllByUserIdAndVisibility(String userId, BoardEntity.Visibility visibility);
    List<BoardEntity> findAllByVisibility(BoardEntity.Visibility visibility);

    List<BoardEntity> findAllByUserId(String userId);

    @Query("SELECT b FROM BoardEntity b JOIN CollabEntity c ON b.boardId = c.boardId WHERE c.userId = :userId")
    List<BoardEntity> findAllByCollaboratorUserId(@Param("userId") String userId);
}
