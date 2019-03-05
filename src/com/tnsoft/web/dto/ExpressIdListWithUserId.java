package com.tnsoft.web.dto;

import java.io.Serializable;
import java.util.List;

public class ExpressIdListWithUserId implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int userId;
	private List<Integer> expressIdList;
	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public List<Integer> getExpressIdList() {
		return expressIdList;
	}
	public void setExpressIdList(List<Integer> expressIdList) {
		this.expressIdList = expressIdList;
	}
	
}
