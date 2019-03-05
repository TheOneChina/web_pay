package com.tnsoft.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.expertise.common.logging.Logger;
import com.expertise.common.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDATag;
import com.tnsoft.hibernate.model.NDATagExpress;
import com.tnsoft.hibernate.model.NDATempExpress;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.hibernate.model.NDAUserExpress;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.ExpressService;
import com.tnsoft.web.service.TagService;
import com.tnsoft.web.servlet.ServletConsts;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;

@Controller
public class ExpressController extends BaseController {

	@Resource(name = "expressService")
	private ExpressService expressService;

	@Resource(name = "tagService")
	private TagService tagService;

	public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();

	public ExpressController() {
		super();
	}

	@RequestMapping("/expressTemperatureHis")
	public String expressTemperatureHis(Model model, String id) {
		if (!validateUser()) {
			return "redirect:/";
		}
		model.addAttribute("expressNo", id);
		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());

		return "view.express.temperatureHistory";
	}
	
	@RequestMapping("/takingExpress")
	public String takingExpress(Model model) {
		Utils.saveLog(lg.getUserId(), "揽收货物", lg.getDomainId());
		if (!validateUser()) {
			return "redirect:/";
		}
		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());

		return "view.express.taking";
	}

	@RequestMapping(value = "/saveSignExpress") // 执行签收动作,注意接收Integer数组
	@ResponseBody
	public Response saveSignExpress(String[] expressNoList, String userId, RedirectAttributes attr) {
		Utils.saveLog(lg.getUserId(), "签收货物", lg.getDomainId());
		Response res;
		if (!validateUser()) {
			res = new Response(Response.ERROR);
			res.setMessage("签收失败！");
			return res;
		}
		res = expressService.signExpress(expressNoList, lg);
		attr.addFlashAttribute("username", lg.getUserName());
		attr.addFlashAttribute("rolename", lg.getDefRole().getRoleName());
		attr.addFlashAttribute("error", true);
		attr.addFlashAttribute("message", res.getMessage());
		return res;
	}

	@RequestMapping("/toWindowExpress")
	public String toWindowExpress(String[] expressNoList, Model model) {
		if (!validateUser()) {
			return "redirect:/";
		}
		// 传过来的是list
		session.setAttribute("expressNoList", expressNoList);
		return "view.express.windowExpress";
	}

	@RequestMapping("/windowExpress")
	@ResponseBody
	public Object windowExpress(HttpSession session, RedirectAttributes attr) {
		if (!validateUser()) {
			return "redirect:/";
		}
		String[] expressNoList = (String[]) session.getAttribute("expressNoList");
		// 返回各个视窗的温湿度曲线
		return expressNoList;
	}
	
	@RequestMapping("/windowTemHum")
	public String temHum(Model model, String expressNo) {
		if (!validateUser()) {
			return "redirect:/";
		}
		model.addAttribute("expressNo", expressNo);
		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());

		return "view.temHum";
	}
	
	@RequestMapping("/exchangerExpress")
	public String exchangerExpress(Model model) {

		Utils.saveLog(lg.getUserId(), "转运货物", lg.getDomainId());
		if (!validateUser()) {
			return "redirect:/";
		}
		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());

		return "view.express.exchanger";
	}

	@RequestMapping("/expressTemperature")
	public ModelAndView expressTemperature(Model model, String id) {
		// 这里id是expressNo
		Utils.saveLog(lg.getUserId(), "查看订单温度", lg.getDomainId());

		if (!validateUser()) {
			return new ModelAndView("redirect:/");
		}

		NDAExpress express = null;

		if (!StringUtils.isEmpty(id)) {
			model.addAttribute("id", id);
			DbSession db = BaseHibernateUtils.newSession();
			try {
				express = DBUtils.getNDAExpress(db, id, lg.getDomainId());
				// (NDAExpress)db.get(NDAExpress.class, id);
			} finally {
				db.close();
			}
		} else {
			express = new NDAExpress();
		}

		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());
		return new ModelAndView("view.express.temperature", "command", express);
	}

	@RequestMapping("/saveExpressTemperature")
	public String saveExpressTemperature(Model model, String id, String temperatureMin, String temperatureMax,
			RedirectAttributes attr) {
		Utils.saveLog(lg.getUserId(), "设置订单温度阀值，低温：" + temperatureMin + " 高温:" + temperatureMax, lg.getDomainId());

		if (!validateUser()) {
			return "view.login";
		}
		Float lowValue = null;
		Float highValue = null;
		try {
			if (!StringUtils.isEmpty(temperatureMin)) {
				lowValue = Float.parseFloat(temperatureMin);
			}
		} catch (Exception e) {
		}
		try {
			if (!StringUtils.isEmpty(temperatureMax)) {
				highValue = Float.parseFloat(temperatureMax);
			}
		} catch (Exception e) {
		}
		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();

			Date now = new Date();
			if (!StringUtils.isEmpty(id)) {
				NDAExpress express = (NDAExpress) db.get(NDAExpress.class, Integer.parseInt(id));
				if (express != null) {
					express.setLastModitied(now);

					if (lowValue != null) {
						express.setTemperatureMin(lowValue);
					}
					if (highValue != null) {
						express.setTemperatureMax(highValue);
					}
				}
			}
			db.commit();
		} finally {
			db.close();
		}

		attr.addFlashAttribute("error", true);
		attr.addFlashAttribute("message", "订单温度设置成功");
		return "redirect:/express";
	}

	@RequestMapping("/express")
	public String express(Model model) {

		if (!validateUser()) {
			return "redirect:/";
		}

		Utils.saveLog(lg.getUserId(), "查看当前订单列表", lg.getDomainId());

		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());

		return "view.express.express";
	}

	@RequestMapping("/history")
	public String historyExpress(Model model) {
		if (!validateUser()) {
			return "redirect:/";
		}
		Utils.saveLog(lg.getUserId(), "查看历史订单列表", lg.getDomainId());

		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());

		return "view.express.history";
	}

	// 2017-03-31
	@RequestMapping("/getExpressBreifInfo")
	@ResponseBody
	public void getExpressBreifInfo(String expressNo, HttpServletResponse resp) throws IOException {

		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		Map<String, String> result = new HashMap<String, String>();
		if (!validateUser() || StringUtils.isEmpty(expressNo)) {
			return;
		}
		DbSession dbSession = BaseHibernateUtils.newSession();
		try {
			NDAExpress express = DBUtils.getNDAExpress(dbSession, expressNo, lg.getDomainId());
			NDATagExpress tagExpress = DBUtils.getNDATagExpressIgnoreStatus(dbSession, express.getId()).get(0);
			List<NDAAlert> alerts = DBUtils.getAllAlertsByExpressId(dbSession, express.getId());
			List<NDATempExpress> ndaTempExpresses = DBUtils.getAllTempesByExpressId(dbSession, express.getId());

			NDATag tag = null;
			float realMaxTemp = 0;
			float realMinTemp = 0;
			float realAveTemp = 0;
			float realMaxHumidity = 0;
			float realMinHumidity = 0;
			float realAveHumidity = 0;
			if (ndaTempExpresses.size() > 0) {

				realMaxTemp = ndaTempExpresses.get(0).getTemperature();
				realMinTemp = ndaTempExpresses.get(0).getTemperature();

				realMaxHumidity = ndaTempExpresses.get(0).getHumidity();
				realMinHumidity = ndaTempExpresses.get(0).getHumidity();

				for (NDATempExpress ndaTempExpress : ndaTempExpresses) {
					if (realMaxTemp < ndaTempExpress.getTemperature()) {
						realMaxTemp = ndaTempExpress.getTemperature();
					}
					if (realMinTemp > ndaTempExpress.getTemperature()) {
						realMinTemp = ndaTempExpress.getTemperature();
					}
					if (realMaxHumidity < ndaTempExpress.getHumidity()) {
						realMaxHumidity = ndaTempExpress.getHumidity();
					}
					if (realMinHumidity > ndaTempExpress.getHumidity()) {
						realMinHumidity = ndaTempExpress.getHumidity();
					}
					realAveTemp += (ndaTempExpress.getTemperature() / ndaTempExpresses.size());
					realAveHumidity += (ndaTempExpress.getHumidity() / ndaTempExpresses.size());
				}
			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (tagExpress == null) {
				result.put("tag", "当前未绑定模块");
			} else {
				result.put("tag", tagExpress.getTagNo());
				tag = DBUtils.getTagByTagNo(dbSession, tagExpress.getTagNo());
			}

			result.put("alertCount", alerts.size() + "");

			result.put("electricity", tag.getElectricity() + "");

			if (express.getCreationTime() != null) {
				result.put("expressStartTime", dateFormat.format(express.getCreationTime()));

			} else {
				result.put("expressStartTime", "无");
			}

			if (express.getCheckOutTime() != null) {
				result.put("expressEndTime", dateFormat.format(express.getCheckOutTime()));
			} else {
				result.put("expressEndTime", "无");
			}

			if (express.getStatus() == Constants.ExpressState.STATE_PENDING) {
				result.put("expressState", "待配送");
			} else if (express.getStatus() == Constants.ExpressState.STATE_ACTIVE) {
				result.put("expressState", "配送中");
			} else if (express.getStatus() == Constants.ExpressState.STATE_FINISHED) {
				result.put("expressState", "已签收");
			} else {
				result.put("expressState", "未知");
			}

			if (express.getTemperatureMax() != null) {
				result.put("maxAlertTemperature", express.getTemperatureMax() + "");
			} else {
				if (tag != null && tag.getTemperatureMax() != null) {
					result.put("maxAlertTemperature", tag.getTemperatureMax() + "");
				} else {
					result.put("maxAlertTemperature", "无");
				}
			}

			if (express.getTemperatureMin() != null) {
				result.put("minAlertTemperature", express.getTemperatureMin() + "");
			} else {
				if (tag != null && tag.getTemperatureMin() != null) {
					result.put("minAlertTemperature", tag.getTemperatureMin() + "");
				} else {
					result.put("minAlertTemperature", "无");
				}
			}

			result.put("realMaxTemp", realMaxTemp + "℃");
			result.put("realMinTemp", realMinTemp + "℃");
			result.put("realAveTemp", String.format("%.2f℃", realAveTemp));

			result.put("realMaxHumidity", realMaxHumidity + "%");
			result.put("realMinHumidity", realMinHumidity + "%");
			result.put("realAveHumidity", String.format("%.2f", realAveHumidity) + "%");

			if (ndaTempExpresses.size() > 0) {
				if (ndaTempExpresses.get(ndaTempExpresses.size() - 1).getCreationTime() != null) {
					result.put("nowTime",
							dateFormat.format(ndaTempExpresses.get(ndaTempExpresses.size() - 1).getCreationTime())
									+ "");
					result.put("nowTemp", ndaTempExpresses.get(ndaTempExpresses.size() - 1).getTemperature() + "℃");
					result.put("nowHumidity", ndaTempExpresses.get(ndaTempExpresses.size() - 1).getHumidity() + "%");
				}
			} else {
				result.put("nowTime", "无");
				result.put("nowTemp", "无");
				result.put("nowHumidity", "无");
			}

			if (express.getDescription() != null) {
				result.put("expressDescription", express.getDescription());
			} else {
				result.put("expressDescription", "");
			}
		} finally {
			dbSession.close();
		}
		out.write(GSON.toJson(result));
	}

	@RequestMapping("/saveTakingExpress")
	public String saveTakingExpress(String expressNo, String tagNo, String description, Integer appointStart,
			Integer appointEnd, RedirectAttributes attr) {
		Utils.saveLog(lg.getUserId(), "揽收订单:" + expressNo, lg.getDomainId());
		if (!validateUser()) {
			return "redirect:/";
		}
		Response res = expressService.saveTakingExpress(expressNo, tagNo, description, appointStart, appointEnd, lg);

		attr.addFlashAttribute("username", lg.getUserName());
		attr.addFlashAttribute("rolename", lg.getDefRole().getRoleName());
		attr.addFlashAttribute("error", true);
		attr.addFlashAttribute("message", res.getMessage());

		return "redirect:/takingExpress";
	}

	// 转运订单处理//////////////////////////////
	@RequestMapping("/saveExchangeExpress")
	public String saveExchangeExpress(Model model, String expressNo, RedirectAttributes attr) {
		Utils.saveLog(lg.getUserId(), "转运订单:" + expressNo, lg.getDomainId());
		if (!validateUser()) {
			return "redirect:/";
		}
		if (null != expressNo) {
			DbSession db = BaseHibernateUtils.newSession();
			// 根据扫描的订单码获得expressId.
			Integer expressId = (Integer) db
					.createQuery("select id from NDAExpress where expressNo=:expressNo and domainId=:domainId")
					.setParameter("expressNo", expressNo).setParameter("domainId", lg.getDomainId()).uniqueResult();
			// 获得当前订单
			NDAExpress express = DBUtils.getNDAExpress(db, expressNo, lg.getDomainId());
			// 获得当前登录用户与订单的关系
			NDAUserExpress userExpress = DBUtils.getNDAUserExpressByExpressIdAndUserId(db, expressId, lg.getUserId());
			try {
				db.beginTransaction();
				Date now = new Date();
				if (express == null) {
					attr.addFlashAttribute("message", "该订单未揽收");
				} else if (express != null && express.getStatus() == Constants.ExpressState.STATE_FINISHED) {
					attr.addFlashAttribute("message", "订单已经签收");
				} else {
					if (userExpress != null) {
						if (express.getStatus() == Constants.ExpressState.STATE_PENDING) {
							// a承运
							userExpress.setStatus(Constants.State.STATE_ACTIVE);
							userExpress.setLastModitied(new Date());
							express.setStatus(Constants.ExpressState.STATE_ACTIVE);
							express.setLastModitied(new Date());
							express.setCheckInTime(new Date());
							attr.addFlashAttribute("message", "订单承运成功");
							db.flush();
						} else if (express.getStatus() == Constants.ExpressState.STATE_ACTIVE) {
							// a又一次承运
							userExpress.setStatus(Constants.State.STATE_ACTIVE);
							userExpress.setLastModitied(new Date());
							express.setStatus(Constants.ExpressState.STATE_ACTIVE);
							express.setLastModitied(new Date());
							express.setCheckInTime(new Date());
							attr.addFlashAttribute("message", "再次承运订单");
							db.flush();
						}
					} else if (userExpress == null) {
						// 新建登录用户订单关系,其他用户订单关系一律全为finished
						// 第一步根据epxressId获得所有更此订单的所有userExpress,状态全置为finished
						List<NDAUserExpress> list = DBUtils.getNDAUserExpressByExpressId(db, expressId,
								lg.getDomainId());
						for (NDAUserExpress ue : list) {
							ue.setStatus(Constants.State.STATE_FINISHED);
						}
						db.flush();
						// 第二部新增登录用户订单关系
						NDAUserExpress userExpress1 = new NDAUserExpress();
						userExpress1.setExpressId(express.getId());
						userExpress1.setUserId(lg.getUserId());
						userExpress1.setDomainId(lg.getDomainId());
						userExpress1.setLastModitied(now);
						userExpress1.setCreationTime(now);
						userExpress1.setStatus(Constants.State.STATE_ACTIVE);
						// 为了防止出bug这里再一次将订单状态更新了一遍
						express.setStatus(Constants.ExpressState.STATE_ACTIVE);
						express.setCheckInTime(new Date());
						express.setLastModitied(new Date());
						db.save(userExpress1);
						db.save(express);
						db.flush();
						attr.addFlashAttribute("message", "订单承运成功");
					}
				}
			} catch (Exception e) {
				Logger.error(e);
			} finally {
				db.commit();
				db.close();
			}
		}

		attr.addFlashAttribute("username", lg.getUserName());
		attr.addFlashAttribute("rolename", lg.getDefRole().getRoleName());
		attr.addFlashAttribute("error", true);
		return "redirect:/exchangerExpress";
	}

	@RequestMapping("/ajaxExpress")
	@ResponseBody
	public Object ajaxExpress(int draw, int start, int length) throws UnsupportedEncodingException {

		if (!validateUser()) {
			return DBUtils.getEmpty();
		}

		DbSession session = BaseHibernateUtils.newSession();
		try {
			Map<String, Object> result = query(session, draw, start, length, " order by id ASC ", false);

			return result;
		} finally {
			session.close();
		}
	}

	@RequestMapping("/expressHistory")
	@ResponseBody
	public Object expressHistory(int draw, int start, int length) throws UnsupportedEncodingException {
		if (!validateUser()) {
			return DBUtils.getEmpty();
		}

		DbSession session = BaseHibernateUtils.newSession();
		try {
			Map<String, Object> result = query(session, draw, start, length, " order by id ASC ", true);
			return result;
		} finally {
			session.close();
		}
	}

	// 第六个参数isHistory表示是否为历史订单
	private Map<String, Object> query(DbSession db, int draw, int start, int length, String defaultOrderBy,
			Boolean isHistory) throws UnsupportedEncodingException {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, String[]> properties = request.getParameterMap();

		String orderSql = "";
		String orderColumn = properties.get("order[0][column]")[0];
		String orderDir = properties.get("order[0][dir]")[0];
		// 点击每列头上的小箭头排序
		switch (orderColumn) {
		case "1":
			orderSql += " order by a.id " + orderDir;
			break;
		case "5":
			orderSql += " order by b.user_id " + orderDir;
			break;
		case "6":
			orderSql += " order by a.checkin_time " + orderDir;
			break;
		}

		String search = new String(properties.get("search[value]")[0].getBytes("ISO-8859-1"), "UTF-8");

		String whereClause = "";
		if (lg.getDefRole().getRoleId() == Constants.Role.SUPER_ADMIN) {
			whereClause = "a.domain_id=" + lg.getDomainId();
		} else {
			whereClause = " a.id IN (SELECT express_id FROM nda_user_express WHERE user_id=" + lg.getUserId() + ")";
		}

		long recordsFiltered = 0;
		// 判断是否为历史订单,三目运算
		long recordsTotal = count(db, whereClause, isHistory);
		result.put("recordsTotal", recordsTotal);// 算出总的记录,分页用

		// String whereClause ="";
		if (!StringUtils.isEmpty(search)) {
			if (!StringUtils.isEmpty(whereClause)) {
				whereClause += " AND ";
			}
			whereClause += " (a.express_no LIKE '%" + search
					+ "%' or a.id IN (select express_id from nda_user_express where user_id in(select id from nda_user where nick_name LIKE '%"
					+ search + "%')) or a.id IN (select express_id from nda_tag_express where tag_no LIKE '%" + search
					+ "%'))";
		}

		String status = properties.get("columns[7][search][value]")[0];

		if (!StringUtils.isEmpty(status)) {
			if (!StringUtils.isEmpty(whereClause)) {
				whereClause += " AND ";
			}
			whereClause += " a.status=" + status + " ";
		}

		/*
		 * if (!StringUtils.isEmpty(whereClause)){
		 * 
		 * whereClause += " AND a.domain_id=" + lg.getDomainId(); }
		 */

		if (!StringUtils.isEmpty(search) || !StringUtils.isEmpty(status)) {
			recordsFiltered = count(db, whereClause, false);
		} else {
			recordsFiltered = recordsTotal;
		}
		result.put("recordsFiltered", recordsFiltered);
		result.put("data", query(db, whereClause, orderSql, start, length, isHistory));// 获取的数据
		return result;

	}

	private int count(DbSession session, String where, Boolean isHistory) {

		String sql = "SELECT COUNT(a.*) FROM nda_express a , nda_user_express b ";

		if (!StringUtils.isEmpty(where)) {

			sql += isHistory
					? (" WHERE a.id=b.express_id and a.status = '" + Constants.ExpressState.STATE_FINISHED + "' AND "
							+ where)
					: (" WHERE a.id=b.express_id and b.status='" + Constants.ExpressState.STATE_ACTIVE
							+ "' and a.status <>'" + Constants.ExpressState.STATE_FINISHED + "' AND " + where);
		}
		SQLQuery query = session.createSQLQuery(sql);
		BigInteger count = (BigInteger) query.uniqueResult();
		return count == null ? 0 : count.intValue();
	}

	// 此方法第六个boolean参数表示是否为历史订单
	private List<NDAExpress> query(DbSession session, String where, String order, int offset, int limit,
			Boolean isHistory) {

		String sql = "SELECT a.* FROM nda_express a ,nda_user_express b ";
		if (!StringUtils.isEmpty(where)) {
			// 三目判断是否为历史订单
			sql += isHistory
					? (" WHERE  a.id=b.express_id and b.status='" + Constants.ExpressState.STATE_FINISHED
							+ "' and a.status ='" + Constants.ExpressState.STATE_FINISHED + "' AND " + where)
					: (" WHERE a.id=b.express_id and b.status='" + Constants.ExpressState.STATE_ACTIVE
							+ "' and a.status <>'" + Constants.ExpressState.STATE_FINISHED + "' AND " + where);
		}
		if (!StringUtils.isEmpty(order)) {
			sql += (" " + order);
		}
		SQLQuery query = session.createSQLQuery(sql);
		query.addEntity(NDAExpress.class);
		BaseHibernateUtils.setLimit(query, offset, limit);
		List<NDAExpress> list = query.list();
		if (!list.isEmpty()) {
			List<NDAExpress> result = new ArrayList<NDAExpress>(list.size());
			for (Object obj : list) {
				NDAExpress e = (NDAExpress) obj;
				if (e.getStatus() == Constants.ExpressState.STATE_PENDING) {
					e.setStatusName("待配送");
				} else if (e.getStatus() == Constants.ExpressState.STATE_ACTIVE) {
					e.setStatusName("配送中");
				} else {
					e.setStatusName("已签收");
				}
				StringBuilder sb = new StringBuilder();

				List<NDATempExpress> tmp = DBUtils.getAllTempesByExpressId(session, e.getId());
				if (tmp.isEmpty()) {
					sb.append(
							"<table cellpadding=\"5\" cellspacing=\"0\" border=\"0\" style=\"padding-left:50px;\" class=\"table\">")
							.append("<tr>").append("<td colspan=\"2\">").append("暂无温度数据").append("</td>")
							.append("</tr>").append("</table>");
				} else {
					sb.append(
							"<table cellpadding=\"5\" cellspacing=\"0\" border=\"0\" style=\"padding-left:50px;\" class=\"table\">");
					sb.append("<tr>");
					sb.append("<td>");
					sb.append("当前温度(°C)");
					sb.append("</td>");
					sb.append("<td>");
					sb.append("报告时间");
					sb.append("</td>");
					sb.append("</tr>");
					for (NDATempExpress t : tmp) {
						sb.append("<tr>");
						sb.append("<td>");
						sb.append(String.format("%.2f", t.getTemperature()));
						sb.append("</td>");
						sb.append("<td>");
						sb.append(t.getCreationTime());
						sb.append("</td>");
						sb.append("</tr>");
					}
					sb.append("</table>");
				}

				e.setCheckInTimeStr(e.getCheckInTime() == null ? "" : Utils.SF.format(e.getCheckInTime()));
				e.setCheckOutTimeStr(e.getCheckOutTime() == null ? "" : Utils.SF.format(e.getCheckOutTime()));
				e.setTemps(sb.toString());
				// 获得user
				if (!isHistory) {
					NDAUser u = getUserByExpressId(session, e.getId());
					if (u != null) {
						e.setUserName(u.getNickName());
					}
				}
				result.add(e);
			}
			return result;
		}
		return Collections.<NDAExpress>emptyList();
	}

	// common
	// private List<NDATempExpress> getTemps(DbSession db, int expressId) {
	// Criteria criteria = db.createCriteria(NDATempExpress.class);
	// criteria.add(Restrictions.eq("expressId", expressId));
	// criteria.addOrder(Order.asc("creationTime"));
	// return criteria.list();
	// }

	private NDAUser getUserByExpressId(DbSession db, int expressId) {

		String hql = "SELECT a FROM NDAUser a, NDAUserExpress b WHERE a.id=b.userId AND b.expressId=:expressId AND b.status=:status";

		Query query = db.createQuery(hql).setParameter("expressId", expressId).setParameter("status",
				Constants.State.STATE_ACTIVE);
		NDAUser user = (NDAUser) query.uniqueResult();
		if (null != user) {
			return user;
		}
		return null;
	}

	@RequestMapping("/ajaxEditExSleepTime")
	@ResponseBody
	public Object ajaxEditExSleepTime(Integer expressId, String time) {
		if (!validateUser()) {
			Response res = new Response(Response.ERROR);
			res.setMessage("模块睡眠时间设置失败！");
			return res;
		}
		Response res = expressService.ajaxEditExSleepTime(expressId, time);

		Utils.saveLog(lg.getUserId(), "设置模块睡眠时间", lg.getDomainId());
		return res;
	}

	@RequestMapping("/editExpressAppointStart")
	@ResponseBody
	public Object editAppointStart(Integer expressId, String time) {
		if (!validateUser()) {
			Response res = new Response(Response.ERROR);
			res.setMessage("预约启动失败！");
			return res;
		}
		Response res = expressService.editAppointStart(expressId, time);

		Utils.saveLog(lg.getUserId(), "设置订单预约启动", lg.getDomainId());
		return res;
	}

	@RequestMapping("/editExpressAppointEnd")
	@ResponseBody
	public Object editAppointEnd(Integer expressId, String time) {
		if (!validateUser()) {
			Response res = new Response(Response.ERROR);
			res.setMessage("预约结束失败！");
			return res;
		}
		Response res = expressService.editAppointEnd(expressId, time);

		Utils.saveLog(lg.getUserId(), "设置订单预约结束", lg.getDomainId());
		return res;
	}

	@RequestMapping("/expressUsers")
	public String expressUsers(Model model, String id) {
		if (!validateUser()) {
			return "redirect:/";
		}
		Utils.saveLog(lg.getUserId(), "查看经手配送员", lg.getDomainId());

		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());
		model.addAttribute("id", id);
		return "view.express.expressUsers";
	}

	@RequestMapping("/getExpressUsers")
	@ResponseBody
	public Object getExpressUsers(Integer expressId, Model model, int draw) {
		// 查看订单经手配送员暂时没有做分页,一个订单经手人个数不至于多到需要分页吧??!!
		Map<String, Object> result = new HashMap<String, Object>();
		if (!validateUser() || null == expressId) {
			return null;
		}
		DbSession dbSession = BaseHibernateUtils.newSession();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat userdateFormat = new SimpleDateFormat("yyyy年MM月dd日 ");
		try {
			List<NDAUserExpress> userExpress = DBUtils.getUserExpressList(dbSession, expressId);
			Long recordsTotal = (Long) dbSession
					.createQuery("select count(*) from NDAUserExpress where expressId=:expressId and status=:status")
					.setParameter("expressId", expressId).setParameter("status", Constants.State.STATE_FINISHED)
					.uniqueResult();
			result.put("recordsTotal", recordsTotal);// 算出总的记录,分页用
			result.put("recordsFiltered", recordsTotal);
			for (NDAUserExpress ue : userExpress) {
				// 根据id获取快递员
				Map<String, Object> map1 = new HashMap<String, Object>();
				NDAUser user = DBUtils.getUserById(dbSession, ue.getUserId());
				String domainName = (String) dbSession.createQuery("select name from NDADomain where id=:id")
						.setParameter("id", user.getDomainId()).uniqueResult();
				map1.put("id", ue.getUserId());
				map1.put("staffNo", user.getStaffNo());
				map1.put("nickName", user.getNickName());
				map1.put("domainName", domainName);
				map1.put("gender", user.getGender());
				map1.put("mobile", user.getMobile());
				map1.put("birthDate", user.getBirthDate());
				map1.put("email", user.getEmail());
				map1.put("iconId", user.getIconId());
				map1.put("address", user.getAddress());
				map1.put("description", user.getDescription());
				map1.put("creationTime", dateFormat.format(ue.getCreationTime()));
				map1.put("lastModitied", dateFormat.format(ue.getLastModitied()));
				map1.put("userCreationTime", userdateFormat.format(user.getCreationTime()));
				list.add(map1);
			}

		} finally {
			dbSession.close();
		}
		result.put("draw", draw);
		result.put("data", list);// 获取的数据
		return result;
	}

	@RequestMapping("/cancelSign")
	public String cancelSign(String expressNo, RedirectAttributes attr) {
		Utils.saveLog(lg.getUserId(), "撤销签收", lg.getDomainId());
		Response res = expressService.cancelSign(lg, expressNo);

		attr.addFlashAttribute("message", res.getMessage());
		attr.addFlashAttribute("error", true);
		return "redirect:/history";
	}

}
