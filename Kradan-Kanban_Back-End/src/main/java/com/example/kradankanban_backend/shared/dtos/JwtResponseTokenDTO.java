package com.example.kradankanban_backend.shared.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseTokenDTO {
    private String access_token;
}
