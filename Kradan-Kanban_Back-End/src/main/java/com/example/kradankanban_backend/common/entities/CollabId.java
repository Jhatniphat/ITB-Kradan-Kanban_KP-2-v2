package com.example.kradankanban_backend.common.entities;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CollabId implements Serializable {
    private String boardId;
    private String userId;
}