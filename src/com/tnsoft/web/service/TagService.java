package com.tnsoft.web.service;

import java.util.List;
import java.util.Set;
import com.tnsoft.hibernate.model.NDAExpress;
import com.tnsoft.hibernate.model.NDATag;
import com.tnsoft.web.model.Response;

public interface TagService extends BaseService<NDATag> {

	public Response scanTag(String tagNo, Integer domainId);

	public List<NDAExpress> getTagExpressHistory(String tagNo);

	public Response saveTagAPConfig(String SSID, String password, String tagNo);

	public Set<NDATag> getTagByUId(Integer userId);

	public NDATag getTagByEId(Integer expressId);

//	public Response editPickTime(String id, String time);

	public Response editBuzzer(String tagNo, int model);

	public Response editTag(String[] tagNo, String SSID, String password, Integer buzzer,Integer appointStart);

	public Response tagTemplate(String[] tagNos, Integer model);
	
	public Response editAppointStart(String tagNo, String time);
	
	public Response makeTag(String tagNo,Integer domainId);
	
}
