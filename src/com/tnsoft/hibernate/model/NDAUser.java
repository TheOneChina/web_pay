/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.913app.com
 */

package com.tnsoft.hibernate.model;

import java.io.Serializable;
import java.util.Arrays;
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

@Entity
@Table(name = "nda_user")
public class NDAUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private int type;
    private byte[] password;
    private String ticket;
    private int attempt;
    private String nickName;
    private String gender;
    private String birthDate;
    private String email;
    private String mobile;
    private Integer iconId;
    private Date creationTime;
    private Date lastModified;
    private Date lastLogin;
    private int status;
    private String staffNo;
    private String address;
    private int domainId;
    
    private String statusName;
    private String description;

    public NDAUser() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_user_id_seq")
    @SequenceGenerator(name = "nda_user_id_seq", sequenceName = "nda_user_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    @Column(name = "type")
    public int getType() {
        return type;
    }

    @Column(name = "password")
    public byte[] getPassword() {
        return password;
    }

    @Column(name = "ticket")
    public String getTicket() {
        return ticket;
    }

    @Column(name = "attempt")
    public int getAttempt() {
        return attempt;
    }

    @Column(name = "nick_name")
    public String getNickName() {
        return nickName;
    }

    @Column(name = "gender")
    public String getGender() {
        return gender;
    }

    @Column(name = "birth_date")
    public String getBirthDate() {
        return birthDate;
    }

    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    @Column(name = "mobile")
    public String getMobile() {
        return mobile;
    }

    @Column(name = "icon_id")
    public Integer getIconId() {
        return iconId;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_time")
    public Date getCreationTime() {
        return creationTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    public Date getLastModified() {
        return lastModified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login")
    public Date getLastLogin() {
        return lastLogin;
    }

    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setIconId(Integer iconId) {
        this.iconId = iconId;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setStaffNo(String staffNo) {
        this.staffNo = staffNo;
    }

    @Column(name = "staff_no")
    public String getStaffNo() {
        return staffNo;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
    
    @Transient
    public String getStatusName() {
        return statusName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Column(name = "address")
    public String getAddress() {
        return address;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    @Column(name = "domain_id")
	public int getDomainId() {
		return domainId;
	}

	public void setDomainId(int domainId) {
		this.domainId = domainId;
	}

	@Override
	public String toString() {
		return "NDAUser [id=" + id + ", name=" + name + ", type=" + type + ", password=" + Arrays.toString(password)
				+ ", ticket=" + ticket + ", attempt=" + attempt + ", nickName=" + nickName + ", gender=" + gender
				+ ", birthDate=" + birthDate + ", email=" + email + ", mobile=" + mobile + ", iconId=" + iconId
				+ ", creationTime=" + creationTime + ", lastModified=" + lastModified + ", lastLogin=" + lastLogin
				+ ", status=" + status + ", staffNo=" + staffNo + ", address=" + address + ", domainId=" + domainId
				+ ", statusName=" + statusName + ", description=" + description + "]";
	}
	
	
}
