package com.example.kradankanban_backend.shared.services;

import com.example.kradankanban_backend.shared.Entities.AuthUser;
import com.example.kradankanban_backend.shared.Entities.UserEntity;
import com.example.kradankanban_backend.shared.UserRepository;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponse;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserEntity user = repository.findByUsername(userName);
        if (user == null) {
            throw new BadCredentialsException("username or password is invalid");
        }
        List<GrantedAuthority> roles = new ArrayList<>();
        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole().toString();
            }
        };
        roles.add(grantedAuthority);
        UserDetails userDetails = new AuthUser(user.getUsername(), user.getPassword() , roles , user.getOid() , user.getEmail() , user.getRole() , user.getName());
        return userDetails;
    }

    public boolean Authentication(String username, String password){
        UserEntity userEntity = repository.findByUsername(username);
        if(userEntity == null){
            return false;
        } else {
            Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 16,32);
            char[] passwordArray = password.toCharArray();
            return argon2.verify(userEntity.getPassword(), passwordArray);
        }
    }
}
