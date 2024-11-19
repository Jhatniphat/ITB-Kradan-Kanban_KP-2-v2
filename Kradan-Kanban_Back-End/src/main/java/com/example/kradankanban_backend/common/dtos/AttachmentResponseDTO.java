package com.example.kradankanban_backend.common.dtos;

import com.example.kradankanban_backend.exceptions.ErrorResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class AttachmentResponseDTO {
    private String message;
    private List<AttachmentDTO> successUpload;
    private List<ErrorResponse.ValidationError> errors;
}
