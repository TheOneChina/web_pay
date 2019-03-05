package com.tnsoft.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.SQLQuery;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDARole;
import com.tnsoft.hibernate.model.NDATag;
import com.tnsoft.hibernate.model.NDATagExpress;
import com.tnsoft.hibernate.model.NDATempExpress;
import com.tnsoft.hibernate.model.Permission;
import com.tnsoft.web.model.LoginSession;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.model.Result;
import com.tnsoft.web.model.SelectItem;
import com.tnsoft.web.model.TempItem;
import com.tnsoft.web.service.AlertService;
import com.tnsoft.web.service.HomeService;
import com.tnsoft.web.service.PermissionService;
import com.tnsoft.web.service.RegisterService;
import com.tnsoft.web.service.RoleService;
import com.tnsoft.web.service.TagService;
import com.tnsoft.web.servlet.ServletConsts;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;

@Controller
public class HomeController extends BaseController {

	@Resource(name = "homeService")
	private HomeService homeService;
	@Resource(name = "tagService")
	private TagService tagService;
	@Resource(name = "alertService")
	private AlertService alertService;
	@Resource(name = "permissionService")
	private PermissionService perService;
	@Resource(name = "roleService")
	private RoleService roleService;
	@Resource(name = "registerService")
	private RegisterService registerService;

	@RequestMapping(value = "/timeout")
	public void sessionTimeout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (request.getHeader("x-requested-with") != null
				&& request.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")) {
			response.getWriter().print("timeout");
			response.getWriter().close();
		} else {
			response.sendRedirect("login");
		}
	}

	@RequestMapping("/editMenu")
	public String editMenu() {
		if (!validateUser() && lg.getDefRole().getRoleId() != Constants.Role.ADMIN) {
			return "";
		}
		return "view.menu.editMenu";
	}

	@RequestMapping("/editRoleMenu")
	public ModelAndView editRoleMenu(Model model) {
		Utils.saveLog(lg.getUserId(), "编辑角色菜单", lg.getDomainId());

		if (!validateUser()) {
			return new ModelAndView("redirect:/");
		}
		if (lg.getDefRole().getRoleId()!=Constants.Role.SUPER_ADMIN) {
			return new ModelAndView("redirect:/");
		}
		List<SelectItem> selectRol=new ArrayList<SelectItem>();
		//获得全部角色菜单关系,放入selectItem
		List<NDARole> roles=roleService.getAllRole();
		for(NDARole r:roles){
			selectRol.add(new SelectItem(r.getId()+"",r.getName()));
		}
		model.addAttribute("roles",selectRol);
		return new ModelAndView("view.menu.editRoleMenu");

	}

	@RequestMapping("fileUpload")
	public String fileUpload(@RequestParam("files") MultipartFile[] files, RedirectAttributes attr)
			throws IllegalStateException, IOException {

		Response res = homeService.fileUpload(files, request);
		attr.addFlashAttribute("error", true);
		attr.addFlashAttribute("message", res.getMessage());
		return "redirect:/editMenu";
	}

	@RequestMapping("/login")
	public String login(Model model, HttpServletRequest request) {
		return "view.login";
	}
	
	@RequestMapping("/home")
	public String adminHome(Model model, HttpServletRequest request) {
		HttpSession session = request.getSession();
		LoginSession lg = (LoginSession) session.getAttribute(ServletConsts.ATTR_USER);
		if (lg == null) {
			model.addAttribute("name", session.getAttribute("remember_name"));
			model.addAttribute("pwd", session.getAttribute("remember_pwd"));
			return "view.login";
		}

		model.addAttribute("username", lg.getUserName());
		model.addAttribute("rolename", lg.getDefRole().getRoleName() + lg.getNickName());
		return "view.home.home";
	}

	@RequestMapping("/query")
	public String query() {
		return "view.query";
	}

	@RequestMapping("/getMenus") // 2017.7.19FBB
	@ResponseBody
	public List<List<Permission>> getMenus(Integer roleId, Integer pId) {
		// roleId不为空加载父菜单,pid不为空就是加载子菜单
		List<List<Permission>> menus = perService.getPermission(roleId);
		return menus;
	}

	@RequestMapping("/ajaxQuery")
	public String ajaxQuery(Model model, String expressNo, String tagNo) {
		DbSession db = BaseHibernateUtils.newSession();
		if (null != tagNo) {
			model.addAttribute("tagNo", tagNo);
			try {
				String sql = "SELECT a.*, b.name AS domainName FROM nda_tag a, nda_domain b WHERE b.id=a.domain_id AND a.tag_no LIKE '%"
						+ tagNo + "%'";
				SQLQuery query = db.createSQLQuery(sql);
				query.addEntity(NDATag.class);
				query.addScalar("domainName", StringType.INSTANCE);
				List<?> list = query.list();
				if (!list.isEmpty()) {
					List<SelectItem> items = new ArrayList<SelectItem>(list.size());
					for (Object o : list) {
						Object[] row = (Object[]) o;
						NDATag tag = (NDATag) row[0];
						SelectItem item = new SelectItem(String.valueOf(tag.getTagNo()),
								tag.getTagNo() + "(" + (String) row[1] + ")");
						items.add(item);
					}
					model.addAttribute("tags", items);
				}
			} finally {
				db.close();
			}
			return "view.tagDetail";
		}

		model.addAttribute("expressNo", expressNo);
		try {
			String sql = "SELECT a.*, b.name AS domainName FROM nda_express a, nda_domain b WHERE b.id=a.domain_id AND a.express_no LIKE'%"
					+ expressNo + "%'";

			SQLQuery query = db.createSQLQuery(sql);
			query.addEntity(NDAExpress.class);
			query.addScalar("domainName", StringType.INSTANCE);
			List<?> list = query.list();
			if (!list.isEmpty()) {
				List<SelectItem> items = new ArrayList<SelectItem>(list.size());
				for (Object o : list) {
					Object[] row = (Object[]) o;
					NDAExpress express = (NDAExpress) row[0];
					SelectItem item = new SelectItem(String.valueOf(express.getId()),
							express.getExpressNo() + "(" + (String) row[1] + ")");
					items.add(item);
				}
				model.addAttribute("expresses", items);
			}
		} finally {
			db.close();
		}

		return "view.queryDetail";
	}

	// ajaxQueryExpress
	@RequestMapping("/ajaxQueryExpress")
	@ResponseBody
	public void ajaxQueryExpress(String expressId, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();

		Result result = new Result(Result.OK);

		List<TempItem> temps = new ArrayList<TempItem>();

		try {

			NDAExpress express = (NDAExpress) db.get(NDAExpress.class, Integer.parseInt(expressId));// DBUtils.getNDAExpress(db,
																									// expressNo);
			if (express.getCheckInTime() != null) {
				result.setBegin("配送开始:" + Utils.SF.format(express.getCheckInTime()));
			} else {
				result.setBegin("未开始配送");
			}

			if (express.getCheckOutTime() != null) {
				result.setEnd("已签收:" + Utils.SF.format(express.getCheckOutTime()));
			} else {
				result.setEnd("未签收");
			}

			List<NDATempExpress> list = DBUtils.getAllTempesByExpressIdDesc(db, express.getId());
			if (!list.isEmpty()) {
				for (NDATempExpress ne : list) {
					temps.add(new TempItem(String.format("%.2f", ne.getTemperature()),
							String.format("%.2f", ne.getHumidity()), Utils.SF.format(ne.getCreationTime())));
				}
			}
		} finally {
			db.close();
		}
		result.setTemps(temps);

		out.write(Utils.GSON.toJson(result));
	}

	// 2017-4-11
	public static final SimpleDateFormat SF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();

	@RequestMapping("/guestAjaxTemp")
	@ResponseBody
	public void guestAjaxTemp(String expressId, HttpServletResponse resp) throws IOException {
		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();
		DbSession db = BaseHibernateUtils.newSession();
		Result result = new Result();
		List<String> time = new ArrayList<String>();
		List<String> temperature = new ArrayList<String>();
		List<String> humidity = new ArrayList<String>();
		try {

			if (expressId != null) {
				List<NDATempExpress> list = DBUtils.getAllTempesByExpressId(db, Integer.parseInt(expressId));
				if (!list.isEmpty()) {
					for (NDATempExpress ndaTempExpress : list) {
						time.add(SF.format(ndaTempExpress.getCreationTime()));
						temperature.add(String.format("%.2f", ndaTempExpress.getTemperature()));
						humidity.add(String.format("%.2f", ndaTempExpress.getHumidity()));
					}
				}
			}
		} finally {
			db.close();
		}

		result.setTime(time);
		result.setTemperature(temperature);
		result.setHumidity(humidity);

		if (temperature.isEmpty() || temperature.size() == 0 || humidity.isEmpty() || humidity.size() == 0) {
			result.setCode(Result.ERROR);
		}
		out.write(GSON.toJson(result));
	}

	@RequestMapping("/guestGetTagBreifInfo")
	@ResponseBody
	public Object guestGetTagBreifInfo(String tagNo) {
		NDATag tag = tagService.getById(tagNo);
		Map<String, Object> map = Utils.ObjToMap(tag);
		// 还需要加入报警相关信息,为了以后方便,直接获得报警对象
		List<NDAAlert> list = alertService.getAlertByTagNo(tagNo);
		// 暂时只讲报警的次数放入到map里
		map.put("alertCount", list.size());
		return map;
	}

	@RequestMapping("/guestGetExpressBreifInfo")
	@ResponseBody
	public void guestGetExpressBreifInfo(String expressId, HttpServletResponse resp) throws IOException {

		resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
		PrintWriter out = resp.getWriter();

		Map<String, String> result = new HashMap<String, String>();

		DbSession dbSession = BaseHibernateUtils.newSession();
		try {
			NDAExpress express = DBUtils.getNDAExpress(dbSession, Integer.parseInt(expressId));
			NDATagExpress tagExpress = DBUtils.getNDATagExpressIgnoreStatus(dbSession, express.getId()).get(0);
			List<NDAAlert> alerts = DBUtils.getAllAlertsByExpressId(dbSession, express.getId());
			List<NDATempExpress> ndaTempExpresses = DBUtils.getAllTempesByExpressId(dbSession, express.getId());
			NDATag tag = null;
			float realMaxTemp = 0;
			float realMinTemp = 0;
			float realAveTemp = 0;
			float realMaxHumidity = 0;
			float realMinHumidity = 0;
			float realAveHumidity = 0;
			if (ndaTempExpresses.size() > 0) {

				realMaxTemp = ndaTempExpresses.get(0).getTemperature();
				realMinTemp = ndaTempExpresses.get(0).getTemperature();

				realMaxHumidity = ndaTempExpresses.get(0).getHumidity();
				realMinHumidity = ndaTempExpresses.get(0).getHumidity();

				for (NDATempExpress ndaTempExpress : ndaTempExpresses) {
					if (realMaxTemp < ndaTempExpress.getTemperature()) {
						realMaxTemp = ndaTempExpress.getTemperature();
					}
					if (realMinTemp > ndaTempExpress.getTemperature()) {
						realMinTemp = ndaTempExpress.getTemperature();
					}
					if (realMaxHumidity < ndaTempExpress.getHumidity()) {
						realMaxHumidity = ndaTempExpress.getHumidity();
					}
					if (realMinHumidity > ndaTempExpress.getHumidity()) {
						realMinHumidity = ndaTempExpress.getHumidity();
					}
					realAveTemp += (ndaTempExpress.getTemperature() / ndaTempExpresses.size());
					realAveHumidity += (ndaTempExpress.getHumidity() / ndaTempExpresses.size());
				}
			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (tagExpress == null) {
				result.put("tag", "当前未绑定模块");
			} else {
				result.put("tag", tagExpress.getTagNo());
				tag = DBUtils.getTagByTagNo(dbSession, tagExpress.getTagNo());
			}

			result.put("alertCount", alerts.size() + "");

			if (express.getExpressNo() != null) {
				result.put("expressNo", express.getExpressNo());
			}

			if (express.getCreationTime() != null) {
				result.put("expressStartTime", dateFormat.format(express.getCreationTime()));

			} else {
				result.put("expressStartTime", "无");
			}

			if (express.getCheckOutTime() != null) {
				result.put("expressEndTime", dateFormat.format(express.getCheckOutTime()));
			} else {
				result.put("expressEndTime", "无");
			}

			if (express.getStatus() == Constants.ExpressState.STATE_PENDING) {
				result.put("expressState", "待配送");
			} else if (express.getStatus() == Constants.ExpressState.STATE_ACTIVE) {
				result.put("expressState", "配送中");
			} else if (express.getStatus() == Constants.ExpressState.STATE_FINISHED) {
				result.put("expressState", "已签收");
			} else {
				result.put("expressState", "未知");
			}

			if (express.getTemperatureMax() != null) {
				result.put("maxAlertTemperature", express.getTemperatureMax() + "");
			} else {
				if (tag != null && tag.getTemperatureMax() != null) {
					result.put("maxAlertTemperature", tag.getTemperatureMax() + "");
				} else {
					result.put("maxAlertTemperature", "无");
				}
			}

			if (express.getTemperatureMin() != null) {
				result.put("minAlertTemperature", express.getTemperatureMin() + "");
			} else {
				if (tag != null && tag.getTemperatureMin() != null) {
					result.put("minAlertTemperature", tag.getTemperatureMin() + "");
				} else {
					result.put("minAlertTemperature", "无");
				}
			}

			result.put("realMaxTemp", realMaxTemp + "℃");
			result.put("realMinTemp", realMinTemp + "℃");
			result.put("realAveTemp", String.format("%.2f℃", realAveTemp));

			result.put("realMaxHumidity", realMaxHumidity + "%");
			result.put("realMinHumidity", realMinHumidity + "%");
			result.put("realAveHumidity", String.format("%.2f", realAveHumidity) + "%");

			if (ndaTempExpresses.size() > 0) {
				if (ndaTempExpresses.get(ndaTempExpresses.size() - 1).getCreationTime() != null) {
					result.put("nowTime",
							dateFormat.format(ndaTempExpresses.get(ndaTempExpresses.size() - 1).getCreationTime())
									+ "");
					result.put("nowTemp", ndaTempExpresses.get(ndaTempExpresses.size() - 1).getTemperature() + "℃");
					result.put("nowHumidity", ndaTempExpresses.get(ndaTempExpresses.size() - 1).getHumidity() + "%");
				}
			} else {
				result.put("nowTime", "无");
				result.put("nowTemp", "无");
				result.put("nowHumidity", "无");
			}
		} finally {
			dbSession.close();
		}

		out.write(GSON.toJson(result));
	}
}
