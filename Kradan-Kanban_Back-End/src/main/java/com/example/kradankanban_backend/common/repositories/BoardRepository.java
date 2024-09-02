package com.example.kradankanban_backend.common.repositories;

import com.example.kradankanban_backend.common.entities.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity , String> {
    BoardEntity findByUserId(String userId);
}
