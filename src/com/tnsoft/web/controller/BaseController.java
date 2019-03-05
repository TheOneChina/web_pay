package com.tnsoft.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.tnsoft.web.model.LoginSession;
import com.tnsoft.web.service.DomainService;
import com.tnsoft.web.servlet.ServletConsts;

public abstract class BaseController {

	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected LoginSession lg;
	protected HttpSession session;
	@Resource(name = "domainService")
	private DomainService domainService;
	@Autowired
	private MessageSource messageSource;

	public BaseController() {

	}

	@ModelAttribute
	public void init(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		this.session = request.getSession();
		lg = (LoginSession) session.getAttribute(ServletConsts.ATTR_USER);
	}

	public int roleId() {
		return lg.getDefRole().getRoleId();
	}

	public boolean validateUser() {
		return lg != null;
	}

	public String getMessage(String messageKey) {
		return messageSource.getMessage(messageKey, new Object[] {}, LocaleContextHolder.getLocale());
	}
	
}
