package com.example.kradankanban_backend.repositories;

import com.example.kradankanban_backend.entities.LimitSettings;
import com.example.kradankanban_backend.entities.StatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StatusRepository extends JpaRepository<StatusEntity, Integer> {
    boolean existsByName(String name);

    @Query("SELECT ls.isEnable FROM LimitSettings ls ")
    Boolean findIsEnable();

    @Modifying
    @Transactional
    @Query("UPDATE LimitSettings ls SET ls.isEnable = ?1")
    void updateIsEnable(Boolean isEnable);

    @Query("SELECT ls.limit FROM LimitSettings ls ")
    int findLimit();
}
