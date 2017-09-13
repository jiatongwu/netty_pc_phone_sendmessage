package cn.xvkang.netty.thread;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import com.google.protobuf.ByteString;

import cn.xvkang.netty.Client;
import cn.xvkang.netty.protocolbuffer.ZhMessageProto;
import io.netty.channel.ChannelFuture;

public class ConsoleThread implements Runnable {
	private String whoami;
	private ChannelFuture future;

	public ConsoleThread(String whoami,ChannelFuture future) {
		this.whoami=whoami;
		this.future = future;
	}

	@Override
	public void run(){
		if (whoami.startsWith("phone")) {

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line = null;
			/*
			 * ZhMessageProto.ZhMessage用于 pc同server phone同server发送信息的格式 contenttype 1是发送文本信息
			 * 2是发送图片文件 3是发送语音文件 4是询问phone的位置 5是发送位置信息 6是报告我是谁 whoami 格式是 phone01 phone02
			 * pc01 pc02目前固定是这种格式
			 */
			try {
				while (!((line = br.readLine()).equals("exit"))) {
					if(Client.isshutdown) {
						break;
					}
					String[] tokens = line.split(" ");
					if (tokens.length == 1 && tokens[0].equals("")) {
						System.out.println("\t使用方法:");
						System.out.println("\t发送文本信息请输入：");
						System.out.println("\t1 文本内容");
						System.out.println("\t发送图片请输入：");
						System.out.println("\t2 /home/wu/1.jpg");
						System.out.println("\t发送语音信息请输入：");
						System.out.println("\t3 /home/wu/1.mp3");
						System.out.println("\t发送位置信息请输入：");
						System.out.println("\t5 北京");
					}
					String contenttype = tokens[0];
					ZhMessageProto.ZhMessage.Builder builder = ZhMessageProto.ZhMessage.newBuilder();
					switch (contenttype) {
					case "1":

						builder.setWhoami(Client.whoami);
						builder.setContenttype("1");
						builder.setContentstring(tokens[1]);
						future.channel().writeAndFlush(builder.build());
						break;
					case "2":
						builder.setWhoami(Client.whoami);
						builder.setContenttype("2");
						try (FileInputStream fis = new FileInputStream(
								tokens[1]);) {
							byte[] buffer = new byte[fis.available()];
							fis.read(buffer);
							builder.setContentfile(ByteString.copyFrom(buffer));
							builder.setContentfilename(Paths.get(tokens[1]).getFileName().toString());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						future.channel().writeAndFlush(builder.build());
						break;
					case "3":
						builder.setWhoami(Client.whoami);
						builder.setContenttype("3");
						try (FileInputStream fis = new FileInputStream(
								tokens[1]);) {
							byte[] buffer = new byte[fis.available()];
							fis.read(buffer);
							builder.setContentfile(ByteString.copyFrom(buffer));
							builder.setContentfilename(Paths.get(tokens[1]).getFileName().toString());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						future.channel().writeAndFlush(builder.build());
						break;
					case "5":
						builder.setWhoami(Client.whoami);
						builder.setContenttype("5");
						builder.setLocationstring(tokens[1]);
						future.channel().writeAndFlush(builder.build());
						break;
					default:
						break;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (whoami.startsWith("pc")) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line = null;
			/*
			 * ZhMessageProto.ZhMessage用于 pc同server phone同server发送信息的格式 contenttype 1是发送文本信息
			 * 2是发送图片文件 3是发送语音文件 4是询问phone的位置 5是发送位置信息 6是报告我是谁 whoami 格式是 phone01 phone02
			 * pc01 pc02目前固定是这种格式
			 */
			try {
				while (!((line = br.readLine()).equals("exit"))) {
					
					if(Client.isshutdown) {
						break;
					}
					String[] tokens = line.split(" ");
					if (tokens.length == 1 && tokens[0].equals("")) {
						System.out.println("\t使用方法:");
						System.out.println("\t查询哪个手机的位置请输入：");
						System.out.println("\t4 phone01");
					}
					String contenttype = tokens[0];
					switch (contenttype) {

					case "4":
						ZhMessageProto.ZhMessage.Builder builder = ZhMessageProto.ZhMessage.newBuilder();
						builder.setWhoami(Client.whoami);
						builder.setContenttype("4");
						builder.setWhichphonelocation(tokens[1]);
						future.channel().writeAndFlush(builder.build());
						break;
					default:
						break;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getWhoami() {
		return whoami;
	}

	public void setWhoami(String whoami) {
		this.whoami = whoami;
	}

	public ChannelFuture getFuture() {
		return future;
	}

	public void setFuture(ChannelFuture future) {
		this.future = future;
	}
	

}
