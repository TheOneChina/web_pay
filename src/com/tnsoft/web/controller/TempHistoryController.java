/*
 * Copyright (c) 2016 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * 
 */
package com.tnsoft.web.controller;

import com.expertise.common.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;

import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDATempExpress;

import com.tnsoft.web.model.Result;
import com.tnsoft.web.servlet.ServletConsts;
import com.tnsoft.web.util.DBUtils;

import com.tnsoft.web.util.Utils;

import java.io.IOException;
import java.io.PrintWriter;

import java.math.BigInteger;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TempHistoryController extends BaseController {
    
    public static final SimpleDateFormat SF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();


    public TempHistoryController() {
        super();
    }
    
    
    @RequestMapping("/temperatureLog")
    public String temperatureLog(Model model, String id){
        Utils.saveLog(lg.getUserId(), "查看温度历史记录", lg.getDomainId());
        if(!validateUser()){
            return "redirect:/";
        }        
        
        model.addAttribute("expressNo", id);
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());
                        
        return "view.tag.temperatureLog";
    }

    @RequestMapping("/temperatureHistory")
    public String temperatureHistory(Model model, String id){
        
        if(!validateUser()){
            return "redirect:/";
        }        
        
        model.addAttribute("expressNo", id);
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());
                        
        return "view.tag.temperatureHistory";
    } 
    
    @RequestMapping("/ajaxTemp")
    @ResponseBody
    public void ajaxTemp(String expressNo, HttpServletResponse resp) throws IOException {
    	resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();  
        DbSession db = BaseHibernateUtils.newSession();
        Result result = new Result();
        List<String> time = new ArrayList<String>();
        List<String> temperature = new ArrayList<String>();
        List<String> humidity = new ArrayList<String>();
        try {
            
            NDAExpress express = null;
            
            if(lg.getDefRole().getRoleId() != Constants.Role.SUPER_ADMIN){
                express = DBUtils.getNDAExpress(db, expressNo, lg.getDomainId());
            } else {
                express = DBUtils.getNDAExpress(db, expressNo, 0);
            }
            if(express != null){
            	
            	List<NDATempExpress> list = DBUtils.getAllTempesByExpressId(db, express.getId());
                
                if(!list.isEmpty()){
                    for(NDATempExpress ne : list){
                        time.add(SF.format(ne.getCreationTime()));
                        temperature.add(String.format("%.2f", ne.getTemperature()));
                        humidity.add(String.format("%.2f", ne.getHumidity()));
                    }
                }                            
            }
            
        }
        finally {
            db.close();
        }
        result.setTime(time);
        result.setTemperature(temperature);
        result.setHumidity(humidity);
        
        if(temperature.isEmpty() || temperature.size() == 0 || humidity.isEmpty() || humidity.size() == 0 ){
            result.setCode(Result.ERROR);
        }        
        out.write(GSON.toJson(result));
    }


    @RequestMapping("/ajaxTempHistory")
    @ResponseBody
    public Object ajaxTempHistory(int draw,int start,int length, String expressNo) {
        if (!validateUser()){
            return "";
        }
                
        
        DbSession session = BaseHibernateUtils.newSession();
        try {
            
            NDAExpress express = null;
            
            if(lg.getDefRole().getRoleId() != Constants.Role.SUPER_ADMIN){
                express = DBUtils.getNDAExpress(session, expressNo, lg.getDomainId());
            } else {
                express = DBUtils.getNDAExpress(session, expressNo, 0);
            }
            
            if(express != null){
                Map<String, Object> result = query(session, draw, start, length, express.getId());
                
                //Logger.info("历史温度列表", GSON.toJson(result));
                
                return result;
            }
        }
        finally {
            session.close();
        }
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("recordsTotal", 0);
        result.put("recordsFiltered", 0);
        
        result.put("data", Collections.<NDATempExpress>emptyList());
        return result;
    }

    private Map<String, Object> query(DbSession db, int draw, int start, int length, int expressId){
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, String[]> properties = request.getParameterMap();
        
        String orderSql = " ORDER BY creation_time DESC";        

        String search = properties.get("search[value]")[0];
        String whereClause = "a.express_id=" + expressId;
        long recordsFiltered = 0;
        long recordsTotal = count(db, whereClause);
        result.put("recordsTotal", recordsTotal);

        if(!StringUtils.isEmpty(search)){
            recordsFiltered = count(db, whereClause);
        }else{
            recordsFiltered = recordsTotal;
        }
        result.put("recordsFiltered", recordsFiltered);
        
        result.put("data", query(db, whereClause, orderSql, start, length));
        return result;

    }

    private int count(DbSession session, String where) {
        
        String sql = "SELECT COUNT(a.*) FROM nda_temperature_express a ";
                
        if(!StringUtils.isEmpty(where)){
            sql += ( " WHERE " + where);
        }
                
        SQLQuery query = session.createSQLQuery(sql);
        BigInteger count = (BigInteger)query.uniqueResult();
        return count == null ? 0 : count.intValue();
    }
    
    private List<NDATempExpress> query(DbSession session, String where, String order, int offset, int limit) {
        
        String sql = "SELECT * FROM nda_temperature_express a ";
        if(!StringUtils.isEmpty(where)){
            sql += (" WHERE " + where);
        }
        
        if(!StringUtils.isEmpty(order)){
            sql += (" " + order);
        }
                                               
        SQLQuery query = session.createSQLQuery(sql);
        query.addEntity(NDATempExpress.class);
        BaseHibernateUtils.setLimit(query, offset, limit);
        List<?> list = query.list(); 
        if(!list.isEmpty()){
            List<NDATempExpress> result = new ArrayList<NDATempExpress>(list.size());
            for(Object obj : list){
                NDATempExpress e = (NDATempExpress)obj;
                e.setTmpValue(String.format("%.2f", e.getTemperature()));
                e.setHumidityValue(String.format("%.2f", e.getHumidity()));
                e.setTimeValue(SF.format(e.getCreationTime()));
                result.add(e);
            }
            return result;
        }
        
        return Collections.<NDATempExpress>emptyList();  
    }    
}
