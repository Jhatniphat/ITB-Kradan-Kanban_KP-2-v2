package com.example.kradankanban_backend.common.repositories;

import com.example.kradankanban_backend.common.entities.LimitSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LimitRepository extends JpaRepository<LimitSettings, Integer> {

}
