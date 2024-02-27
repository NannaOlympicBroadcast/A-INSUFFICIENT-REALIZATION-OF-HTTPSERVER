package Server;

import Util.Resource;
import Util.user;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.prefs.Preferences;

public class ServerMain {
    static void Init(){
        user.GetUsersByCsv();
        Resource.getReousrcesFromCSV();
    }
    static String ip="127.0.0.1";
    static int port=8080;
    public static void main(String[] args) throws IOException {
        Init();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Do You Want to Start The Server in quick mode(Y/N)?");
        if(scanner.next().equals("Y")){
            scanner.close();
            Servermain();
            return;
        }
        System.out.println("Use this server in the environment of");
        System.out.println("1.localhost;\n2.LAN\n3.WAN");

        int mode = scanner.nextInt();
        if(mode==1){

        } else if (mode==2) {
            System.out.println("Enter your private IPv4 address,\nIt could be within 10.0.0.0-10.255.255.255\nor172.16.0.0-172.31.255.255\n or 192.168.0.0-192.168.255.255\nUse command ipconfig to get it" +
                    "\n Enter 'default' to get your previous setting:");
            ip = scanner.next();
            if(ip.equals("default")){
                ip = Preferences.userRoot().get("privateipaddr","127.0.0.1");
                if(ip.equals("127.0.0.1")){
                    System.err.println("no ip used previously");
                }
            }else{
                Preferences.userRoot().put("privateipaddr",ip);
            }
        }else if(mode==3){
            System.out.println("Enter your public IPv4 address:\nEnter default to get your previous setting:");
            ip=scanner.next();
            if(ip.equals("default")){
                ip = Preferences.userRoot().get("publicipaddr","127.0.0.1");
                if(ip.equals("127.0.0.1")){
                    System.err.println("no ip used previously");
                }
            }else{
                Preferences.userRoot().put("publicipaddr",ip);
            }
        }
        System.out.println("Enter the port(-1 for default):");
        port = scanner.nextInt();
        if(port==-1){
            port=Preferences.userRoot().getInt("port",8080);
        }else{
            Preferences.userRoot().putInt("port",port);
        }
        System.out.println("Confirm to start the server on "+ip+":"+port+"?(Y/N)");
        if(scanner.next().equals("Y")){
            scanner.close();
            Servermain();
        }else{
            return;
        }
    }


    static void Servermain() throws IOException {
        ServerSocket ss = new ServerSocket();
        ss.bind(new InetSocketAddress(ip,port));
        System.out.println("Welcome!");
        while(true){
            Socket client = ss.accept();
            ServiceThread st = new ServiceThread(ss,client);
            st.start();
        }
    }
}