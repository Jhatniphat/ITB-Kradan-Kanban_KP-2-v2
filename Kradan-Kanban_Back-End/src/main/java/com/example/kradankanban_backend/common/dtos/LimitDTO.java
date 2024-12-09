package com.example.kradankanban_backend.common.dtos;


import lombok.*;

@Getter
@Setter
@Data
public class LimitDTO {
    private Integer limit;
    private Boolean toggleEnable;
}
