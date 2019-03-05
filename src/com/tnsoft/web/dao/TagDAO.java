package com.tnsoft.web.dao;

import java.util.Set;

import com.tnsoft.hibernate.model.NDATag;

public interface TagDAO extends BaseDAO<NDATag> {

	public NDATag scanTag(String tagNo);

	public Set<NDATag> getDomainTag(Integer domainId);
	
}
