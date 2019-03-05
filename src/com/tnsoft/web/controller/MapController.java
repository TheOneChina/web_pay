/*
 * Copyright (c) 2016 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * 
 */
package com.tnsoft.web.controller;

import com.expertise.common.logging.Logger;
import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDALocateExpress;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MapController extends BaseController {

	public MapController() {
		super();
	}

	@RequestMapping("/map")
	public String map(Model model, String id) {
		Utils.saveLog(lg.getUserId(), "查看订单位置信息", lg.getDomainId());
		if (!validateUser()) {
			return "redirect:/";
		}

		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());

		return "view.map.map";
	}

	@RequestMapping("/ajaxLocation")
	@ResponseBody
	public Object ajaxLocation(String id, String model) {
		System.out.println(model);
		if (!StringUtils.isEmpty(id)) {
			DbSession db = BaseHibernateUtils.newSession();
			try {
				NDAExpress express = DBUtils.getNDAExpress(db, id, lg.getDomainId());// (NDAExpress)db.get(NDAExpress.class,
				// Integer.parseInt(arg0));
				if (express != null) {
					if (model != null) {
						//获取操作点位置
						Criteria criteria = db.createCriteria(NDALocateExpress.class);
						criteria.add(Restrictions.eq("expressId", express.getId()));
						criteria.addOrder(Order.asc("creationTime"));
						//
						
						List<?> list = criteria.list();
						List<NDALocateExpress> result = new ArrayList<NDALocateExpress>(list.size());
						if (!list.isEmpty()) {
							for (Object obj : list) {
								NDALocateExpress l = (NDALocateExpress) obj;
								l.setTime(Utils.SF.format(l.getCreationTime()));
								result.add(l);
							}
						}
						return result;
					
						
					} else {
						Criteria criteria = db.createCriteria(NDALocateExpress.class);
						criteria.add(Restrictions.eq("expressId", express.getId()));
						criteria.addOrder(Order.asc("creationTime"));
						List<?> list = criteria.list();
						List<NDALocateExpress> result = new ArrayList<NDALocateExpress>(list.size());
						if (!list.isEmpty()) {
							for (Object obj : list) {
								NDALocateExpress l = (NDALocateExpress) obj;
								l.setTime(Utils.SF.format(l.getCreationTime()));
								result.add(l);
							}
						}
						return result;
					}

				}
			} catch (Exception e) {
				Logger.error(e);
			} finally {
				db.close();
			}
		}
		return null;
	}
}
