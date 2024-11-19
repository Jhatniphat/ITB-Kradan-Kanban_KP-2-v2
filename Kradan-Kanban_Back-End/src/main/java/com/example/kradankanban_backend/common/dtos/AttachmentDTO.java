package com.example.kradankanban_backend.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class AttachmentDTO {
    private Integer Id;
    private String fileName;
    private String fileType;
    private byte[] fileData;
    private OffsetDateTime uploadedOn;
}
