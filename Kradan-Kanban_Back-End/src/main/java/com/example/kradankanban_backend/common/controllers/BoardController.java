package com.example.kradankanban_backend.common.controllers;


import com.example.kradankanban_backend.common.dtos.BoardNameDTO;
import com.example.kradankanban_backend.common.dtos.DetailBoardDTO;
import com.example.kradankanban_backend.common.entities.BoardEntity;
import com.example.kradankanban_backend.common.services.BoardService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = {"http://localhost:5174" , "http://localhost:5173"})
//@CrossOrigin(origins = "http://ip23kp2.sit.kmutt.ac.th:80")
@RestController
@RequestMapping("/boards")
public class BoardController {
    @Autowired
    private BoardService service;

    @GetMapping("/{boardId}")
    public DetailBoardDTO getBoardById(@PathVariable String boardId) {
        return service.getBoardById(boardId);
    }

    @GetMapping("")
    public DetailBoardDTO getBoardByUserId(@RequestHeader("Authorization") String requestTokenHeader) {
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            String userId = extractUserIdFromToken(jwtToken);
            return service.getBoardByUserId(userId);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authorization header must start with Bearer");
        }
    }

    @PostMapping("")
    public DetailBoardDTO AddNewBoard(@RequestHeader("Authorization") String requestTokenHeader , @RequestBody BoardNameDTO boardNameDTO) {
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            String userId = extractUserIdFromToken(jwtToken);
            return service.AddBoard(userId, boardNameDTO.getName());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authorization header must start with Bearer");
        }
    }

    // Add this method to extract userId from JWT token
    private String extractUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey("N7KgseMPtJ26AEved0ahUKEwj4563eioyFAxUyUGwGHbTODx0Q4dUDCBA") // Use your secret key here
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("oid" , String.class); // Assuming userId is stored in the subject
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }
}
