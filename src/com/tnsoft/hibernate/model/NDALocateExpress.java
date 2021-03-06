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
import javax.persistence.Transient;

@Entity
@Table(name = "nda_location_express")
public class NDALocateExpress implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int id;
    private int domainId;
    private int expressId;
    private double lat;
    private double lng;
    private Date creationTime;
    private Date lastModitied;   
    private String name;
    
    private String time;
    
    public NDALocateExpress() {
        super();
    }

    public void setId(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_location_express_id_seq")
    @SequenceGenerator(name = "nda_location_express_id_seq", sequenceName = "nda_location_express_id_seq")
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

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Column(name = "lat")
    public double getLat() {
        return lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Column(name = "lng")
    public double getLng() {
        return lng;
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

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setTime(String time) {
        this.time = time;
    }
    
    @Transient
    public String getTime() {
        return time;
    }
}
