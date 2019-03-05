/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.913app.com
 */

package com.tnsoft.web.security;

import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

public class LogoutFilterr extends LogoutFilter {
    
    
    public LogoutFilterr(String logoutSuccessUrl, LogoutHandler[] handlers) {
        super(logoutSuccessUrl, handlers);
    }

    public LogoutFilterr(LogoutSuccessHandler logoutSuccessHandler,
                LogoutHandler[] handlers) {
        super(logoutSuccessHandler, handlers);
    }
}
