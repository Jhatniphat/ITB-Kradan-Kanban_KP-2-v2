package com.example.kradankanban_backend.common.dtos;


import com.example.kradankanban_backend.common.entities.BoardEntity;
import com.example.kradankanban_backend.common.entities.CollabEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Getter
@Setter
public class DetailBoardDTO {
//    ! This is a DTO class for Response Body of Add Board API

    private String id;
    private String name;
    private BoardEntity.Visibility visibility;
    private OwnerDTO owner;

    @Data
    @Getter
    @Setter
    public static class OwnerDTO {
        private String oid;
        private String name;
    }

    private List<CollabEntity> collaborators;
}

