/*
 * Copyright (c) 2016 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * 
 */
package com.tnsoft.web.controller;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.SQLQuery;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDATag;
import com.tnsoft.hibernate.model.NDATagExpress;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.TagService;
import com.tnsoft.web.util.Utils;

@Controller
public class TagHistoryController extends BaseController {

	@Resource(name = "tagService")
	private TagService tagService;

	public TagHistoryController() {
		super();
	}

	@RequestMapping("/ajaxTagHistory")
	@ResponseBody
	public Object ajaxTagHistory(int draw, int start, int length) {
		Utils.saveLog(lg.getUserId(), "查看模块历史记录", lg.getDomainId());
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
		String whereClause = "a.express_id IN (SELECT b.id WHERE b.domain_id=" + lg.getDomainId() + ")";
		long recordsTotal = count(db, whereClause);
		result.put("recordsTotal", recordsTotal);

		if (!StringUtils.isEmpty(search)) {
			whereClause += " AND (a.tag_no LIKE '%" + search
					+ "%' OR a.express_id IN (SELECT b.id WHERE b.express_no LIKE '%" + search + "%')) ";
		}

		String status = properties.get("columns[3][search][value]")[0];
		if (!StringUtils.isEmpty(status)) {
			if (!StringUtils.isEmpty(whereClause)) {
				whereClause += " AND ";
			}
			whereClause += " a.status=" + status + " ";
		}

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

		String sql = "SELECT COUNT(a.*) FROM nda_tag_express a, nda_express b ";

		if (!StringUtils.isEmpty(where)) {
			sql += (" WHERE " + where);
		}

		SQLQuery query = session.createSQLQuery(sql);
		BigInteger count = (BigInteger) query.uniqueResult();
		return count == null ? 0 : count.intValue();
	}

	private List<NDATagExpress> query(DbSession session, String where, String order, int offset, int limit) {

		String sql = "SELECT a.* FROM nda_tag_express a, nda_express b ";
		if (!StringUtils.isEmpty(where)) {
			sql += (" WHERE " + where);
		}

		if (!StringUtils.isEmpty(order)) {
			sql += (" " + order);
		}

		SQLQuery query = session.createSQLQuery(sql);
		query.addEntity(NDATagExpress.class);
		BaseHibernateUtils.setLimit(query, offset, limit);
		List<?> list = query.list();
		if (!list.isEmpty()) {
			List<NDATagExpress> result = new ArrayList<NDATagExpress>(list.size());
			for (Object obj : list) {
				NDATagExpress e = (NDATagExpress) obj;
				if (e.getStatus() == Constants.BindState.STATE_ACTIVE) {
					e.setStatusName("绑定中");
				} else {
					e.setStatusName("已解绑");
				}
				NDAExpress express = (NDAExpress) session.get(NDAExpress.class, e.getExpressId());
				if (express != null) {
					e.setExpressNo(express.getExpressNo());
					e.setExpressStatus(express.getStatus());
					
					e.setCheckOutTime(express.getCheckOutTime());
				}
				NDATag tag = tagService.getById(e.getTagNo());
				e.setTagStatus(tag.getStatus());
				result.add(e);
			}
			return result;
		}
		return Collections.<NDATagExpress>emptyList();
	}

	@RequestMapping("/getTagExpressHistory")
	@ResponseBody
	public Response getTagExpressHistory(String tagNo) {
		Response res = new Response();
		List<NDAExpress> list = tagService.getTagExpressHistory(tagNo);
		if (list.size() > 0) {
			res.setCode(Response.OK);
		} else {
			res.setCode(Response.ERROR);
		}
		res.setExpress(list);
		return res;
	}
}
