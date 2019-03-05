/*
 *  Copyright ® 2016 Shanghai TNSOFT Co. Ltd.
 *  All right reserved.
 */
package com.tnsoft.web.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.expertise.common.codec.Hex;

/**
 * 硬件通讯服务器，采用Netty框架开发
 */
public class NdaServer {

	private DefaultChannelGroup allChannels = new DefaultChannelGroup("NdaServer");
	private ChannelFactory serverFactory;
	private AtomicBoolean stopped = new AtomicBoolean(false);
	private int port;// 监听端口号

	public NdaServer(int port) {
		this.port = port;
	}

	/**
	 * 启动服务器
	 */
	public void start() {
		stopped.set(false);

		// 具体步骤按Netty API
		Executor executor = Executors.newCachedThreadPool();
		serverFactory = new NioServerSocketChannelFactory(executor, executor);
		ServerBootstrap sb = new ServerBootstrap(serverFactory);

		sb.setPipelineFactory(new PipelineFactory());
		Channel channel = sb.bind(new InetSocketAddress(port));
		allChannels.add(channel);
	}

	public boolean isRunning() {
		return !stopped.get();
	}

	/**
	 * 关闭服务器并释放资源
	 */
	public void stop() {
		if (stopped.get()) {
			return;
		}
		stopped.set(true);

		ChannelGroupFuture future = allChannels.close();
		future.awaitUninterruptibly(10 * 1000);
		serverFactory.releaseExternalResources();
		allChannels.clear();
	}

	private static final class PipelineFactory implements ChannelPipelineFactory {

		public PipelineFactory() {
		}

		// 设置报文编码，解码处理，已经数据处理
		public ChannelPipeline getPipeline() throws GeneralSecurityException, IOException {
			ChannelPipeline p = Channels.pipeline();
			p.addLast("decoder", new NdaDecoder());// 解码
			p.addLast("encoder", new NdaEncoder());// 编码
			p.addLast("handler", new NdaHandler());// 数据处理
			return p;
		}
	}

	public static void main(String... args) {
		String str = "06010000100A0000100A0000000A31323334353637383930";
		byte[] bytes = Hex.toByteArray(str);
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.get();
		bb.get();
		System.out.println(bb.getInt());
		System.out.println(bb.getInt());
		System.out.println(bb.getInt());
	}

}
