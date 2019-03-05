package com.tnsoft.web.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDATagExpress;
import com.tnsoft.web.dao.TagExpressDAO;

@Repository("tagExpressDAO")
public class TagExpressDAOImpl extends BaseDAOImpl<NDATagExpress> implements TagExpressDAO {
	@Override
	public List<NDATagExpress> getTagExpressHistory(String tagNo) {
		String hql = "from NDATagExpress where tagNo=? and status=?";
		List<NDATagExpress> list = getByHQL(hql, tagNo, Constants.State.STATE_FINISHED);
		return list;
	}

	@Override
	public NDATagExpress getTagExpressByEId(Integer expressId) {
		// TODO Auto-generated method stub
		String hql = "from NDATagExpress where expressId=? and status=?";
		NDATagExpress te = getOneByHQL(hql, expressId, Constants.State.STATE_ACTIVE);
		return te;
	}

	@Override
	public NDATagExpress getLastTagExpressByEId(Integer expressId) {
		// TODO Auto-generated method stub
		String hql = "from NDATagExpress where expressId=? and status=? order by lastModified DESC";
		List<NDATagExpress> list = getByHQL(hql, expressId, Constants.State.STATE_FINISHED);
		//根据时间排序之后,取得第一个,即为最后更新的
		return list.get(0);
	}

	@Override
	public List<NDATagExpress> getTagExpressByTNo(String tagNo) {
		// TODO Auto-generated method stub
		String hql="from NDATagExpress where tagNo=? and status=?";
		List<NDATagExpress> list=getByHQL(hql, tagNo, Constants.State.STATE_ACTIVE);
		return list;
	}

}
