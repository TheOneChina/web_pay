/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.913app.com
 */
package com.tnsoft.web.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class LoginUser extends User {
    
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String nickName;

    public LoginUser(int id, 
                     String nickName,
                     String username, 
                     String password,  
                     final boolean enabled,
                     final boolean accountNonExpired,                      
                     final boolean credentialsNonExpired,                      
                     final boolean accountNonLocked,
                     final Collection<? extends GrantedAuthority> authorities) {
        super(username, 
              password,
              enabled,
              accountNonExpired,
              credentialsNonExpired,
              accountNonLocked,
              authorities);
        this.id = id;
        this.nickName = nickName;
    }


    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }
}
