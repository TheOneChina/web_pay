package com.tnsoft.web.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.SQLQuery;
import org.springframework.stereotype.Service;

import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.hibernate.model.NDAUserExpress;
import com.tnsoft.web.dao.AlertDAO;
import com.tnsoft.web.model.AlertElem;
import com.tnsoft.web.model.ExpressWithAlerts;
import com.tnsoft.web.model.LoginSession;
import com.tnsoft.web.service.AlertService;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;

@Service("alertService")
public class AlertServiceImpl extends BaseServiceImpl<NDAAlert> implements AlertService {

	@Resource(name = "alertDAO")
	private AlertDAO alertDao;

	@Override
	public Object getUnhandledAlerts(LoginSession lg) {
		// TODO Auto-generated method stub
		Map<String, Object> ans = new HashMap<String, Object>();
		List<ExpressWithAlerts> result = new ArrayList<ExpressWithAlerts>();

		DbSession session = BaseHibernateUtils.newSession();
		try {
			int roleId = lg.getRoles().get(0).getRoleId();
			String sql;
			if (roleId == Constants.Role.SUPER_ADMIN) {
				sql = "SELECT * FROM nda_alert a WHERE a.status=" + Constants.AlertState.STATE_ACTIVE
						+ "AND a.express_id IN (SELECT express_id FROM nda_user_express WHERE domain_id="
						+ lg.getDomainId() + ") order by a.creation_time DESC ";
			} else {
				sql = "SELECT * FROM nda_alert a WHERE a.status=" + Constants.AlertState.STATE_ACTIVE
						+ "AND a.express_id IN (SELECT express_id FROM nda_user_express WHERE user_id=" + lg.getUserId()
						+ ") order by a.creation_time DESC ";
			}

			SQLQuery query = session.createSQLQuery(sql);
			query.addEntity(NDAAlert.class);
			// 获取相应的报警信息
			List<?> list = query.list();
			if (!list.isEmpty()) {
				for (Object object : list) {
					NDAAlert ndaAlert = (NDAAlert) object;
					AlertElem alertElem = new AlertElem();

					if (ndaAlert.getStatus() == Constants.AlertState.STATE_ACTIVE) {
						alertElem.setStatusName("未处理");
					} else {
						alertElem.setStatusName("已处理");
					}

					alertElem.setTagNo(ndaAlert.getTagNo());
					if (ndaAlert.getType() == Constants.AlertType.STATE_TEMPHISALERT) {
						if (ndaAlert.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_HIGH) {
							alertElem.setAlertName("温度过高");
						} else if (ndaAlert.getAlertLevel() == Constants.AlertLevel.STATE_NOT_RESPONSE) {
							alertElem.setAlertName("模块失联");
						} else if (ndaAlert.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_LOW) {
							alertElem.setAlertName("温度过低");
						} else {
							alertElem.setAlertName("严重报警");
						}
					}
					if (ndaAlert.getType() == Constants.AlertType.STATE_ELECTRICITY) {
						if (ndaAlert.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_LOW) {
							alertElem.setAlertName("电量不足百分之60");
						} else if (ndaAlert.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_HIGH) {
							alertElem.setAlertName("电量不足百分之40");
						} else if(ndaAlert.getAlertLevel() == Constants.AlertLevel.STATE_SERIOUS){
							alertElem.setAlertName("电量不足百分之20,请尽快充电");
						}
					}
					alertElem.setTime(Utils.SF.format(ndaAlert.getCreationTime()));

					ExpressWithAlerts eWithAlerts = null;
					for (ExpressWithAlerts eWithAlertsTemp : result) {
						if (eWithAlertsTemp.getExpressId() == ndaAlert.getExpressId()) {
							eWithAlerts = eWithAlertsTemp;
							break;
						}
					}
					if (eWithAlerts == null) {
						eWithAlerts = new ExpressWithAlerts();
						eWithAlerts.setExpressId(ndaAlert.getExpressId());
						eWithAlerts.setDomainId(ndaAlert.getDomainId());
						eWithAlerts.updateAlertsShow(
								"<table class=\"table table-striped table-bordered\"><tr><td>报警时间</td><td>报警类型</td><td>状态</td><td>模块编号</td></tr>");
						NDAExpress express = DBUtils.getNDAExpress(session, ndaAlert.getExpressId());
						if (express != null) {
							eWithAlerts.setExpressNo(express.getExpressNo());
						}

						NDAUserExpress ue = DBUtils.getUserByExpressId(session, ndaAlert.getExpressId());
						if (ue != null) {
							NDAUser user = (NDAUser) session.get(NDAUser.class, ue.getUserId());
							if (user != null) {
								eWithAlerts.setUserName(user.getNickName());
								eWithAlerts.setMobile(user.getMobile());
							}
						}
						result.add(eWithAlerts);
					}
					eWithAlerts.addAlertElem(alertElem);
					StringBuilder sb = new StringBuilder("");
					sb.append("<tr><td>").append(alertElem.getTime());
					sb.append("</td><td>").append(alertElem.getAlertName());
					sb.append("</td><td>").append(alertElem.getStatusName());
					sb.append("</td><td>").append(alertElem.getTagNo()).append("</td></tr>");
					eWithAlerts.updateAlertsShow(sb.toString());
				}

				for (int i = 0; i < result.size(); i++) {
					result.get(i).updateAlertsShow("</table>");
				}
			}
		} finally {
			session.close();
		}
		ans.put("data", result);
		return ans;

	}

	@Override
	public List<NDAAlert> getAlertByTagNo(String tagNo) {
		// TODO Auto-generated method stub
		List<NDAAlert> list=alertDao.getAlertByTagNo(tagNo);
		return list;
	}
}
