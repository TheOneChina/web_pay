package com.tnsoft.web.service;

import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.web.model.LoginSession;
import com.tnsoft.web.model.Response;

public interface ExpressService extends BaseService<NDAExpress> {

	public Response cancelSign(LoginSession lg, String expressNo);

	public Response saveTakingExpress(String expressNo, String tagNo, String description, Integer appointStart,
			Integer appointEnd, LoginSession lg);

	public Response signExpress(String[] expressNoList, LoginSession lg);

	public Response saveExpressAttribute(String expressValue, String expressFlag, String userId, String expressId);

	public Response ajaxEditExSleepTime(Integer expressId, String time);
	
	public Response editAppointStart(Integer expressId, String time);
	
	public Response editAppointEnd(Integer expressId, String time); 
	
}
