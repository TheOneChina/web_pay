package com.tnsoft.web.dao;

import java.util.List;

import com.tnsoft.hibernate.model.NDAExpress;

public interface ExpressDAO extends BaseDAO<NDAExpress> {

	public NDAExpress getExpressByNo(String expressNo, Integer domainId);

	public List<NDAExpress> getAdminExpressToSign(Integer start, Integer rows, Integer domainId);

	public Integer getCountByDomainId(Integer domainId);

	public void saveExpressTemperature(Integer expressId, Float maxTemp, Float minTemp);

	public void saveExpressSleepTime(Integer expressId, Float sleepTime);

	public void saveExpressAppointStart(Integer expressId, Float appointStart);
	
	public void saveExpressAppointEnd(Integer expressId, Float appointEnd);
}
