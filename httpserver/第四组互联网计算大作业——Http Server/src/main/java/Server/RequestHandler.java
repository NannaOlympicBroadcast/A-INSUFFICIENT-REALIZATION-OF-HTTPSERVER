package Server;

import Datagram.CToS;
import Datagram.SToC;
import Util.*;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

public class RequestHandler {
    boolean halted = false;
    ServiceThread thread;
    public RequestHandler(ServiceThread t){
        thread=t;

    }
    //TODO: How To Handle The Request From What The Client Has Sent
    //注意：IO使用的类型，能够使得线程访问资源不冲突
    public void HandleRequest() throws IOException, ParseException {

        System.out.println("你好");
        InputStream socket_input_stream = thread.client.getInputStream();
        while (thread.connected){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len;
        while ((len = socket_input_stream.read(buffer)) > 0) {
            bos.write(buffer, 0, len);
            if (len < 2048) break;
        }
        CToS c= new CToS();
        c.allInBytes = bos.toByteArray();
        System.out.println(new String(c.allInBytes));
        c.parseAsHeader();
        thread.connected=c.keep_alive;
        System.out.println(c.keep_alive);
        grep_resources(c);
}
    }


    /**
     @param ctos:传入从客户端得到的从二进制解析而来的报文对象
     */
    void grep_resources(CToS ctos) throws IOException, ParseException {
        this.thread.connected=ctos.keep_alive;
        if(halted){
       Handle500Exception();
        }
        for (Resource r:Resource.resources){
            if(r.path.equals(ctos.uri)){
                if(ctos.operation.equals("GET")){
                    if(r.GETSupported()){
                        //加载资源
                        LoadResourceAndHandleRedirection(r,ctos);
                    }else{
                        Handle405Exception("POST");
                    }
                } else if (ctos.operation.equals("POST")) {
                    if(r.POSTSupported()){
                        //加载资源
                        LoadResourceAndHandleRedirection(r,ctos);
                    }else{
                        Handle405Exception("GET");
                    }
                }
                return;
            }
        }
        //404
        Handle404Exception();
    }

    void Handle404Exception() throws IOException, ParseException {
        SToC sendToClient = new SToC();
        sendToClient.keep_alive=false;
        sendToClient.phrase="Not Found";
        sendToClient.dataFields = Resource._404.load_real();
        sendToClient.statuscode=404;
        sendToClient.headerFields.put("Content-Type","application/html");
        sendToClient.CountContentLength();
        sendToClient.GetDate();
        SendReturnData(sendToClient.SealAsBytes());
    }

    void Handle500Exception() throws IOException,ParseException{
        SToC sendToClient = new SToC();
        sendToClient.keep_alive=false;
        sendToClient.phrase="Internal Server Error";
        sendToClient.dataFields = Resource._500.load_real();
        sendToClient.statuscode=500;
        sendToClient.headerFields.put("Content-Type","application/html");
        sendToClient.CountContentLength();
        sendToClient.GetDate();
        SendReturnData(sendToClient.SealAsBytes());
    }

    void LoadResourceAndHandleRedirection(Resource r,CToS ctos) throws ParseException, IOException {

        if(r.temporary_redirect.isEmpty()){
            if(r.redirect.isEmpty()){
                if(ctos.headerFields.containsKey("If-Modified-Since")){
                SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date if_modified_since = spf.parse(ctos.headerFields.get("If-Modified-Since"));
                SToC sendToClient = new SToC();
                sendToClient.keep_alive=ctos.keep_alive;
                if(new Date().before(if_modified_since)){
                    sendToClient.phrase="Not Modified";
                    sendToClient.statuscode=304;
                    sendToClient.CountContentLength();
                    sendToClient.GetDate();
                    SendReturnData(sendToClient.SealAsBytes());
                }else{
                    sendToClient.phrase="OK";
                    sendToClient.statuscode=200;
                    sendToClient.dataFields=r.load_real();
                    sendToClient.CountContentLength();
                    sendToClient.headerFields.put("Content-Type",r.mimetype);
                    sendToClient.GetDate();
                    SendReturnData(sendToClient.SealAsBytes());
                }}else {
                    SToC sendToClient = new SToC();
                    sendToClient.phrase="OK";
                    sendToClient.statuscode=200;
                    sendToClient.dataFields=r.load_real();
                    sendToClient.CountContentLength();
                    sendToClient.headerFields.put("Content-Type",r.mimetype);
                    sendToClient.GetDate();
                    SendReturnData(sendToClient.SealAsBytes());
                }
            }else{
                SToC sendToClient = new SToC();
                sendToClient.keep_alive=ctos.keep_alive;
                sendToClient.phrase="Moved Permanently";
                sendToClient.statuscode=301;
                sendToClient.contentLength=0;
                sendToClient.headerFields.put("Location",r.redirect);
                sendToClient.GetDate();;
                SendReturnData(sendToClient.SealAsBytes());
            }
        }else{
            if(r.redirect.isEmpty()){
                if(ctos.uri.equals("/testrandomredirect.html")){
                    String url = r.redirection(generateRandomCondition());
                    url="/redirectdest.html";
                    if(url.equals(r.path)){
                        SToC sendToClient = new SToC();
                        sendToClient.keep_alive=ctos.keep_alive;
                        sendToClient.phrase="OK";
                        sendToClient.statuscode=200;
                        sendToClient.dataFields=r.load_real();
                        sendToClient.CountContentLength();
                        sendToClient.headerFields.put("Content-Type",r.mimetype);
                        sendToClient.GetDate();;
                        SendReturnData(sendToClient.SealAsBytes());
                    }else{
                        SToC sendToClient = new SToC();
                        sendToClient.keep_alive=ctos.keep_alive;
                        sendToClient.phrase="Found";
                        sendToClient.statuscode=302;
                        sendToClient.contentLength=0;
                        sendToClient.headerFields.put("Location",url);
                        sendToClient.GetDate();;
                        SendReturnData(sendToClient.SealAsBytes());
                    }
                }
            }else{
                //TODO:Login & Register
                if(ctos.uri.equals("/login")){
                    if (ctos.operation.equals("GET")) {
                        if(ctos.headerFields.containsKey("If-Modified-Since")){
                            SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date if_modified_since = spf.parse(ctos.headerFields.get("If-Modified-Since"));
                            SToC sendToClient = new SToC();
                            sendToClient.keep_alive=ctos.keep_alive;
                            if(new Date().before(if_modified_since)){
                                sendToClient.phrase="Not Modified";
                                sendToClient.statuscode=304;
                                sendToClient.CountContentLength();
                                sendToClient.GetDate();
                                SendReturnData(sendToClient.SealAsBytes());
                                return;
                            }}
                        Resource _login = new Resource("/login.html","text/html",2,"","");
                        byte[] data=_login.load_real();
                        SToC sendToClient = new SToC();
                        sendToClient.keep_alive=ctos.keep_alive;
                        sendToClient.phrase="OK";
                        sendToClient.statuscode=200;
                        sendToClient.dataFields=data;
                        sendToClient.CountContentLength();
                        sendToClient.headerFields.put("Content-Type","text/html");
                        SendReturnData(sendToClient.SealAsBytes());
                        return;
                    }
                    String jstring = new String(ctos.dataFields);
                    String[] uname_n_pwd = json.parseFromJson(jstring);
                    LoginResult resu = user.login(uname_n_pwd[0],uname_n_pwd[1]);
                    SToC sendToClient = new SToC();
                    sendToClient.keep_alive=ctos.keep_alive;
                    sendToClient.phrase="OK";
                    sendToClient.statuscode=200;
                    sendToClient.dataFields=resu.toString().getBytes();
                    sendToClient.CountContentLength();
                    sendToClient.headerFields.put("Content-Type","text/plain");
                    SendReturnData(sendToClient.SealAsBytes());
                }else if (ctos.uri.equals("/register")) {
                    String jstring = new String(ctos.dataFields);
                    String[] uname_n_pwd = json.parseFromJson(jstring);
                    RegisterResult resu = user.register(uname_n_pwd[0],uname_n_pwd[1]);
                    SToC sendToClient = new SToC();
                    sendToClient.keep_alive=ctos.keep_alive;
                    sendToClient.phrase="OK";
                    sendToClient.statuscode=200;
                    sendToClient.dataFields=resu.toString().getBytes();
                    sendToClient.CountContentLength();
                    sendToClient.headerFields.put("Content-Type","text/plain");
                    SendReturnData(sendToClient.SealAsBytes());
                }
            }
        }
    }

    boolean generateRandomCondition(){
        Random r = new Random(114514);
       return r.nextInt()%2==0;
    }




    void Handle405Exception(String Allow) throws IOException, ParseException {
        SToC sendToClient = new SToC();
        sendToClient.keep_alive=false;
        sendToClient.phrase="Method Not Allowed";
        sendToClient.dataFields = Resource._405.load_real();
        sendToClient.statuscode=405;
        sendToClient.headerFields.put("Allow",Allow);
        sendToClient.headerFields.put("Content-Type","application/html");
        sendToClient.CountContentLength();
        sendToClient.GetDate();
        SendReturnData(sendToClient.SealAsBytes());
    }

    void SendReturnData(byte[] data) throws IOException {
        OutputStream o = thread.client.getOutputStream();
        o.write(data);
        o.flush();
    }
}
