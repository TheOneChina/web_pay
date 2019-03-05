package com.tnsoft.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDARole;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.web.model.SelectItem;
import com.tnsoft.web.service.UserService;
import com.tnsoft.web.util.Utils;

@Controller
public class UserController extends BaseController {

	@Resource(name = "userService")
	private UserService userService;

	@RequestMapping("/user")
	public String user(Model model) {
		Utils.saveLog(lg.getUserId(), "查看人员列表", lg.getDomainId());
		if (!validateUser()) {
			return "redirect:/";
		}

		// 参数返回到界面上
		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());
		
		model.addAttribute("roleId", lg.getDefRole().getRoleId());
		return "view.user.user";
	}

	@RequestMapping("/editUser")
	public ModelAndView editUser(Model model, String id) {
		Utils.saveLog(lg.getUserId(), "编辑人员", lg.getDomainId());

		if (!validateUser()) {
			return new ModelAndView("redirect:/");
		}
		
		NDAUser user = null;
		List<NDARole> roles = userService.getRoles(lg);
		List<NDARole> role = new ArrayList<NDARole>();
		List<SelectItem> roles1 = new ArrayList<SelectItem>();
		// 全部的角色
		for (NDARole r : roles) {
			roles1.add(new SelectItem(r.getId() + "", r.getName()));
		}
		List<SelectItem> role1 = new ArrayList<SelectItem>();
		// 如果id不为空,那就是修改admin,以id是否存在来判别是否要为editAdmin.jsp上的输入框赋值,而新增的url不带id参数
		if (!StringUtils.isEmpty(id)) {
			model.addAttribute("id", id);
			DbSession session = BaseHibernateUtils.newSession();
			try {
				user = (NDAUser) session.get(NDAUser.class, Integer.parseInt(id));
				role = userService.getUserRole(user.getId());
				// 选中用户的角色
				for (NDARole r : role) {
					role1.add(new SelectItem(r.getId() + "", r.getName()));
				}
				// 加载选中用户的角色
				model.addAttribute("roleIdx", role1.get(0).getId());
			} finally {
				session.close();
			}
		} else {
			user = new NDAUser();
		}
		List<SelectItem> genders = new ArrayList<SelectItem>();
		genders.add(new SelectItem("男", "男"));
		genders.add(new SelectItem("女", "女"));

		model.addAttribute("genders", genders);
		model.addAttribute("username", lg.getUserName());
		// 动态加载下拉框所有的角色
		model.addAttribute("roles", roles1);

		return new ModelAndView("view.user.editUser", "command", user);

	}

	@RequestMapping(value = "/ajaxSaveUser", method = RequestMethod.POST)
	@ResponseBody
	public String ajaxSaveUser(String nickName, String id, String roleId, String staffNo, String gender,
			String birthDate, String mobile, String address, String description) {
		return userService.saveUser(nickName, id, roleId, staffNo, gender, birthDate, mobile, address, description,
				lg.getDomainId());
	}

	@RequestMapping("/deleteUser")
	public String deleteAdmin(Model model, String id, int mode, RedirectAttributes attr) {
		if (!validateUser()) {
			return "view.login";
		}

		if (Integer.parseInt(id) == lg.getUserId()) {
			attr.addFlashAttribute("error", true);
			attr.addFlashAttribute("message", "不能对自己进行操作");
			return "redirect:/user";
		}

		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();

			Date now = new Date();
			if (!StringUtils.isEmpty(id)) {
				NDAUser user = (NDAUser) db.get(NDAUser.class, Integer.parseInt(id));
				if (user != null) {
					if (mode == 1) {
						user.setLastModified(now);
						attr.addFlashAttribute("message", "人员注销成功");
						user.setStatus(Constants.UserState.STATE_CANCLE);
						Utils.saveLog(lg.getUserId(), "注销人员", lg.getDomainId());

					} else {
						user.setLastModified(now);
						attr.addFlashAttribute("message", "人员恢复成功");
						user.setStatus(Constants.UserState.STATE_NORMAL);
						Utils.saveLog(lg.getUserId(), "恢复人员", lg.getDomainId());
					}
				}
			}
			db.commit();

		} finally {
			db.close();
		}
		attr.addFlashAttribute("error", true);
		return "redirect:/user";
	}

	@RequestMapping("/ajaxUser")
	@ResponseBody
	public Object ajaxAdmin(int draw, int start, int length) {
		if (!validateUser()) {
			return "";
		}

		DbSession session = BaseHibernateUtils.newSession();
		try {
			Map<String, Object> result = query(session, draw, start, length, " order by id ASC ");
			return result;
		} finally {
			session.close();
		}
	}

	private Map<String, Object> query(DbSession db, int draw, int start, int length, String defaultOrderBy) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, String[]> properties = request.getParameterMap();

		String orderSql = "";

		String search = properties.get("search[value]")[0];

		long recordsFiltered = 0;
		
		long recordsTotal = count(db, "a.domainId=" + lg.getDomainId());
		
		result.put("recordsTotal", recordsTotal);

		String whereClause = "";

		if (!StringUtils.isEmpty(search)) {

			whereClause = " (a.nick_name LIKE '%" + search + "%') ";
		}

		String status = properties.get("columns[7][search][value]")[0];
		String roleId = properties.get("columns[1][search][value]")[0];
		if (!StringUtils.isEmpty(status)) {
			if (!StringUtils.isEmpty(whereClause)) {
				whereClause += " AND ";
			}
			whereClause += " a.status=" + status + " ";
		}

		if (!StringUtils.isEmpty(roleId)) {
			if (!StringUtils.isEmpty(whereClause)) {
				whereClause += " AND ";
			}
			whereClause += "a.id in(select user_id from user_role where role_id='" + roleId + " ') ";
		}

		if (!StringUtils.isEmpty(whereClause)) {
			whereClause += " AND ";
		}
		// id=1会把默认用户加载出来
		whereClause += "a.domainId=" + lg.getDomainId();

		if (!StringUtils.isEmpty(search) || !StringUtils.isEmpty(status) || !StringUtils.isEmpty(roleId)) {
			recordsFiltered = count(db, whereClause);
		} else {
			recordsFiltered = recordsTotal;
		}
		result.put("recordsFiltered", recordsFiltered);
		result.put("data", query(db, whereClause, orderSql, start, length));
		return result;

	}

	private int count(DbSession session, String where) {

		String sql = "from NDAUser a ";

		if (!StringUtils.isEmpty(where)) {
			sql += (" WHERE " + where);
		}

		Integer count = userService.count(sql);
		return count == null ? 0 : count.intValue();
	}

	private List<Map<String, Object>> query(DbSession session, String where, String order, int offset, int limit) {

		String sql = "from NDAUser a ";
		if (!StringUtils.isEmpty(where)) {
			sql += (" WHERE " + where);
		}

		if (!StringUtils.isEmpty(order)) {
			sql += (" " + order);
		}
		
		List<NDAUser> list = userService.getByHQLWithLimits(offset, limit, sql);

//		SQLQuery query = session.createSQLQuery(sql);
//		query.addEntity(NDAUser.class);
//		BaseHibernateUtils.setLimit(query, offset, limit);
		// 获取用户list
//		List<?> list = query.list();
		if (!list.isEmpty()) {
			List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
			for (Object obj : list) {
				NDAUser user = (NDAUser) obj;
				// 使用工具把对象变为map
				Map<String, Object> map1 = Utils.ObjToMap(user);
				// 获取页面的roleId,
				List<NDARole> role = userService.getUserRole(user.getId());
				// 暂时默认一个用户只有一个角色
				if (role != null && role.size() > 0) {
					map1.put("roleId", role.get(0).getId());
				}
				result.add(map1);
			}
			return result;
		}

		return Collections.<Map<String, Object>>emptyList();
	}

}
