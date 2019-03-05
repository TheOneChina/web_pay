/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.913app.com
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
import javax.persistence.Transient;

@Entity
@Table(name = "nda_alert_level")
public class NDAAlertLevel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int id;
    private int domainId;
    private String name;
    private float hours;
    private int times;
    private Date creationTime;
    private Date lastModified;
    private int status;
    private String statusKey;
    
    public NDAAlertLevel() {
        super();
    }

    public void setId(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_alert_level_id_seq")
    @SequenceGenerator(name = "nda_alert_level_id_seq", sequenceName = "nda_alert_level_id_seq")
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

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setHours(float hours) {
        this.hours = hours;
    }

    @Column(name = "hours")
    public float getHours() {
        return hours;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    @Column(name = "times")
    public int getTimes() {
        return times;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_time")
    public Date getCreationTime() {
        return creationTime;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    public Date getLastModified() {
        return lastModified;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    public void setStatusKey(String statusKey) {
        this.statusKey = statusKey;
    }
    
    @Transient
    public String getStatusKey() {
        return statusKey;
    }
}
