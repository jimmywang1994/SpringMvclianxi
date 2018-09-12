package SocketLianxi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket=new ServerSocket(8080);
            //等待请求
            Socket socket=serverSocket.accept();
            //接收到请求后使用socket进行通信，创建BufferedReader用于读取数据
            BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line=reader.readLine();
            System.out.println("received from client:"+line);
            PrintWriter pw=new PrintWriter(socket.getOutputStream());
            pw.println("received data:"+line);
            pw.flush();
            pw.close();
            reader.close();
            socket.close();
            serverSocket.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
