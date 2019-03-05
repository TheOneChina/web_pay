 /*
  * Copyright ® 2016 Shanghai TNSOFT Co. Ltd.
  *  All right reserved.
  */
package com.tnsoft.web.server;

public class Nda {
    
    //保存硬件上传的数据
    private byte[] data;
    
    public Nda(byte[] data) {
        this.data = data;
    }
    
    public Nda() {
        super();
    }
    
    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
