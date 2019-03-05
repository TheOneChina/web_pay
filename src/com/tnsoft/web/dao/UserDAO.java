/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.913app.com
 */

package com.tnsoft.web.dao;

import java.util.List;

import com.tnsoft.hibernate.model.NDARole;
import com.tnsoft.hibernate.model.NDAUser;

public interface UserDAO extends BaseDAO<NDAUser> {

	public NDAUser getUserByName(String userName);

	public List<NDARole> getUserRole(Integer UserId);

}
