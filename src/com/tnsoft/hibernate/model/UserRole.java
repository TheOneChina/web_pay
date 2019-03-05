/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.913app.com
 */

package com.tnsoft.hibernate.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "user_role")
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private int roleId;
//    private int domainId;
//    private int flag;
//    private long csn;
    private int status;

    public UserRole() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "user_role_id_seq")
    @SequenceGenerator(name = "user_role_id_seq", sequenceName = "user_role_id_seq")
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    @Column(name = "user_id")
    public int getUserId() {
        return userId;
    }

    @Column(name = "role_id")
    public int getRoleId() {
        return roleId;
    }

//    @Column(name = "domain_id")
//    public int getDomainId() {
//        return domainId;
//    }
//
//    @Column(name = "flag")
//    public int getFlag() {
//        return flag;
//    }
//
//    @Column(name = "csn")
//    public long getCsn() {
//        return csn;
//    }

    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

//    public void setDomainId(int domainId) {
//        this.domainId = domainId;
//    }
//
//    public void setFlag(int flag) {
//        this.flag = flag;
//    }
//
//    public void setCsn(long csn) {
//        this.csn = csn;
//    }

    public void setStatus(int status) {
        this.status = status;
    }

}
