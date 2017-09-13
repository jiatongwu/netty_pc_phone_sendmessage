package cn.xvkang.netty.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.protobuf.ByteString;

import cn.xvkang.netty.Client;
import cn.xvkang.netty.protocolbuffer.ZhMessageProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

public class ZhClientHandler extends ChannelInboundHandlerAdapter {

	/**
	 * ZhMessageProto.ZhMessage用于 pc同server phone同server发送信息的格式 contenttype 1是发送文本信息
	 * 2是发送图片文件 3是发送语音文件 4是询问phone的位置 5是发送位置信息 6是报告我是谁 whoami 格式是 phone01 phone02
	 * pc01 pc02目前固定是这种格式
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		Channel channel=ctx.channel();
		Path clientPath = Paths.get(System.getProperty("user.home"), "test", Client.whoami);
		AttributeKey<String> client_path_key = AttributeKey.valueOf("clientPath");
		channel.attr(client_path_key).set(clientPath.toString());
		try {
			Files.createDirectories(clientPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ZhMessageProto.ZhMessage.Builder builder = ZhMessageProto.ZhMessage.newBuilder();
		builder.setWhoami(Client.whoami);
		builder.setContenttype("6");
		ctx.writeAndFlush(builder.build());
	}

	/**
	 * ZhMessageProto.ZhMessage用于 pc同server phone同server发送信息的格式 contenttype 1是发送文本信息
	 * 2是发送图片文件 3是发送语音文件 4是询问phone的位置 5是发送位置信息 6是报告我是谁(在client ChannelActive时发送的)
	 * whoami 格式是 phone01 phone02 pc01 pc02目前固定是这种格式
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = sdf.format(new Date());
		ZhMessageProto.ZhMessage req = (ZhMessageProto.ZhMessage) msg;
		String contenttype = req.getContenttype();
		Channel channel=ctx.channel();
		//Path clientPath = Paths.get(System.getProperty("user.home"), "test", Client.whoami);
		AttributeKey<String> client_path_key = AttributeKey.valueOf("clientPath");
		String clientPathString=channel.attr(client_path_key).get();
		if (Client.whoami.startsWith("phone")) {
			// 如果是手机只会接收到第4种信息 是从server 发过来的 pc询问手机的位置
			// 那么这里只针对 收到第4种信息时 进行处理
			if (contenttype.equals("4")) {
				ZhMessageProto.ZhMessage.Builder builder = ZhMessageProto.ZhMessage.newBuilder();
				builder.setWhoami(Client.whoami);
				builder.setContenttype("4");
				builder.setSendtowhichpc(req.getSendtowhichpc());
				builder.setLocationstring(Client.whoami + "'s location");
				ctx.writeAndFlush(builder.build());
				System.out.println("接收到pc询问我的位置信息的消息,pc询问者是："+req.getSendtowhichpc());
			}
		} else if (Client.whoami.startsWith("pc")) {
			//如果是pc,会收到很多消息
			if(contenttype.equals("1")) {
				try {
					Files.write(Paths.get(clientPathString).resolve("String" + time), req.getContentstring().getBytes(),
							StandardOpenOption.CREATE_NEW);
					System.out.println("接收到"+req.getWhoami()+"发过来的文本消息:"+req.getContentstring());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if(contenttype.equals("2")) {
				String filename = req.getContentfilename();
				ByteString byteString = req.getContentfile();
				byte[] filebytes = byteString.toByteArray();
				try {
					Files.write(Paths.get(clientPathString).resolve(time+"-"+filename), filebytes, StandardOpenOption.CREATE_NEW);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("接收到"+req.getWhoami()+"发过来的图片信息");
			}else if(contenttype.equals("3")) {
				String filename = req.getContentfilename();
				ByteString byteString = req.getContentfile();
				byte[] filebytes = byteString.toByteArray();
				try {
					Files.write(Paths.get(clientPathString).resolve(time+"-"+filename), filebytes, StandardOpenOption.CREATE_NEW);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("接收到"+req.getWhoami()+"发过来的语音信息");
			}else if(contenttype.equals("4")) {
				try {
					Files.write(Paths.get(clientPathString).resolve("contenttype4locationString" + time), (req.getWhichphonelocation()+":"+req.getLocationstring()).getBytes(),
							StandardOpenOption.CREATE_NEW);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//System.out.println("我刚才向server询问了哪个phone的位置，现在那个phone报告位置到server上了，然后server告诉了我那个phone的位置。");
				System.out.println("手机:"+req.getWhichphonelocation()+"的位置是:"+req.getLocationstring());
			}else if(contenttype.equals("5")) {
				try {
					Files.write(Paths.get(clientPathString).resolve("reportLocation" + time), (req.getWhoami()+":"+ req.getLocationstring()).getBytes(),
							StandardOpenOption.CREATE_NEW);
					System.out.println("接收到phone:"+req.getWhoami()+"发过来的位置信息:"+req.getLocationstring());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
