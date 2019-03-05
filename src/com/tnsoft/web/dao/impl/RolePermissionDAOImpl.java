package com.tnsoft.web.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.RolePermission;
import com.tnsoft.web.dao.RolePermissionDAO;

@Repository("rolePermissionDAO")
public class RolePermissionDAOImpl extends BaseDAOImpl<RolePermission> implements RolePermissionDAO {

	@Override
	public List<RolePermission> getRolePermissionByRId(Integer roleId) {
		// TODO Auto-generated method stub
		String hql = "from RolePermission where roleId=? and status=? order by permissionId ";
		List<RolePermission> list = getByHQL(hql, roleId, Constants.State.STATE_ACTIVE);
		return list;
	}

}
