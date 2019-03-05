package com.tnsoft.web.service;

import java.util.List;

import com.tnsoft.hibernate.model.Permission;

public interface PermissionService extends BaseService<Permission> {

	public List<List<Permission>> getPermission(Integer roleId);
	
}
