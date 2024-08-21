package com.example.kradankanban_backend.shared.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JwtRequestUser {
    @Size(max = 50)
    @NotBlank
    private String userName;
    @Size(max = 14)
    @NotBlank
    private String password;
}