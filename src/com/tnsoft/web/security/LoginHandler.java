/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.913app.com
 */

package com.tnsoft.web.security;

import com.expertise.common.logging.Logger;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

public class LoginHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                HttpServletResponse response, Authentication authentication)
                throws ServletException, IOException {
        super.onAuthenticationSuccess(request, response, authentication);

        Logger.info("LoginHandler.onAuthenticationSuccess() is called!");
    }
    
}
