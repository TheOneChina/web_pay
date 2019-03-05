package com.tnsoft.web.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.web.dao.ExpressDAO;

@Repository("expressDAO")
public class ExpressDAOImpl extends BaseDAOImpl<NDAExpress> implements ExpressDAO {

	@Override
	public NDAExpress getExpressByNo(String expressNo, Integer domainId) {
		// TODO Auto-generated method stub
		String hql = "from NDAExpress where expressNo=? and domainId=?";
		NDAExpress express = getOneByHQL(hql, expressNo, domainId);
		return express;
	}

	@Override
	public List<NDAExpress> getAdminExpressToSign(Integer start, Integer rows, Integer domainId) {
		// TODO Auto-generated method stub
		String hql = "from NDAExpress where domainId=? and status<>?";
		List<NDAExpress> list = getByHQLWithLimits(start, rows, hql, domainId, Constants.ExpressState.STATE_FINISHED);
		return list;
	}

	@Override
	public Integer getCountByDomainId(Integer domainId) {
		// TODO Auto-generated method stub
		String hql = "select count(a) from NDAExpress a where domainId=? and status<>?";
		Integer recordsTotal = count(hql, domainId, Constants.ExpressState.STATE_FINISHED);
		return recordsTotal;
	}

	@Override
	public void saveExpressTemperature(Integer expressId, Float maxTemp, Float minTemp) {
		// TODO Auto-generated method stub
		String hql = "update NDAExpress set temperatureMax=? ,temperatureMin=? where id=?";
		CUDByHql(hql, maxTemp, minTemp, expressId);
	}

	@Override
	public void saveExpressSleepTime(Integer expressId, Float sleepTime) {
		// TODO Auto-generated method stub
		String hql = "update NDAExpress set sleepTime=? where id=?";
		CUDByHql(hql, sleepTime, expressId);
	}

	@Override
	public void saveExpressAppointStart(Integer expressId, Float appointStart) {
		// TODO Auto-generated method stub
		String hql = "update NDAExpress set appointStart=? where id=?";
		CUDByHql(hql, appointStart, expressId);
	}

	@Override
	public void saveExpressAppointEnd(Integer expressId, Float appointEnd) {
		// TODO Auto-generated method stub
		String hql = "update NDAExpress set appointEnd=? where id=?";
		CUDByHql(hql, appointEnd, expressId);
	}

}
