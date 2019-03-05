package com.tnsoft.hibernate.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "nda_tag")
public class NDATag implements Serializable {

	private static final long serialVersionUID = 1L;
	// 设备属性
	private String tagNo;
	private Integer domainId;
	private Integer electricity;
	private Date creationTime;
	private Date lastModitied;
	private Integer status;
	private String token;
	private String bSSID;
	private String name;
	private Integer wifiStatus;//设备wifi的开关状态

	private String statusName;

	// 设备可设置参数
	// 校准值
	private Float precision;
	private Float precisionNow;
	// 上传周期，以分钟为单位
	private Integer sleepTime;// 设备设置
//	private Integer sleepTimeNow;// 设备当前
	private Integer expressSleepTime;// 订单上传周期
	// 蜂鸣器
	private Integer buzzer;
	private Integer buzzerNow;
	// 温度上下限
	private Float temperatureMin;
	private Float temperatureMax;
	private Float temperatureMinNow;
	private Float temperatureMaxNow;
	
	// 湿度上下限
	private Float humidityMin;
	private Float humidityMax;
	private Float humidityMinNow;
	private Float humidityMaxNow;
	
	// WiFi账号密码
	private String SSID;
	private String password;
	private String SSIDNow;
	private String passwordNow;
	
	// 延时启动
	private Integer appointStart;

	public NDATag() {
	}

	@Column(name = "wifi_status")
	public Integer getWifiStatus() {
		return wifiStatus;
	}

	public void setWifiStatus(Integer wifiStatus) {
		this.wifiStatus = wifiStatus;
	}

	@Column(name = "humidity_min")
	public Float getHumidityMin() {
		return humidityMin;
	}

	public void setHumidityMin(Float humidityMin) {
		this.humidityMin = humidityMin;
	}

	@Column(name = "humidity_max")
	public Float getHumidityMax() {
		return humidityMax;
	}

	public void setHumidityMax(Float humidityMax) {
		this.humidityMax = humidityMax;
	}

	@Column(name = "humidity_min_now")
	public Float getHumidityMinNow() {
		return humidityMinNow;
	}

	public void setHumidityMinNow(Float humidityMinNow) {
		this.humidityMinNow = humidityMinNow;
	}

	@Column(name = "humidity_max_now")
	public Float getHumidityMaxNow() {
		return humidityMaxNow;
	}

	public void setHumidityMaxNow(Float humidityMaxNow) {
		this.humidityMaxNow = humidityMaxNow;
	}

	@Column(name = "express_sleep_time")
	public Integer getExpressSleepTime() {
		return expressSleepTime;
	}

	public void setExpressSleepTime(Integer expressSleepTime) {
		this.expressSleepTime = expressSleepTime;
	}

	@Column(name = "buzzer_now")
	public Integer getBuzzerNow() {
		return buzzerNow;
	}

	public void setBuzzerNow(Integer buzzerNow) {
		this.buzzerNow = buzzerNow;
	}

	@Column(name = "temperature_min_now")
	public Float getTemperatureMinNow() {
		return temperatureMinNow;
	}

	public void setTemperatureMinNow(Float temperatureMinNow) {
		this.temperatureMinNow = temperatureMinNow;
	}

	@Column(name = "temperature_max_now")
	public Float getTemperatureMaxNow() {
		return temperatureMaxNow;
	}

	public void setTemperatureMaxNow(Float temperatureMaxNow) {
		this.temperatureMaxNow = temperatureMaxNow;
	}

	@Column(name = "ssid_now")
	public String getSSIDNow() {
		return SSIDNow;
	}

	public void setSSIDNow(String sSIDNow) {
		SSIDNow = sSIDNow;
	}

	@Column(name = "password_now")
	public String getPasswordNow() {
		return passwordNow;
	}

	public void setPasswordNow(String passwordNow) {
		this.passwordNow = passwordNow;
	}

//	@Column(name = "pick_time_now")
//	public Integer getPickTimeNow() {
//		return pickTimeNow;
//	}
//
//	public void setPickTimeNow(Integer pickTimeNow) {
//		this.pickTimeNow = pickTimeNow;
//	}

	public void setTagNo(String tagNo) {
		this.tagNo = tagNo;
	}

	@Id
	@Column(name = "tag_no", nullable = false)
	public String getTagNo() {
		return tagNo;
	}

	public void setDomainId(Integer domainId) {
		this.domainId = domainId;
	}

	@Column(name = "domain_id")
	public Integer getDomainId() {
		return domainId;
	}

	public void setTemperatureMin(Float temperatureMin) {
		this.temperatureMin = temperatureMin;
	}

	@Column(name = "appoint_start")
	public Integer getAppointStart() {
		return appointStart;
	}

	public void setAppointStart(Integer appointStart) {
		this.appointStart = appointStart;
	}

	@Column(name = "temperature_min")
	public Float getTemperatureMin() {
		return temperatureMin;
	}

	public void setTemperatureMax(Float temperatureMax) {
		this.temperatureMax = temperatureMax;
	}

	@Column(name = "temperature_max")
	public Float getTemperatureMax() {
		return temperatureMax;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creation_time")
	public Date getCreationTime() {
		return creationTime;
	}

	public void setLastModitied(Date lastModitied) {
		this.lastModitied = lastModitied;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_modified")
	public Date getLastModitied() {
		return lastModitied;
	}

	@Column(name = "electricity")
	public Integer getElectricity() {
		return electricity;
	}

	public void setElectricity(Integer electricity) {
		this.electricity = electricity;
	}

	@Column(name = "buzzer")
	public Integer getBuzzer() {
		return buzzer;
	}

	public void setBuzzer(Integer buzzer) {
		this.buzzer = buzzer;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	@Transient
	public String getStatusName() {
		return statusName;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Column(name = "token")
	public String getToken() {
		return token;
	}

	public void setBSSID(String bSSID) {
		this.bSSID = bSSID;
	}

	@Column(name = "bssid")
	public String getBSSID() {
		return bSSID;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setSleepTime(Integer sleepTime) {
		this.sleepTime = sleepTime;
	}

	@Column(name = "sleep_time")
	public Integer getSleepTime() {
		return sleepTime;
	}

	@Column(name = "precision")
	public Float getPrecision() {
		return precision;
	}

	public void setPrecision(Float precision) {
		this.precision = precision;
	}

	@Column(name = "ssid")
	public String getSSID() {
		return SSID;
	}

	public void setSSID(String sSID) {
		SSID = sSID;
	}

	@Column(name = "password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

//	@Column(name = "pick_time")
//	public Integer getPickTime() {
//		return pickTime;
//	}
//
//	public void setPickTime(Integer pickTime) {
//		this.pickTime = pickTime;
//	}

	@Column(name = "precision_now")
	public Float getPrecisionNow() {
		return precisionNow;
	}

	public void setPrecisionNow(Float precisionNow) {
		this.precisionNow = precisionNow;
	}

}
