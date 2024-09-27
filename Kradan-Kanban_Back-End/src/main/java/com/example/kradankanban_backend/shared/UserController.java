package com.example.kradankanban_backend.shared;

import com.example.kradankanban_backend.exceptions.AuthenticationFailedException;
import com.example.kradankanban_backend.shared.dtos.JwtRequestUser;
import com.example.kradankanban_backend.shared.dtos.JwtResponseTokenDTO;
import com.example.kradankanban_backend.shared.dtos.UserDataDTO;
import com.example.kradankanban_backend.shared.services.JwtTokenUtil;
import com.example.kradankanban_backend.shared.services.JwtUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;


@CrossOrigin(origins = {"http://localhost:5174" , "http://localhost:5173"})
//@CrossOrigin(origins = "http://ip23kp2.sit.kmutt.ac.th:80")
@RestController
@RequestMapping("")
public class UserController {
    @Autowired
    JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    AuthenticationManager authenticationManager;

//    @PostMapping("")
//    public ResponseEntity login(@RequestBody UserDataDTO userData) {
//        if (service.Authentication(userData.getUserName(), userData.getPassWord())) {
//            return new ResponseEntity(HttpStatus.OK);
//        } else {
//            return new ResponseEntity(HttpStatus.BAD_REQUEST);
//        }
//    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid JwtRequestUser jwtRequestUser) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(jwtRequestUser.getUserName(), jwtRequestUser.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        if (! authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Invalid user or password");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenUtil.generateToken(userDetails);
        String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
        return ResponseEntity.ok(new JwtResponseTokenDTO(token, refreshToken));
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Object> validateToken(@RequestHeader("Authorization") String requestTokenHeader) {
        Claims claims = null;
        String jwtToken = null;
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                claims = jwtTokenUtil.getAllClaimsFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "JWT Token does not begin with Bearer String");
        }
        return ResponseEntity.ok(claims);
    }

    @PostMapping("/token")
    public ResponseEntity refreshToken(@RequestHeader("Authorization") String tokenHeader) {
        String refreshToken = null;
        String oid = null;
        if (tokenHeader != null) {
            if (tokenHeader.startsWith("Bearer ")) {
                refreshToken = tokenHeader.substring(7);
                try {
                    oid = jwtTokenUtil.getUsernameFromToken(refreshToken);
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
                } catch (ExpiredJwtException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
                }
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JWT Token does not begin with Bearer String");
            }
        }
        if (jwtTokenUtil.isTokenExpired(refreshToken)) {
            throw new AuthenticationFailedException("Refresh Token has expired");
        }
        UserDetails userDetails = jwtUserDetailsService.loadUserByOid(oid);
        System.out.println(userDetails);
        String newAccessToken = jwtTokenUtil.generateToken(userDetails);
        Map<String, String> response = new HashMap<>();
        response.put("access_token", newAccessToken);
        return ResponseEntity.ok(response);
    }
}
