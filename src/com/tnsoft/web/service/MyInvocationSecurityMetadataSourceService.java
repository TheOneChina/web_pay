package com.tnsoft.web.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.tnsoft.hibernate.model.Permission;
import com.tnsoft.web.dao.PermissionDAO;

public class MyInvocationSecurityMetadataSourceService implements FilterInvocationSecurityMetadataSource {

	@Autowired
	private PermissionDAO PermissionDAO;
	
	private HashMap<String, Collection<ConfigAttribute>> map = null;
	
	public void loadResourceDefine(){
		map = new HashMap<>();
		Collection<ConfigAttribute> configAttributes;
		ConfigAttribute configAttribute;
		List<Permission> permissions = PermissionDAO.findAll();
		for (Permission permission : permissions) {
			configAttributes = new ArrayList<>();
			configAttribute = new SecurityConfig(permission.getName());
			configAttributes.add(configAttribute);
			map.put(permission.getUrl(), configAttributes);
		}
	}
	
	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes() {
		return null;
	}

	@Override
	public Collection<ConfigAttribute> getAttributes(Object obj) throws IllegalArgumentException {
		if (map == null) {
			loadResourceDefine();
		}
		HttpServletRequest request = ((FilterInvocation)obj).getHttpRequest();
		String resUrl;
		AntPathRequestMatcher matcher;
		for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
			resUrl = iterator.next();
			matcher = new AntPathRequestMatcher(resUrl);
			if (matcher.matches(request)) {
				return map.get(resUrl);
			}
		}
		return null;
	}

	@Override
	public boolean supports(Class<?> class1) {
		return true;
	}
}
