package com.example.kradankanban_backend.shared.services;

import com.example.kradankanban_backend.shared.Entities.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Value("#{${jwt.max-token-interval-hour}*60*60*1000}")
    private long JWT_TOKEN_VALIDITY;
    @Value("#{${jwt.max-refresh-token-interval-hour}*60*60*1000}")
    private long JWT_REFRESH_TOKEN_VALIDITY;
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        return claims;
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        AuthUser authUser = (AuthUser) userDetails;
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", authUser.getName());
        claims.put("oid", authUser.getOid());
        claims.put("email", authUser.getEmail());
        claims.put("role", authUser.getRole());
        return doGenerateToken(claims, userDetails.getUsername(), JWT_TOKEN_VALIDITY);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        AuthUser authUser = (AuthUser) userDetails;
        Map<String, Object> claims = new HashMap<>();
        claims.put("oid", authUser.getOid());
        return doGenerateToken(claims, authUser.getOid(), JWT_REFRESH_TOKEN_VALIDITY);
    }

    private String doGenerateToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder().setHeaderParam("typ", "JWT").
                setClaims(claims)
                .setSubject(subject)
                .setIssuer("https://intproj23.sit.kmutt.ac.th/kp2/")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signatureAlgorithm, SECRET_KEY).compact();
    }



    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
