package com.example.kradankanban_backend.common.entities;

import com.example.kradankanban_backend.common.dtos.DetailBoardDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "board", schema = "intergrate-kp-2")
public class BoardEntity {
    @Id
    @Size(max = 10)
    @Column(name = "boardId", nullable = false, length = 10)
    private String boardId;

    @Size(max = 120)
    @NotNull
    @Column(name = "boardName", nullable = false, length = 120)
    private String boardName;

    @Size(max = 36)
    @Column(name = "userId", length = 36)
    private String userId;
}