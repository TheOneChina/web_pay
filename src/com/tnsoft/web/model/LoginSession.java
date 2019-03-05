/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.913app.com
 */
package com.tnsoft.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.tnsoft.hibernate.model.NDADomain;

/**
 * 登录session
 */
public class LoginSession implements Serializable {

	private static final long serialVersionUID = 1L;

	// 用户名
	private String userName;
	private String nickName;
	// 用户id
	private int userId;
	// sessionId
	private long sessionId;
	private String ticket;
	private String preferences;
	private int domainId;
	// domainPath
	private NDADomain domain;

	// 用户角色列表
	private List<Role> roles;
	// 用户默认角色
	private Role defRole;

	public LoginSession() {
		roles = new ArrayList<Role>();
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getUserId() {
		return userId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getTicket() {
		return ticket;
	}

	public void setPreferences(String preferences) {
		this.preferences = preferences;
	}

	public String getPreferences() {
		return preferences;
	}

	public void add(String roleName, boolean def, int roleId) {
		Role role = new Role(roleName, roleId);
		roles.add(role);
		if (defRole == null || def) {
			defRole = role;
		}
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setDefRole(Role defRole) {
		this.defRole = defRole;
	}

	public Role getDefRole() {
		return defRole;
	}

	public int getDomainId() {
		return domainId;
	}

	public void setDomainId(int domainId) {
		this.domainId = domainId;
	}

	public NDADomain getDomain() {
		return domain;
	}

	public void setDomain(NDADomain domain) {
		this.domain = domain;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public static final class Role implements Serializable {

		private static final long serialVersionUID = 1L;

		private String roleName;
		private int roleId;

		public Role() {
		}

		public Role(String roleName, int roleId) {
			this.roleName = roleName;
			this.roleId = roleId;
		}

		public String getRoleName() {
			return roleName;
		}

		public void setRoleId(int roleId) {
			this.roleId = roleId;
		}

		public int getRoleId() {
			return roleId;
		}
	}
}
