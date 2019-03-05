package com.tnsoft.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aliyuncs.exceptions.ClientException;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.RegisterService;
import com.tnsoft.web.util.SmsUtil;
import com.tnsoft.web.util.Utils;

@Controller
@RequestMapping("/register")
public class RegisterController extends BaseController {

	@Resource(name = "registerService")
	private RegisterService registerService;
	
	public RegisterController() {
		super();
	}
	
	@RequestMapping("/page")
	public String register(Model model, HttpServletRequest request) {
		return "view.register";
	}
	
	@RequestMapping("/sendCode")
	public String sendCode(String mobile, HttpSession session) {
		Response response = registerService.sendCode(mobile);
		if (response.getCode() == 0) {
			session.setAttribute("mobileCode", response.getMessage());
		}
		return response.getCode()+"";
	}
	
	@RequestMapping(value = "/submit", method = RequestMethod.POST)
	@ResponseBody
	public String submitRegister(String username, String password, String phone) {
		return registerService.creatNewUserAndDomain(username.trim(), password.trim(), phone.trim());
	}
}
