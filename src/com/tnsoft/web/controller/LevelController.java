/*
 * Copyright (c) 2016 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * 
 */
package com.tnsoft.web.controller;

import com.expertise.common.util.StringUtils;

import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAAlertLevel;

import com.tnsoft.hibernate.model.NDATag;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.web.model.SelectItem;

import com.tnsoft.web.util.Utils;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LevelController extends BaseController {
//    
//        
//    public LevelController() {
//        super();
//    }
//    
//    @RequestMapping("/deleteAlertLevel")
//    public String deleteAlertLevel(Model model, String id, int mode, RedirectAttributes attr) {
//        if(!validateUser()){
//            return "view.login";
//        }
//                        
//        DbSession db = BaseHibernateUtils.newSession();
//        try {
//            db.beginTransaction();
//            
//            Date now = new Date();
//            if(!StringUtils.isEmpty(id)){
//                NDAAlertLevel user = (NDAAlertLevel)db.get(NDAAlertLevel.class, Integer.parseInt(id));
//                if(user != null){
//                    if(mode == 1){
//                        user.setLastModified(now);
//                        attr.addFlashAttribute("message", "报警级别关闭成功");
//                        user.setStatus(Constants.State.STATE_DISABLED);
//                        Utils.saveLog(lg.getUserId(), "关闭报警级别", lg.getDomainId());
//
//                    } else {
//                        user.setLastModified(now);
//                        attr.addFlashAttribute("message", "报警级别启用成功");
//                        user.setStatus(Constants.State.STATE_ACTIVE);
//                        Utils.saveLog(lg.getUserId(), "启用报警级别", lg.getDomainId());
//
//                    }
//                }
//            }
//            
//            db.commit();
//            
//        }
//        finally {
//            db.close();
//        }
//        
//        attr.addFlashAttribute("error", true);
//
//        
//        return "redirect:/level";
//    }
//    
//    @RequestMapping("/level")
//    public String level(Model model){
//        Utils.saveLog(lg.getUserId(), "查看报警级别列表", lg.getDomainId());
//        if(!validateUser()){
//            return "redirect:/";
//        }        
//        
//        model.addAttribute("username", lg.getUserName());
//        model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());
//                        
//        return "view.level.level";
//    }
//    
//    @RequestMapping("/editLevel")
//    public ModelAndView editLevel(Model model, String id) {
//        
//        if(!validateUser()){
//            return new ModelAndView("redirect:/");
//        }        
//        
//        NDAAlertLevel level = null;
//        
//        if(!StringUtils.isEmpty(id)){
//            model.addAttribute("id", id);
//            DbSession session = BaseHibernateUtils.newSession();
//            try {
//                level = (NDAAlertLevel)session.get(NDAAlertLevel.class, Integer.parseInt(id));
//            }
//            finally {
//                session.close();
//            }
//        } else {
//            level = new NDAAlertLevel();
//        }
//        
//        model.addAttribute("username", lg.getUserName());
//        model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());
//        return new ModelAndView("view.level.editLevel", "command", level);
//    }
//    
//    @RequestMapping("/saveLevel")
//    public String saveLevel(Model model, String id, String hours, int times, RedirectAttributes attr) {
//        
//        if(!validateUser()){
//            return "view.login";
//        }
//        
//        float hour = 0;
//        
//        try{
//            hour = Float.parseFloat(hours);
//        } catch(Exception e){
//        }        
//        
//        DbSession db = BaseHibernateUtils.newSession();
//        try {
//            db.beginTransaction();
//            
//            Date now = new Date();
//            if(!StringUtils.isEmpty(id)){
//                NDAAlertLevel level = (NDAAlertLevel)db.get(NDAAlertLevel.class, Integer.parseInt(id));
//                if(level != null){
//                    level.setHours(hour);
//                    level.setTimes(times);
//                    level.setLastModified(now);
//                }
//            }
//            
//            db.commit();
//            
//        }
//        finally {
//            db.close();
//        }
//        
//        attr.addFlashAttribute("error", true);
//        attr.addFlashAttribute("message", "报警级别设置成功");
//        Utils.saveLog(lg.getUserId(), "设置报警级别", lg.getDomainId());
//        return "redirect:/level";
//    }
//
//    @RequestMapping("/ajaxLevel")
//    @ResponseBody
//    public Object ajaxLevel(int draw,int start,int length) {
//        if (!validateUser()){
//            return "";
//        }
//        
//        DbSession session = BaseHibernateUtils.newSession();
//        try {
//            Map<String, Object> result = query(session, draw, start, length," order by id ASC ");
//            return result;
//        }
//        finally {
//            session.close();
//        }
//    }
//
//    private Map<String, Object> query(DbSession db, int draw, int start, int length, String defaultOrderBy){
//        Map<String, Object> result = new HashMap<String, Object>();
//        Map<String, String[]> properties = request.getParameterMap();
//        
//        String orderSql = "";        
//
//        String search = properties.get("search[value]")[0];
//        
//        long recordsFiltered = 0;
//        long recordsTotal = count(db, null);
//        result.put("recordsTotal", recordsTotal);
//        
//        String whereClause ="a.domain_id=" + lg.getDomainId();
//        
//        if (!StringUtils.isEmpty(search)){
//             
//            whereClause += " AND (a.express_no LIKE '%"+search+"%') ";
//        }                
//        
//        
//        if (!StringUtils.isEmpty(whereClause)){
//            
//            whereClause += " AND a.domain_id=" + lg.getDomainId();
//        }                        
//
//        if(!StringUtils.isEmpty(search)){
//            recordsFiltered = count(db, whereClause);
//        }else{
//            recordsFiltered = recordsTotal;
//        }
//        result.put("recordsFiltered", recordsFiltered);
//        
//        result.put("data", query(db, whereClause, orderSql, start, length));
//        return result;
//
//    }
//
//    private int count(DbSession session, String where) {
//        
//        String sql = "SELECT COUNT(a.*) FROM nda_alert_level a ";
//                
//        if(!StringUtils.isEmpty(where)){
//            sql += ( " WHERE " + where);
//        }
//                
//        SQLQuery query = session.createSQLQuery(sql);
//        BigInteger count = (BigInteger)query.uniqueResult();
//        return count == null ? 0 : count.intValue();
//    }
//    
//    private List<NDAAlertLevel> query(DbSession session, String where, String order, int offset, int limit) {
//        
//        String sql = "SELECT * FROM nda_alert_level a ";
//        if(!StringUtils.isEmpty(where)){
//            sql += (" WHERE " + where);
//        }
//        
//        if(!StringUtils.isEmpty(order)){
//            sql += (" " + order);
//        }
//                                               
//        SQLQuery query = session.createSQLQuery(sql);
//        query.addEntity(NDAAlertLevel.class);
//        BaseHibernateUtils.setLimit(query, offset, limit);
//        List<?> list = query.list(); 
//        if(!list.isEmpty()){
//            List<NDAAlertLevel> result = new ArrayList<NDAAlertLevel>(list.size());
//            for(Object obj : list){
//                NDAAlertLevel e = (NDAAlertLevel)obj;
//                
//                e.setStatusKey(e.getStatus() == Constants.State.STATE_ACTIVE ? "启用" : "关闭");
//                result.add(e);
//            }
//            return result;
//        }
//        
//        return Collections.<NDAAlertLevel>emptyList();  
//    }    
//
}
