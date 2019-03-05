package com.tnsoft.web.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDARole;
import com.tnsoft.web.dao.RoleDAO;

@Repository("roleDAO")
public class RoleDAOImpl extends BaseDAOImpl<NDARole> implements RoleDAO {

	@Override
	public List<NDARole> getRoles() {
		// TODO Auto-generated method stub
		String hql = "from NDARole where status=? and id <> 1";
		List<NDARole> list = getByHQL(hql,Constants.State.STATE_ACTIVE);
		return list;
	}

	@Override
	public List<NDARole> getAdminRoles() {
		// TODO Auto-generated method stub
		//获取普通管理员在添加员工时,能为员工选择的角色
		String hql = "from NDARole where status=? and id <> 1  and id <> 2";
		List<NDARole> list = getByHQL(hql,Constants.State.STATE_ACTIVE);
		return list;
	}
	
	
	
}
