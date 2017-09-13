package cn.xvkang.netty.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.protobuf.ByteString;

import cn.xvkang.netty.protocolbuffer.ZhMessageProto;
import cn.xvkang.netty.utils.ChannelGroups;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

public class ZhServerHandler extends ChannelInboundHandlerAdapter {

	/**
	 * ZhMessageProto.ZhMessage用于 pc同server phone同server发送信息的格式 contenttype 1是发送文本信息
	 * 2是发送图片文件 3是发送语音文件 4是询问phone的位置 5是发送位置信息 6是报告我是谁(在client ChannelActive时发送的) 
	 * whoami 格式是 phone01 phone02
	 * pc01 pc02目前固定是这种格式
	 * 
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = sdf.format(new Date());
		ZhMessageProto.ZhMessage req = (ZhMessageProto.ZhMessage) msg;
		Channel channel = ctx.channel();
		AttributeKey<String> client_path_key = AttributeKey.valueOf("clientPath");
		String clientPathString = channel.attr(client_path_key).get();
		AttributeKey<String> user_id_key = AttributeKey.valueOf("userid");
		String userid = channel.attr(user_id_key).get();
		String contenttype = req.getContenttype();
		if (contenttype.equals("1")) {
			// 先在本地存一份，然后发给所有pc端
			if (userid.startsWith("phone")) {
				try {
					Files.write(Paths.get(clientPathString).resolve("String" + time), req.getContentstring().getBytes(),
							StandardOpenOption.CREATE_NEW);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("服务器接收到"+req.getWhoami()+"文本消息:"+req.getContentstring());
				ChannelGroups.CHANNEL_GROUP.forEach(c -> {
					String c_userid=c.attr(user_id_key).get();
					if(c_userid.startsWith("pc")) {
						c.writeAndFlush(msg);
					}
				});
			} else if (userid.startsWith("pc")) {

			}
		} else if (contenttype.equals("2")) {
			//发的是图片文件
			// 先在本地存一份，然后发给所有pc端
			if (userid.startsWith("phone")) {
				String filename = req.getContentfilename();
				ByteString byteString = req.getContentfile();
				byte[] filebytes = byteString.toByteArray();
				try {
					Files.write(Paths.get(clientPathString).resolve(time+"-"+filename), filebytes, StandardOpenOption.CREATE_NEW);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("服务器接收到"+req.getWhoami()+"图片:"+req.getContentfilename());
				//FileUtils.writeByteArrayToFile(new File("/home/wu/test/server/" + filename), filebytes);
				ChannelGroups.CHANNEL_GROUP.forEach(c -> {
					String c_userid=c.attr(user_id_key).get();
					if(c_userid.startsWith("pc")) {
						c.writeAndFlush(msg);
					}
				});
			} else if (userid.startsWith("pc")) {

			}
			
		} else if (contenttype.equals("3")) {
			//发的是语音文件
			if (userid.startsWith("phone")) {
				String filename = req.getContentfilename();
				ByteString byteString = req.getContentfile();
				byte[] filebytes = byteString.toByteArray();
				try {
					Files.write(Paths.get(clientPathString).resolve(time+"-"+filename), filebytes, StandardOpenOption.CREATE_NEW);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("服务器接收到"+req.getWhoami()+"语音:"+req.getContentfilename());
				//FileUtils.writeByteArrayToFile(new File("/home/wu/test/server/" + filename), filebytes);
				ChannelGroups.CHANNEL_GROUP.forEach(c -> {
					String c_userid=c.attr(user_id_key).get();
					if(c_userid.startsWith("pc")) {
						c.writeAndFlush(msg);
					}
				});
			} else if (userid.startsWith("pc")) {
				
			}
			// 先在本地存一份，然后发给所有pc端
		} else if (contenttype.equals("4")) {
			//发的是pc询问phone的位置
			if (userid.startsWith("phone")) {
				System.out.println("服务器接收到"+req.getWhoami()+"反馈位置信息："+req.getLocationstring()+"给"+ req.getSendtowhichpc());
				String locationstring=req.getLocationstring();
				String sendtowhichpc=req.getSendtowhichpc();
				ChannelGroups.CHANNEL_GROUP.forEach(c -> {
					String c_userid=c.attr(user_id_key).get();
					if(c_userid.equals(sendtowhichpc)) {
						ZhMessageProto.ZhMessage.Builder builder = ZhMessageProto.ZhMessage.newBuilder();
						builder.setWhoami("server");
						builder.setContenttype("4");
						builder.setLocationstring(locationstring);
						builder.setWhichphonelocation(userid);
						c.writeAndFlush(builder.build());
					}
				});
			} else if (userid.startsWith("pc")) {
				System.out.println("服务器接收到"+req.getWhoami()+"询问phone:"+req.getWhichphonelocation()+"位置信息");
				String whichphonelocation=req.getWhichphonelocation();
				ChannelGroups.CHANNEL_GROUP.forEach(c -> {
					String c_userid=c.attr(user_id_key).get();
					if(c_userid.equals(whichphonelocation)) {
						ZhMessageProto.ZhMessage.Builder builder = ZhMessageProto.ZhMessage.newBuilder();
						builder.setWhoami("server");
						builder.setContenttype("4");
						builder.setSendtowhichpc(userid);
						c.writeAndFlush(builder.build());
					}
				});
			}

		} else if (contenttype.equals("5")) {
			System.out.println("服务器接收到"+req.getWhoami()+"报告位置信息"+req.getLocationstring());
			//发的是phone报告位置
			if (userid.startsWith("phone")) {
				try {
					Files.write(Paths.get(clientPathString).resolve("reportLocation" + time), (userid+":"+ req.getLocationstring()).getBytes(),
							StandardOpenOption.CREATE_NEW);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ChannelGroups.CHANNEL_GROUP.forEach(c -> {
					String c_userid=c.attr(user_id_key).get();
					if(c_userid.startsWith("pc")) {
						c.writeAndFlush(msg);
					}
				});
			} else if (userid.startsWith("pc")) {

			}

		} else if (contenttype.equals("6")) {
			// 当客户端channelActive时会发送contenttype为6的数据包。 报告我是谁
			channel.attr(user_id_key).set(req.getWhoami());
			Path clientPath = Paths.get(System.getProperty("user.home"), "test", "server", req.getWhoami());
			channel.attr(client_path_key).set(clientPath.toString());
			try {
				Files.createDirectories(clientPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ChannelGroups.add(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ChannelGroups.discard(ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ChannelGroups.discard(ctx.channel());
		ctx.close();
	}

}
