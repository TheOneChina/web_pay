package com.tnsoft.web.dao;

import java.util.List;

import com.tnsoft.hibernate.model.NDAUserExpress;

public interface UserExpressDAO extends BaseDAO<NDAUserExpress> {

	public NDAUserExpress getUserExpress(Integer userId, Integer expressId);

	public List<NDAUserExpress> getUserExpressByUId(int start, int rows, Integer userId);

	public List<NDAUserExpress> getUserExpressByUId(Integer userId);

	public Integer getCountByUserId(Integer userId);

	public NDAUserExpress getUserExpressByEId(Integer expressId);

}
