package com.example.kradankanban_backend.common.dtos;


import com.example.kradankanban_backend.common.entities.BoardEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class VisibilityDTO {
    private BoardEntity.Visibility visibility;
}
