package com.example.kradankanban_backend.common.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@Entity
@Table(name = "task", schema = "intergrate-kp-2")
public class TaskEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @NotNull
    @NotBlank
    @Size(min = 0 ,max = 100, message = "size must be between 0 and 100")
    @Column(name = "title")
    private String title;


    @Size(max = 500, message = "size must be between 0 and 500")
    @Column(name = "description")
    private String description;

    @Size(max = 30, message = "size must be between 0 and 30")
    @Column(name = "assignees")
    private String assignees;

    @Column(name = "status")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Column(name = "createdOn", insertable = false, updatable = false)
    private LocalDateTime createdOn;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Column(name = "updatedOn", insertable = false, updatable = false)
    private LocalDateTime updatedOn;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tk_boardId", nullable = false)
    private BoardEntity tkBoard;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttachmentEntity> attachments;

    public void setTitle(String title) {
        if(title != null)
            this.title = title.trim();
    }

    public void setDescription(String description) {
        if ( description == null || description.isBlank()) {
            this.description = null;
        } else this.description = description.trim();
    }

    public void setAssignees(String assignees) {
        if (assignees == null || assignees.isBlank()) {
            this.assignees = null;
        }else this.assignees = assignees.trim();
    }

    public void setStatus(String status) {
        if (status == null) {
            status = "NO_STATUS";
        }
        this.status = status;
    }

}
