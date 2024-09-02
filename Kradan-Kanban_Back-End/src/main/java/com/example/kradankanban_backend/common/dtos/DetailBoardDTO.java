package com.example.kradankanban_backend.common.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailBoardDTO {
//    ! This is a DTO class for Response Body of Add Board API

    private String id;
    private String name;
    private OwnerDTO owner;


    @Getter
    @Setter
    public static class OwnerDTO {
        private String oid;
        private String name;
    }

}

