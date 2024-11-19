package com.example.kradankanban_backend.common.services;


import com.example.kradankanban_backend.common.dtos.AttachmentDTO;
import com.example.kradankanban_backend.common.dtos.AttachmentResponseDTO;
import com.example.kradankanban_backend.common.entities.AttachmentEntity;
import com.example.kradankanban_backend.common.entities.TaskEntity;
import com.example.kradankanban_backend.common.repositories.AttachmentRepository;
import com.example.kradankanban_backend.common.repositories.TaskRepository;
import com.example.kradankanban_backend.exceptions.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service

public class FileService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    public List<AttachmentDTO> getAllAttachments(Integer taskId) {
        TaskEntity task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("No Task found"));
        return task.getAttachments().stream()
                .map(entity -> new AttachmentDTO(
                        entity.getId(), // Assuming attachmentId is convertible to int
                        entity.getFileName(),
                        entity.getFileType(),
                        entity.getFileData(),
                        entity.getUploadedOn().atOffset(ZoneOffset.UTC) // Convert LocalDateTime to OffsetDateTime
                ))
                .toList();
    }

    public List<AttachmentResponseDTO> uploadAttachments(Integer taskId, List<MultipartFile> files) {
        // Response structure
        AttachmentResponseDTO responseDTO = new AttachmentResponseDTO();
        List<AttachmentDTO> successfulUploads = new ArrayList<>();
        List<ErrorResponse.ValidationError> errors = new ArrayList<>();

        // Retrieve TaskEntity
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found for ID: " + taskId));

        // Validate total attachments for the task
        if (task.getAttachments().size() + files.size() > 10) {
            errors.add(new ErrorResponse.ValidationError(
                    "attachments",
                    "Task cannot have more than 10 attachments."
            ));
            responseDTO.setMessage("Validation failed.");
            responseDTO.setErrors(errors);
            return List.of(responseDTO); // Early return for task-level validation failure
        }

        for (MultipartFile file : files) {
            try {
                // Validate file size (20 MB limit)
                if (file.getSize() > 20 * 1024 * 1024) {
                    errors.add(new ErrorResponse.ValidationError(
                            file.getOriginalFilename(),
                            "File size exceeds the 20 MB limit."
                    ));
                    continue;
                }

                // Check for duplicate files in the task's attachments
                boolean isDuplicate = task.getAttachments().stream()
                        .anyMatch(existingAttachment -> existingAttachment.getFileName().equals(file.getOriginalFilename()));
                if (isDuplicate) {
                    errors.add(new ErrorResponse.ValidationError(
                            file.getOriginalFilename(),
                            "File with the same name already exists in the task."
                    ));
                    continue;
                }

                // Save attachment to database
                AttachmentEntity entity = new AttachmentEntity();
                entity.setFileName(file.getOriginalFilename());
                entity.setFileType(file.getContentType());
                entity.setFileData(file.getBytes());
                entity.setUploadedOn(LocalDateTime.now());
                entity.setTask(task);

                attachmentRepository.save(entity);

                // Convert to DTO for response
                successfulUploads.add(new AttachmentDTO(
                        entity.getId(), // Assuming attachmentId can be converted
                        entity.getFileName(),
                        entity.getFileType(),
                        entity.getFileData(),
                        entity.getUploadedOn().atOffset(ZoneOffset.UTC)
                ));

            } catch (Exception ex) {
                // Log error and add it to the response
                errors.add(new ErrorResponse.ValidationError(file.getOriginalFilename(), ex.getMessage()));
            }
        }

        // Set response
        responseDTO.setMessage("File upload completed with results.");
        responseDTO.setSuccessUpload(successfulUploads);
        responseDTO.setErrors(errors);

        return List.of(responseDTO);
    }

    public AttachmentDTO deleteAttachment(Integer attachmentId) {
        AttachmentEntity entity = attachmentRepository.findById(String.valueOf(attachmentId))
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found for ID: " + attachmentId));

        AttachmentDTO dto = new AttachmentDTO(
                entity.getId(), // Assuming attachmentId can be converted
                entity.getFileName(),
                entity.getFileType(),
                entity.getFileData(),
                entity.getUploadedOn().atOffset(ZoneOffset.UTC)
        );

        attachmentRepository.delete(entity);
        return dto;
    }
}
