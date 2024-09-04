package com.example.kradankanban_backend.common.repositories;

import com.example.kradankanban_backend.common.entities.StatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<StatusEntity, Integer> {
    boolean existsByName(String name);

    @Query("SELECT ls.isEnable FROM LimitSettings ls WHERE ls.lsBoard = :boardId")
    Boolean findIsEnable(String boardId);

    @Modifying
    @Transactional
    @Query("UPDATE LimitSettings ls SET ls.isEnable = ?1")
    void updateIsEnable(Boolean isEnable);

    @Query("SELECT ls.limit FROM LimitSettings ls ")
    int findLimit();

    List<StatusEntity> findAllByStBoard(String boardId);

    Optional<StatusEntity> findByStBoardAndId(String boardId, int id);
}
