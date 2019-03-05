 /*
  * Copyright ® 2016 Shanghai TNSOFT Co. Ltd.
  *  All right reserved.
  */
package com.tnsoft.web.server;

import com.expertise.common.io.IoHelper;

import com.expertise.common.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * 将Nda对象转化为二进制，用于向智能硬件发送数据
 */
public class NdaEncoder extends OneToOneEncoder {
    
    public NdaEncoder() {
        super();
    }
    
    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) {
        if (!(msg instanceof Nda)) {
            return msg;
        }
        
        Nda command = (Nda)msg;
        byte[] rawData = command.getData();

        return ChannelBuffers.wrappedBuffer(rawData);
    }

}
