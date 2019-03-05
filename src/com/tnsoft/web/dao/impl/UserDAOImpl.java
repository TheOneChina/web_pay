/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.913app.com
 */

package com.tnsoft.web.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.tnsoft.hibernate.model.NDARole;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.web.dao.UserDAO;

@Repository("userDAO")
public class UserDAOImpl extends BaseDAOImpl<NDAUser> implements UserDAO {

	@Override
	public NDAUser getUserByName(String userName) {
		String hql = "from NDAUser where name=?";
		List<NDAUser> userList = getByHQL(hql, userName);
		if (!userList.isEmpty()) {
			return userList.get(0);
		}
		return null;
	}

	@Override
	public List<NDARole> getUserRole(Integer userId) {
		// TODO Auto-generated method stub
		String hql = "select roleId from UserRole where userId=?";
		List<Integer> list = getSession().createQuery(hql).setParameter(0, userId).list();
		List<NDARole> role = new ArrayList<NDARole>();

		for (Integer i : list) {
			role = getSession().createCriteria(NDARole.class).add(Restrictions.eq("id", i)).list();
		}
		return role;
	}

}
