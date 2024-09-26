package com.example.kradankanban_backend.common.services;


import com.example.kradankanban_backend.common.dtos.DetailBoardDTO;
import com.example.kradankanban_backend.common.dtos.VisibilityDTO;
import com.example.kradankanban_backend.common.entities.BoardEntity;
import com.example.kradankanban_backend.common.entities.LimitSettings;
import com.example.kradankanban_backend.common.entities.StatusEntity;
import com.example.kradankanban_backend.common.repositories.BoardRepository;
import com.example.kradankanban_backend.common.repositories.LimitRepository;
import com.example.kradankanban_backend.common.repositories.StatusRepository;
import com.example.kradankanban_backend.exceptions.BadRequestException;
import com.example.kradankanban_backend.exceptions.ForbiddenException;
import com.example.kradankanban_backend.exceptions.ItemNotFoundException;
import com.example.kradankanban_backend.exceptions.WrongBoardException;
import com.example.kradankanban_backend.shared.Entities.UserEntity;
import com.example.kradankanban_backend.shared.UserRepository;
import io.viascom.nanoid.NanoId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BoardService {

    @Autowired
    BoardRepository repository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LimitRepository limitRepository;

    @Autowired
    StatusRepository statusRepository;

    @Autowired
    private ModelMapper modelMapper;

    private DetailBoardDTO convertToDetailBoardDTO(BoardEntity board) {
        DetailBoardDTO.OwnerDTO owner = new DetailBoardDTO.OwnerDTO();
        owner.setOid(board.getUserId());
        owner.setName(userRepository.findById(board.getUserId()).orElseThrow(() -> new ItemNotFoundException(board.getUserId() + " does not exist")).getName());

        DetailBoardDTO dto = new DetailBoardDTO();
        dto.setId(board.getBoardId());
        dto.setName(board.getBoardName());
        dto.setVisibility(board.getVisibility());
        dto.setOwner(owner);

        return dto;
    }

    public List<DetailBoardDTO> getBoardByUserId(String userId) {
        List<BoardEntity> privateBoards = repository.findAllByUserIdAndVisibility(userId, BoardEntity.Visibility.PRIVATE);
        List<BoardEntity> publicBoards = repository.findAllByVisibility(BoardEntity.Visibility.PUBLIC);

        List<DetailBoardDTO> result = new ArrayList<>();

        for (BoardEntity board : privateBoards) {
            DetailBoardDTO dto = convertToDetailBoardDTO(board);
            result.add(dto);
        }

        for (BoardEntity board : publicBoards) {
            DetailBoardDTO dto = convertToDetailBoardDTO(board);
            result.add(dto);
        }

        return result;
    }

//    public DetailBoardDTO getBoardByUserId(String userId) {
//        BoardEntity board = repository.findByUserId(userId);
////        if (board == null) {
////            throw new ItemNotFoundException("Board not found");
////        }
//
//
//        if (board != null) {
//            DetailBoardDTO.OwnerDTO owner = new DetailBoardDTO.OwnerDTO();
//            owner.setOid(board.getUserId());
//            owner.setName(userRepository.findById(board.getUserId()).orElseThrow(() -> new ItemNotFoundException(board.getUserId() + "does not exist'")).getName());
//
//            DetailBoardDTO dto = new DetailBoardDTO();
//            dto.setId(board.getBoardId());
//            dto.setName(board.getBoardName());
//            dto.setVisibility(board.getVisibility());
//            dto.setOwner(owner);
//
//            return dto;
//        }
//        return modelMapper.map(repository.findAll(), DetailBoardDTO.class);
//    }

    public DetailBoardDTO getBoardById(String boardId) {
        BoardEntity board = repository.findById(boardId).orElseThrow(() -> new WrongBoardException(boardId + "does not exist'"));
        DetailBoardDTO.OwnerDTO owner = new DetailBoardDTO.OwnerDTO();
        owner.setOid(board.getUserId());
        owner.setName(userRepository.findById(board.getUserId()).orElseThrow(() -> new ItemNotFoundException(board.getUserId() + "does not exist'")).getName());

        DetailBoardDTO dto = new DetailBoardDTO();
        dto.setId(board.getBoardId());
        dto.setName(board.getBoardName());
        dto.setVisibility(board.getVisibility());
        dto.setOwner(owner);
        return dto;
    }

    @Transactional
    public DetailBoardDTO AddBoard(String userId, String BoardName) {

        // ? Check if user already has a board
        BoardEntity oldBoard = repository.findByUserId(userId);
        if (oldBoard != null) {
            throw new BadRequestException("User already has a board");
        }

        BoardEntity board = new BoardEntity();
        board.setUserId(userId);
        board.setBoardName(BoardName);
        board.setVisibility(BoardEntity.Visibility.PRIVATE);
        board.setBoardId(NanoId.generate(10));


        DetailBoardDTO.OwnerDTO owner = new DetailBoardDTO.OwnerDTO();
        owner.setOid(userId);
        owner.setName(userRepository.findById(userId).orElseThrow(() -> new ItemNotFoundException(userId + "does not exist'")).getName());

        DetailBoardDTO dto = new DetailBoardDTO();
        dto.setId(board.getBoardId());
        dto.setName(board.getBoardName());
        dto.setVisibility(board.getVisibility());
        dto.setOwner(owner);

        try {
            BoardEntity newBoard = repository.save(board);
            List<StatusEntity> statusEntity = new ArrayList<>();
            StatusEntity status1 = new StatusEntity("No Status", "", newBoard.getBoardId());
            StatusEntity status2 = new StatusEntity("To Do", "", newBoard.getBoardId());
            StatusEntity status3 = new StatusEntity("Doing", "", newBoard.getBoardId());
            StatusEntity status4 = new StatusEntity("Done", "", newBoard.getBoardId());
            statusEntity.add(status1);
            statusEntity.add(status2);
            statusEntity.add(status3);
            statusEntity.add(status4);
            statusRepository.saveAll(statusEntity);

            LimitSettings limitSettings = new LimitSettings();
            limitSettings.setLsBoard(newBoard.getBoardId());
            limitSettings.setLimit(10);
            limitSettings.setIsEnable(false);
            limitRepository.save(limitSettings);

            return dto;
        } catch (Exception e) {
            System.out.println(e);
            throw new InternalError("Cannot add board");
        }
    }

    @Transactional
    public VisibilityDTO editVisibility(String boardId, VisibilityDTO visibility) {
        BoardEntity board = repository.findById(boardId).orElseThrow(() -> new WrongBoardException(boardId + "does not exist'"));
        board.setVisibility(visibility.getVisibility());
        repository.save(board);
        return visibility;
    }

    public void CheckOwnerAndVisibility(String boardId, String userId,String requestMethod) {
        BoardEntity board = repository.findByBoardId(boardId).orElseThrow(() -> new WrongBoardException(boardId + "does not exist'"));
        boolean isOwner = board.getUserId().equals(userId);
        if (userId != null) {
            if (!isOwner && board.getVisibility().equals(BoardEntity.Visibility.PRIVATE)) {
                throw new ForbiddenException("Not Owner and Private Board");
            }
            if (!isOwner && board.getVisibility().equals(BoardEntity.Visibility.PUBLIC) && !requestMethod.equals("GET")) {
                throw new ForbiddenException("Not Owner and Public Board");
            }
        } else if (requestMethod.equals("GET") && board.getVisibility().equals(BoardEntity.Visibility.PRIVATE)) {
            throw new ForbiddenException("Not Owner and Private Board");
        }
    }
}
