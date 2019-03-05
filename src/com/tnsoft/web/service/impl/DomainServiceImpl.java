package com.tnsoft.web.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.tnsoft.hibernate.model.NDADomain;
import com.tnsoft.web.dao.DomainDAO;
import com.tnsoft.web.service.DomainService;

@Service("domainService")
public class DomainServiceImpl extends BaseServiceImpl<NDADomain> implements DomainService{
	
	@Resource(name="domainDAO")
	private DomainDAO domainDao;
	

}
