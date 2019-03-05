package com.tnsoft.web.service.impl;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.expertise.common.logging.Logger;
import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDARole;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.web.dao.RoleDAO;
import com.tnsoft.web.dao.UserDAO;
import com.tnsoft.web.dao.UserRoleDAO;
import com.tnsoft.web.model.LoginSession;
import com.tnsoft.web.model.Result;
import com.tnsoft.web.service.UserService;
import com.tnsoft.web.util.AuthUtils;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;

@Service("userService")
public class UserServiceImpl extends BaseServiceImpl<NDAUser> implements UserService {

	@Resource(name = "userDAO")
	private UserDAO userDao;
	@Resource(name = "roleDAO")
	private RoleDAO roleDao;
	@Resource(name = "userRoleDAO")
	private UserRoleDAO userRoleDao;

	@Override
	public String saveUser(String nickName, String id, String roleId, String staffNo, String gender, String birthDate,
			String mobile, String address, String description, int domainId) {
		Result result = new Result();
		DbSession db = BaseHibernateUtils.newSession();

		if (!Utils.isMobileNO(mobile)) {
			result.setCode(Result.ERROR);
			result.setMessage("请输入11位有效手机号码");
			return Utils.GSON.toJson(result);
		}
		// 当id存在时,说明该用户是在编辑操作,此时不需要验证电话号码与工号是否存在
		if (Integer.parseInt(id) == 0) {
			if (DBUtils.mobileExist(db, mobile)) {
				result.setCode(Result.ERROR);
				result.setMessage("该手机号已被注册");
				return Utils.GSON.toJson(result);
			}

			if (DBUtils.staffNoExist(db, staffNo)) {
				result.setCode(Result.ERROR);
				result.setMessage("该工号已经存在");
				return Utils.GSON.toJson(result);
			}
		}
		Date now = new Date();
		// 当id存在时,说明该用户是在编辑操作
		if (!StringUtils.isEmpty(id) && Integer.parseInt(id) > 0) {
			NDAUser user = userDao.getById(Integer.parseInt(id));

			if (user != null) {
				user.setGender(gender);
				user.setBirthDate(birthDate);
				user.setStaffNo(staffNo);
				user.setType(Constants.Role.ADMIN);
				user.setNickName(nickName);
				user.setMobile(mobile);
				user.setAddress(address);
				user.setDescription(description);
				user.setLastModified(now);

			}
			userDao.save(user);
			// 更新用户角色表
			userRoleDao.updateUserRole(id, roleId);
			result.setCode(Result.OK);
			result.setMessage("人员编辑成功");
			return Utils.GSON.toJson(result);
		} else {
			// 新增加
			NDAUser user = new NDAUser();
			user.setName(mobile);
			user.setGender(gender);
			user.setBirthDate(birthDate);
			user.setStaffNo(staffNo);
			user.setNickName(nickName);
			user.setType(Constants.Role.ADMIN);
			user.setMobile(mobile);
			user.setAddress(address);
			user.setDescription(description);
			user.setCreationTime(now);
			user.setLastModified(now);
			user.setStatus(Constants.State.STATE_ACTIVE);
			user.setDomainId(domainId);
			try {
				if (mobile.length() > 6) {
					user.setPassword(
							AuthUtils.hash(mobile, AuthUtils.newPassword(mobile.substring(mobile.length() - 6))));
				} else {
					user.setPassword(AuthUtils.hash(mobile, AuthUtils.newPassword("123456")));
				}
			} catch (GeneralSecurityException e) {
				Logger.error(e);
			}
			userDao.save(user);
			// 新增用户角色表
			userRoleDao.saveUserRole(user.getId(), Integer.parseInt(roleId));
		}

		result.setCode(Result.OK);
		result.setMessage("人员新增成功");
		return Utils.GSON.toJson(result);
	}

	@Override
	public List<NDARole> getUserRole(Integer userId) {
		// TODO Auto-generated method stub
		List<NDARole> list = userDao.getUserRole(userId);
		return list;
	}

	@Override
	public List<NDARole> getRoles(LoginSession lg) {
		// TODO Auto-generated method stub
		List<NDARole> list=new ArrayList<NDARole>();
		if(lg.getDomain().getDomainPath().equals("/")){
			list=roleDao.getRoles();
		}else{
			list=roleDao.getAdminRoles();
		}
		return list;
	}

}
