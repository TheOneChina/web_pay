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
@Table(name = "nda_log")
public class NDALog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String userName;
    private String operation;
    private String time;
    private Date operationTime;
    private int domainId;
    
    public NDALog() {
        super();
    }

    public void setId(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_log_id_seq")
    @SequenceGenerator(name = "nda_log_id_seq", sequenceName = "nda_log_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "user_name")
    public String getUserName() {
        return userName;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Column(name = "operation")
    public String getOperation() {
        return operation;
    }

    public void setTime(String time) {
        this.time = time;
    }
    
    @Transient
    public String getTime() {
        return time;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "operation_time")
    public Date getOperationTime() {
        return operationTime;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    @Column(name = "domain_id")
    public int getDomainId() {
        return domainId;
    }
}
