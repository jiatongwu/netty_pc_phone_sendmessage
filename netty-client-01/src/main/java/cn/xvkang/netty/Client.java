package cn.xvkang.netty;

import cn.xvkang.netty.handler.ZhClientHandler;
import cn.xvkang.netty.protocolbuffer.ZhMessageProto;
import cn.xvkang.netty.thread.ConsoleThread;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class Client {
	public static boolean isshutdown=false;
	static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));
	public static String whoami = "pc001";

	public static void main(String[] args) throws Exception {
		//serverIp serverPort whoami 三个参数
		if (args.length != 3) {
			System.out.println("必须有三个参数：serverIp serverPort whoami");
			return;
		}
		String serverIp = args[0];
		int serverPort = Integer.parseInt(args[1]);
		whoami = args[2];
		// Configure the client.
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
							ch.pipeline().addLast(new ProtobufEncoder());
							ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
							ch.pipeline().addLast(new ProtobufDecoder(ZhMessageProto.ZhMessage.getDefaultInstance()));
							p.addLast(new ZhClientHandler());
						}
					});

			ChannelFuture future = b.connect(serverIp, serverPort).sync();
			ConsoleThread consoleThread=new ConsoleThread(whoami, future) ;
			Thread t=new Thread(consoleThread);
			t.start();
			future.channel().closeFuture().sync();
			Client.isshutdown=true;
		} finally {
			group.shutdownGracefully();
			
		}

	}

}
