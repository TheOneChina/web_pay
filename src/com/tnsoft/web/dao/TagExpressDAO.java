package com.tnsoft.web.dao;

import java.util.List;

import com.tnsoft.hibernate.model.NDATagExpress;

public interface TagExpressDAO extends BaseDAO<NDATagExpress> {

	public List<NDATagExpress> getTagExpressHistory(String tagNo);

	public NDATagExpress getTagExpressByEId(Integer expressId);
	
	public List<NDATagExpress> getTagExpressByTNo(String tagNo);
	
	public NDATagExpress getLastTagExpressByEId(Integer expressId);
}
