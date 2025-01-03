package com.example.kradankanban_backend.common.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "status", schema = "intergrate-kp-2")
@AllArgsConstructor
@NoArgsConstructor
public class StatusEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @NotNull
    @NotBlank
    @Size(min = 0 ,max = 50, message = "size must be between 0 and 50")
    @Column(name = "statusName")
    private String name;

    @Size(min = 0 ,max = 200, message = "size must be between 0 and 200")
    @Column(name = "statusDescription")
    private String description;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss'Z'" , timezone = "UTC")
    @Column(name = "createdOn", insertable = false, updatable = false)
    private LocalDateTime createdOn;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss'Z'" , timezone = "UTC")
    @Column(name = "updatedOn", insertable = false, updatable = false)
    private LocalDateTime updatedOn;

    @Column(name = "color")
    private String color = "b2b2b2";

    @Column(name = "st_boardId" , nullable = false)
    private String stBoard;

    public StatusEntity(String noStatus, String description, String boardId) {
        this.name = noStatus;
        this.description = description;
        this.stBoard = boardId;
    }

    public void setName(String name) {
        if (name != null) {
            name = name.trim();
        }
        this.name = name;
    }

    public void setDescription(String description) {
        if (description == null || description.isBlank()) {
            this.description = null;
        } else this.description = description.trim();
    }

    public void setColor(String color) {
        if (color == null || color.isBlank()) {
            this.color = "b2b2b2";
        } else {
            this.color = color;
        }
    }


}
