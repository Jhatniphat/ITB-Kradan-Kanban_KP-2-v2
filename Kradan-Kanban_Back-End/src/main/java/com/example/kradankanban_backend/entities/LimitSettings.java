package com.example.kradankanban_backend.entities;

import jakarta.persistence.*;
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

}
