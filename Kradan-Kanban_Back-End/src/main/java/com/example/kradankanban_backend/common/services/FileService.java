package com.example.kradankanban_backend.common.services;


import com.example.kradankanban_backend.common.dtos.AttachmentDTO;
import com.example.kradankanban_backend.common.dtos.AttachmentResponseDTO;
import com.example.kradankanban_backend.common.entities.AttachmentEntity;
import com.example.kradankanban_backend.common.entities.TaskEntity;
import com.example.kradankanban_backend.common.properties.FileStorageProperties;
import com.example.kradankanban_backend.common.repositories.AttachmentRepository;
import com.example.kradankanban_backend.common.repositories.TaskRepository;
import com.example.kradankanban_backend.exceptions.ErrorResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Getter
public class FileService {

    private final Path fileStorageLocation;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    public FileService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            if (!Files.exists(this.fileStorageLocation)) {
                Files.createDirectories(this.fileStorageLocation);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String store(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("Invalid file path sequence: " + fileName);
            }
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName, ex);
        }
    }

    public void removeResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (IOException ex) {
            throw new RuntimeException("File operation error: " + fileName, ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File operation error: " + fileName, ex);
        }
    }

    public Resource downloadAttachment(Integer attachmentId) {
        AttachmentEntity entity = attachmentRepository.findById(String.valueOf(attachmentId))
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found for ID: " + attachmentId));

        return loadFileAsResource(entity.getFileName());
    }

    public List<AttachmentDTO> getAllAttachments(Integer taskId) {
        TaskEntity task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("No Task found"));
        return task.getAttachments().stream()
                .map(entity -> new AttachmentDTO(
                        entity.getId(),
                        entity.getFileName(),
                        entity.getFileType(),
                        entity.getFileData(),
                        entity.getUploadedOn().atOffset(ZoneOffset.UTC)
                ))
                .toList();
    }

    public List<AttachmentResponseDTO> uploadAttachments(Integer taskId, List<MultipartFile> files) {
        AttachmentResponseDTO responseDTO = new AttachmentResponseDTO();
        List<AttachmentDTO> successfulUploads = new ArrayList<>();
        List<ErrorResponse.ValidationError> errors = new ArrayList<>();
        List<AttachmentEntity> attachmentToSave = new ArrayList<>();

        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found for ID: " + taskId));

        if (task.getAttachments().size() + files.size() > 10) {
            errors.add(new ErrorResponse.ValidationError(
                    "attachments",
                    "Task cannot have more than 10 attachments."
            ));
            responseDTO.setMessage("Validation failed.");
            responseDTO.setErrors(errors);
            return List.of(responseDTO);
        }

        for (MultipartFile file : files) {
            try {
                if (file.getSize() > 20 * 1024 * 1024) {
                    errors.add(new ErrorResponse.ValidationError(
                            file.getOriginalFilename(),
                            "File size exceeds the 20 MB limit."
                    ));
                    continue;
                }

                boolean isDuplicate = task.getAttachments().stream()
                        .anyMatch(existingAttachment -> existingAttachment.getFileName().equals(file.getOriginalFilename()));
                if (isDuplicate) {
                    errors.add(new ErrorResponse.ValidationError(
                            file.getOriginalFilename(),
                            "File with the same name already exists in the task."
                    ));
                    continue;
                }

                String storedFileName = store(file);
                AttachmentEntity entity = new AttachmentEntity();
                entity.setFileName(storedFileName);
                entity.setFileType(file.getContentType());
                entity.setFileData(file.getBytes());
                entity.setUploadedOn(LocalDateTime.now());
                entity.setTask(task);
                attachmentToSave.add(entity);
                successfulUploads.add(new AttachmentDTO(
                        entity.getId(),
                        entity.getFileName(),
                        entity.getFileType(),
                        entity.getFileData(),
                        entity.getUploadedOn().atOffset(ZoneOffset.UTC)
                ));

            } catch (Exception ex) {
                errors.add(new ErrorResponse.ValidationError(file.getOriginalFilename(), ex.getMessage()));
            }
        }

        attachmentRepository.saveAll(attachmentToSave);
        successfulUploads = attachmentToSave.stream()
                .map(entity -> new AttachmentDTO(
                        entity.getId(),
                        entity.getFileName(),
                        entity.getFileType(),
                        entity.getFileData(),
                        entity.getUploadedOn().atOffset(ZoneOffset.UTC)
                ))
                .collect(Collectors.toList());
        responseDTO.setMessage("File upload completed with results.");
        responseDTO.setSuccessUpload(successfulUploads);
        responseDTO.setErrors(errors);

        return List.of(responseDTO);
    }

    public AttachmentDTO deleteAttachment(Integer attachmentId) {
        AttachmentEntity entity = attachmentRepository.findById(String.valueOf(attachmentId))
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found for ID: " + attachmentId));
        removeResource(entity.getFileName());
        AttachmentDTO dto = new AttachmentDTO(
                entity.getId(),
                entity.getFileName(),
                entity.getFileType(),
                entity.getFileData(),
                entity.getUploadedOn().atOffset(ZoneOffset.UTC)
        );
        attachmentRepository.delete(entity);
        return dto;
    }
}
