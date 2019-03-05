package com.tnsoft.web.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.hibernate.SQLQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDATag;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.TagService;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;

@Controller
public class TagController extends BaseController {

	@Resource(name = "tagService")
	private TagService tagService;

	public TagController() {
		super();
	}

	@RequestMapping("/tags")
	public String tags(Model model) {
		Utils.saveLog(lg.getUserId(), "查看模块列表", lg.getDomainId());
		if (!validateUser()) {
			return "redirect:/";
		}

		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());
		
		return "view.tag.tags";
	}
	

// 导出所有Bin文件	
//	@RequestMapping("/exportAll")
//	public void exportAll() {
//		List<NDATag> tags = tagService.getAll();
//		for (NDATag ndaTag : tags) {
//			String tmp = ndaTag.getTagNo();        
//	        byte[] arr = new byte[128];
//	        byte[] t = StringUtils.toBytesQuietly(tmp);
//	        System.arraycopy(t, 0, arr, 0, t.length);
//	        FileOutputStream fop = null;
//	        String name = ndaTag.getName();
//	        String path = null;
//	        if (name!=null && name.length()>0) {
//	        	path = "c:/coldchain/" + ndaTag.getName() + ".bin";
//			}else {
//				path = "c:/coldchain/" + ndaTag.getTagNo() + ".bin";
//			}
//	        
//	        try {
//	        	File file = new File(path);
//	        	   fop = new FileOutputStream(file);
//	        	   if (!file.exists()) {
//	        	    file.createNewFile();
//	        	   }
//	        	   fop.write(arr, 0, arr.length);
//	        	   System.out.println("Done!   " + path);
//	        	   fop.flush();
//	        	   fop.close();
//
//			} catch (Exception e) {
//				// TODO: handle exception
//			} finally {
//				if (fop!=null) {
//					try {
//						fop.close();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//	        	   
//		}
//	}
	
	@RequestMapping("/calibrate")
	public ModelAndView calibrate(String tagNo, Model model) {
		Utils.saveLog(lg.getUserId(), "校准设备", lg.getDomainId());
		if (!validateUser()) {
			return new ModelAndView("redirect:/");
		}
		if(lg.getDefRole().getRoleId()!=Constants.Role.SUPER_ADMIN){
			return new ModelAndView("redirect:/");
		}

		return new ModelAndView("view.tag.calibrate");
	}
	
	@RequestMapping("/APConfig")
	public ModelAndView APConfig(String tagNo, Model model) {
		Utils.saveLog(lg.getUserId(), "设置AP", lg.getDomainId());

		if (!validateUser()) {
			return new ModelAndView("redirect:/");
		}
		// 获得tag
		NDATag tag = tagService.getById(tagNo);
		model.addAttribute("tagNo", tagNo);

		return new ModelAndView("view.tag.apConfig", "command", tag);

	}

	@RequestMapping("/saveTagAPConfig")
	public String saveTagAPConfig(String SSID, String password, String tagNo, Model model, RedirectAttributes attr) {
		Utils.saveLog(lg.getUserId(), "保存AP密码", lg.getDomainId());

		if (!validateUser()) {
			return "redirect:/";
		}
		Response res = tagService.saveTagAPConfig(SSID, password, tagNo);
		if (res.getCode() == 0) {
			attr.addFlashAttribute("message", "wifi设置成功");
		} else {
			attr.addFlashAttribute("message", "设置失败");
		}
		attr.addFlashAttribute("error", true);
		return "redirect:/tags";

	}

	@RequestMapping("/editTag")
	public String editTag(String[] tagNos, HttpSession session) {
		if (!validateUser()) {
			return "redirect:/";
		}
		// 传过来的是list
		session.setAttribute("tagNos", tagNos);
		return "view.tag.editTag";
	}

	@RequestMapping("/saveEditTag")
	public String saveEditTag(String SSID, Integer buzzer, String password, Integer appointStart, HttpSession session,
			RedirectAttributes attr) {
		Utils.saveLog(lg.getUserId(), "批量设置设备", lg.getDomainId());
		String[] tagNos = (String[]) session.getAttribute("tagNos");
		Response res = tagService.editTag(tagNos, SSID, password, buzzer, appointStart);

		attr.addFlashAttribute("message", res.getMessage());
		attr.addFlashAttribute("error", true);
		return "redirect:/tags";
	}

	@RequestMapping("/tagTemplate")
	public String tagTemplate(String[] tagNos, Integer model, RedirectAttributes attr) {
		Utils.saveLog(lg.getUserId(), "使用场景模板", lg.getDomainId());
		Response res = tagService.tagTemplate(tagNos, model);

		attr.addFlashAttribute("message", res.getMessage());
		attr.addFlashAttribute("error", true);
		return "redirect:/tags";
	}

	@RequestMapping("/temperature")
	public ModelAndView temperature(Model model, String id) {
		Utils.saveLog(lg.getUserId(), "查看模块温度记录", lg.getDomainId());
		if (!validateUser()) {
			return new ModelAndView("redirect:/");
		}
		NDATag tag = null;

		if (!StringUtils.isEmpty(id)) {
			model.addAttribute("id", id);
			tag = tagService.getById(id);
		} else {
			tag = new NDATag();
		}

		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());
		return new ModelAndView("view.tag.temperature", "command", tag);
	}

	@RequestMapping("/saveTemperature")
	public String saveTemperature(Model model, String tagNo, String temperatureMin, String temperatureMax,
			RedirectAttributes attr) {
		if (!validateUser()) {
			return "view.login";
		}

		Float lowValue = null;
		Float highValue = null;

		try {
			if (!StringUtils.isEmpty(temperatureMin)) {
				lowValue = Float.parseFloat(temperatureMin);
			}
		} catch (Exception e) {
		}

		try {
			if (!StringUtils.isEmpty(temperatureMax)) {
				highValue = Float.parseFloat(temperatureMax);
			}
		} catch (Exception e) {
		}

		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();

			Date now = new Date();
			if (!StringUtils.isEmpty(tagNo)) {
				NDATag tag = (NDATag) db.get(NDATag.class, tagNo);
				if (tag != null) {
					tag.setLastModitied(now);
					if (lowValue != null) {
						tag.setTemperatureMin(lowValue);
					} else {
						tag.setTemperatureMin(null);
					}
					if (highValue != null) {
						tag.setTemperatureMax(highValue);
					} else {
						tag.setTemperatureMax(null);
					}
				}
			}

			db.commit();

		} finally {
			db.close();
		}

		attr.addFlashAttribute("error", true);
		attr.addFlashAttribute("message", "模块温度设置成功");
		Utils.saveLog(lg.getUserId(), "设置模块温度", lg.getDomainId());
		return "redirect:/tags";
	}

	@RequestMapping("/createTag")
	@ResponseBody
	public Object createTag(Model model, Integer times) {
		Response resp = new Response(Response.ERROR);
		resp.setCode(0);
		resp.setMessage("新增成功");
		if (!validateUser()) {
			resp.setMessage("新增失败！");
			return resp;
		}
		if (lg.getDefRole().getRoleId() != Constants.Role.SUPER_ADMIN) {
			resp.setMessage("无权新增！");
			return resp;
		}
		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();
			for (int i = 0; i < times; i++) {
				NDATag tag = new NDATag();
				tag.setStatus(Constants.TagState.STATE_ACTIVE);
				tag.setTagNo(Utils.getUUID());
				tag.setSSID("znll");// 新出厂的wifi名
				tag.setPassword("88886666");// 新出厂的wifi密码
				tag.setElectricity(100);// 新出厂的电量100满格
				tag.setPrecision(0f);// 新出厂的误差为0
				tag.setBuzzer(Constants.TagBuzzerState.STATE_OFF);//新出厂的关闭蜂鸣器
				tag.setSleepTime(0);// 新出厂的长睡眠
				db.save(tag);
			}
			db.commit();
		} finally {
			db.close();
		}

		Utils.saveLog(lg.getUserId(), "新增模块", lg.getDomainId());

		return resp;
	}

	@RequestMapping("/scanTag") // 就是比对,数据库中有就显示
	@ResponseBody
	public Response scanTag(String tagNo) {
		Utils.saveLog(lg.getUserId(), "扫描模块", lg.getDomainId());
		if (!validateUser()) {
			return new Response(1, "扫描失败");
		}
		Response res = tagService.scanTag(tagNo, lg.getDomainId());
		return res;
	}
	
	@RequestMapping("/makeTag") // 直接入库方便测试,之后会删除
	@ResponseBody
	public Response makeTag(String tagNo) {
		Utils.saveLog(lg.getUserId(), "直接入库", lg.getDomainId());
		if (!validateUser()) {
			return new Response(1, "操作失败");
		}
		Response res = tagService.makeTag(tagNo, lg.getDomainId());
	    
		return res;
	}
	
	
	@RequestMapping("/editBuzzer")
	public String editBuzzer(String tagNo, int mode, RedirectAttributes attr) {
		if (!validateUser()) {
			return "view.login";
		}
		// 该参数model不是modelAndView中的Model,是蜂鸣器的关闭模式还是开启模式
		Response res = tagService.editBuzzer(tagNo, mode);
		attr.addFlashAttribute("message", res.getMessage());
		attr.addFlashAttribute("error", true);

		return "redirect:/tags";
	}

	@RequestMapping("/editAppointStart")
	@ResponseBody
	public Object editAppointStart(String id, String time) {
		if (!validateUser()) {
			return "view.login";
		}
		Response res = tagService.editAppointStart(id, time);

		return res;
	}

	@RequestMapping("/deleteTag")
	public String deleteTag(Model model, String id, int mode, RedirectAttributes attr) {

		if (!validateUser()) {
			return "view.login";
		}

		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();

			Date now = new Date();
			if (!StringUtils.isEmpty(id)) {
				NDATag tag = (NDATag) db.get(NDATag.class, id);
				if (tag != null) {
					tag.setLastModitied(now);
					if (mode == 1) {
						attr.addFlashAttribute("message", "模块禁用成功");
						Utils.saveLog(lg.getUserId(), "禁用模块", lg.getDomainId());
						tag.setStatus(Constants.TagState.STATE_DELETE);
					} else {
						attr.addFlashAttribute("message", "模块启用成功");
						Utils.saveLog(lg.getUserId(), "启用模块", lg.getDomainId());
						tag.setStatus(Constants.TagState.STATE_ACTIVE);
						if (DBUtils.hasBind(db, tag.getTagNo())) {
							tag.setStatus(Constants.TagState.STATE_WORKING);
						}
					}
				}
			}
			db.commit();
		} finally {
			db.close();
		}

		attr.addFlashAttribute("error", true);

		return "redirect:/tags";
	}

	@RequestMapping("/tagHistory")
	public String tagHistory(Model model) {

		if (!validateUser()) {
			return "redirect:/";
		}
		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName());

		return "view.tag.history";
	}

	@RequestMapping("/ajaxEditSleepTime")
	@ResponseBody
	public Object ajaxEditSleepTime(String id, String time) {
		if (!validateUser()) {
			Response resp = new Response(Response.ERROR);
			resp.setMessage("设置失败！");
			return resp;
		}

		DbSession session = BaseHibernateUtils.newSession();
		try {
			session.beginTransaction();

			NDATag tag = (NDATag) session.get(NDATag.class, id);
			if (tag != null) {
				tag.setSleepTime(Integer.parseInt(time));
			}
			session.commit();
		} finally {
			session.close();
		}
		Response resp = new Response(Response.OK);
		resp.setMessage("设置成功！");
		Utils.saveLog(lg.getUserId(), "设置模块睡眠时间", lg.getDomainId());
		return resp;

	}

	@RequestMapping("/ajaxEditName")
	@ResponseBody
	public Object ajaxEditName(String id, String name) {
		if (!validateUser()) {
			Response resp = new Response(Response.ERROR);
			resp.setMessage("模块名称编辑失败！");
			return resp;
		}

		DbSession session = BaseHibernateUtils.newSession();
		try {
			session.beginTransaction();

			NDATag tag = (NDATag) session.get(NDATag.class, id);
			if (tag != null) {
				tag.setName(name);
			}

			session.commit();
		} finally {
			session.close();
		}
		Response resp = new Response(Response.OK);
		resp.setMessage("模块名称编辑成功！");
		Utils.saveLog(lg.getUserId(), "编辑模块名称", lg.getDomainId());
		return resp;

	}

	@RequestMapping("/ajaxTags")
	@ResponseBody
	public Object ajaxTags(int draw, int start, int length) {
		if (!validateUser()) {
			return "";
		}
		DbSession session = BaseHibernateUtils.newSession();
		try {
			Map<String, Object> result = query(session, draw, start, length, " order by id ASC ");
			return result;
		} finally {
			session.close();
		}
	}

	private Map<String, Object> query(DbSession db, int draw, int start, int length, String defaultOrderBy) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, String[]> properties = request.getParameterMap();

		String orderSql = "ORDER BY a.creation_time ASC";

		String search = properties.get("search[value]")[0];

		String whereClause = "";
		if (lg.getDefRole().getRoleId() != Constants.Role.SUPER_ADMIN) {
			whereClause = " a.domain_id=" + lg.getDomainId();
		}

		long recordsFiltered = 0;
		long recordsTotal = count(db, whereClause);
		result.put("recordsTotal", recordsTotal);

		// String whereClause ="a.domain_id="+lg.getDomainId();

		if (!StringUtils.isEmpty(search)) {
			if (!StringUtils.isEmpty(whereClause)) {
				whereClause += " AND ";
			}

			whereClause += " (a.tag_no LIKE '%" + search + "%') ";
		}

		String status = properties.get("columns[5][search][value]")[0];
		if (!StringUtils.isEmpty(status)) {
			if (!StringUtils.isEmpty(whereClause)) {
				whereClause += " AND ";
			}
			whereClause += " a.status=" + status + " ";
		}

		if (!StringUtils.isEmpty(search) || !StringUtils.isEmpty(status)) {
			recordsFiltered = count(db, whereClause);
		} else {
			recordsFiltered = recordsTotal;
		}
		result.put("recordsFiltered", recordsFiltered);

		result.put("data", query(db, whereClause, orderSql, start, length));
		return result;

	}

//	@RequestMapping("/editPickTime")
//	@ResponseBody
//	public Response editPickTime(String id, String time) {
//		if (!validateUser()) {
//			Response resp = new Response(Response.ERROR);
//			resp.setMessage("采集时间设置失败！");
//			return resp;
//		}
//		Utils.saveLog(lg.getUserId(), "设置模块采集时间", lg.getDomainId());
//		Response res = tagService.editPickTime(id, time);
//		return res;
//
//	}

	private int count(DbSession session, String where) {

		String sql = "SELECT COUNT(a.*) FROM nda_tag a ";

		if (!StringUtils.isEmpty(where)) {
			sql += (" WHERE " + where);
		}

		SQLQuery query = session.createSQLQuery(sql);
		BigInteger count = (BigInteger) query.uniqueResult();
		return count == null ? 0 : count.intValue();
	}

	private List<NDATag> query(DbSession session, String where, String order, int offset, int limit) {

		String sql = "SELECT * FROM nda_tag a ";

		if (!StringUtils.isEmpty(where)) {
			sql += (" WHERE " + where);
		}

		if (!StringUtils.isEmpty(order)) {
			sql += (" " + order);
		}
		SQLQuery query = session.createSQLQuery(sql);
		query.addEntity(NDATag.class);
		BaseHibernateUtils.setLimit(query, offset, limit);
		List<?> list = query.list();
		if (!list.isEmpty()) {
			List<NDATag> result = new ArrayList<NDATag>(list.size());
			for (Object obj : list) {
				NDATag e = (NDATag) obj;
				if (e.getStatus() == Constants.TagState.STATE_ACTIVE) {
					e.setStatusName("启用");
				} else if (e.getStatus() == Constants.TagState.STATE_WORKING) {
					e.setStatusName("在线工作中");
				} else if (e.getStatus() == Constants.TagState.STATE_OFFLINE) {
					e.setStatusName("离线工作中");
				} else {
					e.setStatusName("禁用");
				}
				result.add(e);
			}
			return result;
		}

		return Collections.<NDATag>emptyList();
	}

}
