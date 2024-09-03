package com.example.kradankanban_backend.common.repositories;

import com.example.kradankanban_backend.common.entities.LimitSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LimitRepository extends JpaRepository<LimitSettings, String> {

}
