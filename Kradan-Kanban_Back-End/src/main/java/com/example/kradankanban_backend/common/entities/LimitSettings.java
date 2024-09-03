package com.example.kradankanban_backend.common.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Data
@Table(name = "limitSettings")
public class LimitSettings {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "limit")
    private Integer limit;

    @Column(name = "isEnable", columnDefinition = "TINYINT", length = 1, nullable = false)
    private Boolean isEnable;

    @NotNull
    @Column(name = "ls_boardId")
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "ls_boardId", nullable = false)
    private String lsBoard;



}
