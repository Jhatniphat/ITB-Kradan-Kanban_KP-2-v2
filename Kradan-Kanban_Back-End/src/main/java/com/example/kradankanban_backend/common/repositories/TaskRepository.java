package com.example.kradankanban_backend.common.repositories;

import com.example.kradankanban_backend.common.entities.BoardEntity;
import com.example.kradankanban_backend.common.entities.TaskEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Integer> {

    @Transactional
    @Query("SELECT t FROM TaskEntity t LEFT JOIN StatusEntity s ON t.status = s.name WHERE (:filterStatuses IS NULL OR s.name IN :filterStatuses) ORDER BY " +
            "CASE WHEN :sortBy = 'status.name' THEN s.name " +
            "WHEN :sortBy = 'status.id' THEN s.id " +
            "WHEN :sortBy = 'id' THEN t.id " +
            "WHEN :sortBy = 'title' THEN t.title " +
            "WHEN :sortBy = 'assignees' THEN t.assignees " +
            "ELSE t.id END")
    List<TaskEntity> findTasksWithSortingAndFiltering(@Param("sortBy") String sortBy, @Param("filterStatuses") List<String> filterStatuses);


    @Transactional
    @Modifying
    @Query(value = "update TaskEntity set status = :newStatus where status like :oldStatus")
    void updateTaskStatus(@Param("oldStatus") String oldStatus,@Param("newStatus") String newStatus);

    boolean existsByStatus(String status);

    long countByStatus(String status);

    List<TaskEntity> findByTkBoard_BoardId(String boardId);

    Optional<TaskEntity> findByTkBoardAndId(BoardEntity board, int id);

    Optional<TaskEntity> findByTkBoard_BoardIdAndId(String boardId, int taskId);

    boolean existsByIdAndTkBoard(int taskId , BoardEntity board);

    void deleteAllByTkBoard_BoardId(String boardId);
}
