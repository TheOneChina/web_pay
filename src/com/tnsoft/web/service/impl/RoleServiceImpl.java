package com.tnsoft.web.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.tnsoft.hibernate.model.NDARole;
import com.tnsoft.web.dao.RoleDAO;
import com.tnsoft.web.service.RoleService;

@Service("roleService")
public class RoleServiceImpl extends BaseServiceImpl<NDARole> implements RoleService {

	@Resource(name = "roleDAO")
	private RoleDAO roleDao;

	@Override
	public List<NDARole> getAllRole() {
		// TODO Auto-generated method stub
		List<NDARole> list = roleDao.getRoles();
		return list;
	}

}
