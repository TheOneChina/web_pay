package com.tnsoft.web.dao;

import java.util.List;

import com.tnsoft.hibernate.model.NDAAlert;

public interface AlertDAO extends BaseDAO<NDAAlert>{

	public List<NDAAlert> getAlertByTagNo(String tagNo);
	
}
