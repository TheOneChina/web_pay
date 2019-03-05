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
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "crash")
public class Crash implements Serializable {

    private static final long serialVersionUID = 1L;

    private String hash;
    private String appVersion;
    private String deviceId;
    private String stacktrace;
    private int crashCount;
    private int state;
    private Date lastModified;
    private long csn;

    public Crash() {
    }

    public Crash(String stacktrace, String appVersion) {
        this.stacktrace = stacktrace;
        this.appVersion = appVersion;
    }

    @Id
    @Column(name = "hash", nullable = false)
    public String getHash() {
        return hash;
    }

    @Column(name = "app_version")
    public String getAppVersion() {
        return appVersion;
    }

    @Column(name = "device_id")
    public String getDeviceId() {
        return deviceId;
    }

    @Column(name = "stacktrace")
    public String getStacktrace() {
        return stacktrace;
    }

    @Column(name = "crash_count")
    public int getCrashCount() {
        return crashCount;
    }

    @Column(name = "state")
    public int getState() {
        return state;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    public Date getLastModified() {
        return lastModified;
    }

    @Column(name = "csn")
    public long getCsn() {
        return csn;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }

    public void setCrashCount(int crashCount) {
        this.crashCount = crashCount;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public void setCsn(long csn) {
        this.csn = csn;
    }

}
