package com.example.kradankanban_backend.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "status", schema = "intergrate-kp-2")
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

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "status" , cascade = CascadeType.ALL)
    private Set<TaskEntity> recordings = new HashSet<>();

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
}
