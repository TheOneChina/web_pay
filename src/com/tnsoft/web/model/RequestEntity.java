package com.tnsoft.web.model;

import java.io.Serializable;

import java.util.Date;

/**
 * 硬件发送到云端指令映射实体
 */
public class RequestEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String path;
	private String method;
	private Meta meta;
	private Body body;
	private int nonce;
	private Date Time;
	private float SleepTime;
	private String ssid;
	private String password;
	private Float precision;
	private Float tmax;
	private Float tmin;
	private Float hmax;
	private Float hmin;
	private Integer buzzer;
	
	
	public RequestEntity() {
		super();
	}

	public void setNonce(int nonce) {
		this.nonce = nonce;
	}

	public int getNonce() {
		return nonce;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public void setMeta(RequestEntity.Meta meta) {
		this.meta = meta;
	}

	public RequestEntity.Meta getMeta() {
		return meta;
	}

	public void setBody(RequestEntity.Body body) {
		this.body = body;
	}

	public RequestEntity.Body getBody() {
		return body;
	}

	public void setTime(Date Time) {
		this.Time = Time;
	}

	public Date getTime() {
		return Time;
	}

	public float getSleepTime() {
		return SleepTime;
	}

	public void setSleepTime(float sleepTime) {
		SleepTime = sleepTime;
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

	public Float getPrecision() {
		return precision;
	}

	public void setPrecision(Float precision) {
		this.precision = precision;
	}

	public Float getTmax() {
		return tmax;
	}

	public void setTmax(Float tmax) {
		this.tmax = tmax;
	}

	public Float getTmin() {
		return tmin;
	}

	public void setTmin(Float tmin) {
		this.tmin = tmin;
	}

	public Float getHmax() {
		return hmax;
	}

	public void setHmax(Float hmax) {
		this.hmax = hmax;
	}

	public Float getHmin() {
		return hmin;
	}

	public void setHmin(Float hmin) {
		this.hmin = hmin;
	}

	public Integer getBuzzer() {
		return buzzer;
	}

	public void setBuzzer(Integer buzzer) {
		this.buzzer = buzzer;
	}




	/////////////////////////////////////////////////////////////
	public static class Meta implements Serializable {

		private static final long serialVersionUID = 1L;

		private String Authorization;

		public void setAuthorization(String Authorization) {
			this.Authorization = Authorization;
		}

		public String getAuthorization() {
			return Authorization;
		}
	}

	public static class Body implements Serializable {

		private static final long serialVersionUID = 1L;

		private String encrypt_method;
		private String bssid;
		private String token;
		private Datapoint datapoint;
		private String offlineData;

		public void setEncrypt_method(String encrypt_method) {
			this.encrypt_method = encrypt_method;
		}

		public String getEncrypt_method() {
			return encrypt_method;
		}

		public void setBssid(String bssid) {
			this.bssid = bssid;
		}

		public String getBssid() {
			return bssid;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getToken() {
			return token;
		}

		public void setDatapoint(RequestEntity.Datapoint datapoint) {
			this.datapoint = datapoint;
		}

		public RequestEntity.Datapoint getDatapoint() {
			return datapoint;
		}

		public String getOfflineData() {
			return offlineData;
		}

		public void setOfflineData(String offlineData) {
			this.offlineData = offlineData;
		}
	}

	public static class Datapoint implements Serializable {

		private static final long serialVersionUID = 1L;

		private String x;// 温度
		private String y;// 湿度
		private Integer vdd;//电池电压
		private Integer wifi; //wifi开关状态

		public void setX(String x) {
			this.x = x;
		}

		public String getX() {
			return x;
		}

		public void setY(String y) {
			this.y = y;
		}

		public String getY() {
			return y;
		}

		public Integer getVdd() {
			return vdd;
		}

		public void setVdd(Integer vdd) {
			this.vdd = vdd;
		}

		public Integer getWifi() {
			return wifi;
		}

		public void setWifi(Integer wifi) {
			this.wifi = wifi;
		}

	}

}
