/*
 * Copyright (c) 2016 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.tn-soft.cn
 */
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

@Entity
@Table(name = "nda_user_express")
public class NDAUserExpress implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int id;
    private int domainId;
    private int expressId;
    private int userId;
    private Date creationTime;
    private Date lastModitied;
    private int status;
    
    public NDAUserExpress() {
        super();
    }

    public void setId(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_user_express_id_seq")
    @SequenceGenerator(name = "nda_user_express_id_seq", sequenceName = "nda_user_express_id_seq")
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

    public void setExpressId(int expressId) {
        this.expressId = expressId;
    }

    @Column(name = "express_id")
    public int getExpressId() {
        return expressId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Column(name = "user_id")
    public int getUserId() {
        return userId;
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

    public void setStatus(int status) {
        this.status = status;
    }

    @Column(name = "status")
    public int getStatus() {
        return status;
    }

	@Override
	public String toString() {
		return "NDAUserExpress [id=" + id + ", domainId=" + domainId + ", expressId=" + expressId + ", userId=" + userId
				+ ", creationTime=" + creationTime + ", lastModitied=" + lastModitied + ", status=" + status + "]";
	}
    
    
}
