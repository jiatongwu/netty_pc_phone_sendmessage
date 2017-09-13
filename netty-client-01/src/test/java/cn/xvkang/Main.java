package cn.xvkang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		/*
		 * ZhMessageProto.ZhMessage用于 pc同server phone同server发送信息的格式 contenttype 1是发送文本信息
		 * 2是发送图片文件 3是发送语音文件 4是询问phone的位置 5是发送位置信息 6是报告我是谁 whoami 格式是 phone01 phone02
		 * pc01 pc02目前固定是这种格式
		 */
		while (!((line = br.readLine()).equals("exit"))) {
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
			switch (contenttype) {
			case "1":
				
				break;
			case "2":

				break;
			case "3":

				break;
			case "5":

				break;
			default:
				break;
			}
		}
	}
}
