package com.example.kradankanban_backend.common.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddEditTaskDTO {
    private String title;
    private String description;
    private String assignees;
    private String status;
}
