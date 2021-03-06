package com.tnsoft.web.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tnsoft.hibernate.model.Permission;
import com.tnsoft.web.dao.PermissionDAO;

@Repository("permissionDAO")
public class PermissionDAOImpl extends BaseDAOImpl<Permission> implements PermissionDAO {

	@Override
	public List<Permission> findAll() {
		String hql = "from Permission";
		return getByHQL(hql);
	}

	@Override
	public List<Permission> findByUserId(int userId) {
		String hql = "from Permission p where p.id in(select rp.permissionId from RolePermission rp where rp.roleId in(select ur.roleId from UserRole ur where ur.userId=?))";
		List<Permission> list = getByHQL(hql, userId);
		if (!list.isEmpty()) {
			return list;
		}
		return null;
	}



}
