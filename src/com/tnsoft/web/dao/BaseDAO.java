package com.tnsoft.web.dao;

import java.io.Serializable;
import java.util.List;

public interface BaseDAO<T> {

	public void save(T entity);

	public void update(T entity);

	public void delete(Serializable id);

	public Integer count(String hql, Object... params);
	
	public T getById(Serializable id);

	public T getOneByHQL(String hql, Object... params);
	
	public List<T> getAll();
	
	public List<T> getByHQL(String hql, Object... params);

	public List<T> getByHQLWithLimits(Integer first, Integer rows, String hql, Object... params);

	public void CUDByHql(String hql, Object... params);

	public void clear();

}
