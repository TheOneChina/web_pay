package com.tnsoft.web.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;

import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.hibernate.model.NDAAlertLevel;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDATagExpress;

/**
 * 自动定时脚本，用于检测硬件是否未响应
 */
public class TagServices {

	public static Calendar CL = Calendar.getInstance();

	public TagServices() {
		super();
	}

	public Runnable getTagNotResponseService() {
		return new TagNotResponseService();
	}

	public static final class TagNotResponseService implements Runnable {

		@Override
		public void run() {
			DbSession db = BaseHibernateUtils.newSession();
			try {
				db.beginTransaction();

				List<NDAAlertLevel> levels = getNDAAlertLevel(db);
				for (NDAAlertLevel level : levels) {
					if (level != null && level.getStatus() == Constants.State.STATE_ACTIVE) {
						Date now = new Date();
						CL.setTime(new Date());
						// 获取指定时间段内未上传数据的智能硬件
						CL.add(Calendar.MINUTE, -((int) (level.getHours() * 60)));
						String sql = "SELECT * FROM nda_tag_express WHERE domain_id=" + level.getDomainId()
								+ " AND status=" + Constants.BindState.STATE_ACTIVE + " AND "
								+ "express_id NOT IN (SELECT express_id FROM nda_temperature_express WHERE creation_time >=:time)";
						SQLQuery query = db.createSQLQuery(sql);
						query.setParameter("time", CL.getTime());
						query.addEntity(NDATagExpress.class);
						List<?> list = query.list();
						if (!list.isEmpty()) {
							for (Object obj : list) {
								NDATagExpress n = (NDATagExpress) obj;
								List<NDAExpress> express = getExpressByTag(db, n.getTagNo());

								NDAAlert alert = new NDAAlert();
								alert.setAlertLevel(Constants.AlertLevel.STATE_NORAML_HIGH);
								alert.setType(Constants.AlertType.STATE_NOT_RESPONSE);
								alert.setCreationTime(now);
//								alert.setCsn(BaseHibernateUtils.nextCsn(db));
								alert.setDomainId(n.getDomainId());
								alert.setLastModitied(now);
								alert.setTagNo(n.getTagNo());
								alert.setStatus(Constants.AlertState.STATE_ACTIVE);
								db.save(alert);
								db.flush();

							}
						}
					}
				}

				db.commit();
			} finally {
				db.close();
			}
		}
	}

	private static List<NDAExpress> getExpressByTag(DbSession db, String tagNo) {

		String sql = "SELECT * FROM nda_express WHERE id IN (SELECT express_id FROM nda_tag_express WHERE tag_no='"
				+ tagNo + "' AND status=" + Constants.BindState.STATE_ACTIVE + ") ";
		SQLQuery query = db.createSQLQuery(sql);
		query.addEntity(NDAExpress.class);
		return (List<NDAExpress>) query.list();

	}

	private static List<NDAAlertLevel> getNDAAlertLevel(DbSession db) {
		Criteria criteria = db.createCriteria(NDAAlertLevel.class);
		criteria.add(Restrictions.eq("status", Constants.State.STATE_ACTIVE));
		return criteria.list();
	}

}
