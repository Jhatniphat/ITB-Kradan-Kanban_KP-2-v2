package com.example.kradankanban_backend.common.services;

import com.example.kradankanban_backend.common.dtos.DetailBoardDTO;
import com.example.kradankanban_backend.common.entities.BoardEntity;
import com.example.kradankanban_backend.common.entities.LimitSettings;
import com.example.kradankanban_backend.common.entities.StatusEntity;
import com.example.kradankanban_backend.common.repositories.BoardRepository;
import com.example.kradankanban_backend.common.repositories.LimitRepository;
import com.example.kradankanban_backend.common.repositories.StatusRepository;
import com.example.kradankanban_backend.exceptions.BadRequestException;
import com.example.kradankanban_backend.exceptions.ItemNotFoundException;
import com.example.kradankanban_backend.shared.UserRepository;
import io.viascom.nanoid.NanoId;
import org.springframework.beans.factory.annotation.Autowired;
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

    public DetailBoardDTO getBoardByUserId(String userId) {
        BoardEntity board = repository.findByUserId(userId);
        if (board == null) {
            throw  new ItemNotFoundException("Board not found");
        }
        DetailBoardDTO.OwnerDTO owner = new DetailBoardDTO.OwnerDTO();
        owner.setOid(board.getUserId());
        owner.setName(userRepository.findById(board.getUserId()).orElseThrow(() -> new ItemNotFoundException(board.getUserId() + "does not exist'")).getUsername() );

        DetailBoardDTO dto = new DetailBoardDTO();
        dto.setId(board.getBoardId());
        dto.setName(board.getBoardName());
        dto.setOwner(owner);

        return dto;
    }

    public DetailBoardDTO getBoardById(String boardId) {
        BoardEntity board = repository.findById(boardId).orElseThrow(() -> new ItemNotFoundException(boardId + "does not exist'"));
        DetailBoardDTO.OwnerDTO owner = new DetailBoardDTO.OwnerDTO();
        owner.setOid(board.getUserId());
        owner.setName(userRepository.findById(board.getUserId()).orElseThrow(() -> new ItemNotFoundException(board.getUserId() + "does not exist'")).getUsername() );

        DetailBoardDTO dto = new DetailBoardDTO();
        dto.setId(board.getBoardId());
        dto.setName(board.getBoardName());
        dto.setOwner(owner);

        return dto;
    }

    @Transactional
    public DetailBoardDTO AddBoard(String userId , String BoardName) {

        LimitSettings limitSettings = new LimitSettings();
        limitSettings.setLimit(10);
        limitSettings.setIsEnable(false);
        limitSettings.setId(1);

//        ! ยังไม่ได้ทำให้ดักว่ามีได้แค่ 1 board ต่อ 1 user
        BoardEntity board = new BoardEntity();
        board.setUserId(userId);
        board.setBoardName(BoardName);
        board.setBoardId(NanoId.generate(10));

        DetailBoardDTO.OwnerDTO owner = new DetailBoardDTO.OwnerDTO();
        owner.setOid(userId);
        owner.setName(userRepository.findById(userId).orElseThrow(() -> new ItemNotFoundException(userId + "does not exist'")).getUsername() );

        DetailBoardDTO dto = new DetailBoardDTO();
        dto.setId(board.getBoardId());
        dto.setName(board.getBoardName());
        dto.setOwner(owner);

//        try {
            BoardEntity newBoard = repository.save(board);
            limitSettings.setLsBoard(newBoard.getBoardId());
            System.out.println(limitSettings);
            limitRepository.save(limitSettings);
            System.out.println(board);
            List<StatusEntity> statusEntity = new ArrayList<>();
            StatusEntity status1 = new StatusEntity("No Status", "", newBoard.getBoardId());
            StatusEntity status2 = new StatusEntity("a", "", newBoard.getBoardId());
            StatusEntity status3 = new StatusEntity("b", "", newBoard.getBoardId());
            StatusEntity status4 = new StatusEntity("c", "", newBoard.getBoardId());
            statusEntity.add(status1);
            statusEntity.add(status2);
            statusEntity.add(status3);
            statusEntity.add(status4);
            statusRepository.saveAll(statusEntity);
            return dto;
//        } catch (Exception e) {
//            System.out.println(e);
//            throw new InternalError("Cannot add board");
//
//        }

    }
}
