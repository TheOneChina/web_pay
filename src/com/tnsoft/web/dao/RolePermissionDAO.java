package com.tnsoft.web.dao;

import java.util.List;

import com.tnsoft.hibernate.model.RolePermission;

public interface RolePermissionDAO extends BaseDAO<RolePermission>{
	
	public List<RolePermission> getRolePermissionByRId(Integer roleId);
	
}
