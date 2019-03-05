///*
// * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
// * All right reserved.
// * Visit our website www.913app.com
// */
//package com.tnsoft.hibernate.model;
//
//import java.io.Serializable;                     此实体类作废!!!!!!!!
//															此实体类作废!!!!!!!!
//import javax.persistence.Column; 
//import javax.persistence.Entity;                     此实体类作废!!!!!!!!
//import javax.persistence.GeneratedValue;           此实体类作废!!!!!!!!
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.SequenceGenerator;
//import javax.persistence.Table;
//
//@Entity
//@Table(name = "nda_alert_express")
//public class NDAAlertExpress implements Serializable {
//    
//    private static final long serialVersionUID = 1L;
//    
//    private int id;
//    private int domainId;
//    private String tagNo;
//    private int alertId;
//    private int expressId;
//    
//    public NDAAlertExpress() {
//        super();
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO, generator = "nda_alert_express_id_seq")
//    @SequenceGenerator(name = "nda_alert_express_id_seq", sequenceName = "nda_alert_express_id_seq")
//    @Column(name = "id", nullable = false)
//    public int getId() {
//        return id;
//    }
//
//    public void setDomainId(int domainId) {
//        this.domainId = domainId;
//    }
//
//    @Column(name = "domain_id")
//    public int getDomainId() {
//        return domainId;
//    }
//
//    public void setTagNo(String tagNo) {
//        this.tagNo = tagNo;
//    }
//
//    @Column(name = "tag_no")
//    public String getTagNo() {
//        return tagNo;
//    }
//
//    public void setAlertId(int alertId) {
//        this.alertId = alertId;
//    }
//
//    @Column(name = "nda_alert_id")
//    public int getAlertId() {
//        return alertId;
//    }
//
//    public void setExpressId(int expressId) {
//        this.expressId = expressId;
//    }
//
//    @Column(name = "express_id")
//    public int getExpressId() {
//        return expressId;
//    }
//}
