package com.easychat.test;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SocketClient {
    public static void main(String[] args) {
        Socket socket = null;
        try {
            socket = new Socket("localhost",10024);

            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            System.out.println("请输入内容...");

            Scanner scanner = new Scanner(System.in);
            String next = scanner.nextLine();

            printWriter.println(next);
            printWriter.flush();

            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String s = bufferedReader.readLine();
            System.out.println("收到客户端消息："+s);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
