package com.example.kradankanban_backend.dtos;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DetailTaskDTO {
    private int id;
    private String title;
    private String description;
    private String assignees;
    private Object status;
}
