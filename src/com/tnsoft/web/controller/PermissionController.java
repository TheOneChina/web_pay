package com.tnsoft.web.controller;

import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import com.tnsoft.web.service.PermissionService;

@Controller
public class PermissionController {

	@Resource(name = "permissionService")
	private PermissionService perService;
	
	
}
