package com.example.kradankanban_backend.common.dtos;


import com.example.kradankanban_backend.common.entities.CollabEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class CollabDTO {
    private String oid;
    private String name;
    private String email;
    private CollabEntity.AccessRight accessRight;
    private OffsetDateTime addedOn;
}
