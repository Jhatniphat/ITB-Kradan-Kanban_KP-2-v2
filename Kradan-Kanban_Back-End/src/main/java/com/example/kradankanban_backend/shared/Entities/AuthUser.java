package com.example.kradankanban_backend.shared.Entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
public class AuthUser extends User implements Serializable {

    String name;
    String oid;
    String email;
    Role role;

    public AuthUser() {
        super("anonymous", "", new ArrayList<GrantedAuthority>());
    }

    public AuthUser(String userName, String password) {
        super(userName, password, new ArrayList<GrantedAuthority>());
    }

    public AuthUser(String userName, String password, Collection<? extends GrantedAuthority> authorities , String oid , String email , Role role , String name) {
        super(userName, password, authorities);
        this.oid = oid;
        this.email = email;
        this.role = role;
        this.name = name;
    }
}
