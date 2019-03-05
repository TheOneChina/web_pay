package com.tnsoft.web.security;

import com.expertise.common.codec.Hex;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.hibernate.model.Permission;
import com.tnsoft.web.dao.PermissionDAO;
import com.tnsoft.web.dao.UserDAO;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginUserDetailsService implements UserDetailsService {
        
    @Autowired
    private UserDAO userDAO;        
    
    @Autowired
	private PermissionDAO permissionDAO;

    public LoginUserDetailsService() {
    }

    @Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		NDAUser user = userDAO.getUserByName(username);
		if (user != null) {
			List<Permission> permissions = permissionDAO.findByUserId(user.getId());
			List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
			for(Permission permission : permissions){
				if (null != permission && null != permission.getName()) {
					GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(permission.getName());
					grantedAuthorities.add(grantedAuthority);
				}
			}
			boolean enabled = true;
	        boolean accountNonExpired = true;
	        boolean credentialsNonExpired = true;
	        boolean accountNonLocked = true;
			return new LoginUser( user.getId(),
                    (!StringUtils.isEmpty(user.getStaffNo()) ? "-"+user.getStaffNo() : ""),
                    user.getName(), 
                    Hex.toHexString(user.getPassword()), 
                    enabled, 
                    accountNonExpired, 
                    credentialsNonExpired, 
                    accountNonLocked,
                    grantedAuthorities);
		}else {
			throw new UsernameNotFoundException(username + "，该用户名不存在！");
		}
	}
}
