package SimpleHttpProtocol;

import SocketLianxi.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class SimpleHttp {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel channel=ServerSocketChannel.open();
        channel.socket().bind(new InetSocketAddress(8080));//监听8080端口
        //取消阻塞模式
        channel.configureBlocking(false);
        //为ssc注册选择器
        Selector selector=Selector.open();
        channel.register(selector, SelectionKey.OP_ACCEPT);
        //等待请求，期间阻塞
        while (true){
            //每次等待3秒
            if(selector.select(3000)==0){
                continue;
            }
            //获取待处理的selectionKey
            Iterator<SelectionKey> keyIterator=selector.selectedKeys().iterator();
            while (keyIterator.hasNext()){
                SelectionKey key=keyIterator.next();
                new Thread(new HttpHandler(key)).run();
                keyIterator.remove();
            }
        }
    }
}
class HttpHandler implements Runnable{
    //缓冲大小
    private int bufferSize=1024;
    private String localCharSet="UTF-8";
    private SelectionKey selectionKey;

    public HttpHandler(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    public void handleAccept() throws IOException {
        SocketChannel clientChannel=((ServerSocketChannel)selectionKey.channel()).accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selectionKey.selector(),SelectionKey.OP_READ, ByteBuffer.allocate(bufferSize));
    }

    public void handledRead() throws IOException {
        //获取channel
        SocketChannel channel=(SocketChannel)selectionKey.channel();
        //获取buffer并重置
        ByteBuffer buffer=(ByteBuffer)selectionKey.attachment();
        buffer.clear();
        //没有读到内容则关闭
        if(channel.read(buffer)==-1){
            channel.close();
        }else{
            //接收请求数据
            buffer.flip();
            String receivedString= Charset.forName(localCharSet).newDecoder().decode(buffer).toString();
            //控制台打印请求报文头
            String[] requestMessage=receivedString.split("\r\n");
            for(String s:requestMessage){
                System.out.println(s);
                if(s.isEmpty()){
                    break;
                }
            }

            //控制台打印首行信息
            String[] firstLine=requestMessage[0].split(" ");
            System.out.println();
            System.out.println("Method:\t "+firstLine[0]);
            System.out.println("url:\t"+firstLine[1]);
            System.out.println("Http version:\t"+firstLine[2]);
            System.out.println();

            //返回到客户端
            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append("HTTP/1.1 200 OK\r\n");//响应报文首行，200表示成功
            stringBuilder.append("Content-Type:text/html;charset="+localCharSet+"\r\n");
            stringBuilder.append("<html><head><title>显示报文</title></head><body>");
            stringBuilder.append("接收到的报文是：</br>");
            for(String s:requestMessage){
                stringBuilder.append(s+"</br>");
            }
            stringBuilder.append("</body></html>");
            buffer=ByteBuffer.wrap(stringBuilder.toString().getBytes(localCharSet));
            channel.write(buffer);
            channel.close();
        }
    }

    @Override
    public void run() {
        try {
            if(selectionKey.isAcceptable()){
                handleAccept();
            }
            if(selectionKey.isReadable()){
                handledRead();
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}