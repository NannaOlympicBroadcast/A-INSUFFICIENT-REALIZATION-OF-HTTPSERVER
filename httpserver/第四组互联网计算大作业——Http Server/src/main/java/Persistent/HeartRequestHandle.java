package Persistent;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class HeartRequestHandle {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        System.out.println("Start Testing On port:");
        HeartRequestHandle hhh = new HeartRequestHandle(Integer.parseInt(s.next()));
        hhh.start();
    }
    int port;//要持续监听的窗口
    boolean running;
    long receiveTimeDelay = 8000;//超过8s没有接收到心跳包则断开连接
    long lastReceiveTime;
    long warnAdvance = 3000;//断开连接前3s会发送警告信息
    boolean isWarned = false;//表明是否已发送过警告

    public HeartRequestHandle(int port){
        this.port = port;
    }

    public void start(){
        running = true;
        System.out.println("进程启动");
        new connWatchDog().start();
    }

    public void halt(){
        running = false;
        System.out.println("进程关闭");
    }

    class connWatchDog extends Thread{
        @Override
        public void start(){
            System.out.println(lastReceiveTime);
            try{
                ServerSocket ss = new ServerSocket(port);
                while(running){
                    Socket s = ss.accept();
                    lastReceiveTime=System.currentTimeMillis();
                    new socketAction(s).start();
                }
            }catch(Exception e){
                e.printStackTrace();
                halt();
            }
        }
    }
    class socketAction extends Thread{
        Socket s;
        InputStream in;
        OutputStream out;
        boolean run = true;
        public socketAction(Socket s){
            this.s= s;
            try {
                in = s.getInputStream();
                out = s.getOutputStream();
            }catch(Exception e){
                e.printStackTrace();
                halt(e.toString());
            }
        }

        @Override
        public void start(){
            while(running && run){
                if(System.currentTimeMillis() - lastReceiveTime > receiveTimeDelay){
                    //超时仍未收到心跳报文，则关闭连接
                    halt("超时未接受到心跳报文");
                }else{//持续监听对应端口，看是否有报文传来

                    //若超过5s未收到,则警告
                    if(System.currentTimeMillis() - lastReceiveTime > receiveTimeDelay - warnAdvance){
                        if(isWarned){
                            System.out.println("若3s后仍无心跳包，则断开与" + s.getRemoteSocketAddress() +"的连接！");
                            isWarned = true;
                        }
                    }

                    //接收心跳包并回复
                    try {
                        if (in.available() > 0) {
                            String request = new String(in.readNBytes(in.available()));
                            if(request.contains("心跳包")){
                                System.out.println("接收来自" + s.getRemoteSocketAddress() + "的心跳包:"+request);
                                lastReceiveTime = System.currentTimeMillis();
                                out.write("维持连接".getBytes());
                                out.flush();
                            }
                        }else{
                            Thread.sleep(10);//若当前没有心跳包传来，则10ms后再检查输入流
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                        halt(e.toString());
                    }
                }
            }
        }

        public void halt(String cause){
            try {
                out.write("连接断开".getBytes());//告知客户端断开连接
                out.flush();
            }catch(Exception e){
                e.printStackTrace();
            }

            run = false;
            try {
                s.close();
            }catch(Exception e){
                e.printStackTrace();
            }
            System.out.println("关闭与" + s.getRemoteSocketAddress() + "的连接"+"原因是"+cause);
        }

    }
    }

