package com.tnsoft.web.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDATag;
import com.tnsoft.hibernate.model.NDATagExpress;
import com.tnsoft.hibernate.model.NDAUser;
import com.tnsoft.hibernate.model.NDAUserExpress;
import com.tnsoft.hibernate.model.UserRole;
import com.tnsoft.web.dao.ExpressDAO;
import com.tnsoft.web.dao.TagDAO;
import com.tnsoft.web.dao.TagExpressDAO;
import com.tnsoft.web.dao.UserDAO;
import com.tnsoft.web.dao.UserExpressDAO;
import com.tnsoft.web.dao.UserRoleDAO;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.TagService;

@Service("tagService")
public class TagServiceImpl extends BaseServiceImpl<NDATag> implements TagService {

	@Resource(name = "tagDAO")
	private TagDAO tagDao;
	@Resource(name = "tagExpressDAO")
	private TagExpressDAO tagExpressDao;
	@Resource(name = "expressDAO")
	private ExpressDAO expressDao;
	@Resource(name = "userDAO")
	private UserDAO userDao;
	@Resource(name = "userExpressDAO")
	private UserExpressDAO userExpressDao;
	@Resource(name = "userRoleDAO")
	private UserRoleDAO userRoleDao;

	/* (non-Javadoc)
	 * @see com.tnsoft.web.service.TagService#scanTag(java.lang.String, java.lang.Integer)
	 */
	public Response scanTag(String tagNo, Integer domainId) {
		NDATag tag = tagDao.scanTag(tagNo);
		Response res=new Response();
		if (null == tag) {
			res.setCode(1);
			res.setMessage("不是该站点设备");
			return res;
		}
		try {
			tag.setCreationTime(new Date());
			tag.setDomainId(domainId);
			tag.setLastModitied(new Date());
			tagDao.update(tag);
			res.setCode(0);
			res.setMessage("成功");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			res.setCode(1);
			res.setMessage("失败");
			throw new RuntimeException(e);
			
		}
		return res;
		
	}

	@Override
	public List<NDAExpress> getTagExpressHistory(String tagNo) {
		// TODO Auto-generated method stub
		List<NDATagExpress> list = tagExpressDao.getTagExpressHistory(tagNo);
		List<NDAExpress> list2 = new ArrayList<NDAExpress>();
		for (NDATagExpress te : list) {
			list2.add(expressDao.getById(te.getExpressId()));
		}
		return list2;
	}

	@Override
	public Response saveTagAPConfig(String SSID, String password, String tagNo) {
		// TODO Auto-generated method stub
		// 获得tag
		Response res = new Response();
		try {
			NDATag tag = tagDao.getById(tagNo);
			tag.setSSID(SSID);
			tag.setPassword(password);
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
	public Set<NDATag> getTagByUId(Integer userId) {
		// 虽然暂时只需要电量和设备编号,但是为了以后方便维护,将所有记录查询出来
		NDAUser user = userDao.getById(userId);
		// 使用Set来存放tag,放入已经存在的对象会被覆盖,
		Set<NDATag> tags = new HashSet<NDATag>();
		// 根据userId找到active状态的订单
		UserRole ur = userRoleDao.getRoleByUId(userId);
		// 如果是admin则整个站点都可以看到
		if (ur.getRoleId() == Constants.Role.SUPER_ADMIN) {
			tags = tagDao.getDomainTag(user.getDomainId());
		} else {
			// 普通员工能看到的设备
			List<NDAUserExpress> userEpxress = userExpressDao.getUserExpressByUId(userId);
			for (NDAUserExpress ue : userEpxress) {
				NDATag tag = getTagByEId(ue.getExpressId());
				// 加到tagsset中.自动过滤相同的
				tags.add(tag);
			}
		}
		return tags;
	}

	@Override
	public NDATag getTagByEId(Integer expressId) {
		// TODO Auto-generated method stub
		// 再根据获得的订单找到订单设备关系
		NDATagExpress tagExpress = tagExpressDao.getTagExpressByEId(expressId);
		// 根据订单设备关系找到设备编号,根基设备编号找到设备
		NDATag tag = tagDao.getById(tagExpress.getTagNo());

		return tag;
	}

//	@Override
//	public Response editPickTime(String tagNo, String time) {
//		// TODO Auto-generated method stub
//		Response res = new Response();
//		try {
//			NDATag tag = getById(tagNo);
//			if (tag != null) {
//				tag.setPickTime(Integer.parseInt(time));
//				res.setCode(0);
//				res.setMessage("设置成功");
//			}
//		} catch (Exception e) {
//			res.setCode(1);
//			res.setMessage("设置失败");
//			e.printStackTrace();
//		}
//		return res;
//	}

	@Override
	public Response editTag(String[] tagNos, String SSID, String password, Integer buzzer,Integer appointStart) {
		// TODO Auto-generated method stub
		Response res = new Response();
		try {
			for (String tagNo : tagNos) {
				NDATag tag = getById(tagNo);
				if (tag != null) {
					if(!SSID.equals("")){
						tag.setSSID(SSID);
					}
					if(!password.equals("")){
						tag.setPassword(password);
					}
					tag.setBuzzer(buzzer);
					tag.setAppointStart(appointStart);
				}
			}
			res.setCode(0);
			res.setMessage("设置成功");
		} catch (Exception e) {
			res.setCode(1);
			res.setMessage("设置失败");
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public Response editBuzzer(String tagNo, int model) {
		// TODO Auto-generated method stub
		Response res = new Response();
		try {
			NDATag tag = tagDao.getById(tagNo);
			tag.setBuzzer(model);
			res.setCode(0);
			res.setMessage(model == 0 ? "关闭蜂鸣器成功" : "开启蜂鸣器成功");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			res.setCode(1);
			res.setMessage("设置失败");
		}
		return res;
	}

	@Override
	public Response tagTemplate(String[] tagNos, Integer model) {
		// TODO Auto-generated method stub
		Response res = new Response();
		try {
			for (String tagNo : tagNos) {
				NDATag tag = getById(tagNo);
				if (model == 1) {
					// 设为移动
					tag.setBuzzer(0);
					tag.setLastModitied(new Date());
//					tag.setPickTime(1);
					tag.setSleepTime(5);
					res.setMessage("批量移动设置成功");
				} else {
					// 设为固定
					tag.setBuzzer(0);
					tag.setLastModitied(new Date());
//					tag.setPickTime(10);
					tag.setSleepTime(30);
					res.setMessage("批量固定设置成功");
				}
			}
			res.setCode(0);
		} catch (Exception e) {
			res.setCode(1);
			res.setMessage("设置失败");
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public Response editAppointStart(String tagNo, String time) {
		// TODO Auto-generated method stub
		Response res = new Response();
		try {
			NDATag tag = getById(tagNo);
			if (tag != null) {
				tag.setAppointStart(Integer.parseInt(time));
				res.setCode(0);
				res.setMessage("设置成功");
			}
		} catch (Exception e) {
			res.setCode(1);
			res.setMessage("设置失败");
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public Response makeTag(String tagNo, Integer domainId) {
		Response res=new Response();
		try {
			NDATag tag = new NDATag();
			tag.setTagNo(tagNo);
			tag.setStatus(Constants.TagState.STATE_ACTIVE);
			tag.setSSID("znll");// 新出厂的wifi名
			tag.setPassword("88886666");// 新出厂的wifi密码
			tag.setElectricity(100);// 新出厂的电量100满格
			tag.setPrecision(0f);// 新出厂的误差为0
			tag.setBuzzer(Constants.TagBuzzerState.STATE_OFF);// 新出厂的关闭蜂鸣器
			tag.setSleepTime(0);// 新出厂的长睡眠
			tag.setCreationTime(new Date());
			tag.setDomainId(domainId);
			tag.setLastModitied(new Date());
			save(tag);
			res.setCode(0);
			res.setMessage("成功");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			res.setCode(1);
			res.setMessage("失败");
			throw new RuntimeException(e);
			
		}
		return res;
		
	}

}
