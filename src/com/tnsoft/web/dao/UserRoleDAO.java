package com.tnsoft.web.dao;

import com.tnsoft.hibernate.model.UserRole;

public interface UserRoleDAO extends BaseDAO<UserRole> {

	public void updateUserRole(String userId, String roleId);
	
//	public void saveUserRole(String userId,String roleId);
	
	public void saveUserRole(int userId, int roleId);
	
	public UserRole getRoleByUId(Integer userId);
}
