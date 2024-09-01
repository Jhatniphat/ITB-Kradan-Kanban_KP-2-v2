package com.example.kradankanban_backend.common.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "limitSettings")
public class LimitSettings {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "limit")
    private Integer limit;

    @Column(name = "isEnable")
    private Boolean isEnable;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ls_boardId", nullable = false)
    private BoardEntity lsBoard;

}
