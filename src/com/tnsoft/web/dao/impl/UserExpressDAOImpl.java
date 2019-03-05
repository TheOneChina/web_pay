package com.tnsoft.web.dao.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAUserExpress;
import com.tnsoft.web.dao.UserDAO;
import com.tnsoft.web.dao.UserExpressDAO;
import com.tnsoft.web.dao.UserRoleDAO;

@Repository("userExpressDAO")
public class UserExpressDAOImpl extends BaseDAOImpl<NDAUserExpress> implements UserExpressDAO {

	@Resource(name = "userRoleDAO")
	private UserRoleDAO userRoleDao;
	@Resource(name = "userDAO")
	private UserDAO userDao;

	@Override
	public NDAUserExpress getUserExpress(Integer userId, Integer expressId) {
		// TODO Auto-generated method stub
		String hql = "from NDAUserExpress where userId=? and expressId=? ";
		NDAUserExpress ue = getOneByHQL(hql, userId, expressId);
		return ue;
	}

	@Override
	public Integer getCountByUserId(Integer userId) {
		// TODO Auto-generated method stub
		// 获得配送员手上所有持有的订单
		String hql = "select count(a) from NDAUserExpress a where userId=? and status=?";
		Integer recordsTotal = count(hql, userId, Constants.State.STATE_ACTIVE);
		return recordsTotal;
	}

	@Override
	public List<NDAUserExpress> getUserExpressByUId(int start, int rows, Integer userId) {
		// TODO Auto-generated method stub
		// 获得配送员手上所有持有的订单
		String hql = "from NDAUserExpress where userId=? and status=?";
		List<NDAUserExpress> ue = getByHQLWithLimits(start, rows, hql, userId, Constants.State.STATE_ACTIVE);
		return ue;
	}

	@Override
	public List<NDAUserExpress> getUserExpressByUId(Integer userId) {
		// TODO Auto-generated method stub
		// 获得配送员手上所有持有的订单
		String hql = "from NDAUserExpress where userId=? and status=?";
		List<NDAUserExpress> ue = getByHQL(hql, userId, Constants.State.STATE_ACTIVE);
		return ue;
	}

	@Override
	public NDAUserExpress getUserExpressByEId(Integer expressId) {
		// TODO Auto-generated method stub
		String hql = "from NDAUserExpress where expressId=? and status=?";
		NDAUserExpress userExpress = getOneByHQL(hql, expressId, Constants.State.STATE_ACTIVE);
		return userExpress;
	}

}
