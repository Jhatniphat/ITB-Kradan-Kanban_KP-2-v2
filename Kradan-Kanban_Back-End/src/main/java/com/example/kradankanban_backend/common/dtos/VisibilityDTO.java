package com.example.kradankanban_backend.common.dtos;


import com.example.kradankanban_backend.common.entities.BoardEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class VisibilityDTO {
    @NotNull
    @NotBlank
    private BoardEntity.Visibility visibility;
}
