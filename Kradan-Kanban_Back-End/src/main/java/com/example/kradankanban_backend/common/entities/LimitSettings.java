package com.example.kradankanban_backend.common.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "limitSettings", schema = "intergrate-kp-2")
public class LimitSettings {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;

    //    @Id
    @NotNull
    @Column(name = "ls_boardId")
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "ls_boardId", nullable = false)
    private String lsBoard;


    @Column(name = "`limit`")
    private Integer limit;

    @Column(name = "isEnable", columnDefinition = "TINYINT", length = 1, nullable = false)
    private Boolean isEnable;
}
