package com.tnsoft.web.service;

import java.io.Serializable;
import java.util.List;

public interface BaseService<T> {

	public void save(T entity);
	
	public void update(T entity);
	
	public void delete(Serializable id);
	
	public Integer count(String hql, Object... params);
	
	public List<T> getAll();
	
	public T getById(Serializable id);
	
	public List<T> getByHQL(String hql, Object... params);
	
	public List<T> getByHQLWithLimits(Integer first, Integer rows, String hql, Object... params);
}
