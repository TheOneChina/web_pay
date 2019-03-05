package com.tnsoft.web.dao;

import java.util.List;

import com.tnsoft.hibernate.model.Permission;

public interface PermissionDAO extends BaseDAO<Permission> {

	public List<Permission> findAll();

	public List<Permission> findByUserId(int userId);

}
