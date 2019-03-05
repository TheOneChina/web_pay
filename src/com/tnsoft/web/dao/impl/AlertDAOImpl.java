package com.tnsoft.web.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.web.dao.AlertDAO;

@Repository("alertDAO")
public class AlertDAOImpl extends BaseDAOImpl<NDAAlert> implements AlertDAO {

	@Override
	public List<NDAAlert> getAlertByTagNo(String tagNo) {
		// TODO Auto-generated method stub
		String hql = "from NDAAlert where tagNo=? and status=?";
		List<NDAAlert> list = getByHQL(hql, tagNo, Constants.AlertState.STATE_ACTIVE);
		return list;
	}

}
