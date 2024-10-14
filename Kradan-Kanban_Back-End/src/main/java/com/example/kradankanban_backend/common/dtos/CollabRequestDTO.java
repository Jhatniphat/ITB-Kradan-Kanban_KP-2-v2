package com.example.kradankanban_backend.common.dtos;

import com.example.kradankanban_backend.common.entities.CollabEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CollabRequestDTO {
    @NotNull
    private String email;
    private CollabEntity.AccessRight accessRight;
}
