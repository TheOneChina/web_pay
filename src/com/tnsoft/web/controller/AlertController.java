/*
 * Copyright (c) 2016 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * 
 */
package com.tnsoft.web.controller;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.SQLQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.hibernate.model.NDAAlertLevel;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.hibernate.model.NDAUserExpress;
import com.tnsoft.web.service.AlertService;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;

@Controller
public class AlertController extends BaseController {

	@Resource(name = "alertService")
	private AlertService service;

	public AlertController() {
		super();
	}

	@RequestMapping("/alertHistory")
	public String alertsAll(Model model) {

		Utils.saveLog(lg.getUserId(), "查看历史报警记录", lg.getDomainId());

		if (!validateUser()) {
			return "redirect:/";
		}

		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());

		return "view.alert.alertHistory";
	}

	@RequestMapping("/alertsNow")
	public String alertsNow(Model model) {

		Utils.saveLog(lg.getUserId(), "查看当前报警列表", lg.getDomainId());

		if (!validateUser()) {
			return "redirect:/";
		}

		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());

		return "view.alert.alertsNow";
	}

	@RequestMapping("/ajaxAlerts")
	@ResponseBody
	public Object ajaxAlerts(int draw, int start, int length) {
		if (!validateUser()) {
			return "";
		}

		DbSession session = BaseHibernateUtils.newSession();
		try {
			Map<String, Object> result = query(session, draw, start, length);
			return result;
		} finally {
			session.close();
		}
	}

	@RequestMapping("/getUnhandledAlerts")
	@ResponseBody
	public Object getUnhandledAlerts() {
		if (!validateUser()) {
			return "";
		}
		return service.getUnhandledAlerts(lg);
	}

	@RequestMapping("/deleteAlert")
	public String deleteAlert(Model model, String id, int mode, RedirectAttributes attr) {

		Utils.saveLog(lg.getUserId(), "关闭报警", lg.getDomainId());

		if (!validateUser()) {
			return "view.login";
		}

		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();

			Date now = new Date();
			if (!StringUtils.isEmpty(id)) {
				NDAAlert alert = (NDAAlert) db.get(NDAAlert.class, Integer.parseInt(id));
				if (alert != null) {
					if (mode == 1) {
						alert.setLastModitied(now);
						attr.addFlashAttribute("message", "报警关闭成功");
						alert.setStatus(Constants.AlertState.STATE_FINISHED);
					} else {
						alert.setLastModitied(now);
						attr.addFlashAttribute("message", "报警关闭成功");
						alert.setStatus(Constants.State.STATE_FINISHED);
					}
				}
			}

			db.commit();

		} finally {
			db.close();
		}
		attr.addFlashAttribute("error", true);
		return "redirect:/alertHistory";
	}

	@RequestMapping("/deleteAlertsByExpressId")
	public String deleteAlertsByExpressId(Model model, String id, int mode, RedirectAttributes attr) {

		Utils.saveLog(lg.getUserId(), "关闭订单号为" + id + "的所有报警", lg.getDomainId());
		if (!validateUser()) {
			return "view.login";
		}
		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();

			Date now = new Date();
			if (!StringUtils.isEmpty(id)) {
				List<NDAAlert> list = DBUtils.getUnhandledAlertsByExpressId(db, Integer.parseInt(id));
				for (int i = 0; i < list.size(); i++) {
					list.get(i).setLastModitied(now);
					list.get(i).setStatus(Constants.State.STATE_FINISHED);
				}
				attr.addFlashAttribute("message", "报警关闭成功");
			}
			db.commit();
		} finally {
			db.close();
		}
		attr.addFlashAttribute("error", true);
		return "redirect:/alertsNow";
	}

	private Map<String, Object> query(DbSession db, int draw, int start, int length) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, String[]> properties = request.getParameterMap();

		String orderSql = "";

		String search = properties.get("search[value]")[0];

		int roleId = lg.getDefRole().getRoleId();

		long recordsFiltered = 0;
		/*
		 * long recordsTotal = count(db, roleId == Constants.Role.ADMIN ?
		 * "domain_id=" + lg.getDomainId() :
		 * " tag_no IN (SELECT tag_no FROM nda_tag_express WHERE express_id IN (SELECT express_id FROM "
		 * + "nda_user_express WHERE user_id=" + lg.getUserId() + "))");
		 */
		// String sql = "SELECT * FROM nda_alert WHERE express_id IN (" +
		// " SELECT express_id FROM nda_user_express WHERE user_id=" + userId +
		// ") AND csn>" + csn + " AND status=" +
		// Constants.AlertState.STATE_ACTIVE;

		long recordsTotal = count(db, roleId == Constants.Role.ADMIN ? "domain_id=" + lg.getDomainId()
				: " express_id IN (SELECT express_id FROM nda_user_express WHERE user_id=" + lg.getUserId() + ")");

		result.put("recordsTotal", recordsTotal);

		String whereClause = "";

		if (!StringUtils.isEmpty(search)) {

			whereClause = " (a.tag_no LIKE '%" + search + "%') ";
		}

		String status = properties.get("columns[6][search][value]")[0];
		if (!StringUtils.isEmpty(status)) {
			if (!StringUtils.isEmpty(whereClause)) {
				whereClause += " AND ";
			}
			whereClause += " a.status=" + status + " ";
		}

		String level = properties.get("columns[1][search][value]")[0];
		if (!StringUtils.isEmpty(level)) {
			if (!StringUtils.isEmpty(whereClause)) {
				whereClause += " AND ";
			}
			whereClause += " a.nda_alert_level_id=" + level + " ";
		}

		if (!StringUtils.isEmpty(whereClause)) {
			whereClause += " AND ";
		}

		whereClause += (roleId == Constants.Role.ADMIN ? "domain_id=" + lg.getDomainId()
				: "express_id IN (SELECT express_id FROM nda_user_express WHERE user_id=" + lg.getUserId() + ")");

		if (!StringUtils.isEmpty(search) || !StringUtils.isEmpty(status)) {
			recordsFiltered = count(db, whereClause);
		} else {
			recordsFiltered = recordsTotal;
		}
		result.put("recordsFiltered", recordsFiltered);

		result.put("data", query(db, whereClause, orderSql, start, length));
		return result;
	}

	private int count(DbSession session, String where) {

		String sql = "SELECT COUNT(a.*) FROM nda_alert a ";

		if (!StringUtils.isEmpty(where)) {
			sql += (" WHERE " + where);
		}

		SQLQuery query = session.createSQLQuery(sql);
		BigInteger count = (BigInteger) query.uniqueResult();
		return count == null ? 0 : count.intValue();
	}

	private List<NDAAlert> query(DbSession session, String where, String order, int offset, int limit) {

		String sql = "SELECT * FROM nda_alert a ";
		if (!StringUtils.isEmpty(where)) {
			sql += (" WHERE " + where);
		}

		if (!StringUtils.isEmpty(order)) {
			sql += (" " + order);
		}

		SQLQuery query = session.createSQLQuery(sql);
		query.addEntity(NDAAlert.class);
		BaseHibernateUtils.setLimit(query, offset, limit);
		List<?> list = query.list();
		if (!list.isEmpty()) {
			List<NDAAlert> result = new ArrayList<NDAAlert>(list.size());
			for (Object obj : list) {
				NDAAlert e = (NDAAlert) obj;
				if (e.getStatus() == Constants.AlertState.STATE_ACTIVE) {

					e.setStatusName("未处理");
				} else {
					e.setStatusName("已处理");
				}

				if (e.getType() == Constants.AlertType.STATE_TEMPHISALERT) {
					if (e.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_HIGH) {
						e.setAlertName("温度过高");
					} else if (e.getAlertLevel() == Constants.AlertLevel.STATE_NOT_RESPONSE) {
						e.setAlertName("模块失联");
					} else if (e.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_LOW) {
						e.setAlertName("温度过低");
					} else {
						e.setAlertName("严重报警");
					}
				}
				if (e.getType() == Constants.AlertType.STATE_ELECTRICITY) {
					if (e.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_LOW) {
						e.setAlertName("电量不足百分之60");
					} else if (e.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_HIGH) {
						e.setAlertName("电量不足百分之40");
					} else if (e.getAlertLevel() == Constants.AlertLevel.STATE_SERIOUS) {
						e.setAlertName("电量不足百分之20,请尽快充电");
					}
				}

				e.setTime(Utils.SF.format(e.getCreationTime()));

				NDAExpress express = DBUtils.getNDAExpress(session, e.getExpressId());
				if (express != null) {
					e.setExpress(express.getExpressNo());
				}

				NDAUserExpress ue = DBUtils.getUserByExpressId(session, e.getExpressId());
				if (ue != null) {
					NDAUser user = (NDAUser) session.get(NDAUser.class, ue.getUserId());
					if (user != null) {
						e.setUserName(user.getNickName());
						e.setMobile(user.getMobile());
					}
				}
				result.add(e);
			}
			return result;
		}
		return Collections.<NDAAlert>emptyList();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	/// 从Levelcontroller转过来
	@RequestMapping("/deleteAlertLevel")
	public String deleteAlertLevel(Model model, String id, int mode, RedirectAttributes attr) {
		if (!validateUser()) {
			return "view.login";
		}

		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();

			Date now = new Date();
			if (!StringUtils.isEmpty(id)) {
				NDAAlertLevel user = (NDAAlertLevel) db.get(NDAAlertLevel.class, Integer.parseInt(id));
				if (user != null) {
					if (mode == 1) {
						user.setLastModified(now);
						attr.addFlashAttribute("message", "报警级别关闭成功");
						user.setStatus(Constants.State.STATE_DISABLED);
						Utils.saveLog(lg.getUserId(), "关闭报警级别", lg.getDomainId());

					} else {
						user.setLastModified(now);
						attr.addFlashAttribute("message", "报警级别启用成功");
						user.setStatus(Constants.State.STATE_ACTIVE);
						Utils.saveLog(lg.getUserId(), "启用报警级别", lg.getDomainId());

					}
				}
			}

			db.commit();

		} finally {
			db.close();
		}

		attr.addFlashAttribute("error", true);

		return "redirect:/level";
	}

	@RequestMapping("/level")
	public String level(Model model) {
		Utils.saveLog(lg.getUserId(), "查看报警级别列表", lg.getDomainId());
		if (!validateUser()) {
			return "redirect:/";
		}

		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());

		return "view.alert.level";
	}

	@RequestMapping("/editLevel")
	public ModelAndView editLevel(Model model, String id) {

		if (!validateUser()) {
			return new ModelAndView("redirect:/");
		}

		NDAAlertLevel level = null;

		if (!StringUtils.isEmpty(id)) {
			model.addAttribute("id", id);
			DbSession session = BaseHibernateUtils.newSession();
			try {
				level = (NDAAlertLevel) session.get(NDAAlertLevel.class, Integer.parseInt(id));
			} finally {
				session.close();
			}
		} else {
			level = new NDAAlertLevel();
		}

		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());
		return new ModelAndView("view.alert.editLevel", "command", level);
	}

	@RequestMapping("/saveLevel")
	public String saveLevel(Model model, String id, String hours, int times, RedirectAttributes attr) {

		if (!validateUser()) {
			return "view.login";
		}

		float hour = 0;

		try {
			hour = Float.parseFloat(hours);
		} catch (Exception e) {
		}

		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();

			Date now = new Date();
			if (!StringUtils.isEmpty(id)) {
				NDAAlertLevel level = (NDAAlertLevel) db.get(NDAAlertLevel.class, Integer.parseInt(id));
				if (level != null) {
					level.setHours(hour);
					level.setTimes(times);
					level.setLastModified(now);
				}
			}

			db.commit();

		} finally {
			db.close();
		}

		attr.addFlashAttribute("error", true);
		attr.addFlashAttribute("message", "报警级别设置成功");
		Utils.saveLog(lg.getUserId(), "设置报警级别", lg.getDomainId());
		return "redirect:/level";
	}

	@RequestMapping("/ajaxLevel")
	@ResponseBody
	public Object ajaxLevel(int draw, int start, int length) {
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
		long recordsTotal = countLevel(db, null);
		result.put("recordsTotal", recordsTotal);

		String whereClause = "a.domain_id=" + lg.getDomainId();

		if (!StringUtils.isEmpty(search)) {

			whereClause += " AND (a.express_no LIKE '%" + search + "%') ";
		}

		if (!StringUtils.isEmpty(whereClause)) {

			whereClause += " AND a.domain_id=" + lg.getDomainId();
		}

		if (!StringUtils.isEmpty(search)) {
			recordsFiltered = count(db, whereClause);
		} else {
			recordsFiltered = recordsTotal;
		}
		result.put("recordsFiltered", recordsFiltered);

		result.put("data", ajaxLevel(db, whereClause, orderSql, start, length));
		return result;

	}

	private int countLevel(DbSession session, String where) {

		String sql = "SELECT COUNT(a.*) FROM nda_alert_level a ";

		if (!StringUtils.isEmpty(where)) {
			sql += (" WHERE " + where);
		}

		SQLQuery query = session.createSQLQuery(sql);
		BigInteger count = (BigInteger) query.uniqueResult();
		return count == null ? 0 : count.intValue();
	}

	private List<NDAAlertLevel> ajaxLevel(DbSession session, String where, String order, int offset, int limit) {

		String sql = "SELECT * FROM nda_alert_level a ";
		if (!StringUtils.isEmpty(where)) {
			sql += (" WHERE " + where);
		}

		if (!StringUtils.isEmpty(order)) {
			sql += (" " + order);
		}

		SQLQuery query = session.createSQLQuery(sql);
		query.addEntity(NDAAlertLevel.class);
		BaseHibernateUtils.setLimit(query, offset, limit);
		List<?> list = query.list();
		if (!list.isEmpty()) {
			List<NDAAlertLevel> result = new ArrayList<NDAAlertLevel>(list.size());
			for (Object obj : list) {
				NDAAlertLevel e = (NDAAlertLevel) obj;

				e.setStatusKey(e.getStatus() == Constants.State.STATE_ACTIVE ? "启用" : "关闭");
				result.add(e);
			}
			return result;
		}

		return Collections.<NDAAlertLevel>emptyList();
	}

}
