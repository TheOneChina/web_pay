/*
 * Copyright (c) 2016 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * 
 */
package com.tnsoft.web.model;

import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.hibernate.model.NDAExpress;

import com.tnsoft.hibernate.model.NDATempExpress;

import java.io.Serializable;

import java.util.List;

public class Response implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int OK = 0;
	public static final int ERROR = 1;

	private int code;

	private String message;
	private String ticket;
	private int userId;
	private long syncTime;
	private int roleId;

	private long extra;
	private boolean needSync;

	private List<NDAExpress> express;
	private List<NDATempExpress> temps;
	private List<Alert> alerts;

	private List<NDAAlert> ndaAlerts;

	public Response() {
		super();
	}

	public Response(int code) {
		this.code = code;
	}

	public Response(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getTicket() {
		return ticket;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getUserId() {
		return userId;
	}

	public void setExtra(long extra) {
		this.extra = extra;
	}

	public long getExtra() {
		return extra;
	}

	public void setSyncTime(long syncTime) {
		this.syncTime = syncTime;
	}

	public long getSyncTime() {
		return syncTime;
	}

	public void setNeedSync(boolean needSync) {
		this.needSync = needSync;
	}

	public boolean isNeedSync() {
		return needSync;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setExpress(List<NDAExpress> express) {
		this.express = express;
	}

	public List<NDAExpress> getExpress() {
		return express;
	}

	public void setTemps(List<NDATempExpress> temps) {
		this.temps = temps;
	}

	public List<NDATempExpress> getTemps() {
		return temps;
	}

	public void setAlerts(List<Alert> alerts) {
		this.alerts = alerts;
	}

	public List<Alert> getAlerts() {
		return alerts;
	}

	public List<NDAAlert> getNdaAlerts() {
		return ndaAlerts;
	}

	public void setNdaAlerts(List<NDAAlert> ndaAlerts) {
		this.ndaAlerts = ndaAlerts;
	}

	@Override
	public String toString() {
		return "Response [code=" + code + ", message=" + message + ", ticket=" + ticket + ", userId=" + userId
				+ ", syncTime=" + syncTime + ", roleId=" + roleId + ", extra=" + extra + ", needSync=" + needSync
				+ ", express=" + express + ", temps=" + temps + ", alerts=" + alerts + ", ndaAlerts=" + ndaAlerts + "]";
	}

}
