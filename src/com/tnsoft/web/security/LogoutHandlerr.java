/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.913app.com
 */

package com.tnsoft.web.security;

import com.expertise.common.logging.Logger;

import com.tnsoft.web.servlet.ServletConsts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

public class LogoutHandlerr implements LogoutHandler {
    
    public LogoutHandlerr() {
        super();
    }
    
    @Override
    public void logout(HttpServletRequest request,
            HttpServletResponse response, Authentication authentication) {
        //clear sessions
        HttpSession session = request.getSession();
        session.removeAttribute(ServletConsts.ATTR_USER);
        
        Logger.info("LogoutHandlerr.logout() is called!");

    }

}
