package com.tnsoft.hibernate.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * 
 * @author wangyuxing
 *
 */
@Entity
@Table(name = "nda_express")
public class NDAExpress implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	private int domainId;
	private String expressNo;
	private Date creationTime;
	private Date lastModitied;
	private Date checkInTime;
	private Date checkOutTime;
	private int status;
	private Float temperatureMin;
	private Float temperatureMax;
	private Integer sleepTime;

	private String statusName;
	private String temps;
	private String userName;
	private String description;

	private String checkInTimeStr;
	private String checkOutTimeStr;

	private Integer appointStart;
	private Integer appointEnd;

	public NDAExpress() {
		super();
	}

	public void setId(int id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_express_id_seq")
	@SequenceGenerator(name = "nda_express_id_seq", sequenceName = "nda_express_id_seq")
	@Column(name = "id", nullable = false)
	public int getId() {
		return id;
	}

	public void setDomainId(int domainId) {
		this.domainId = domainId;
	}

	@Column(name = "domain_id")
	public int getDomainId() {
		return domainId;
	}

	public void setExpressNo(String expressNo) {
		this.expressNo = expressNo;
	}

	@Column(name = "express_no")
	public String getExpressNo() {
		return expressNo;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creation_time")
	public Date getCreationTime() {
		return creationTime;
	}

	public void setLastModitied(Date lastModified) {
		this.lastModitied = lastModified;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_modified")
	public Date getLastModitied() {
		return lastModitied;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Column(name = "status")
	public int getStatus() {
		return status;
	}

	public void setCheckInTime(Date checkInTime) {
		this.checkInTime = checkInTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "checkin_time")
	public Date getCheckInTime() {
		return checkInTime;
	}

	public void setCheckOutTime(Date checkOutTime) {
		this.checkOutTime = checkOutTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "checkout_time")
	public Date getCheckOutTime() {
		return checkOutTime;
	}

	public void setTemps(String temps) {
		this.temps = temps;
	}

	@Transient
	public String getTemps() {
		return temps;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	@Transient
	public String getStatusName() {
		return statusName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Transient
	public String getUserName() {
		return userName;
	}

	public void setCheckInTimeStr(String checkInTimeStr) {
		this.checkInTimeStr = checkInTimeStr;
	}

	@Column(name = "sleep_time")
	public Integer getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(Integer sleepTime) {
		this.sleepTime = sleepTime;
	}

	@Transient
	public String getCheckInTimeStr() {
		return checkInTimeStr;
	}

	public void setCheckOutTimeStr(String checkOutTimeStr) {
		this.checkOutTimeStr = checkOutTimeStr;
	}

	@Transient
	public String getCheckOutTimeStr() {
		return checkOutTimeStr;
	}

	public void setTemperatureMin(Float temperatureMin) {
		this.temperatureMin = temperatureMin;
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

	@Column(name = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "appoint_start")
	public Integer getAppointStart() {
		return appointStart;
	}

	public void setAppointStart(Integer appointStart) {
		this.appointStart = appointStart;
	}

	@Column(name = "appoint_end")
	public Integer getAppointEnd() {
		return appointEnd;
	}

	public void setAppointEnd(Integer appointEnd) {
		this.appointEnd = appointEnd;
	}

	@Override
	public String toString() {
		return "NDAExpress [id=" + id + ", domainId=" + domainId + ", expressNo=" + expressNo + ", creationTime="
				+ creationTime + ", lastModitied=" + lastModitied + ", checkInTime=" + checkInTime + ", checkOutTime="
				+ checkOutTime + ", status=" + status + ", temperatureMin=" + temperatureMin + ", temperatureMax="
				+ temperatureMax + ", sleepTime=" + sleepTime + ", statusName=" + statusName + ", temps=" + temps
				+ ", userName=" + userName + ", description=" + description + ", checkInTimeStr=" + checkInTimeStr
				+ ", checkOutTimeStr=" + checkOutTimeStr + ", appointStart=" + appointStart + ", appointEnd="
				+ appointEnd + "]";
	}
	
	
}
