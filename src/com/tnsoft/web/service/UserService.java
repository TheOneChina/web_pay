package com.tnsoft.web.service;

import java.util.List;

import com.tnsoft.hibernate.model.NDARole;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.web.model.LoginSession;

public interface UserService extends BaseService<NDAUser> {

	public String saveUser(String nickName, String id,String roleId, String staffNo, String gender, String birthDate, String mobile,
			String address, String description, int domainId);

	public List<NDARole> getUserRole(Integer userId);
	
	public List<NDARole> getRoles(LoginSession lg); 
}
