package SocketLianxi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String msg="SocketLianxi.Client Data";
        try{
            //创建一个socket，跟本机8080端口进行通信
            Socket socket=new Socket("127.0.0.1",8080);
            PrintWriter pw=new PrintWriter(socket.getOutputStream());
            BufferedReader is=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw.println(msg);
            pw.flush();
            String line=is.readLine();
            System.out.println("received from server:"+line);
            pw.close();
            is.close();
            socket.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
