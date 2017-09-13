package cn.xvkang.netty;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import cn.xvkang.netty.handler.ZhServerHandler;
import cn.xvkang.netty.protocolbuffer.ZhMessageProto;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class Server {
	private String bindhostip;
	private int port;

	public Server(String bindhostip, int port) {
		this.bindhostip = bindhostip;
		this.port = port;
	}

	public void start() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap sbs = new ServerBootstrap().group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress(port))
					.childHandler(new ChannelInitializer<SocketChannel>() {

						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
							ch.pipeline().addLast(new ProtobufEncoder());
							ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
							ch.pipeline().addLast(new ProtobufDecoder(ZhMessageProto.ZhMessage.getDefaultInstance()));
							ch.pipeline().addLast(new ZhServerHandler());
						};

					}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
			// 绑定端口，开始接收进来的连接
			ChannelFuture future = sbs.bind(bindhostip, port).sync();
			
			System.out.println("Server start listen at " + bindhostip+":" + port);
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		//serverIp  serverPort 两个参数
		Path homePath = Paths.get(System.getProperty("user.home"),"test","server");
		Files.createDirectories(homePath);
		String bindhostip = "127.0.0.1";
		int port=8888;
		if (args.length== 1) {
			bindhostip = args[0];
		} else if (args.length > 1) {
			port = Integer.parseInt(args[1]);
			bindhostip = args[0];
		} 
		new Server(bindhostip, port).start();
	}

}
