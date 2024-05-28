package com.easychat.test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(10024);
            System.out.println("服务已启动");
            Socket socket = server.accept();
            String ip = socket.getInetAddress().getHostAddress();

            System.out.println("有客户端连接ip:" +ip+"   端口:"+socket.getPort());

            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String s = bufferedReader.readLine();
            System.out.println("收到客户端消息："+s);


            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);

            printWriter.println("收到");
            printWriter.flush();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
