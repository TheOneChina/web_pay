/*
 * Copyright (c) 2016 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * 
 */
package com.tnsoft.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.expertise.common.codec.Hex;
import com.expertise.common.logging.Logger;
import com.expertise.common.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.hibernate.model.NDAAlertLevel;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDALocateExpress;
import com.tnsoft.hibernate.model.NDATag;
import com.tnsoft.hibernate.model.NDATagExpress;
import com.tnsoft.hibernate.model.NDATempExpress;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.hibernate.model.NDAUserExpress;
import com.tnsoft.hibernate.model.UserRole;
import com.tnsoft.web.dto.ExpressGatherInfo;
import com.tnsoft.web.model.Alert;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.ExpressService;
import com.tnsoft.web.service.TagService;
import com.tnsoft.web.service.UserService;
import com.tnsoft.web.servlet.ServletConsts;
import com.tnsoft.web.util.AuthUtils;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;

@Controller
public class ProtocolController {

	public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();

	public ProtocolController() {
		super();
	}

	@Resource(name = "tagService")
	private TagService tagService;
	@Resource(name = "userService")
	private UserService userService;
	@Resource(name = "expressService")
	private ExpressService expressService;

	@RequestMapping(value = "/protocol/changePwd", method = RequestMethod.POST)
	@ResponseBody
	public void changePwd(int userId, String pwd, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();
			Response response = new Response(Response.OK);
			NDAUser account = (NDAUser) db.get(NDAUser.class, userId);
			try {
				account.setPassword(AuthUtils.hash(account.getName(), AuthUtils.newPassword(pwd)));
			} catch (GeneralSecurityException e) {
				Logger.error(e);
			}
			db.commit();
			Utils.saveLog(account.getId(), "APP用户修改密码", DBUtils.getDomainIdByUserId(db, account.getId()));

			out.write(GSON.toJson(response));
			return;
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	@RequestMapping(value = "/protocol/login", method = RequestMethod.POST)
	@ResponseBody
	public void login(String name, String pwd, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();
			Criteria criteria = db.createCriteria(NDAUser.class);
			criteria.add(Restrictions.eq("name", name)); //$NON-NLS-1$
			NDAUser account = (NDAUser) criteria.uniqueResult();
			if (account == null) {
				Response response = new Response(Response.ERROR);
				out.write(GSON.toJson(response));
				return;
			}

			int auth = AuthUtils.authWithPassword(account, Hex.toByteArray(pwd), false);
			switch (auth) {
			case AuthUtils.AUTH_OK:
				break;
			case AuthUtils.AUTH_FAILED:
			case AuthUtils.AUTH_ATTEMPT_EXCEED:
			case AuthUtils.AUTH_DISABLED:
				db.commit();
				Response response = new Response(auth);
				out.write(GSON.toJson(response));
				return;
			default:
				throw new IllegalStateException("Invalid auth result: " + auth);
			}

			Response response = new Response(Response.OK);

			response.setRoleId(account.getType());
			db.commit();

			String ticket = account.getTicket();
			response.setTicket(ticket);
			response.setUserId(account.getId());
			response.setMessage(account.getNickName());

			Utils.saveLog(account.getId(), "APP用户登录", DBUtils.getDomainIdByUserId(db, account.getId()));

			out.write(GSON.toJson(response));
			return;
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	// 揽收（旧版本）
	@RequestMapping(value = "/protocol/gather", method = RequestMethod.POST)
	@ResponseBody
	public void gather(int userId, String expressNo, String tagNo, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {

			db.beginTransaction();

			Utils.saveLog(userId, "APP揽收货物", DBUtils.getDomainIdByUserId(db, userId));

			Date now = new Date();
			NDAUser user = (NDAUser) db.get(NDAUser.class, userId);
			int domainId = DBUtils.getDomainIdByUserId(db, user.getId());
			NDATag tag = (NDATag) db.get(NDATag.class, tagNo);
			// 判断模块是否存在
			if (tag == null) {
				out.write(GSON.toJson(new Response(Response.ERROR, "设备编号不存在！")));
				return;
			}
			// 判断模块是否属于本站点
			if (tag.getDomainId() != domainId) {
				out.write(GSON.toJson(new Response(Response.ERROR, "非本站点设备！")));
				return;
			}

			if (tag != null) {
				// 保存订单，已经存在的需要过滤
				String[] tmp = expressNo.split(",");
				for (int i = 0, cnt = tmp.length; i < cnt; i++) {
					NDAExpress express = DBUtils.getNDAExpress(db, tmp[i], domainId);
					if (express == null) {
						express = new NDAExpress();
						express.setDomainId(domainId);
						express.setExpressNo(expressNo);
						express.setLastModitied(now);
						express.setCreationTime(now);
						express.setStatus(Constants.ExpressState.STATE_PENDING);
						db.save(express);
						db.flush();
					} else {
						if (express.getStatus() == Constants.ExpressState.STATE_FINISHED) {
							// express.setStatus(Constants.ExpressState.STATE_PENDING);
							continue;
						}
					}

					// 如果是配送员揽收则进入配送中
					if (user.getType() == 4) {
						express.setStatus(Constants.ExpressState.STATE_ACTIVE);
					}

					// 绑定
					NDATagExpress nt = DBUtils.getNDATagExpress(db, tagNo, express.getId());
					if (nt != null) {
						nt.setStatus(Constants.BindState.STATE_DELETE);
						nt.setLastModified(now);
					}

					NDATagExpress nt1 = new NDATagExpress();
					nt1.setCreationTime(now);
					nt1.setDomainId(domainId);
					nt1.setExpressId(express.getId());
					nt1.setLastModified(now);
					nt1.setStatus(Constants.BindState.STATE_ACTIVE);
					nt1.setTagNo(tagNo);
					db.save(nt1);
					db.flush();

					NDAUserExpress nur = DBUtils.getUserExpress(db, userId, express.getId());
					if (nur == null) {
						nur = new NDAUserExpress();
						nur.setCreationTime(now);
						nur.setDomainId(domainId);
						nur.setExpressId(express.getId());
						nur.setLastModitied(now);
						nur.setStatus(Constants.State.STATE_ACTIVE);
						nur.setUserId(userId);
						db.save(nur);
						db.flush();
					} else {
						nur.setLastModitied(now);
						nur.setStatus(Constants.State.STATE_ACTIVE);
					}
				}

				tag.setStatus(Constants.TagState.STATE_WORKING);
			}

			db.commit();

			out.write(GSON.toJson(new Response(Response.OK)));
			return;

		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	// 揽收
	@RequestMapping(value = "/protocol/gather_v1", method = RequestMethod.POST)
	@ResponseBody
	public void gather(String data, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {
			ExpressGatherInfo expressGatherInfo = GSON.fromJson(data, ExpressGatherInfo.class);
			db.beginTransaction();
			int domainId = DBUtils.getDomainIdByUserId(db, expressGatherInfo.getUserId());
			Utils.saveLog(expressGatherInfo.getUserId(), "APP揽收货物", domainId);

			Date now = new Date();
			NDAUser user = (NDAUser) db.get(NDAUser.class, expressGatherInfo.getUserId());
			NDATag tag = (NDATag) db.get(NDATag.class, expressGatherInfo.getTagNo());

			// 判断模块是否存在
			if (tag == null) {
				out.write(GSON.toJson(new Response(Response.ERROR, "设备编号不存在！")));
				return;
			}
			// 判断模块是否属于本站点
			if (tag.getDomainId() != domainId) {
				out.write(GSON.toJson(new Response(Response.ERROR, "非本站点设备！")));
				return;
			}

			// 保存订单，已经存在的需要过滤
			List<ExpressGatherInfo.ExpressNoAndDescription> list = expressGatherInfo.getExpressNoAndDescriptions();
			for (int i = 0; i < list.size(); i++) {
				String expressNoTemp = list.get(i).getExpressNo().trim();
				if (expressNoTemp.length() < 1) {
					continue;
				}
				NDAExpress express = DBUtils.getNDAExpress(db, expressNoTemp, domainId);
				if (express == null) {
					express = new NDAExpress();
					express.setDomainId(domainId);
					express.setExpressNo(expressNoTemp);
					express.setLastModitied(now);
					express.setCreationTime(now);
					express.setDescription(list.get(i).getDescription());
					express.setStatus(Constants.ExpressState.STATE_PENDING);
					db.save(express);
					db.flush();
				} else {
					if (express.getStatus() == Constants.ExpressState.STATE_FINISHED) {
						continue;
					}
				}

				// 如果是配送员揽收则进入配送中
				if (user.getType() == 4) {
					express.setStatus(Constants.ExpressState.STATE_ACTIVE);
				}

				// 解绑之前的绑定
				List<NDATagExpress> ntList = DBUtils.getNdaTagExpressList(db, express.getId());
				if (ntList != null && ntList.size() > 0) {
					for (NDATagExpress nt : ntList) {
						nt.setStatus(Constants.BindState.STATE_DELETE);
						nt.setLastModified(now);
					}
				}

				NDATagExpress nt1 = new NDATagExpress();
				nt1.setCreationTime(now);
				nt1.setDomainId(domainId);
				nt1.setExpressId(express.getId());
				nt1.setLastModified(now);
				nt1.setStatus(Constants.BindState.STATE_ACTIVE);
				nt1.setTagNo(expressGatherInfo.getTagNo());
				db.save(nt1);
				db.flush();

				NDAUserExpress nur = DBUtils.getUserExpress(db, expressGatherInfo.getUserId(), express.getId());
				if (nur == null) {
					nur = new NDAUserExpress();
					nur.setCreationTime(now);
					nur.setDomainId(domainId);
					nur.setExpressId(express.getId());
					nur.setLastModitied(now);
					nur.setStatus(Constants.State.STATE_ACTIVE);
					nur.setUserId(expressGatherInfo.getUserId());
					db.save(nur);
					db.flush();
				} else {
					nur.setLastModitied(now);
					nur.setStatus(Constants.State.STATE_ACTIVE);
				}
			}

			tag.setStatus(Constants.TagState.STATE_WORKING);

			db.commit();

			out.write(GSON.toJson(new Response(Response.OK)));
			return;

		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	/* fbb */
	@RequestMapping(value = "/protocol/exchange", method = RequestMethod.POST)
	@ResponseBody
	public void exchange(int userId, String expressNo, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		Response response = new Response(Response.OK);
		try {
			db.beginTransaction();
			Utils.saveLog(userId, "APP转运货物", DBUtils.getDomainIdByUserId(db, userId));

			Date now = new Date();
			NDAUser user = (NDAUser) db.get(NDAUser.class, userId);
			// 获得当前用户的domainId
			int domainId = DBUtils.getDomainIdByUserId(db, user.getId());
			String[] allExpressNoStr = expressNo.split(",");

			// 遍历所有的订单编号,更改状态
			for (String expressNoStr : allExpressNoStr) {
				NDAExpress express = DBUtils.getNDAExpress(db, expressNoStr, domainId);
				if (express != null) {
					// 根据扫描的订单码获得expressId.
					Integer expressId = (Integer) db
							.createQuery("select id from NDAExpress where expressNo=:expressNo and domainId=:domainId")
							.setParameter("expressNo", expressNoStr).setParameter("domainId", domainId).uniqueResult();
					NDAUserExpress userExpress = DBUtils.getNDAUserExpress(db, expressId, domainId);
					// 判断是不是同一个用户
					if (userExpress.getUserId() != userId) { // 第一步,根据订单的id.将上一个快递员的状态变为finished,第二个参数不是expressNo!!!!!!!获得订单用户关系,UserExpress没有expressNo属性
						userExpress.setLastModitied(now);
						userExpress.setStatus(Constants.State.STATE_FINISHED);
						db.flush();// 让hibernate按照逻辑顺序保存,
						// 第二步,新建一个关系对象,获取当前登录的快递员信息加入到用户订单关系表中
						NDAUserExpress userExpress1 = new NDAUserExpress();
						userExpress1.setExpressId(express.getId());
						userExpress1.setUserId(userId);
						userExpress1.setDomainId(domainId);
						userExpress1.setLastModitied(now);
						userExpress1.setCreationTime(now);
						userExpress1.setStatus(Constants.State.STATE_ACTIVE);
						// 为了防止出bug这里再一次将订单状态更新了一遍
						express.setStatus(Constants.ExpressState.STATE_ACTIVE);
						express.setLastModitied(now);
						db.save(userExpress1);
						db.save(express);
						db.flush();
					} else {
						if (express.getStatus() == Constants.ExpressState.STATE_PENDING) {
							userExpress.setLastModitied(now);
							userExpress.setStatus(Constants.State.STATE_ACTIVE);
							express.setStatus(Constants.ExpressState.STATE_ACTIVE);
							express.setLastModitied(now);
						} else {
							response.setCode(Response.ERROR);
							response.setMessage("您已承运此订单");
						}
					}

				} else {
					response.setCode(Response.ERROR);
					response.setMessage("无此订单");
				}
			}
		} catch (Exception e) {
			Logger.error(e);
			response.setCode(Response.ERROR);
		} finally {
			db.commit();
			db.close();
		}
		out.write(GSON.toJson(response));
	}

	@RequestMapping(value = "/protocol/signing", method = RequestMethod.POST)
	@ResponseBody
	public void signing(int userId, String expressNo, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		Response result = new Response(Response.OK);
		try {
			db.beginTransaction();

			Utils.saveLog(userId, "APP签收货物", DBUtils.getDomainIdByUserId(db, userId));

			Date now = new Date();
			NDAUser user = (NDAUser) db.get(NDAUser.class, userId);
			int domainId = DBUtils.getDomainIdByUserId(db, user.getId());

			// 增加批量签收
			String[] tmp = expressNo.split(",");
			for (int i = 0, cnt = tmp.length; i < cnt; i++) {
				NDAExpress express = DBUtils.getNDAExpress(db, tmp[i], domainId);

				if (express != null) {

					// 修改订单状态
					express.setCheckOutTime(now);
					express.setLastModitied(now);
					express.setStatus(Constants.ExpressState.STATE_FINISHED);

					// 状态
					// String sql = "UPDATE nda_user_express SET status=" +
					// Constants.State.STATE_FINISHED
					// + " WHERE user_id=" + user.getId() + " AND express_id=" +
					// express.getId();
					// db.createSQLQuery(sql).executeUpdate();
					NDAUserExpress ndaUserExpress = DBUtils.getNDAUserExpressByExpressIdAndUserId(db, express.getId(),
							user.getId());
					ndaUserExpress.setLastModitied(now);
					ndaUserExpress.setStatus(Constants.State.STATE_FINISHED);

					// 对于没有设置温度阈值的订单，将tag阈值保存至express，以便解绑后查询订单阈值；
					NDATagExpress nte = DBUtils.getNDATagExpress(db, express.getId());
					if (nte != null) {
						NDATag tag = DBUtils.getTagByTagNo(db, nte.getTagNo());
						if (tag != null) {
							if (tag.getTemperatureMax() != null && express.getTemperatureMax() == null) {
								express.setTemperatureMax(tag.getTemperatureMax());
							}
							if (tag.getTemperatureMin() != null && express.getTemperatureMin() == null) {
								express.setTemperatureMin(tag.getTemperatureMin());
							}
						}
					}
					/*
					 * 将此部分移至模块数据处理处，以解决签收后无法记录模块的离线记录； // tag状态 NDATagExpress
					 * nte = DBUtils.getNDATagExpress(db, express.getId()); if
					 * (nte != null) { String sql =
					 * "SELECT COUNT(*) FROM nda_tag_express WHERE status=" +
					 * Constants.BindState.STATE_ACTIVE + " AND " + " tag_no='"
					 * + nte.getTagNo() + "'"; SQLQuery query =
					 * db.createSQLQuery(sql); BigInteger count = (BigInteger)
					 * query.uniqueResult(); if (count != null &&
					 * count.intValue() == 1) { NDATag tag = (NDATag)
					 * db.get(NDATag.class, nte.getTagNo()); if (tag != null) {
					 * tag.setStatus(Constants.TagState.STATE_ACTIVE); } } //
					 * 解除绑定 sql = "UPDATE nda_tag_express SET status="
					 * +Constants.BindState.STATE_DELETE + " WHERE express_id="
					 * + express.getId();
					 * db.createSQLQuery(sql).executeUpdate(); }
					 */

				} else {
					result.setCode(Response.ERROR);
				}
			}
			db.commit();
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(result));
	}

	@RequestMapping(value = "/protocol/location", method = RequestMethod.POST)
	@ResponseBody
	public void location(int userId, String lat, String lng, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();
			Date now = new Date();
			double latt = Double.parseDouble(lat);
			double lngg = Double.parseDouble(lng);
			NDAUser user = (NDAUser) db.get(NDAUser.class, userId);
			int domainId = DBUtils.getDomainIdByUserId(db, user.getId());
			// 更新坐标
			String sql = "SELECT * FROM nda_user_express WHERE user_id=" + user.getId() + " AND status="
					+ Constants.State.STATE_ACTIVE;
			SQLQuery query = db.createSQLQuery(sql);
			query.addEntity(NDAUserExpress.class);
			List<?> list = query.list();
			if (!list.isEmpty()) {
				for (Object obj : list) {
					NDAUserExpress nue = (NDAUserExpress) obj;
					
					NDALocateExpress ne = new NDALocateExpress();
					ne.setCreationTime(now);
					ne.setDomainId(domainId);
					ne.setExpressId(nue.getExpressId());
					ne.setLastModitied(now);
					ne.setName(user.getNickName());
					ne.setLat(latt);
					ne.setLng(lngg);
					
					db.save(ne);
					db.flush();
				}
			}

			db.commit();
			out.write(GSON.toJson(new Response(Response.OK)));
			return;

		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	// 当前订单
	@RequestMapping(value = "/protocol/express", method = RequestMethod.POST)
	@ResponseBody
	public void express(int userId, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {

			Utils.saveLog(userId, "APP查看当前订单列表", DBUtils.getDomainIdByUserId(db, userId));

			Response r = new Response(Response.OK);
			String sql = "SELECT * FROM nda_express WHERE id IN (SELECT express_id FROM nda_user_express WHERE user_id="
					+ userId + " AND status<" + Constants.State.STATE_FINISHED + " )" + "ORDER BY last_modified DESC";
			UserRole ur = DBUtils.getRoleByUserId(db, userId);
			int domainId = DBUtils.getDomainIdByUserId(db, userId);
			if (ur.getRoleId() == Constants.Role.ADMIN) {
				sql = "SELECT * FROM nda_express WHERE domain_id=" + domainId + " AND status<"
						+ Constants.ExpressState.STATE_FINISHED + " ORDER BY last_modified DESC";
			}
			
			SQLQuery query = db.createSQLQuery(sql);
			query.addEntity(NDAExpress.class);
			List<NDAExpress> list = query.list();
			r.setExpress(list);

			out.write(GSON.toJson(r));
			return;

		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	// 误操作时恢复为待配送
	@RequestMapping(value = "/protocol/signToTaking", method = RequestMethod.POST)
	@ResponseBody
	public void signToTaking(int userId, int expressId, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		Response r = new Response(Response.OK);
		Date now = new Date();
		try {
			db.beginTransaction();
			// 降序排列，最后修改的放在最前面
			Utils.saveLog(userId, "APP修改已签收订单为待配送订单", DBUtils.getDomainIdByUserId(db, userId));
			String sql = "SELECT * FROM nda_user_express WHERE express_id=" + expressId + " AND status="
					+ Constants.State.STATE_FINISHED + " ORDER BY last_modified DESC";// 找出与该订单关系解绑的所有记录（人）
			SQLQuery query = db.createSQLQuery(sql);
			query.addEntity(NDAUserExpress.class);
			List<NDAUserExpress> list = query.list();

			if (userId == list.get(0).getUserId()) {
				NDAUserExpress ndaUserExpress = DBUtils.getNDAUserExpressByExpressIdAndUserId(db, expressId, userId);
				ndaUserExpress.setLastModitied(now);
				ndaUserExpress.setStatus(Constants.State.STATE_ACTIVE);
				db.flush();// 让hibernate按照逻辑顺序保存,

				NDAExpress ndaExpress = DBUtils.getNDAExpress(db, expressId);
				ndaExpress.setLastModitied(now);
				ndaExpress.setStatus(Constants.ExpressState.STATE_PENDING);
				db.flush();
			} else {
				// 如果是别人修改的则没有权限
				r.setCode(Response.ERROR);
				r.setMessage("很抱歉！您没有权限修改此订单的状态。");
			}
			db.commit();

		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(r));
	}

	// 订单温度
	@RequestMapping(value = "/protocol/expressTmp", method = RequestMethod.POST)
	@ResponseBody
	public void expressTmp(int userId, int expressId, int offset, int limit, HttpServletResponse resp)
			throws IOException {
		Logger.error("查看订单温度，订单序号: " + expressId);
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {
			Response r = new Response(Response.OK);
			if (userId > 0) {
				Utils.saveLog(userId, "APP查看订单温度", DBUtils.getDomainIdByUserId(db, userId));
			}
			Criteria criteria = db.createCriteria(NDATempExpress.class);
			criteria.add(Restrictions.eq("expressId", expressId));
			criteria.addOrder(Order.desc("creationTime"));

			if (offset >= 0 && limit > 0) {
				BaseHibernateUtils.setLimit(criteria, offset * limit, limit);
			}

			List<NDATempExpress> list = criteria.list();
			r.setTemps(list);

			out.write(GSON.toJson(r));
			return;

		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	// 2017-4-7
	// 订单温度图表数据获取
	private final int dataNums = 10000;

	@RequestMapping(value = "/protocol/expressChart", method = RequestMethod.POST)
	@ResponseBody
	public void expressChart(int userId, int expressId, HttpServletResponse resp) throws IOException {
		Logger.error("获取订单温度图表数据，订单序号: " + expressId);
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {
			Response r = new Response(Response.OK);
			if (userId > 0) {
				Utils.saveLog(userId, "APP获取订单温度图表数据", DBUtils.getDomainIdByUserId(db, userId));
			}

			List<NDATempExpress> list = DBUtils.getAllTempesByExpressId(db, expressId);

			if (list.size() <= dataNums) {
				r.setTemps(list);
			} else {
				r.setTemps(list.subList(0, dataNums));
			}
			out.write(GSON.toJson(r));
			return;
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	// 2017-4-11 app获取订单报表信息
	@RequestMapping(value = "/protocol/expressBriefInfo", method = RequestMethod.POST)
	@ResponseBody
	public void expressBriefInfo(int userId, int expressId, HttpServletResponse resp) throws IOException {
		Logger.error("获取订单报表数据，订单序号: " + expressId);
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		Map<String, String> result = new HashMap<String, String>();

		DbSession dbSession = BaseHibernateUtils.newSession();
		try {
			if (userId > 0) {
				Utils.saveLog(userId, "APP获取订单报表数据", DBUtils.getDomainIdByUserId(dbSession, userId));
			}

			NDAExpress express = DBUtils.getNDAExpress(dbSession, expressId);
			NDATagExpress tagExpress = DBUtils.getNDATagExpressIgnoreStatus(dbSession, express.getId()).get(0);
			;
			List<NDAAlert> alerts = DBUtils.getAllAlertsByExpressId(dbSession, express.getId());
			List<NDATempExpress> ndaTempExpresses = DBUtils.getAllTempesByExpressId(dbSession, express.getId());
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
			}

			result.put("alertCount", alerts.size() + "");

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
				result.put("maxAlertTemperature", "无");
			}

			if (express.getTemperatureMin() != null) {
				result.put("minAlertTemperature", express.getTemperatureMin() + "");
			} else {
				result.put("minAlertTemperature", "无");
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

			result.put("expressId", expressId + "");

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

	// 2017-4-12 app非登录查询订单
	class TempStruct {
		String differName;
		int expressId;
	}

	class ListTempStruct {
		List<TempStruct> expressByNoList = new ArrayList<ProtocolController.TempStruct>();
	}

	@RequestMapping(value = "/protocol/getExpressesByNo", method = RequestMethod.POST)
	@ResponseBody
	public void getExpressesByNo(String expressNo, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		ListTempStruct result = new ListTempStruct();
		try {
			String sql = "SELECT a.*, b.name AS domainName FROM nda_express a, nda_domain b WHERE b.id=a.domain_id AND a.express_no='"
					+ expressNo + "'";
			SQLQuery query = db.createSQLQuery(sql);
			query.addEntity(NDAExpress.class);
			query.addScalar("domainName", StringType.INSTANCE);
			List<?> list = query.list();
			if (list.size() > 0) {
				for (Object o : list) {
					Object[] row = (Object[]) o;
					NDAExpress express = (NDAExpress) row[0];
					TempStruct tStruct = new TempStruct();
					tStruct.differName = (String) row[1];
					tStruct.expressId = express.getId();
					result.expressByNoList.add(tStruct);
				}
			}
		} finally {
			db.close();
		}
		out.write(GSON.toJson(result));
	}

	@RequestMapping(value = "/protocol/getExpressesByTagNo", method = RequestMethod.POST)
	@ResponseBody
	public void getExpressesByTagNo(String tagNo, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		ListTempStruct result = new ListTempStruct();
		try {
			String sql = "SELECT * FROM nda_express WHERE id IN(SELECT express_id FROM nda_tag_express WHERE tag_no='"
					+ tagNo + "') ORDER BY creation_time DESC";
			SQLQuery query = db.createSQLQuery(sql);
			query.addEntity(NDAExpress.class);
			List<?> list = query.list();
			if (list.size() > 0) {
				for (Object o : list) {
					NDAExpress express = (NDAExpress) o;
					TempStruct tStruct = new TempStruct();
					tStruct.differName = express.getExpressNo();
					tStruct.expressId = express.getId();
					result.expressByNoList.add(tStruct);
				}
			}
		} finally {
			db.close();
		}
		out.write(GSON.toJson(result));
	}

	// 历史订单
	@RequestMapping(value = "/protocol/expressHis", method = RequestMethod.POST)
	@ResponseBody
	public void expressHis(int userId, int offset, int limit, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {
			Utils.saveLog(userId, "APP查看历史订单", DBUtils.getDomainIdByUserId(db, userId));

			Response r = new Response(Response.OK);
			// 普通用户将与其解绑的订单返回
			String sql = "SELECT * FROM nda_express WHERE id IN (SELECT express_id FROM nda_user_express WHERE user_id="
					+ userId + " AND status=" + Constants.State.STATE_FINISHED + " )" + " ORDER BY last_modified DESC";
			UserRole ur = DBUtils.getRoleByUserId(db, userId);
			int domainId = DBUtils.getDomainIdByUserId(db, userId);
			// 超级用户将该站点所有签收的订单返回
			if (ur.getRoleId() == Constants.Role.ADMIN) {
				sql = "SELECT * FROM nda_express WHERE domain_id=" + domainId + " AND status="
						+ Constants.ExpressState.STATE_FINISHED + " ORDER BY checkout_time DESC";
			}
			SQLQuery query = db.createSQLQuery(sql);
			query.addEntity(NDAExpress.class);
			if (offset >= 0 && limit > 0) {
				BaseHibernateUtils.setLimit(query, offset * limit, limit);
			}
			List<NDAExpress> list = query.list();
			r.setExpress(list);
			out.write(GSON.toJson(r));
			return;

		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	// 报警信息
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/protocol/alerts", method = RequestMethod.POST)
	@ResponseBody
	public void alerts(int userId, long csn, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {

			Utils.saveLog(userId, "APP查看报警列表", DBUtils.getDomainIdByUserId(db, userId));

			Response r = new Response(Response.OK);
			int roleId = DBUtils.getRoleByUserId(db, userId).getRoleId();
			String sql;
			if (roleId == Constants.Role.ADMIN) {
				int domainId = DBUtils.getDomainIdByUserId(db, userId);
				sql = "SELECT * FROM nda_alert a WHERE a.status=" + Constants.AlertState.STATE_ACTIVE
						+ "AND a.express_id IN (SELECT express_id FROM nda_user_express WHERE domain_id=" + domainId
						+ ") order by a.creation_time DESC ";
			} else {
				sql = "SELECT * FROM nda_alert a WHERE a.status=" + Constants.AlertState.STATE_ACTIVE
						+ "AND a.express_id IN (SELECT express_id FROM nda_user_express WHERE user_id=" + userId
						+ ") order by a.creation_time DESC ";
			}

			SQLQuery query = db.createSQLQuery(sql);
			query.addEntity(NDAAlert.class);
			BaseHibernateUtils.setLimit(query, 0, 10);
			List<?> list = query.list();
			if (!list.isEmpty()) {
				List<Alert> alerts = new ArrayList<Alert>();
				for (Object obj : list) {
					NDAAlert a = (NDAAlert) obj;
					Alert alert = new Alert();
					alert.setAlertLevel(a.getAlertLevel());
					alert.setCreationTime(a.getCreationTime());
//					alert.setCsn(a.getCsn());
					alert.setDomainId(a.getDomainId());
					alert.setId(a.getId());
					alert.setLastModitied(a.getLastModitied());
					alert.setStatus(a.getStatus());
					alert.setTagNo(a.getTagNo());
					List<NDAExpress> exs = new ArrayList<NDAExpress>();
					exs.add(getExpressById(db, a.getExpressId()));
					alert.setExpress(exs);

					r.setExtra(Math.max(r.getExtra(), alert.getCsn()));

					alerts.add(alert);
				}

				Collections.sort(alerts, new Comparator() {
					public int compare(Object a, Object b) {
						int one = ((Alert) a).getStatus();
						int two = ((Alert) b).getStatus();
						return one - two;
					}
				});

				r.setAlerts(alerts);
			}

			String result = GSON.toJson(r);

			// Logger.error(result);

			out.write(result);
			return;

		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	// 分页获取所有报警订单
	@RequestMapping(value = "/protocol/hisAlertExpresses", method = RequestMethod.POST)
	@ResponseBody
	public void hisAlertExpresses(int userId, int offset, int limit, HttpServletResponse resp) throws IOException {
		System.out.println("****************** " + offset + " ; " + limit);
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {

			Utils.saveLog(userId, "APP查看历史报警订单列表", DBUtils.getDomainIdByUserId(db, userId));

			Response r = new Response(Response.OK);
			
			//2017年8月2日
			
			int roleId = DBUtils.getRoleByUserId(db, userId).getRoleId();
			String sql;
			if (roleId == Constants.Role.ADMIN) {
				int domainId = DBUtils.getDomainIdByUserId(db, userId);
				sql = "SELECT * FROM nda_express WHERE id IN (SELECT a.express_id FROM nda_alert a WHERE a.status="
						+ Constants.AlertState.STATE_FINISHED + " AND a.domain_id=" + domainId
						+ " order by a.creation_time DESC )";
			} else {
				sql = "SELECT * FROM nda_express WHERE id IN (SELECT a.express_id FROM nda_alert a WHERE a.status="
						+ Constants.AlertState.STATE_FINISHED
						+ " AND a.express_id IN (SELECT express_id FROM nda_user_express WHERE user_id=" + userId
						+ ") order by a.creation_time DESC) ";
			}

			SQLQuery query = db.createSQLQuery(sql);
			query.addEntity(NDAExpress.class);
			if (offset >= 0 && limit > 0) {
				BaseHibernateUtils.setLimit(query, offset * limit, limit);
			}
			List<NDAExpress> list = query.list();
			if (!list.isEmpty()) {
				r.setExpress(list);
			}
			String result = GSON.toJson(r);
			out.write(result);
			return;
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	// 根据订单id获取订单历史报警
	@RequestMapping(value = "/protocol/getExpressAlerts", method = RequestMethod.POST)
	@ResponseBody
	public void getExpressAlerts(int userId, int expressId, int offset, int limit, HttpServletResponse resp)
			throws IOException {

		System.out.println("-------------------" + offset);
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {

			Utils.saveLog(userId, "APP查看订单报警详情", DBUtils.getDomainIdByUserId(db, userId));
			Response r = new Response(Response.OK);
			String sql = "SELECT * FROM nda_alert WHERE express_id=" + expressId + " AND status="
					+ Constants.AlertState.STATE_FINISHED + " order by creation_time DESC ";
			SQLQuery query = db.createSQLQuery(sql);
			query.addEntity(NDAAlert.class);
			if (offset >= 0 && limit > 0) {
				BaseHibernateUtils.setLimit(query, offset * limit, limit);
			}
			List<NDAAlert> list = query.list();
			if (!list.isEmpty()) {
				r.setNdaAlerts(list);
			}
			String result = GSON.toJson(r);
			out.write(result);
			return;
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	// 关闭一般报警
	@RequestMapping(value = "/protocol/closeAlert", method = RequestMethod.POST)
	@ResponseBody
	public void closeAlert(int userId, int alertId, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();

			Utils.saveLog(userId, "APP关闭报警", DBUtils.getDomainIdByUserId(db, userId));

			Response r = new Response(Response.OK);

			// 2017-4-6
			String sql = "SELECT * FROM nda_alert WHERE express_id IN ("
					+ " SELECT express_id FROM nda_user_express WHERE user_id=" + userId + ")";

			SQLQuery query = db.createSQLQuery(sql);
			query.addEntity(NDAAlert.class);
			List<?> list = query.list();
			if (!list.isEmpty()) {
				for (Object object : list) {
					NDAAlert ndaAlert = (NDAAlert) object;
					if (ndaAlert != null) {
						if (ndaAlert.getId() <= alertId) {
							ndaAlert.setStatus(Constants.AlertState.STATE_FINISHED);
						}
					}
				}
			}
			// end

			db.commit();

			out.write(GSON.toJson(r));
			return;

		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	// 温度上传
	@RequestMapping(value = "/protocol/upload1")
	@ResponseBody
	public void upload1(float x, float y, String token, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();

			Date now = new Date();

			if (!StringUtils.isEmpty(token)) {
				NDATag tag = (NDATag) db.get(NDATag.class, token);
				if (tag != null) {
					List<NDAExpress> express = getExpressByTag(db, tag.getTagNo());
					// 保存温度
					for (NDAExpress ex : express) {
						NDATempExpress a = new NDATempExpress();
						a.setCreationTime(now);// TODO
						a.setDomainId(tag.getDomainId());
						a.setExpressId(ex.getId());
						a.setLastModitied(now);
						a.setTemperature(y);
						db.save(a);
						db.flush();
					}

					// 判断是否超过范围即是否触发报警
					if (y < tag.getTemperatureMin() || y > tag.getTemperatureMax()) {
						// 判断该tag出发普通报警次数
						NDAAlertLevel level = (NDAAlertLevel) db.get(NDAAlertLevel.class, 1);
						if (level != null && level.getHours() > 0) {
							int count = getAlertCount(db, tag.getTagNo(), level.getHours());
							if (count > level.getTimes() - 1) {
								NDAAlert alert = new NDAAlert();
								alert.setAlertLevel(Constants.AlertLevel.STATE_SERIOUS);
								alert.setCreationTime(now);
//								alert.setCsn(BaseHibernateUtils.nextCsn(db));
								alert.setDomainId(tag.getDomainId());
								alert.setLastModitied(now);
								alert.setTagNo(tag.getTagNo());
								alert.setStatus(Constants.AlertState.STATE_ACTIVE);
								db.save(alert);
								db.flush();

							}
						}

						// 生成报警信息
						NDAAlert alert = new NDAAlert();
						alert.setAlertLevel(Constants.AlertLevel.STATE_NORAML_LOW);
						alert.setCreationTime(now);
//						alert.setCsn(BaseHibernateUtils.nextCsn(db));
						alert.setDomainId(tag.getDomainId());
						alert.setLastModitied(now);
						alert.setTagNo(tag.getTagNo());
						alert.setStatus(Constants.AlertState.STATE_ACTIVE);
						db.save(alert);
						db.flush();

					}
				}
			}

			db.commit();

			out.write("温度上传成功");
			return;

		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}
		out.write(GSON.toJson(new Response(Response.ERROR)));
	}

	/*
	 * //温度上传
	 * 
	 * @RequestMapping(value = "/protocol/upload", method = RequestMethod.GET)
	 * 
	 * @ResponseBody public void uploadGet(@RequestBody RequestEntity
	 * requestEntity, HttpServletResponse resp) throws IOException {
	 * Logger.error("收到GET数据:" + GSON.toJson(requestEntity));
	 * 
	 * String result = null;
	 * 
	 * resp.setContentType(ServletConsts.CONTENT_TYPE_JSON); PrintWriter out =
	 * resp.getWriter(); DbSession db = BaseHibernateUtils.newSession(); try {
	 * db.beginTransaction();
	 * 
	 * Date now = new Date();
	 * 
	 * if(requestEntity.getNonce() > 0){ //
	 * if(requestEntity.getMethod().equalsIgnoreCase("POST")){ String tk =
	 * requestEntity.getMeta().getAuthorization(); int pos =
	 * tk.indexOf("token"); String key = tk.substring(pos + 5).trim(); float y =
	 * requestEntity.getBody().getDatapoint().getY();
	 * if(!StringUtils.isEmpty(key)){ NDATag tag = (NDATag)db.get(NDATag.class,
	 * key); tag.setLastModitied(now); if(tag != null){ List<NDAExpress> express
	 * = getExpressByTag(db, tag.getTagNo()); //保存温度 for(NDAExpress ex :
	 * express) { NDATempExpress a = new NDATempExpress();
	 * a.setCreationTime(now);//TODO a.setDomainId(tag.getDomainId());
	 * a.setExpressId(ex.getId()); a.setLastModitied(now); a.setTemperature(y);
	 * db.save(a); db.flush(); }
	 * 
	 * 
	 * //判断是否超过范围即是否触发报警 if(y < tag.getTemperatureMin() || y >
	 * tag.getTemperatureMax()){ //判断该tag出发普通报警次数 NDAAlertLevel level =
	 * (NDAAlertLevel)db.get(NDAAlertLevel.class, 1); if(level != null &&
	 * level.getHours() > 0){ int count = getAlertCount(db, tag.getTagNo(),
	 * level.getHours()); if(count > level.getTimes() - 1){ NDAAlert alert = new
	 * NDAAlert(); alert.setAlertLevel(Constants.AlertLevel.STATE_SERIOUS);
	 * alert.setCreationTime(now); alert.setCsn(BaseHibernateUtils.nextCsn(db));
	 * alert.setDomainId(tag.getDomainId()); alert.setLastModitied(now);
	 * alert.setTagNo(tag.getTagNo());
	 * alert.setStatus(Constants.AlertState.STATE_ACTIVE); db.save(alert);
	 * db.flush();
	 * 
	 * for(NDAExpress ex : express) { NDAAlertExpress a = new NDAAlertExpress();
	 * a.setAlertId(alert.getId()); a.setDomainId(tag.getDomainId());
	 * a.setExpressId(ex.getId()); a.setTagNo(tag.getTagNo()); db.save(a);
	 * db.flush(); } } }
	 * 
	 * //生成报警信息 NDAAlert alert = new NDAAlert();
	 * alert.setAlertLevel(Constants.AlertLevel.STATE_NORAML);
	 * alert.setCreationTime(now); alert.setCsn(BaseHibernateUtils.nextCsn(db));
	 * alert.setDomainId(tag.getDomainId()); alert.setLastModitied(now);
	 * alert.setTagNo(tag.getTagNo());
	 * alert.setStatus(Constants.AlertState.STATE_ACTIVE); db.save(alert);
	 * db.flush();
	 * 
	 * for(NDAExpress ex : express) { NDAAlertExpress a = new NDAAlertExpress();
	 * a.setAlertId(alert.getId()); a.setDomainId(tag.getDomainId());
	 * a.setExpressId(ex.getId()); a.setTagNo(tag.getTagNo()); db.save(a);
	 * db.flush(); } } } }
	 * 
	 * UploadResponse up = new UploadResponse(); UploadResponse.Datapoint d =
	 * new UploadResponse.Datapoint(); d.setAt(Utils.SF.format(now));
	 * d.setCreated(Utils.SF.format(now)); d.setUpdated(Utils.SF.format(now));
	 * d.setX((int)requestEntity.getBody().getDatapoint().getX());
	 * d.setY((int)requestEntity.getBody().getDatapoint().getY());
	 * up.setStatus(200); up.setDatapoint(d); result = GSON.toJson(up);
	 * 
	 * 
	 * } else { //设备认证 String tk = requestEntity.getMeta().getAuthorization();
	 * int pos = tk.indexOf("token"); String key = tk.substring(pos + 5).trim();
	 * NDATag tag = (NDATag)db.get(NDATag.class, key); if(tag != null){ String
	 * date = Utils.SF.format(now); AuthResponse au = new AuthResponse();
	 * AuthResponse.Device device = new AuthResponse.Device();
	 * device.setActivate_status(1);
	 * device.setActivated_at(Utils.SF.format(tag.getCreationTime()));
	 * device.setBSSID(tag.getBSSID());
	 * device.setCreated(Utils.SF.format(tag.getCreationTime()));
	 * device.setDescription("device-description-" + tag.getName());
	 * device.setId(1); device.setIs_frozen(0); device.setIs_private(1);
	 * device.setKey_id(1);
	 * device.setLast_active(Utils.SF.format(tag.getLastModitied()));
	 * device.setLast_pull(Utils.SF.format(tag.getLastModitied()));
	 * device.setLocation(""); device.setMetadata(tag.getBSSID() +
	 * "temperature"); device.setName("device-name-"+tag.getName());
	 * device.setProduct_id(1); device.setProductbatch_id(1);
	 * device.setPtype(12335); device.setSerial(tag.getName());
	 * device.setStatus(2); device.setUpdated(date); device.setVisibly(1);
	 * au.setStatus(200); au.setMessage("device identified");
	 * au.setNonce(requestEntity.getNonce()); au.setDevice(device);
	 * 
	 * result = GSON.toJson(au);
	 * 
	 * }
	 * 
	 * } } else { //进入激活 NDATag tag = null; String token =
	 * requestEntity.getBody().getToken(); tag = this.getTagByToken(db, token);
	 * 
	 * if(tag == null){ tag = new NDATag(); tag.setCreationTime(now);
	 * tag.setDomainId(1); tag.setLastModitied(now);
	 * tag.setStatus(Constants.TagState.STATE_ACTIVE);
	 * tag.setTagNo(Utils.getUUID()); tag.setName(Utils.generateShortUuid());
	 * tag.setBSSID(requestEntity.getBody().getBSSID()); db.save(tag); }
	 * 
	 * RegisterResponse r = new RegisterResponse(); r.setStatus("200");
	 * r.setToken(token); r.setDevice(tag.getTagNo()); r.setKey(tag.getTagNo());
	 * result = GSON.toJson(r); }
	 * 
	 * 
	 * db.commit();
	 * 
	 * return;
	 * 
	 * } catch (Exception e) { Logger.error(e); } finally { db.close(); }
	 * 
	 * if(result != null){ out.write(result); Logger.error("回复GET数据:" + result);
	 * } }
	 * 
	 * //温度上传
	 * 
	 * @RequestMapping(value = "/protocol/upload", method = RequestMethod.POST)
	 * 
	 * @ResponseBody public void upload(@RequestBody RequestEntity
	 * requestEntity, HttpServletResponse resp) throws IOException {
	 * 
	 * Logger.error("收到POST数据:" + GSON.toJson(requestEntity)); String result =
	 * null;
	 * 
	 * resp.setContentType(ServletConsts.CONTENT_TYPE_JSON); PrintWriter out =
	 * resp.getWriter(); DbSession db = BaseHibernateUtils.newSession(); try {
	 * db.beginTransaction();
	 * 
	 * Date now = new Date();
	 * 
	 * if(requestEntity.getNonce() > 0){ //
	 * if(requestEntity.getMethod().equalsIgnoreCase("POST")){ String tk =
	 * requestEntity.getMeta().getAuthorization(); int pos =
	 * tk.indexOf("token"); String key = tk.substring(pos + 5).trim(); float y =
	 * requestEntity.getBody().getDatapoint().getY();
	 * if(!StringUtils.isEmpty(key)){ NDATag tag = (NDATag)db.get(NDATag.class,
	 * key); tag.setLastModitied(now); if(tag != null){ List<NDAExpress> express
	 * = getExpressByTag(db, tag.getTagNo()); //保存温度 for(NDAExpress ex :
	 * express) { NDATempExpress a = new NDATempExpress();
	 * a.setCreationTime(now);//TODO a.setDomainId(tag.getDomainId());
	 * a.setExpressId(ex.getId()); a.setLastModitied(now); a.setTemperature(y);
	 * db.save(a); db.flush(); }
	 * 
	 * 
	 * //判断是否超过范围即是否触发报警 if(y < tag.getTemperatureMin() || y >
	 * tag.getTemperatureMax()){ //判断该tag出发普通报警次数 NDAAlertLevel level =
	 * (NDAAlertLevel)db.get(NDAAlertLevel.class, 1); if(level != null &&
	 * level.getHours() > 0){ int count = getAlertCount(db, tag.getTagNo(),
	 * level.getHours()); if(count > level.getTimes() - 1){ NDAAlert alert = new
	 * NDAAlert(); alert.setAlertLevel(Constants.AlertLevel.STATE_SERIOUS);
	 * alert.setCreationTime(now); alert.setCsn(BaseHibernateUtils.nextCsn(db));
	 * alert.setDomainId(tag.getDomainId()); alert.setLastModitied(now);
	 * alert.setTagNo(tag.getTagNo());
	 * alert.setStatus(Constants.AlertState.STATE_ACTIVE); db.save(alert);
	 * db.flush();
	 * 
	 * for(NDAExpress ex : express) { NDAAlertExpress a = new NDAAlertExpress();
	 * a.setAlertId(alert.getId()); a.setDomainId(tag.getDomainId());
	 * a.setExpressId(ex.getId()); a.setTagNo(tag.getTagNo()); db.save(a);
	 * db.flush(); } } }
	 * 
	 * //生成报警信息 NDAAlert alert = new NDAAlert();
	 * alert.setAlertLevel(Constants.AlertLevel.STATE_NORAML);
	 * alert.setCreationTime(now); alert.setCsn(BaseHibernateUtils.nextCsn(db));
	 * alert.setDomainId(tag.getDomainId()); alert.setLastModitied(now);
	 * alert.setTagNo(tag.getTagNo());
	 * alert.setStatus(Constants.AlertState.STATE_ACTIVE); db.save(alert);
	 * db.flush();
	 * 
	 * for(NDAExpress ex : express) { NDAAlertExpress a = new NDAAlertExpress();
	 * a.setAlertId(alert.getId()); a.setDomainId(tag.getDomainId());
	 * a.setExpressId(ex.getId()); a.setTagNo(tag.getTagNo()); db.save(a);
	 * db.flush(); } } } }
	 * 
	 * UploadResponse up = new UploadResponse(); UploadResponse.Datapoint d =
	 * new UploadResponse.Datapoint(); d.setAt(Utils.SF.format(now));
	 * d.setCreated(Utils.SF.format(now)); d.setUpdated(Utils.SF.format(now));
	 * d.setX((int)requestEntity.getBody().getDatapoint().getX());
	 * d.setY((int)requestEntity.getBody().getDatapoint().getY());
	 * up.setStatus(200); up.setDatapoint(d); result = GSON.toJson(up);
	 * 
	 * 
	 * } else { //设备认证 String tk = requestEntity.getMeta().getAuthorization();
	 * int pos = tk.indexOf("token"); String key = tk.substring(pos + 5).trim();
	 * NDATag tag = (NDATag)db.get(NDATag.class, key); if(tag != null){ String
	 * date = Utils.SF.format(now); AuthResponse au = new AuthResponse();
	 * AuthResponse.Device device = new AuthResponse.Device();
	 * device.setActivate_status(1);
	 * device.setActivated_at(Utils.SF.format(tag.getCreationTime()));
	 * device.setBSSID(tag.getBSSID());
	 * device.setCreated(Utils.SF.format(tag.getCreationTime()));
	 * device.setDescription("device-description-" + tag.getName());
	 * device.setId(1); device.setIs_frozen(0); device.setIs_private(1);
	 * device.setKey_id(1);
	 * device.setLast_active(Utils.SF.format(tag.getLastModitied()));
	 * device.setLast_pull(Utils.SF.format(tag.getLastModitied()));
	 * device.setLocation(""); device.setMetadata(tag.getBSSID() +
	 * "temperature"); device.setName("device-name-"+tag.getName());
	 * device.setProduct_id(1); device.setProductbatch_id(1);
	 * device.setPtype(12335); device.setSerial(tag.getName());
	 * device.setStatus(2); device.setUpdated(date); device.setVisibly(1);
	 * au.setStatus(200); au.setMessage("device identified");
	 * au.setNonce(requestEntity.getNonce()); au.setDevice(device);
	 * 
	 * result = GSON.toJson(au);
	 * 
	 * }
	 * 
	 * } } else { //进入激活 NDATag tag = null; String token =
	 * requestEntity.getBody().getToken(); tag = this.getTagByToken(db, token);
	 * 
	 * if(tag == null){ tag = new NDATag(); tag.setCreationTime(now);
	 * tag.setDomainId(1); tag.setLastModitied(now);
	 * tag.setStatus(Constants.TagState.STATE_ACTIVE);
	 * tag.setTagNo(Utils.getUUID()); tag.setName(Utils.generateShortUuid());
	 * tag.setBSSID(requestEntity.getBody().getBSSID()); db.save(tag); }
	 * 
	 * RegisterResponse r = new RegisterResponse(); r.setStatus("200");
	 * r.setToken(token); r.setDevice(tag.getTagNo()); r.setKey(tag.getTagNo());
	 * result = GSON.toJson(r); }
	 * 
	 * 
	 * db.commit();
	 * 
	 * return;
	 * 
	 * } catch (Exception e) { Logger.error(e); } finally { db.close(); }
	 * 
	 * if(result != null){ out.write(result); Logger.error("回复POST数据:" +
	 * result); } }
	 */

	private int getAlertCount(DbSession db, String tagNo, float hour) {

		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.MINUTE, -((int) (hour * 60)));

		String sql = "SELECT COUNT(*) FROM nda_alert WHERE tag_no='" + tagNo + "' AND creation_time<=:time";
		SQLQuery query = db.createSQLQuery(sql);
		query.setParameter("time", c.getTime());
		BigInteger count = (BigInteger) query.uniqueResult();
		return count == null ? 0 : count.intValue();
	}

	private List<NDAExpress> getExpressByTag(DbSession db, String tagNo) {

		String sql = "SELECT * FROM nda_express WHERE id IN (SELECT express_id FROM nda_tag_express WHERE tag_no='"
				+ tagNo + "' AND status=" + Constants.BindState.STATE_ACTIVE + ") ";
		SQLQuery query = db.createSQLQuery(sql);
		query.addEntity(NDAExpress.class);
		return (List<NDAExpress>) query.list();

	}

	private NDATag getTagByToken(DbSession db, String token) {
		Criteria criteria = db.createCriteria(NDATag.class);
		criteria.add(Restrictions.eq("token", token));
		return (NDATag) criteria.uniqueResult();

	}

	private NDAExpress getExpressById(DbSession db, int expressId) {
		String sql = "SELECT * FROM nda_express WHERE id=" + expressId;
		SQLQuery query = db.createSQLQuery(sql);
		query.addEntity(NDAExpress.class);
		return (NDAExpress) query.list().get(0);
	}

	@RequestMapping(value = "/protocol/toTaking", method = RequestMethod.POST)
	@ResponseBody
	public void toTaking(int userId, String expressNo, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		Response response = new Response(Response.OK);

		try {
			db.beginTransaction();

			Utils.saveLog(userId, "APP待转运货物", DBUtils.getDomainIdByUserId(db, userId));

			Date now = new Date();
			NDAUser user = (NDAUser) db.get(NDAUser.class, userId);
			int domainId = DBUtils.getDomainIdByUserId(db, user.getId());
			String[] allExpressNoStr = expressNo.split(",");
			for (String expressNoStr : allExpressNoStr) {
				NDAExpress express = DBUtils.getNDAExpress(db, expressNoStr, domainId);
				if (express != null) {
					// 修改nda_express表中的订单状态
					express.setLastModitied(now);
					express.setCheckOutTime(now);
					express.setStatus(Constants.ExpressState.STATE_PENDING);

					// 修改nda_user_express表中的状态(解绑)
					// String sql = "UPDATE nda_user_express SET status=" +
					// Constants.State.STATE_FINISHED + " WHERE express_id="
					// + express.getId() + " AND user_id=" + userId;
					// db.createSQLQuery(sql).executeUpdate();
					NDAUserExpress ndaUserExpress = DBUtils.getNDAUserExpressByExpressIdAndUserId(db, express.getId(),
							userId);
					ndaUserExpress.setLastModitied(now);
					ndaUserExpress.setStatus(Constants.State.STATE_FINISHED);

				} else {
					response.setCode(Response.ERROR);
					response.setMessage("操作失败");
				}
			}
		} catch (Exception e) {
			Logger.error(e);
			response.setCode(Response.ERROR);
		} finally {
			db.commit();
			db.close();
		}
		out.write(GSON.toJson(response));
	}

	@RequestMapping(value = "/protocol/electricityByTag ", method = RequestMethod.POST)
	@ResponseBody
	public Object getElectricityByTag(Integer userId) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Set<NDATag> tags = tagService.getTagByUId(userId);
		for (NDATag tag : tags) {
			// 暂定为电量低于30
			if (tag.getElectricity() <= 30) {
				// 将需要传到app的电量和设备编号,封装为map,放入list
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("tagNo", tag.getTagNo());
				map.put("electricity", tag.getElectricity());
				list.add(map);
			}
		}
		return list;
	}

	@RequestMapping(value = "/protocol/expressAttribute ", method = RequestMethod.POST)
	@ResponseBody
	public Object saveExpressAttribute(String expressValue, String expressFlag, String userId, String expressId) {
		Response res = expressService.saveExpressAttribute(expressValue, expressFlag, userId, expressId);
		return res;
	}

}
