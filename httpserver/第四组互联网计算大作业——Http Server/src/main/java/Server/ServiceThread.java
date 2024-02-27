package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;

public class ServiceThread extends Thread{
public ServerSocket server;
boolean connected;
public Socket client;
public ServiceThread(ServerSocket server,Socket client){
    System.out.println("客户端"+client.getRemoteSocketAddress()+"connected");
    this.server=server;
    this.client=client;
}
public void disconnect(){
    connected=false;
}



    @Override
    public void start() {
        connected = true;
            RequestHandler handler = new RequestHandler(this);
            try {
                handler.HandleRequest();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
                try {

                    client.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
    }
}
