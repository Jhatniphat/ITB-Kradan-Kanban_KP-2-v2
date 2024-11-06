package com.example.kradankanban_backend.shared.filters;

import com.example.kradankanban_backend.common.entities.CollabEntity;
import com.example.kradankanban_backend.common.services.BoardService;
import com.example.kradankanban_backend.common.services.CollabService;
import com.example.kradankanban_backend.exceptions.*;
import com.example.kradankanban_backend.shared.Entities.AuthUser;
import com.example.kradankanban_backend.shared.services.JwtTokenUtil;
import com.example.kradankanban_backend.shared.services.JwtUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private BoardService boardService;
    @Autowired
    private CollabService collabService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            final String requestTokenHeader = request.getHeader("Authorization");
            String username = null;
            String jwtToken = null;
            boolean isTokenValid = false;
            String tokenError = null;
            if (request.getRequestURI().equals("/token") || request.getRequestURI().equals("/login")) {
                chain.doFilter(request, response);
                return;
            }
            if (requestTokenHeader != null) {
                if (requestTokenHeader.startsWith("Bearer ")) {
                    jwtToken = requestTokenHeader.substring(7);
                    try {
                        username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                        isTokenValid = true;
                    } catch (IllegalArgumentException e) {
                        tokenError = e.getMessage();
                    } catch (ExpiredJwtException e) {
                        tokenError = e.getMessage();
                    }
                } else {
                    tokenError = "JWT Token does not begin with Bearer String";
                }
            }
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
            if (request.getRequestURI().startsWith("/v3/boards/")) {

                handleRequest(request, isTokenValid, tokenError);
            } else if (!isTokenValid && !request.getRequestURI().equals("/v3/boards")) {
                throw new AuthenticationFailedException("Invalid token");
            }
            chain.doFilter(request, response);
        } catch (Exception e) {
            handleException(response, e, request.getRequestURI());
        }
    }

    private void handleRequest(HttpServletRequest request, boolean isTokenValid, String tokenError) {
        String[] uri = request.getRequestURI().split("/");
        String boardId = uri[3];
        String requestMethod = request.getMethod();
        AuthUser currentUser = JwtUserDetailsService.getCurrentUser();
        boolean isBoardExist = boardService.boardExists(boardId);
        if (!isBoardExist) {
            if (!isTokenValid && !requestMethod.equals("GET")) {
                throw new AuthenticationFailedException("Unauthorized");
            } else {
                throw new ItemNotFoundException("BOARD NOT FOUND");
            }
        }
        boolean isPublic = boardService.isBoardPublic(boardId);
        if (currentUser != null) {
            if (uri.length > 5 && uri[4].equals("collabs") && uri[5].equals("invitation")) {
                return;
            }
            boolean isOwner = boardService.isBoardOwner(boardId,currentUser.getOid());
            boolean isCollaborator = collabService.isCollaborator(boardId, currentUser.getOid());
            boolean isWriteAccess = collabService.isWriteAccess(boardId, currentUser.getOid());
            if (isCollaborator) {
                if (isWriteAccess) {
                    if ((requestMethod.equals("POST") || requestMethod.equals("PATCH")) && !( uri.length > 4 && (uri[4].equals("tasks") || uri[4].equals("statuses")))) {
                        throw new ForbiddenException("Write access not allowed for this request");
                    }
                    if (requestMethod.equals("DELETE") && request.getRequestURI().contains("/collabs/")) {
                        String collabId = uri[uri.length - 1];
                        if (!collabId.equals(currentUser.getOid())) {
                            throw new ForbiddenException("collaborator cannot delete other collaborators");
                        }
                    }
                } else {
                    if (requestMethod.equals("DELETE") && request.getRequestURI().contains("/collabs/")) {
                        String collabId = uri[uri.length - 1];
                        if (!collabId.equals(currentUser.getOid())) {
                            throw new ForbiddenException("collaborator cannot delete other collaborators");
                        }
                    }
                    else if (!requestMethod.equals("GET")) {
                        throw new ForbiddenException("Read access not allowed for this request");
                    }
                }
            } if (!isOwner && (!isPublic || !requestMethod.equals("GET"))) {
                throw new ForbiddenException("FORBIDDEN");
            }
        } else {
            if (!isPublic) {
                if (requestMethod.equals("GET")) {
                    throw new ForbiddenException("Board is private, Only Collaborator and Owner can access");
                } else {
                    throw new AuthenticationFailedException(tokenError);
                }
            } else {
                if (!requestMethod.equals("GET")) {
                    throw new AuthenticationFailedException(tokenError);
                }
            }
        }
    }

    public void handleException(HttpServletResponse response, Exception e, String uri) throws IOException {
        HttpStatus status;
        String message;

        if (e instanceof AuthenticationFailedException) {
            status = HttpStatus.UNAUTHORIZED;
            message = e.getMessage();
        } else if (e instanceof ItemNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = e.getMessage();
        } else if (e instanceof ForbiddenException) {
            status = HttpStatus.FORBIDDEN;
            message = e.getMessage();
        } else {
            status = HttpStatus.UNAUTHORIZED;
            message = "An unexpected error occurred";
        }

        response.setStatus(status.value());
        response.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse(status.value(), message, uri);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
}



