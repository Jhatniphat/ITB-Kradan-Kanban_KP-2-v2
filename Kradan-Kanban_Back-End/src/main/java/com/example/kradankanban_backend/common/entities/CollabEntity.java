package com.example.kradankanban_backend.common.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "collab")
@IdClass(CollabId.class)
public class CollabEntity {
    @Id
    @Column(name = "board_boardId")
    private String boardId;

    @Id
    private String userId;

    @Column(name = "accessRight", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessRight accessRight = AccessRight.READ;

    @ManyToOne
    @JoinColumn(name = "board_boardId", insertable = false, updatable = false)
    @JsonBackReference
    private BoardEntity board;

    @Column(name = "added_On", insertable = false, updatable = false)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssXXX", timezone="UTC")
    private OffsetDateTime addedOn;

    public enum AccessRight {
        READ, WRITE
    }

}