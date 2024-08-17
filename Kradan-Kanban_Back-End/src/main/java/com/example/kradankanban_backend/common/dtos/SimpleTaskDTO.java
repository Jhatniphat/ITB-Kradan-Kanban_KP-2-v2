package com.example.kradankanban_backend.common.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class SimpleTaskDTO {
    private int id;
    private String title;
    private String assignees;
    private Object status;
}
