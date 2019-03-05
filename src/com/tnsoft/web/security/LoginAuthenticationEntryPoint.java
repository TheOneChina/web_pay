/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.913app.com
 */
package com.tnsoft.web.security;

import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

public class LoginAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        //HttpServletRequest httpRequest = (HttpServletRequest)request;
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))){
            response.setCharacterEncoding("UTF-8");
            response.addHeader("Error-Json", "{code:302, msg:'Session Timeout',script:''}");
            response.setStatus(300);
        } else{
            super.commence(request, response, authException);
        }
    }
}
