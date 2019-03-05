package com.tnsoft.web.service;

import com.tnsoft.web.model.Response;

public interface RegisterService {

	public boolean isUsernameAble(String username); //查询用户名是否存在
	
	public Response sendCode(String mobile);
	
	public String creatNewUserAndDomain(String username, String password, String phone);
}
