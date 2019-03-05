package com.tnsoft.web.model;

import java.io.Serializable;

public class UploadResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private int status;
	private Datapoint datapoint;

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public void setDatapoint(UploadResponse.Datapoint datapoint) {
		this.datapoint = datapoint;
	}

	public UploadResponse.Datapoint getDatapoint() {
		return datapoint;
	}

	public UploadResponse() {
		super();
	}

	public static class Datapoint {
//		private String updated;
		private String created;
//		private int visibly = 1;
//		private int datastream_id = 16;
//		private String at;
		private String y;
		private String x;
		private int dstime;
		private boolean change;
		private String ssid;
		private String password;
//		private Float precision;
//		private Float tmax;
//		private Float tmin;
		private Integer buzzer;
//		private Float hmax;
//		private Float hmin;

		//因硬件按位取值，需将float转为固定位数的String.
		private String precision;
		private String tmax;
		private String tmin;
		private String hmax;
		private String hmin;
		
		
		
		
		
//		public Float getHmax() {
//			return hmax;
//		}
//
//		public void setHmax(Float hmax) {
//			this.hmax = hmax;
//		}
//
//		public Float getHmin() {
//			return hmin;
//		}
//
//		public void setHmin(Float hmin) {
//			this.hmin = hmin;
//		}

		public String getPrecision() {
			return precision;
		}

		public void setPrecision(String precision) {
			this.precision = precision;
		}

		public String getTmax() {
			return tmax;
		}

		public void setTmax(String tmax) {
			this.tmax = tmax;
		}

		public String getTmin() {
			return tmin;
		}

		public void setTmin(String tmin) {
			this.tmin = tmin;
		}

		public String getHmax() {
			return hmax;
		}

		public void setHmax(String hmax) {
			this.hmax = hmax;
		}

		public String getHmin() {
			return hmin;
		}

		public void setHmin(String hmin) {
			this.hmin = hmin;
		}

		

		public String getSsid() {
			return ssid;
		}

		public void setSsid(String ssid) {
			this.ssid = ssid;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

//		public Float getPrecision() {
//			return precision;
//		}
//
//		public void setPrecision(Float precision) {
//			this.precision = precision;
//		}
//
//		public Float getTmax() {
//			return tmax;
//		}
//
//		public void setTmax(Float tmax) {
//			this.tmax = tmax;
//		}
//
//		public Float getTmin() {
//			return tmin;
//		}
//
//		public void setTmin(Float tmin) {
//			this.tmin = tmin;
//		}

		public Integer getBuzzer() {
			return buzzer;
		}

		public void setBuzzer(Integer buzzer) {
			this.buzzer = buzzer;
		}

//		public int getPicktime() {
//			return picktime;
//		}
//
//		public void setPicktime(int picktime) {
//			this.picktime = picktime;
//		}

//		public void setUpdated(String updated) {
//			this.updated = updated;
//		}
//
//		public String getUpdated() {
//			return updated;
//		}

		public void setCreated(String created) {
			this.created = created;
		}

		public String getCreated() {
			return created;
		}

//		public void setVisibly(int visibly) {
//			this.visibly = visibly;
//		}
//
//		public int getVisibly() {
//			return visibly;
//		}
//
//		public void setDatastream_id(int datastream_id) {
//			this.datastream_id = datastream_id;
//		}
//
//		public int getDatastream_id() {
//			return datastream_id;
//		}
//
//		public void setAt(String at) {
//			this.at = at;
//		}
//
//		public String getAt() {
//			return at;
//		}

		public void setY(String y) {
			this.y = y;
		}

		public String getY() {
			return y;
		}

		public void setX(String x) {
			this.x = x;
		}

		public String getX() {
			return x;
		}

		public void setDstime(int dstime) {
			this.dstime = dstime;
		}

		public int getDstime() {
			return dstime;
		}

		public boolean isChange() {
			return change;
		}

		public void setChange(boolean change) {
			this.change = change;
		}
	}
}
