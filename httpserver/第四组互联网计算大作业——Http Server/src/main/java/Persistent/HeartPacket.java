package Persistent;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class HeartPacket {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        System.out.println("Enter Persistent Test With Default Settings?(Y/N)");
        if(s.next().equals("Y")){
            HeartPacket hp = new HeartPacket("127.0.0.1",8080);
            hp.start();
        }else{
            System.out.println("Enter the address in the form of <ip>:<port>?");
            String[] ipandport = s.next().split(":");
            HeartPacket hp = new HeartPacket(ipandport[0],Integer.parseInt(ipandport[1]));
            hp.start();
        }
    }
    String ip="127.0.0.1";
    int port=8080;
    Socket s;
    InputStream in;
    OutputStream out;
    boolean connected = false;
    long lastSendTime;
    long checkDelay = 10;//每隔10ms检查一下输入流
    long KeepAliveDelay = 2000;//每隔2000ms发送一个心跳包
    int maxTrial = 10;//至多发送10个心跳包后，就要断开连接

    public HeartPacket(String ip,int port){
        this.ip = ip;
        this.port = port;
    }

    public void start(){
        try {
            //建立连接
            s = new Socket(ip, port);
            in = s.getInputStream();
            out = s.getOutputStream();
            connected = true;

            //启动线程
            lastSendTime = System.currentTimeMillis();
            new Thread(new HeartSendWatchDog()).start();
            new Thread(new ReceiveWatchDog()).start();

            System.out.println("<<<<---- 长连接已建立 ---->>>>");
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("长连接建立失败！");
        }

    }

    public void stop(String cause){
        connected = false;
        System.out.println("<<<<---- 连接断开 ---->>>>");
        System.out.println("原因是:"+cause);
    }

    class HeartSendWatchDog implements Runnable{
        int attemptTimes = 0;//记录已发送的心跳包个数
        public void run(){
            while(connected){
                if(System.currentTimeMillis() - lastSendTime > KeepAliveDelay) {
                    if(attemptTimes > maxTrial){
                        System.out.println("发送心跳包已达阈值");
                        stop("到达阈值");
                    }else{
                        //发送心跳包
                        String heart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\t心跳包";
                        try {
                            out.write(heart.getBytes());
                            out.flush();
                        }catch(Exception e){
                            e.printStackTrace();
                            stop(e.toString());
                        }
                        attemptTimes++;
                        lastSendTime = System.currentTimeMillis();

                        System.out.println("\r\n发送第" + attemptTimes + "个心跳包");
                    }
                }else{
                    try {
                        Thread.sleep(checkDelay);
                    }catch(Exception e){
                        e.printStackTrace();
                        stop(e.toString());
                    }
                }
            }
        }
    }
    class ReceiveWatchDog implements Runnable {
        public void run() {
            while (connected){
            try {
                InputStream socket_input_stream = in;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                int len;
                while ((len = socket_input_stream.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                    if (len < 2048) break;
                }
                String res = new String(bos.toByteArray());
                System.out.println(res);
                if(res.contains("维持连接")){
                    System.out.println("服务器已经处理");
                }else{
                    stop("服务端主动断开连接"+res);
                }
            }catch(Exception e){
                e.printStackTrace();
                stop(e.toString());
            }
        }}
    }

}
