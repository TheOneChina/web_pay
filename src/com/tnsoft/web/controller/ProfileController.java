package com.tnsoft.web.controller;

import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.web.model.Response;

import com.tnsoft.web.util.Utils;

import java.security.GeneralSecurityException;

import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ProfileController extends BaseController {
        
    public ProfileController() {
        super();
    }
    
    @RequestMapping("/profile")
    public String profile(Model model){
        
        
        if(!validateUser()){
            return "redirect:/";
        }        
        
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("nickname", lg.getNickName());
        model.addAttribute("id", lg.getUserId());
                        
        return "view.profile.profile";
    }
    
    @RequestMapping("/ajaxChangeProfile")
    @ResponseBody
    public Object ajaxChangeProfile(int id, String name, String pwd) {
        
        if(!validateUser()){
            Response resp = new Response(Response.ERROR);
            resp.setMessage("操作失败！");
            return resp;        
        }
                
        Response resp = new Response(Response.OK);

        DbSession db = BaseHibernateUtils.newSession();
        try {
            db.beginTransaction();
            
            Date now = new Date();
            NDAUser user = (NDAUser)db.get(NDAUser.class, id);
            if(user != null){
                user.setName(name);
                try {
                    user.setPassword(Utils.hash(name, Utils.newPassword(pwd)));
                } catch (GeneralSecurityException e) {
                }
                user.setLastModified(now);

                resp.setMessage("修改成功！");
            } else {
                resp.setCode(Response.ERROR);
                resp.setMessage("修改失败，该用户不存在！");
            }
            
            db.commit();
            
        }
        finally {
            db.close();
        }
                
        return resp;        
    }

}
