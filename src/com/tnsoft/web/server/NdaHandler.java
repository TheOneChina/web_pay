package com.tnsoft.web.server;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.springframework.stereotype.Component;

import com.expertise.common.logging.Logger;
import com.expertise.common.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Constants;
import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.hibernate.model.NDAAlertLevel;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDATag;
import com.tnsoft.hibernate.model.NDATempExpress;
import com.tnsoft.web.model.AuthResponse;
import com.tnsoft.web.model.RequestEntity;
import com.tnsoft.web.model.UploadResponse;
import com.tnsoft.web.util.Utils;

/**
 * 数据处理，处理NdaDecoder解析出的数据
 * 
 * ctrl+f搜索FBB,每个方法前面都有
 * 
 */
@Component
public class NdaHandler extends SimpleChannelUpstreamHandler {

	public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();
	public static final SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public NdaHandler() {
	}

	/**
	 * 报文处理
	 * 
	 * @param ctx
	 *            ChannelHandlerContext
	 * @param event
	 *            MessageEvent
	 * @throws Exception
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org.
	 * jboss.netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.MessageEvent)
	 */

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
		// 获取待处理对象

		Nda nda = (Nda) event.getMessage();
		String result = null;
		// 获得将nda转为byte再转为utf-8,
		String tmp = StringUtils.toStringQuietly(nda.getData());
		// Logger.error("*********************************");
		// Logger.error(tmp);
		// Logger.error("*********************************");

		// 将返回的字符串数据转换为RequestEntity对象，方便处理
		RequestEntity requestEntity = null;
		try {
			// 程序运行至此,就已经将报文消息变为对象,可以开始逻辑判断fbb
			requestEntity = GSON.fromJson(tmp, RequestEntity.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (requestEntity == null) {
			return;
		}

		DbSession db = BaseHibernateUtils.newSession();
		try {
			db.beginTransaction();
			Date now = new Date();

			// 获取上传的token值
			String tk = requestEntity.getMeta().getAuthorization();
			int pos = tk.indexOf("token");
			String key = tk.substring(pos + 6).trim();
			
			System.out.println("****KEY********"+key);

			// 处理上传温度指令
			if (requestEntity.getPath().equalsIgnoreCase("/v1/datastreams/tem_hum/datapoint/")) {
				Logger.error("***********收到温度数据*************");
				String temp = requestEntity.getBody().getDatapoint().getX();
				int position = temp.indexOf("-");
				float y = 0;
				if (position < 0) {
					y = Float.parseFloat(temp);
				} else {
					int pp = temp.indexOf(".");
					StringBuilder sb = new StringBuilder();
					sb.append(temp.substring(0, pp + 1));
					sb.append(temp.substring(pp + 2));
					y = Float.parseFloat(sb.toString());
				}

				float humidity = 0;
				String hString = requestEntity.getBody().getDatapoint().getY();
				if (!StringUtils.isEmpty(hString)) {
					humidity = Float.parseFloat(hString);
					if (humidity > 90.00) {
						humidity = 90.00f;
					} else if (humidity < 10.00) {
						humidity = 10.00f;
					}
				}

				// 获取智能硬件以及带过来的设备信息
				NDATag tag = (NDATag) db.get(NDATag.class, key);

				if (tag != null && tag.getStatus() != Constants.TagState.STATE_DELETE) {

					Date dataTime = null;
					Integer electricity = null;
					if (requestEntity.getTime() != null) {
						Calendar c = Calendar.getInstance();
						c.setTime(requestEntity.getTime());
						c.add(Calendar.SECOND, (int) (requestEntity.getSleepTime() * 60));
						dataTime = c.getTime();
					} else {
						dataTime = now;
						electricity = requestEntity.getBody().getDatapoint().getVdd();
						if (electricity != null && electricity > 0) {
							tag.setElectricity(electricity);
						}
						Integer wifiStatus = requestEntity.getBody().getDatapoint().getWifi();
						if (wifiStatus != null) {
							tag.setWifiStatus(wifiStatus);
						}
						tag.setLastModitied(now);
						db.save(tag);
						db.flush();
					}

					// 过滤非有效温度数据
					if (y != 42949626.11) {

						// Logger.error("该智能硬件绑定订单数量为:" + express.size());
						// 保存温度到该智能硬件绑定的订单中,FBB在这一步计算延迟启动时间
						List<NDAExpress> express = getExpressByTag(db, tag.getTagNo());

						// 电量报警
						if (requestEntity.getTime() != null && electricity != null) {
							// 先是电量
							// if (electricity < 50) {
							// // 判断该tag出发普通报警次数,FBB
							// Logger.error("触发报警, 电量仅剩余:" + electricity);
							// // 生成报警信息nda_tag_alert
							// for (NDAExpress e : express) {
							// NDAAlert alert = new NDAAlert();
							// alert.setCreationTime(uploadTime == null ? now : uploadTime);
							// // alert.setCsn(BaseHibernateUtils.nextCsn(db));
							// alert.setType(Constants.AlertType.STATE_ELECTRICITY);//
							// type为1是温度报警,2是电量,3是蜂鸣器,4精度相差太大
							// alert.setExpressId(e.getId());
							// // 电量的判断
							// if (electricity <= 60 && electricity > 40) {
							// alert.setAlertLevel(Constants.AlertLevel.STATE_NORAML_LOW);
							// } else if (electricity <= 40 && electricity > 20) {
							// alert.setAlertLevel(Constants.AlertLevel.STATE_NORAML_HIGH);
							// } else if (electricity <= 20) {
							// alert.setAlertLevel(Constants.AlertLevel.STATE_SERIOUS);
							// }
							//
							// alert.setDomainId(tag.getDomainId());
							// alert.setLastModitied(now);
							// alert.setTagNo(tag.getTagNo());
							// alert.setStatus(Constants.AlertState.STATE_ACTIVE);
							// db.save(alert);
							// db.flush();
							// }
							// }
						}

						for (NDAExpress ex : express) {
							if (requestEntity.getTime() == null
									&& ex.getStatus() == Constants.ExpressState.STATE_FINISHED) {
								// 解除绑定
								String sql = "UPDATE nda_tag_express SET status=" + Constants.BindState.STATE_DELETE
										+ " WHERE express_id=" + ex.getId();
								db.createSQLQuery(sql).executeUpdate();
								continue;
							}

							NDATempExpress a = new NDATempExpress();
							Calendar exTime = Calendar.getInstance();
							exTime.setTime(ex.getCreationTime());

							// 如果订单的预约启动时间不为空,则以订单的优先,为空则用设备的预约启动时间(延时启动时间)
							Integer appointStart = null;
							
							if (ex.getAppointStart() != null) {
								appointStart = ex.getAppointStart();
							}else if (tag.getAppointStart() != null) {
								appointStart = tag.getAppointStart();
							}
							if (appointStart != null) {
								exTime.add(Calendar.SECOND, (int) (appointStart * 60));
							}

							if (exTime.after(dataTime)) {
								continue;
							}

							// 订单的预约结束时间,因为设备无此属性.所以不用做判断
							if (ex.getAppointEnd() != null) {
								exTime.setTime(ex.getCreationTime());
								// 订单创建时间加上预约结束时间
								exTime.add(Calendar.SECOND, (int) (ex.getAppointEnd() * 60));
								if (exTime.before(dataTime)) {
									continue;
								}
							}

							// 2017-4-10 过滤签收后订单，不再保存签收后的温度
							if (ex.getCheckOutTime() != null) {
								Calendar exCheckOutTime = Calendar.getInstance();
								exCheckOutTime.setTime(ex.getCheckOutTime());
								if (exCheckOutTime.before(dataTime)) {
									continue;
								}
							}
							// end
							a.setCreationTime(dataTime);

							a.setDomainId(tag.getDomainId());
							a.setExpressId(ex.getId());
							a.setLastModitied(now);
							a.setTemperature(y);
							a.setHumidity(humidity);
							db.save(a);
							db.flush();
							///////////////////////////////// 开始报警逻辑

							Float max = null;
							Float min = null;
							// 记录温度之后,判断是否超过范围相当于是否触发报警
							if (ex.getTemperatureMax() != null && ex.getTemperatureMin() != null) {
								max = ex.getTemperatureMax();
								min = ex.getTemperatureMin();
							} else if (tag.getTemperatureMax() != null && tag.getTemperatureMin() != null) {
								max = tag.getTemperatureMax();
								min = tag.getTemperatureMin();
							}
							if (min != null && max != null) {
								if (y < min || y > max) {
									// 判断该tag出发普通报警次数,FBB
									Logger.error("触发报警: 低温设置=" + min + " 高温设置=" + max);
									////// 将sleepTime变为2分钟,订单的上传周期
//									String hql = "update NDATag set sleepTime=:sleepTime ,pickTime=:pickTime where tagNo=:tagNo";
//									db.getSession().createQuery(hql).setParameter("sleepTime", 2)
//											.setParameter("pickTime", 1f).setParameter("tagNo", tag.getTagNo())
//											.executeUpdate();
//									db.flush();
//									// 订单的上传周期也要改
//									String hql2 = "update NDAExpress set sleepTime=:sleepTime where id=:id";
//									db.getSession().createQuery(hql2).setParameter("sleepTime", 2)
//											.setParameter("id", ex.getId()).executeUpdate();
									//////////
									List<NDAAlertLevel> levels = getAlertLevel(db, tag.getDomainId());
									NDAAlertLevel level = null;
									if (levels != null && levels.size() > 0) {
										level = levels.get(0);
									}

									if (level != null && level.getStatus() == Constants.State.STATE_ACTIVE) {
										int count = getAlertCount(db, ex.getId(), level.getHours());
										System.out.println(count);
										if (count > level.getTimes() - 1) {
											NDAAlert alert = new NDAAlert();
											alert.setAlertLevel(Constants.AlertLevel.STATE_SERIOUS);
											alert.setCreationTime(dataTime);
											// alert.setCsn(BaseHibernateUtils.nextCsn(db));
											alert.setDomainId(tag.getDomainId());
											alert.setType(Constants.AlertType.STATE_TEMPHISALERT);
											alert.setLastModitied(now);
											alert.setTagNo(tag.getTagNo());
											alert.setExpressId(ex.getId());
											alert.setStatus(Constants.AlertState.STATE_ACTIVE);
											db.save(alert);
											db.flush();
										}
									}
									// 生成报警信息
									NDAAlert alert = new NDAAlert();
									alert.setAlertLevel(y > max ? Constants.AlertLevel.STATE_NORAML_HIGH
											: Constants.AlertLevel.STATE_NORAML_LOW);
									alert.setCreationTime(dataTime);
									// alert.setCsn(BaseHibernateUtils.nextCsn(db));
									alert.setDomainId(tag.getDomainId());
									alert.setLastModitied(now);
									alert.setType(Constants.AlertType.STATE_TEMPHISALERT);
									alert.setExpressId(ex.getId());
									alert.setTagNo(tag.getTagNo());
									alert.setStatus(Constants.AlertState.STATE_ACTIVE);
									db.save(alert);
									db.flush();
								}
							}
						}
					}
					

					if (requestEntity.getTime() == null) {
						// Logger.error("***********不带时间数据（在线数据）*************");
						// 2017年4月10日 检测模块没有绑定订单后，使模块进行休眠
						UploadResponse up = new UploadResponse();
						UploadResponse.Datapoint d = new UploadResponse.Datapoint();
						// d.setAt(Utils.SF1.format(now));
						d.setCreated(Utils.SF1.format(now));
						// d.setUpdated(Utils.SF1.format(now));
						d.setX(requestEntity.getBody().getDatapoint().getX());
						d.setY(requestEntity.getBody().getDatapoint().getY());

						// 检查设置参数是否需要发送
						boolean change = false;
						// 蜂鸣器
						if (null != tag.getBuzzer()) {
							if (null == tag.getBuzzerNow()) {
								change = true;
//								d.setBuzzer(tag.getBuzzer());
//								d.setChange(1);
							}else if (!tag.getBuzzer().equals(tag.getBuzzerNow())) {
								change = true;
//								d.setBuzzer(tag.getBuzzer());
//								d.setChange(1);
							}
						}
						// AP名和密码
						if (null != tag.getSSID()) {
							if (null == tag.getSSIDNow()) {
								change = true;
//								d.setSsid(tag.getSSID());
//								d.setChange(1);
							}else if (!tag.getSSID().equals(tag.getSSIDNow())) {
								change = true;
//								d.setSsid(tag.getSSID());
//								d.setChange(1);
							}
						}
						if (null != tag.getPassword()) {
							if (null == tag.getPasswordNow()) {
								change = true;
//								d.setPassword(tag.getPassword());
//								d.setChange(1);
							}else if (!tag.getPassword().equals(tag.getPasswordNow())) {
								change = true;
//								d.setPassword(tag.getPassword());
//								d.setChange(1);
							}
							
						}
						
						// 温度上下限
//						if (null != tag.getTemperatureMax()) {
//							if (null == tag.getTemperatureMaxNow()) {
//								change = true;
////								d.setTmax(tag.getTemperatureMax());
////								d.setChange(1);
//							}else if (!tag.getTemperatureMax().equals(tag.getTemperatureMaxNow())) {
//								change = true;
////								d.setTmax(tag.getTemperatureMax());
////								d.setChange(1);
//							}
//						}
//						if (null != tag.getTemperatureMin()) {
//							if (null == tag.getTemperatureMinNow()) {
//								change = true;
////								d.setTmin(tag.getTemperatureMin());
////								d.setChange(1);
//							}else if (!tag.getTemperatureMin().equals(tag.getTemperatureMinNow())) {
//								change = true;
////								d.setTmin(tag.getTemperatureMin());
////								d.setChange(1);
//							}
//						}
//						
//						// 湿度上下限
//						if (null != tag.getHumidityMax()) {
//							if (null == tag.getHumidityMaxNow()) {
//								change = true;
////								d.setHmax(tag.getHumidityMax());
////								d.setChange(1);
//							}else if (!tag.getHumidityMax().equals(tag.getHumidityMaxNow())) {
//								change = true;
////								d.setHmax(tag.getHumidityMax());
////								d.setChange(1);
//							}
//						}
//						if (null != tag.getHumidityMin()) {
//							if (null == tag.getHumidityMinNow()) {
//								change = true;
////								d.setHmin(tag.getHumidityMin());
////								d.setChange(1);
//							}else if (!tag.getHumidityMin().equals(tag.getHumidityMinNow())) {
//								change = true;
////								d.setHmin(tag.getHumidityMin());
////								d.setChange(1);
//							}
//						}
						
						//校准值
						if (null != tag.getPrecision()) {
							if (null == tag.getPrecisionNow()) {
								change = true;
//								d.setPrecision(tag.getPrecision());
//								d.setChange(1);
							}else if (!tag.getPrecision().equals(tag.getPrecisionNow())) {
								change = true;
//								d.setPrecision(tag.getPrecision());
//								d.setChange(1);
							}
						}
						
						//全部设置都需要添加到报文中
						if (change) {
							d.setChange(true);
							d.setBuzzer(tag.getBuzzer());
							d.setSsid(tag.getSSID());
							d.setPassword(tag.getPassword());
//							if (null == tag.getTemperatureMax()) {
//								d.setTmax("09999");
//							}else {
//								d.setTmax(transFromFloat(tag.getTemperatureMax()));
//							}
//							
//							if (null == tag.getTemperatureMin()) {
//								d.setTmin("-9999");
//							}else {
//								d.setTmin(transFromFloat(tag.getTemperatureMin()));
//							}
//							if (null == tag.getHumidityMax()) {
//								d.setHmax("09999");
//							}else {
//								d.setHmax(transFromFloat(tag.getHumidityMax()));
//							}
//							if (null == tag.getHumidityMin()) {
//								d.setHmin("00000");
//							}else {
//								d.setHmin(transFromFloat(tag.getHumidityMin()));
//							}
							if (null == tag.getPrecision()) {
								d.setPrecision("00000");
							}else {
								d.setPrecision(transFromFloat(tag.getPrecision()));
							}
							
						}
						

						// 若有订单上传周期以订单上传周期为准，否则用设备上传周期
						if (null != tag.getExpressSleepTime() && tag.getExpressSleepTime() > 0) {
							d.setDstime(tag.getExpressSleepTime());
						} else if (null != tag.getSleepTime()) {
							d.setDstime(tag.getSleepTime());
						}

						// 检测到设备未绑定订单时将订单上传周期置为0，发送睡眠时间0给设备。
						if (getExpressByTag(db, tag.getTagNo()) == null
								|| getExpressByTag(db, tag.getTagNo()).size() == 0) {
							tag.setStatus(Constants.TagState.STATE_ACTIVE);
							tag.setExpressSleepTime(0);
							db.save(tag);
							db.flush();
							d.setDstime(0);
						}

						up.setStatus(200);
						up.setDatapoint(d);

						result = GSON.toJson(up);
						// end
					} else {
						// Logger.error("***********带时间数据（离线数据）*************");
						StringBuffer sb = new StringBuffer();
						sb.append("{");
						sb.append("\"").append("offline_message$GET").append("\"");
						sb.append(",");
						sb.append("\"").append("SleepTime").append("\"").append(":");
						sb.append(requestEntity.getSleepTime());
						sb.append("}");
						result = sb.toString();
					}
				}
				// 从这里开始是FBB离线数据
			} else if (requestEntity.getPath().equalsIgnoreCase("offline")) {
				// 离线数据压缩传输解析
				NDATag tag = (NDATag) db.get(NDATag.class, key);

				if (tag != null && tag.getStatus() != Constants.TagState.STATE_DELETE) {
					List<MyDataStruct> dataList = new ArrayList<MyDataStruct>();

					if (requestEntity.getBody().getOfflineData() != null) {
						// 离线数据处理
						String[] offlineDataElems = requestEntity.getBody().getOfflineData().split(";");
						for (int i = 0; i < offlineDataElems.length; i++) {
							String[] elems = offlineDataElems[i].split(",");
							if (elems.length != 4) {
								continue;
							}
							float elemTemperature = Float.parseFloat(elems[0]);
							float eleHumidity = Float.parseFloat(elems[1]);
							if (eleHumidity > 90.00) {
								eleHumidity = 90.00f;
							} else if (eleHumidity < 10.00) {
								eleHumidity = 10.00f;
							}
							Date elemDate = simpleDate.parse(elems[2]);
							long elemAddSecond = Long.parseLong(elems[3]) * 60 * 1000;
							Date realElemDate = new Date(elemDate.getTime() + elemAddSecond);
							dataList.add(new MyDataStruct(elemTemperature, eleHumidity, realElemDate));
						}
					}
					// 对数据List进行处理
					for (MyDataStruct myDataStruct : dataList) {
						if (myDataStruct.getTemperature() == 42949626.11) {
							// 温度数据无效
							continue;
						}
						tag.setLastModitied(now);
						List<NDAExpress> expresses = getExpressByTag(db, tag.getTagNo());

						for (NDAExpress ndaExpress : expresses) {
							NDATempExpress a = new NDATempExpress();

							Calendar c = Calendar.getInstance();
							c.setTime(myDataStruct.getTime());
							Calendar exCreateTime = Calendar.getInstance();
							exCreateTime.setTime(ndaExpress.getCreationTime());

							if (c.before(exCreateTime)) {
								continue;
							}

							if (ndaExpress.getCheckOutTime() != null) {
								Calendar exCheckOutTime = Calendar.getInstance();
								exCheckOutTime.setTime(ndaExpress.getCheckOutTime());
								if (c.after(exCheckOutTime)) {
									continue;
								}
							}
							a.setCreationTime(myDataStruct.getTime());
							a.setDomainId(tag.getDomainId());
							a.setExpressId(ndaExpress.getId());
							a.setLastModitied(now);
							a.setTemperature(myDataStruct.getTemperature());
							a.setHumidity(myDataStruct.getHumidity());
							db.save(a);
							db.flush();

							Float max = null;
							Float min = null;
							// 判断是否超过范围即是否触发报警
							if (ndaExpress.getTemperatureMax() != null && ndaExpress.getTemperatureMin() != null) {
								max = ndaExpress.getTemperatureMax();
								min = ndaExpress.getTemperatureMin();
							} else if (tag.getTemperatureMax() != null && tag.getTemperatureMin() != null) {
								max = tag.getTemperatureMax();
								min = tag.getTemperatureMin();
							}

							if (min != null || max != null) {
								if (myDataStruct.getTemperature() < min || myDataStruct.getTemperature() > max) {
									// 判断该tag出发普通报警次数
									Logger.error("触发报警: 低温设置=" + min + " 高温设置=" + max);
									// 将sleepTime变为2分钟,FBB
									////// 将sleepTime变为2分钟,FBB
									// String hql = "update NDATag set sleepTime=:sleepTime ,pickTime=?pickTime
									// where tagNo=:tagNo";
									// db.getSession().createQuery(hql).setParameter("sleepTime", 2)
									// .setParameter("pickTime", 1).setParameter("tagNo", tag.getTagNo())
									// .executeUpdate();
									// db.flush();
									// // 订单的上传周期也要改
									// String hql2 = "update NDAExpress set sleepTime=:sleepTime where id=:id";
									// db.getSession().createQuery(hql2).setParameter("sleepTime", 2f)
									// .setParameter("id", ndaExpress.getId()).executeUpdate();
									//////////
									////////////
									List<NDAAlertLevel> levels = getAlertLevel(db, tag.getDomainId());
									NDAAlertLevel level = null;
									if (levels != null && levels.size() > 0) {
										level = levels.get(0);
									}

									if (level != null && level.getStatus() == Constants.State.STATE_ACTIVE) {
										int count = getAlertCount(db, ndaExpress.getId(), level.getHours());
										if (count > level.getTimes() - 1) {
											NDAAlert alert = new NDAAlert();
											alert.setAlertLevel(Constants.AlertLevel.STATE_SERIOUS);
											alert.setCreationTime(myDataStruct.getTime());
											// alert.setCsn(BaseHibernateUtils.nextCsn(db));
											alert.setDomainId(tag.getDomainId());
											alert.setLastModitied(now);
											alert.setType(Constants.AlertType.STATE_TEMPHISALERT);
											alert.setTagNo(tag.getTagNo());
											alert.setExpressId(ndaExpress.getId());
											alert.setStatus(Constants.AlertState.STATE_ACTIVE);
											db.save(alert);
											db.flush();
										}
									}
									// 生成报警信息
									NDAAlert alert = new NDAAlert();
									alert.setAlertLevel(
											myDataStruct.getTemperature() > max ? Constants.AlertLevel.STATE_NORAML_HIGH
													: Constants.AlertLevel.STATE_NORAML_LOW);
									alert.setCreationTime(myDataStruct.getTime());
									// alert.setCsn(BaseHibernateUtils.nextCsn(db));
									alert.setDomainId(tag.getDomainId());
									alert.setLastModitied(now);
									alert.setType(Constants.AlertType.STATE_TEMPHISALERT);
									alert.setExpressId(ndaExpress.getId());
									alert.setTagNo(tag.getTagNo());
									alert.setStatus(Constants.AlertState.STATE_ACTIVE);
									db.save(alert);
									db.flush();
								}
							}
						}
					}
				}
				// 上传指令返回数据
				StringBuffer sb = new StringBuffer();
				sb.append("{");
				sb.append("\"").append("offline_message$GET").append("\"");
				sb.append(",");
				sb.append("\"").append("SleepTime").append("\"").append(":");
				sb.append(requestEntity.getSleepTime());
				sb.append("}");
				result = sb.toString();
			} else if (requestEntity.getPath().equalsIgnoreCase("feedback")) {
				// 设置反馈数据处理
				NDATag tag = (NDATag) db.get(NDATag.class, key);
				if (null != requestEntity.getSsid()) {
					tag.setSSIDNow(requestEntity.getSsid());
				}
				if (null != requestEntity.getPassword()) {
					tag.setPasswordNow(requestEntity.getPassword());
				}
				if (null != requestEntity.getPrecision()) {
					float temp = requestEntity.getPrecision();
					temp /= 100;
					tag.setPrecisionNow(temp);
				}
//				if (null != requestEntity.getTmax()) {
//					float temp = requestEntity.getTmax();
//					temp /= 100;
//					tag.setTemperatureMaxNow(temp);
//				}
//				if (null != requestEntity.getTmin()) {
//					float temp = requestEntity.getTmin();
//					temp /= 100;
//					tag.setTemperatureMinNow(temp);
//				}
//				if (null != requestEntity.getHmax()) {
//					float temp = requestEntity.getHmax();
//					temp /= 100;
//					tag.setHumidityMaxNow(temp);
//				}
//				if (null != requestEntity.getHmin()) {
//					float temp = requestEntity.getHmin();
//					temp /= 100;
//					tag.setHumidityMinNow(temp);
//				}
				if (null != requestEntity.getBuzzer()) {
					tag.setBuzzerNow(requestEntity.getBuzzer());
				}
				db.save(tag);
				db.flush();
			} else {
				// 进入激活
				// Logger.error("***********进行设备激活*************");
				NDATag tag = null;
				// Logger.error("***********设备编号:" + key + "*************");
				tag = (NDATag) db.get(NDATag.class, key);// getTagBySSID(db,
															// requestEntity.getBody().getBssid());
				if (tag != null) {
					String date = Utils.SF.format(now);
					// 设置激活指令返回的数据
					AuthResponse au = new AuthResponse();
					AuthResponse.Device device = new AuthResponse.Device();
					device.setActivate_status(1);
					device.setActivated_at(Utils.SF.format(tag.getCreationTime()));
					device.setBSSID(tag.getBSSID());
					device.setCreated(Utils.SF.format(tag.getCreationTime()));
					device.setDescription("device-description-" + tag.getName());
					device.setId(1);
					device.setIs_frozen(0);
					device.setIs_private(1);
					device.setKey_id(1);
					device.setLast_active(Utils.SF.format(tag.getLastModitied()));
					device.setLast_pull(Utils.SF.format(tag.getLastModitied()));
					device.setLocation("");
					device.setMetadata(tag.getBSSID() + "temperature");
					device.setName("device-name-" + tag.getName());
					device.setProduct_id(1);
					device.setProductbatch_id(1);
					device.setPtype(12335);
					device.setSerial(tag.getName());
					device.setStatus(2);
					device.setUpdated(date);
					device.setVisibly(1);
					au.setStatus(200);
					au.setMessage("device identified");
					au.setNonce(requestEntity.getNonce());
					au.setDevice(device);

					result = GSON.toJson(au);
				}
			}
			db.commit();
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			db.close();
		}

		if (result != null) {
			// Logger.error("***********准备回复***********");
			result = result.replaceAll("\n", "");
			result = result.replaceAll(" ", "");
			result = result.replace("$", " ");
			result = result.replace("activate_status\":1", "activate_status\": 1");
			result = result.replace("status\":200", "status\": 200");
			result = result.replace("nonce\":", "nonce\": ");
			// result = result.replace("nonce\":", "nonce\": ");

			Logger.error("回复数据:" + result);
			result = StringEscapeUtils.unescapeJava(result);
			Channel channel = event.getChannel();
			if (channel != null) {
				Logger.error("**************回复数据************");
				channel.write(new Nda(StringUtils.toBytesQuietly(result)));
			}
		} else {
			Logger.error("无数据可回复");
		}
	}

	private List<NDAAlertLevel> getAlertLevel(DbSession db, Integer domainId) {
		Criteria criteria = db.createCriteria(NDAAlertLevel.class);
		criteria.add(Restrictions.eq("domainId", domainId));
		criteria.addOrder(Order.asc("id"));
		return criteria.list();
	}

	/**
	 * 异常处理
	 * 
	 * @param ctx
	 *            ChannelHandlerContext对象
	 * @param event
	 *            异常事件
	 * @throws Exception
	 *             异常
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent event) throws Exception {
		Throwable cause = event.getCause();
		if (!(cause instanceof IOException)) {
			Logger.error(cause);
		}
		ctx.getChannel().close();
	}

	/*
	 * private int getAlertCount(DbSession db, String tagNo, float hour){
	 * 
	 * Calendar c = Calendar.getInstance(); c.setTime(new Date());
	 * c.add(Calendar.MINUTE, -((int)(hour * 60)));
	 * 
	 * String sql = "SELECT COUNT(*) FROM nda_alert WHERE tag_no='" + tagNo +
	 * "' AND creation_time<=:time"; SQLQuery query = db.createSQLQuery(sql);
	 * query.setParameter("time", c.getTime()); BigInteger count =
	 * (BigInteger)query.uniqueResult(); return count == null ? 0 :
	 * count.intValue(); }
	 */

	/**
	 * 判断当前运单已报警数量
	 * 
	 * @param db
	 * @param expresId
	 *            运单id
	 * @param hour
	 *            时间段
	 * @return
	 */
	private int getAlertCount(DbSession db, int expresId, float hour) {

		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.MINUTE, -((int) (hour * 60)));

		String sql = "SELECT COUNT(*) FROM nda_alert WHERE express_id='" + expresId + "' AND creation_time<=:time";
		SQLQuery query = db.createSQLQuery(sql);
		query.setParameter("time", c.getTime());
		BigInteger count = (BigInteger) query.uniqueResult();
		return count == null ? 0 : count.intValue();
	}

	/**
	 * 获取智能硬件绑定的运单
	 * 
	 * @param db
	 * @param tagNo
	 *            智能硬件编号
	 * @return
	 */
	private List<NDAExpress> getExpressByTag(DbSession db, String tagNo) {

		String sql = "SELECT * FROM nda_express WHERE id IN (SELECT express_id FROM nda_tag_express WHERE tag_no='"
				+ tagNo + "' AND status=" + Constants.BindState.STATE_ACTIVE + ") ";
		SQLQuery query = db.createSQLQuery(sql);
		query.addEntity(NDAExpress.class);
		return (List<NDAExpress>) query.list();

	}
	
	/**
	 * 针对硬件模块只能读取固定4位整数加1位符号位，将float数据进行*100后转成固定位数的String.
	 * @param in
	 * @return
	 */
	private static String transFromFloat(float in) {
		if (in>=100 || in <= -100) {
			return "-0000";
		}
		StringBuilder sBuilder = new StringBuilder();
		if (in >= 0) {
			sBuilder.append("0");
		}else {
			sBuilder.append("-");
			in = 0 - in;
		}
		int temp = (int) (in * 100);
		String tempS = temp + "";
		int size = tempS.length();
		for(;size < 4;size++) {
			sBuilder.append("0");
		}
		sBuilder.append(tempS);
		return sBuilder.toString();
	}

}

class MyDataStruct {

	float temperature;
	float humidity;
	Date time;

	public float getTemperature() {
		return temperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public float getHumidity() {
		return humidity;
	}

	public void setHumidity(float humidity) {
		this.humidity = humidity;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public MyDataStruct() {
		super();
	}

	public MyDataStruct(float temperature, float humidity, Date time) {
		this.temperature = temperature;
		this.humidity = humidity;
		this.time = time;
	}

}
