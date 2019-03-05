/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * 
 */
package com.tnsoft.web.util;

import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.web.model.LoginSession;

import com.tnsoft.web.security.LoginUser;

import java.security.SecureRandom;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;

/**
 * 登录信息，登录信息会保存在浏览器cookies里
 */
public final class LoginSessionFactory {

	private LoginSessionFactory() {
	}

	public static LoginSession getLoginSession(DbSession db, LoginUser user) {
		LoginSession loginSession = new LoginSession();
		loginSession.setUserName(user.getUsername());
		loginSession.setUserId(user.getId());
		loginSession.setNickName(user.getNickName());
		loginSession.setSessionId(new SecureRandom().nextLong());	
		loginSession.setDomainId(DBUtils.getDomainIdByUserId(db, user.getId()));
		loginSession.setDomain(DBUtils.getDomainByUserId(db, user.getId()));
		
		String sql = "SELECT nda_role.name as rname,flag,nda_role.id as rid FROM user_role,nda_role WHERE nda_role.status="
				+ Constants.State.STATE_ACTIVE + " AND nda_role.id=role_id AND user_id=:userId";
		SQLQuery query = db.createSQLQuery(sql);
		query.addScalar("rname", StringType.INSTANCE);
		query.addScalar("flag", IntegerType.INSTANCE);
		query.addScalar("rid", IntegerType.INSTANCE);
		query.setParameter("userId", user.getId());
		List<?> list = query.list();
		for (Object obj : list) {
			Object[] row = (Object[]) obj;
			String roleName = (String) row[0];
			Integer flag = (Integer) row[1];
			Integer roleId = (Integer) row[2];
			loginSession.add(roleName, flag == 1, roleId);
		}
		return loginSession;
	}

	public static LoginSession getLoginSession(DbSession db, NDAUser user) {
		LoginSession loginSession = new LoginSession();
		loginSession.setUserName(user.getName());
		loginSession.setUserId(user.getId());
		// loginSession.setTicket(user.getTicket());
		// loginSession.setPreferences(user.getPreferences());
		loginSession.setSessionId(new SecureRandom().nextLong());
		loginSession.setDomainId(user.getDomainId());

		String sql = "SELECT nda_role.name as rname,flag,nda_role.id as rid FROM user_role,nda_role WHERE nda_role.status="
				+ Constants.State.STATE_ACTIVE + " AND nda_role.id=role_id AND user_id=:userId";
		SQLQuery query = db.createSQLQuery(sql);
		query.addScalar("rname", StringType.INSTANCE);
		query.addScalar("flag", IntegerType.INSTANCE);
		query.addScalar("rid", IntegerType.INSTANCE);
		query.setParameter("userId", user.getId());
		List<?> list = query.list();
		for (Object obj : list) {
			Object[] row = (Object[]) obj;
			String roleName = (String) row[0];
			Integer flag = (Integer) row[1];
			Integer roleId = (Integer) row[2];
			loginSession.add(roleName, flag == 1, roleId);
		}
		return loginSession;
	}
}
