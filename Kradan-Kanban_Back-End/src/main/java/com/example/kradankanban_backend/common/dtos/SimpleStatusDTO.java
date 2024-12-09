package com.example.kradankanban_backend.common.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class SimpleStatusDTO {
    private int id;
    private String name;
    private String description;
    private String color;
}
