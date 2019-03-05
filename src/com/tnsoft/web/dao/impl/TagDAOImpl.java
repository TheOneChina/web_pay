package com.tnsoft.web.dao.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.tnsoft.hibernate.model.NDATag;
import com.tnsoft.web.dao.TagDAO;

@Repository("tagDAO")
public class TagDAOImpl extends BaseDAOImpl<NDATag> implements TagDAO {

	@Override
	public NDATag scanTag(String tagNo) {
		// TODO Auto-generated method stub
		String hql = "from NDATag where tagNo=?";
		NDATag tag = (NDATag) getOneByHQL(hql, tagNo);
		return tag;
	}

	@Override
	public Set<NDATag> getDomainTag(Integer domainId) {
		// TODO Auto-generated method stub
		String hql = "from NDATag where domainId=?";
		List<NDATag> list = getByHQL(hql, domainId);
		Set<NDATag> tags = new HashSet<NDATag>(list);
		return tags;
	}

}
