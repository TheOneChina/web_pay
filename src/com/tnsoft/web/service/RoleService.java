package com.tnsoft.web.service;

import java.util.List;

import com.tnsoft.hibernate.model.NDARole;

public interface RoleService extends BaseService<NDARole> {

	public List<NDARole> getAllRole();
	
}
