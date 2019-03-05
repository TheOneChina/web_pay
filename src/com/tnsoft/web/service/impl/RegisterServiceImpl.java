package com.tnsoft.web.service.impl;

import java.security.GeneralSecurityException;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.aliyuncs.exceptions.ClientException;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDADomain;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.web.dao.DomainDAO;
import com.tnsoft.web.dao.UserDAO;
import com.tnsoft.web.dao.UserRoleDAO;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.RegisterService;
import com.tnsoft.web.util.SmsUtil;
import com.tnsoft.web.util.Utils;

@Service("registerService")
public class RegisterServiceImpl implements RegisterService {

	@Resource(name = "userDAO")
	private UserDAO userDAO;
	
	@Resource(name = "domainDAO")
	private DomainDAO domainDAO;
	
	@Resource(name = "userRoleDAO")
	private UserRoleDAO userRoleDAO;
	
	@Override
	public boolean isUsernameAble(String username) {
		if (username != null) {
			username = username.trim();
			if (username.length()>=8) {
				if (null == userDAO.getUserByName(username)) {
					return true;
				}
			}
		}
		return false;
	}

	private static String getSMScode() {
		int code = (int)(Math.random()*(9999-1000+1))+1000;
		return code + "";
	}

	@Override
	public String creatNewUserAndDomain(String username, String password, String phone) {
		if (!isUsernameAble(username)) {
			return "用户名不可用！";
		}
		if (null == password) {
			return "密码不能为空！";
		}
		if (null != password && password.length()<6) {
			return "密码不能低于6位！";
		}
		if (!Utils.isMobileNO(phone)) {
			return "请输入正确的手机号";
		}
		
		Date now = new Date();
		//在根节点下创建一个新的站点
		NDADomain domain = new NDADomain();
		domain.setAddress("");
		domain.setCreationTime(now);
		domain.setDescription("");
		domain.setDomainPath("/1/");
		domain.setLastModified(now);
		domain.setName(username);
		domain.setPhone(phone);
		domain.setPreferences("");
		domain.setStatus(Constants.DomainState.STATE_ACTIVE);
		domainDAO.save(domain);
		
		NDAUser user = new NDAUser();
		user.setName(username);
		user.setNickName(username);
		user.setMobile(phone);
		try {
			user.setPassword(Utils.hash(username, Utils.newPassword(password)));
		} catch (GeneralSecurityException e) {
		}
		
		//其他值设为默认
		user.setAddress("");
		user.setAttempt(0);
		user.setCreationTime(now);
		user.setDescription("");
		user.setGender("男");
		user.setLastModified(now);
		user.setStaffNo("");
		user.setStatus(1);
		user.setType(Constants.Role.ADMIN);
		user.setDomainId(domain.getId());
		userDAO.save(user);
		
		userRoleDAO.saveUserRole(user.getId(), Constants.Role.ADMIN);
		
		return "注册成功";	
	}

	@Override
	public Response sendCode(String mobile) {
		Response response = new Response();
		if (!Utils.isMobileNO(mobile)) {
			response.setCode(1);
			response.setMessage("请输入正确的手机号");
			return response;
		}
		String code = getSMScode();
		response.setMessage(code);
		try {
			SmsUtil.sendCodeSms(mobile, code);
		} catch (ClientException e) {
			e.printStackTrace();
		}
		response.setCode(0);
		return response;
	}

}
