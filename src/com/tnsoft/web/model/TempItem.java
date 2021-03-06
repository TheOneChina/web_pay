/*
 * Copyright (c) 2015 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * Visit our website www.913app.com
 */
package com.tnsoft.web.model;

import java.io.Serializable;

public class TempItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String temperature;
    private String humidity;
    private String time;
    
    public TempItem() {
        super();
    }
    
    public TempItem(String temperature, String humidity, String time){
        this.temperature = temperature;
        this.humidity = humidity;
        this.time = time;
    }

    public void setValue(String temperature) {
        this.temperature = temperature;
    }

    public String getValue() {
        return temperature;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

	public String getHumidity() {
		return humidity;
	}

	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}
}
