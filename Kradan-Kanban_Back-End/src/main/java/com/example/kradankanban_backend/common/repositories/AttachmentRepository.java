package com.example.kradankanban_backend.common.repositories;

import com.example.kradankanban_backend.common.entities.AttachmentEntity;
import com.example.kradankanban_backend.common.entities.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<AttachmentEntity, String> {
}
