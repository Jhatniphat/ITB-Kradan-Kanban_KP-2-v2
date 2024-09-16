package com.example.kradankanban_backend.common.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardNameDTO {
    @NotNull
    @NotBlank
    @Size(max = 120)
    private String name;
}