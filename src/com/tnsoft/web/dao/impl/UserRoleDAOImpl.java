package com.tnsoft.web.dao.impl;

import org.springframework.stereotype.Repository;

import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.UserRole;
import com.tnsoft.web.dao.UserRoleDAO;

@Repository("userRoleDAO")
public class UserRoleDAOImpl extends BaseDAOImpl<UserRole> implements UserRoleDAO {

	@Override
	public void updateUserRole(String userId, String roleId) {
		// TODO Auto-generated method stub
		String hql = "update UserRole ur set ur.roleId=? where userId=?";
		CUDByHql(hql, Integer.parseInt(roleId), Integer.parseInt(userId));
	}

//	@Override
//	public void saveUserRole(String userId, String roleId) {
//		// TODO Auto-generated method stub
//		UserRole ur = new UserRole();
//		ur.setUserId(Integer.parseInt(userId));
//		ur.setStatus(Constants.State.STATE_ACTIVE);
//		ur.setRoleId(Integer.parseInt(roleId));
//		save(ur);
//	}

	@Override
	public UserRole getRoleByUId(Integer userId) {
		// TODO Auto-generated method stub
		String hql = "from UserRole where userId=?";
		UserRole ur = getOneByHQL(hql, userId);
		return ur;
	}

	@Override
	public void saveUserRole(int userId, int roleId) {
		if (userId < 1 || roleId < 2) {
			return;
		}
		UserRole ur = new UserRole();
		ur.setUserId(userId);
		ur.setRoleId(roleId);
		ur.setStatus(Constants.UserRoleState.STATE_NORMAL);
		save(ur);
	}

}
