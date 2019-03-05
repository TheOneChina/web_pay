package com.tnsoft.web.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tnsoft.hibernate.model.NDADomain;
import com.tnsoft.web.dao.DomainDAO;

@Repository("domainDAO")
public class DomainDAOImpl extends BaseDAOImpl<NDADomain> implements DomainDAO {

}
