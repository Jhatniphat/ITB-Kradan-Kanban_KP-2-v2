package com.example.kradankanban_backend.common.dtos;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DetailTaskWithTimeOnDTO {
    private int id;
    private String title;
    private String description;
    private String assignees;
    private Object status;
    private String createdOn;
    private String updatedOn;
}
