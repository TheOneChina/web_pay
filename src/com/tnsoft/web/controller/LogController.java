/*
 * Copyright (c) 2016 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * 
 */
package com.tnsoft.web.controller;

import com.expertise.common.util.StringUtils;

import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAAlertLevel;

import com.tnsoft.hibernate.model.NDALog;

import com.tnsoft.web.util.Utils;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LogController extends BaseController {

	public LogController() {
		super();
	}

	@RequestMapping("/logs")
	public String logs(Model model) {

		if (!validateUser()) {
			return "redirect:/";
		}
		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());

		return "view.log.logs";
	}

	@RequestMapping("/ajaxLogs")
	@ResponseBody
	public Object ajaxLogs(int draw, int start, int length) {
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

		String orderSql = " ORDER BY a.operation_time DESC";

		String search = properties.get("search[value]")[0];

		String whereClause = "";
		
		if (lg.getDefRole().getRoleId() == Constants.Role.SUPER_ADMIN) {
			whereClause += " a.domain_id=" + lg.getDomainId();
		} else {
			whereClause = " a.user_id=" + lg.getUserId() + "";
		}
		
		long recordsFiltered = 0;
		long recordsTotal = count(db, whereClause);
		result.put("recordsTotal", recordsTotal);
		
		if (!StringUtils.isEmpty(search)) {
			whereClause = " (a.operation LIKE '%" + search + "%') ";
		}

		if (!StringUtils.isEmpty(search)) {
			recordsFiltered = count(db, whereClause);
		} else {
			recordsFiltered = recordsTotal;
		}
		result.put("recordsFiltered", recordsFiltered);
		result.put("data", query(db, whereClause, orderSql, start, length));
		return result;

	}

	private int count(DbSession session, String where) {
		String sql = "SELECT COUNT(a.*) FROM nda_log a ";
		if (!StringUtils.isEmpty(where)) {
			sql += (" WHERE " + where);
		}

		SQLQuery query = session.createSQLQuery(sql);
		BigInteger count = (BigInteger) query.uniqueResult();
		return count == null ? 0 : count.intValue();
	}

	private List<NDALog> query(DbSession session, String where, String order, int offset, int limit) {

		String sql = "SELECT * FROM nda_log a ";
		if (!StringUtils.isEmpty(where)) {
			sql += (" WHERE " + where);
		}

		if (!StringUtils.isEmpty(order)) {
			sql += (" " + order);
		}

		SQLQuery query = session.createSQLQuery(sql);
		query.addEntity(NDALog.class);
		BaseHibernateUtils.setLimit(query, offset, limit);
		List<?> list = query.list();
		if (!list.isEmpty()) {
			List<NDALog> result = new ArrayList<NDALog>(list.size());
			for (Object obj : list) {
				NDALog e = (NDALog) obj;
				e.setTime(Utils.SF.format(e.getOperationTime()));
				result.add(e);
			}
			return result;
		}

		return Collections.<NDALog>emptyList();
	}

}
