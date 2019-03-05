package com.tnsoft.web.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.hibernate.model.NDADomain;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDATag;
import com.tnsoft.hibernate.model.NDATagExpress;
import com.tnsoft.hibernate.model.NDATempExpress;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.hibernate.model.NDAUserExpress;
import com.tnsoft.hibernate.model.UserRole;

/**
 * 通用的数据库操作
 */
public final class DBUtils {

	public DBUtils() {
		super();
	}

	/**
	 * 判断当前智能硬件是否绑定订单
	 * 
	 * @param session
	 * @param tagNo
	 *            智能硬件编码
	 * @return
	 */
	public static boolean hasBind(DbSession session, String tagNo) {
		Criteria criteria = session.createCriteria(NDATagExpress.class);
		criteria.add(Restrictions.eq("tagNo", tagNo));
		criteria.add(Restrictions.eq("status", Constants.BindState.STATE_ACTIVE));
		return !criteria.list().isEmpty();
	}

	/**
	 * 判断员工编码是否存在
	 * 
	 * @param session
	 * @param staffNo
	 *            员工编码
	 * @return
	 */
	public static boolean staffNoExist(DbSession session, String staffNo) {
		Criteria criteria = session.createCriteria(NDAUser.class);
		criteria.add(Restrictions.eq("staffNo", staffNo));
		return !criteria.list().isEmpty();
	}

	/**
	 * 判断手机号是否存在
	 * 
	 * @param session
	 * @param phone
	 *            手机号码
	 * @return
	 */
	public static boolean mobileExist(DbSession session, String phone) {
		Criteria criteria = session.createCriteria(NDAUser.class);
		criteria.add(Restrictions.eq("name", phone));
		return !criteria.list().isEmpty();
	}

	/**
	 * 根据硬件编码和订单id获取绑定关系
	 * 
	 * @param db
	 * @param tagNo
	 *            智能硬件编码
	 * @param expressId
	 *            订单id
	 * @return
	 */
	public static NDATagExpress getNDATagExpress(DbSession db, String tagNo, int expressId) {
		Criteria criteria = db.createCriteria(NDATagExpress.class);
		criteria.add(Restrictions.eq("tagNo", tagNo));
		criteria.add(Restrictions.eq("expressId", expressId));
		criteria.add(Restrictions.eq("status", Constants.BindState.STATE_ACTIVE));
		return (NDATagExpress) criteria.uniqueResult();
	}

	/**
	 * 根据订单id获取绑定关系
	 * 
	 * @param db
	 * @param expressId
	 *            订单id
	 * @return
	 */
	public static NDATagExpress getNDATagExpress(DbSession db, int expressId) {
		Criteria criteria = db.createCriteria(NDATagExpress.class);
		criteria.add(Restrictions.eq("expressId", expressId));
		criteria.add(Restrictions.eq("status", Constants.BindState.STATE_ACTIVE));
		return (NDATagExpress) criteria.uniqueResult();
	}

	/**
	 * 根据订单id获取订单所有绑定过的模块
	 * 
	 * @param db
	 * @param expressId
	 * @return
	 */
	public static List<NDATagExpress> getNDATagExpressIgnoreStatus(DbSession db, int expressId) {
		Criteria criteria = db.createCriteria(NDATagExpress.class);
		criteria.add(Restrictions.eq("expressId", expressId));
		criteria.addOrder(Order.desc("lastModified"));
		return criteria.list();
	}

	public static List<NDATagExpress> getNdaTagExpressList(DbSession db, int expressId) {
		Criteria criteria = db.createCriteria(NDATagExpress.class);
		criteria.add(Restrictions.eq("expressId", expressId));
		criteria.add(Restrictions.eq("status", Constants.BindState.STATE_ACTIVE));
		return criteria.list();
	}

	/**
	 * 根据订单id获取订单所有经手过的配送员fbb
	 * 
	 * @param db
	 * @param expressId
	 * @return
	 */
	public static List<NDAUserExpress> getUserExpressList(DbSession db, int expressId) {
		Criteria criteria = db.createCriteria(NDAUserExpress.class);
		criteria.add(Restrictions.eq("expressId", expressId));
		criteria.addOrder(Order.asc("creationTime"));
		return criteria.list();
	}

	public static NDAUser getUserById(DbSession db, int userId) {
		Criteria criteria = db.createCriteria(NDAUser.class);
		criteria.add(Restrictions.eq("id", userId));
		return (NDAUser) criteria.uniqueResult();
	}

	/**
	 * 根据用户id和订单id获取绑定关系
	 * 
	 * @param db
	 * @param userId
	 *            用户id
	 * @param expressId
	 *            订单id
	 * @return
	 */
	public static NDAUserExpress getUserExpress(DbSession db, int userId, int expressId) {
		Criteria criteria = db.createCriteria(NDAUserExpress.class);
		criteria.add(Restrictions.eq("userId", userId));
		criteria.add(Restrictions.eq("expressId", expressId));
		return (NDAUserExpress) criteria.uniqueResult();
	}

	/**
	 * 根据站点id 和 订单编号 获取订单
	 * 
	 * @param db
	 * @param expressNo
	 *            订单编号
	 * @param domainId
	 *            站点id
	 * @return
	 */
	public static NDAExpress getNDAExpress(DbSession db, String expressNo, int domainId) {
		Criteria criteria = db.createCriteria(NDAExpress.class);
		criteria.add(Restrictions.eq("expressNo", expressNo));
		if (domainId > 0) {
			criteria.add(Restrictions.eq("domainId", domainId));
		}
		return (NDAExpress) criteria.uniqueResult();
	}

	/**
	 * 根据订单id 获取订单
	 * 
	 * @param db
	 * @param expressId
	 *            订单id
	 * @return
	 */
	public static NDAExpress getNDAExpress(DbSession db, int expressId) {
		Criteria criteria = db.createCriteria(NDAExpress.class);
		criteria.add(Restrictions.eq("id", expressId));
		return (NDAExpress) criteria.uniqueResult();
	}
	/**/

	public static NDAUserExpress getNDAUserExpress(DbSession db, Integer expressId, Integer domainId) {
		Criteria criteria = db.createCriteria(NDAUserExpress.class);
		criteria.add(Restrictions.eq("expressId", expressId)).add(Restrictions.eq("domainId", domainId));
		return (NDAUserExpress) criteria.uniqueResult();
	}

	public static NDAUserExpress getNDAUserExpressByExpressIdAndUserId(DbSession db, Integer expressId,
			Integer userId) {
		Criteria criteria = db.createCriteria(NDAUserExpress.class);
		criteria.add(Restrictions.eq("expressId", expressId)).add(Restrictions.eq("userId", userId));
		return (NDAUserExpress) criteria.uniqueResult();
	}

	public static List<NDAUserExpress> getNDAUserExpressByExpressId(DbSession db, Integer expressId, Integer domainId) {
		Criteria criteria = db.createCriteria(NDAUserExpress.class);
		criteria.add(Restrictions.eq("expressId", expressId)).add(Restrictions.eq("domainId", domainId));
		List<NDAUserExpress> list = criteria.list();
		return list;
	}

	/**
	 * 订单编号 获取订单
	 * 
	 * @param db
	 * @param expressNo
	 *            订单编号
	 * @return
	 */
	public static List<NDAExpress> getNDAExpressByNo(DbSession db, String expressNo) {
		Criteria criteria = db.createCriteria(NDAExpress.class);
		criteria.add(Restrictions.eq("expressNo", expressNo));
		return criteria.list();
	}

	/**
	 * 根据用户id获取站点
	 * 
	 * @param db
	 * @param userId
	 *            用户id
	 * @return
	 */
	public static int getDomainIdByUserId(DbSession db, int userId) {
		Criteria criteria = db.createCriteria(NDAUser.class);
		criteria.add(Restrictions.eq("id", userId));
		NDAUser user = (NDAUser) criteria.uniqueResult();
		return user.getDomainId();
	}

	public static NDADomain getDomainByUserId(DbSession db, int userId) {
		Criteria criteria = db.createCriteria(NDADomain.class);
		criteria.add(Restrictions.eq("id", getDomainIdByUserId(db, userId)));
		NDADomain domain = (NDADomain) criteria.uniqueResult();
		return domain;
	}

	/**
	 * 根据订单id获取和用户的绑定关系
	 * 
	 * @param db
	 * @param expressId
	 *            订单id
	 * @return
	 */
	public static NDAUserExpress getUserByExpressId(DbSession db, int expressId) {
		Criteria criteria = db.createCriteria(NDAUserExpress.class);
		criteria.add(Restrictions.eq("expressId", expressId));
		criteria.add(Restrictions.eq("status", Constants.State.STATE_ACTIVE));
		return (NDAUserExpress) criteria.uniqueResult();
	}

	public static UserRole getRoleByUserId(DbSession db, int userId) {
		Criteria criteria = db.createCriteria(UserRole.class);
		criteria.add(Restrictions.eq("userId", userId));
		return (UserRole) criteria.uniqueResult();
	}

	public static Map<String, Object> getEmpty() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("recordsTotal", 0);
		result.put("recordsFiltered", 0);
		result.put("data", Collections.<Object>emptyList());
		return result;
	}

	/*
	 * 根据订单号获得所有报警信息
	 */
	public static List<NDAAlert> getAllAlertsByExpressId(DbSession dbSession, int expressId) {
		String sql = "SELECT * FROM nda_alert WHERE express_id=" + expressId;
		SQLQuery query = dbSession.createSQLQuery(sql);
		query.addEntity(NDAAlert.class);
		return query.list();
	}

	public static List<NDAAlert> getUnhandledAlertsByExpressId(DbSession dbSession, int expressId) {
		String sql = "SELECT * FROM nda_alert WHERE express_id=" + expressId + " AND status="
				+ Constants.AlertState.STATE_ACTIVE;
		SQLQuery query = dbSession.createSQLQuery(sql);
		query.addEntity(NDAAlert.class);
		return query.list();
	}

	public static List<NDATempExpress> getAllTempesByExpressId(DbSession dbSession, int expressId) {
		Criteria criteria = dbSession.createCriteria(NDATempExpress.class);
		criteria.add(Restrictions.eq("expressId", expressId));
		criteria.addOrder(Order.asc("creationTime"));
		return criteria.list();
	}

	public static List<NDATempExpress> getAllTempesByExpressIdDesc(DbSession dbSession, int expressId) {
		Criteria criteria = dbSession.createCriteria(NDATempExpress.class);
		criteria.add(Restrictions.eq("expressId", expressId));
		criteria.addOrder(Order.desc("creationTime"));
		return criteria.list();
	}

	public static NDATag getTagByTagNo(DbSession db, String tagNo) {
		Criteria criteria = db.createCriteria(NDATag.class);
		criteria.add(Restrictions.eq("tagNo", tagNo));
		NDATag tag = (NDATag) criteria.uniqueResult();
		return tag;
	}
}
