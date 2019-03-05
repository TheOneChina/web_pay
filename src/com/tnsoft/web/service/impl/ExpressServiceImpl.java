package com.tnsoft.web.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDATag;
import com.tnsoft.hibernate.model.NDATagExpress;
import com.tnsoft.hibernate.model.NDAUserExpress;
import com.tnsoft.web.dao.ExpressDAO;
import com.tnsoft.web.dao.TagDAO;
import com.tnsoft.web.dao.TagExpressDAO;
import com.tnsoft.web.dao.UserExpressDAO;
import com.tnsoft.web.model.LoginSession;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.ExpressService;

@Repository("expressService")
public class ExpressServiceImpl extends BaseServiceImpl<NDAExpress> implements ExpressService {

	@Resource(name = "expressDAO")
	private ExpressDAO expressDao;
	@Resource(name = "userExpressDAO")
	private UserExpressDAO userExpressDao;
	@Resource(name = "tagDAO")
	private TagDAO tagDao;
	@Resource(name = "tagExpressDAO")
	private TagExpressDAO tagExpressDao;

	@Override
	public Response cancelSign(LoginSession lg, String expressNo) {
		Response res = new Response();
		// 获得用户订单关系,如果是管理员
		NDAExpress express = expressDao.getExpressByNo(expressNo, lg.getDomainId());
		// 根据已经签收的订单编号获取订单用户关系
		NDAUserExpress userExpress = userExpressDao.getUserExpress(lg.getUserId(), express.getId());
		// 根据订单获取设备,因为已经误签收,所以查询设备时选择已经解绑的
		if (express != null) {
			if (userExpress != null) {
				// 撤销订单签收相关信息,变回转运状态
				express.setDomainId(lg.getDomainId());
				express.setLastModitied(new Date());
				express.setStatus(Constants.ExpressState.STATE_ACTIVE);
				express.setCheckOutTime(null);
				// 撤销用户订单签收关系,改回转运状态
				userExpress.setLastModitied(new Date());
				userExpress.setStatus(Constants.State.STATE_ACTIVE);
				res.setMessage("撤销签收成功");
				res.setCode(Response.OK);
			} else if (userExpress == null) {
				if (lg.getDefRole().getRoleId() == Constants.Role.SUPER_ADMIN) {
					// 此操作比较敏感,超级管理员撤销可以新增用户订单关系
					// 撤销订单签收相关信息,变回转运状态
					express.setDomainId(lg.getDomainId());
					express.setLastModitied(new Date());
					express.setStatus(Constants.ExpressState.STATE_ACTIVE);
					express.setCheckInTime(null);
					// 新增超级用户与订单的关系
					NDAUserExpress ur = new NDAUserExpress();
					ur.setUserId(lg.getUserId());
					ur.setExpressId(express.getId());
					ur.setDomainId(lg.getDomainId());
					ur.setCreationTime(new Date());
					ur.setLastModitied(new Date());
					ur.setStatus(Constants.State.STATE_ACTIVE);
					// 保存对象
					userExpressDao.save(ur);

					res.setMessage("撤销成功");
					res.setCode(Response.OK);
				} else {
					// 不然无法执行此操作
					res.setMessage("只能由本人执行此操作");
					res.setCode(Response.ERROR);
				}
			}
			// 撤销还需要,涉及tagExpress操作,这里比较操蛋,只能根据时间获取最后一次的TagExpress,并将其状态变为1
			NDATagExpress tagExpress = tagExpressDao.getLastTagExpressByEId(express.getId());
			tagExpress.setStatus(Constants.State.STATE_ACTIVE);
		} else {
			res.setMessage("撤销失败");
			res.setCode(Response.ERROR);
		}
		return res;
	}

	@Override
	public Response saveTakingExpress(String expressNo, String tagNo, String description, Integer appointStart,
			Integer appointEnd, LoginSession lg) {
		// TODO Auto-generated method stub
		Date now = new Date();
		Response res = new Response();
		NDAExpress express = expressDao.getExpressByNo(expressNo, lg.getDomainId());
		NDATag tag = tagDao.getById(tagNo);
		// 默认十五分钟

		if (tag == null) {
			res.setCode(1);
			res.setMessage("没有该设备");
		} else if (tag.getElectricity() < 50) {
			res.setCode(1);
			res.setMessage("电量不足");
		} else if (tag.getDomainId() == null || tag.getDomainId() != lg.getDomainId()) {
			res.setCode(1);
			res.setMessage("绑定失败，非本站点注册或未注册！");
		} else if (tag != null) {
			if (tag.getSleepTime().floatValue() == 0) {
				tag.setSleepTime(15);
			}
			if (express == null) {
				express = new NDAExpress();
				express.setDomainId(lg.getDomainId());
				express.setExpressNo(expressNo);
				express.setLastModitied(now);
				express.setCreationTime(now);
				express.setDescription(description);
				express.setSleepTime(tag.getSleepTime());
				express.setAppointStart(appointStart);
				express.setAppointEnd(appointEnd);
				express.setStatus(Constants.ExpressState.STATE_PENDING);
				expressDao.save(express);
			} else if (express != null && express.getStatus() == Constants.ExpressState.STATE_ACTIVE) {
				res.setCode(1);
				res.setMessage("该订单已在配送中");
				return res;
			} else if (express != null && express.getStatus() == Constants.ExpressState.STATE_FINISHED) {
				res.setCode(1);
				res.setMessage("该订单已签收");
				return res;
			}

			// 如果订单之前绑定设备了,则解绑之前的设备
			// 根据订单编号获取之前绑定的设备,然后解绑
			NDATagExpress tagExpress = tagExpressDao.getTagExpressByEId(express.getId());
			// 如果存在绑定关系则更新,设为delete
			if (tagExpress != null) {
				tagExpress.setStatus(Constants.BindState.STATE_DELETE);
				tagExpress.setLastModified(now);
				tagExpressDao.update(tagExpress);
			}
			// 新增记录,下面代码不能放在else中
			NDATagExpress te1 = new NDATagExpress();
			te1.setCreationTime(now);
			te1.setDomainId(lg.getDomainId());
			te1.setExpressId(express.getId());
			te1.setLastModified(now);
			te1.setStatus(Constants.BindState.STATE_ACTIVE);
			te1.setTagNo(tagNo);
			tagExpressDao.save(te1);

			// 设备启用,
			tag.setStatus(Constants.TagState.STATE_WORKING);
			tagDao.save(tag);

			// 揽收,同时也涉及订单用户关系表,新增加
			NDAUserExpress ur = userExpressDao.getUserExpress(lg.getUserId(), express.getId());
			if (ur == null) {
				ur = new NDAUserExpress();
				ur.setCreationTime(now);
				ur.setDomainId(lg.getDomainId());
				ur.setExpressId(express.getId());
				ur.setLastModitied(now);
				ur.setStatus(Constants.State.STATE_ACTIVE);
				ur.setUserId(lg.getUserId());
				userExpressDao.save(ur);
			}
			res.setCode(0);
			res.setMessage("揽收成功");
		}
		return res;
	}

	@Override
	public Response signExpress(String[] expressNoList, LoginSession lg) {
		// TODO Auto-generated method stub
		// 遍历页面传过来的expressNoList,此操作设计三张表,挨个更新
		Response res = new Response();
		try {
			for (String expressNo : expressNoList) {
				// 第一步,获取订单,然后更新,第一张表的操作
				NDAExpress express = expressDao.getExpressByNo(expressNo, lg.getDomainId());
				express.setLastModitied(new Date());
				express.setCheckOutTime(new Date());
				express.setStatus(Constants.ExpressState.STATE_FINISHED);
				// 第二步,获取用户订单关系,第二张表的操作
				NDAUserExpress userExpress = userExpressDao.getUserExpress(lg.getUserId(), express.getId());
				if (userExpress != null) {
					userExpress.setLastModitied(new Date());
					userExpress.setStatus(Constants.State.STATE_FINISHED);
				} else {
					// 将订单更新为finished
					NDAUserExpress userExpress3 = userExpressDao.getUserExpressByEId(express.getId());
					userExpress3.setStatus(Constants.State.STATE_FINISHED);
					// 新增
					NDAUserExpress userExpress2 = new NDAUserExpress();
					userExpress2.setCreationTime(new Date());
					userExpress2.setDomainId(lg.getDomainId());
					userExpress2.setExpressId(express.getId());
					userExpress2.setLastModitied(new Date());
					userExpress2.setStatus(Constants.State.STATE_FINISHED);
					userExpress2.setUserId(lg.getUserId());
				}

				// 第三步,获取设备订单关系表,第三张表的操作
				NDATagExpress tagExpress = tagExpressDao.getTagExpressByEId(express.getId());
				tagExpress.setLastModified(new Date());
				tagExpress.setStatus(Constants.State.STATE_FINISHED);
			}
			res.setCode(0);
			res.setMessage("签收成功");
		} catch (Exception e) {
			// TODO: handle exception
			res.setCode(1);
			res.setMessage("签收出错");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return res;
	}

	@Override
	public Response saveExpressAttribute(String expressValue, String expressFlag, String userId, String expressId) {
		// java1.7之后支持String类型的switch case
		Response res = new Response();
		try {
			switch (expressFlag) {
			case "tempLimit":
				String[] temperature = expressValue.split(",");
				Float maxTemp = Float.parseFloat(temperature[0]);
				Float minTemp = Float.parseFloat(temperature[1]);
				expressDao.saveExpressTemperature(Integer.parseInt(expressId), maxTemp, minTemp);
				break;
			case "period":
				Float sleepTime = Float.parseFloat(expressValue);
				expressDao.saveExpressSleepTime(Integer.parseInt(expressId), sleepTime);
				break;
			case "beginTime":
				Float appointStart = Float.parseFloat(expressValue);
				expressDao.saveExpressAppointStart(Integer.parseInt(expressId), appointStart);
				break;
			case "endTime":
				Float appointEnd = Float.parseFloat(expressValue);
				expressDao.saveExpressAppointEnd(Integer.parseInt(expressId), appointEnd);
				break;
			}
			res.setCode(Response.OK);
			res.setMessage("操作成功");
		} catch (Exception e) {
			// TODO: handle exception
			res.setCode(Response.ERROR);
			res.setMessage("操作失败");
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public Response ajaxEditExSleepTime(Integer expressId, String time) {
		// TODO Auto-generated method stub
		Response res = new Response();
		try {
			// 获得绑定的订单设备关系
			NDATagExpress tagExpress = tagExpressDao.getTagExpressByEId(expressId);
			// 根据设备id获得所有绑定的订单
			List<NDATagExpress> list = tagExpressDao.getTagExpressByTNo(tagExpress.getTagNo());
			// 遍历,将所有订单的sleepTime变为设置的值
			for (NDATagExpress te : list) {
				NDAExpress express = expressDao.getById(te.getExpressId());
				express.setSleepTime(Integer.parseInt(time));
			}
			// 再将设备sleeptime变为这个值
			tagDao.getById(tagExpress.getTagNo()).setExpressSleepTime(Integer.parseInt(time));
			res.setCode(0);
			res.setMessage("设置成功");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			res.setCode(1);
			res.setMessage("设置失败");
		}
		return res;
	}

	@Override
	public Response editAppointStart(Integer expressId, String time) {
		// TODO Auto-generated method stub
		Response res = new Response();
		try {
			NDAExpress express = expressDao.getById(expressId);
			express.setAppointStart(Integer.parseInt(time));
			res.setCode(0);
			res.setMessage("设置成功");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			res.setCode(1);
			res.setMessage("设置失败");
		}
		return res;
	}

	@Override
	public Response editAppointEnd(Integer expressId, String time) {
		// TODO Auto-generated method stub
		Response res = new Response();
		try {
			NDAExpress express = expressDao.getById(expressId);
			express.setAppointEnd(Integer.parseInt(time));// 支持小数点
			res.setCode(0);
			res.setMessage("设置成功");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			res.setCode(1);
			res.setMessage("设置失败");
		}
		return res;
	}
}
