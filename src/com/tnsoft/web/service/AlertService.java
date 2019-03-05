package com.tnsoft.web.service;

import java.util.List;

import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.web.model.LoginSession;

public interface AlertService extends BaseService<NDAAlert> {

	// 获取当前报警
	public Object getUnhandledAlerts(LoginSession lg);

	// 根据设备编号去查询该设备是否有报警
	public List<NDAAlert> getAlertByTagNo(String tagNo);
}
