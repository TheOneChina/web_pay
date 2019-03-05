package com.tnsoft.web.dao;

import java.util.List;

import com.tnsoft.hibernate.model.NDARole;

public interface RoleDAO extends BaseDAO<NDARole>{
	
	public List<NDARole> getRoles();
	
	public List<NDARole> getAdminRoles();
	
	
}
