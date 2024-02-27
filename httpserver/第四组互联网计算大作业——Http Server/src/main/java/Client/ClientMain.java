package Client;

import Util.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class ClientMain {
    protected static boolean persistent_client=true;
    protected static Socket connection;
    protected static InputStream in;
    protected static String if_modified_since;
    protected static OutputStream out;
    protected static boolean connected;     //是否建立连接
    public static String ip="127.0.0.1";
    public static int port=8080;
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Persistent Client?(Y/N)");
        Scanner sca = new Scanner(System.in);
        String ans = sca.next();
        if(ans.equals("Y")){
            persistent_client=true;
        }else{
            persistent_client=false;
        }
        System.out.println("填写If-Modified-Since:2024-06-23 12:34:45?");
        Scanner sc = new Scanner(System.in);
        if(sc.next().equals("Y")){
            if_modified_since = "2024-06-23 12:34:45";}else {
            if_modified_since="";
        }        //创建http的get请求
        System.out.println("Quick Connecting?(Y/N)?");
        Scanner scanner = new Scanner(System.in);
        if(scanner.next().equals("Y")){
            Clientmain();
            return;
        }else{
            System.out.println("Enter the host in the format <ip>:<port>");
            String[] addrs = scanner.next().split(":");
            ip =addrs[0];
            port=Integer.parseInt(addrs[1]);
            Clientmain();
        }
        scanner.close();
    }
    public static void Clientmain() throws IOException, InterruptedException {
        boolean running = true;
        while(running){
            //TODO:设计交互命令，分为四种模式GET POST REGISTER LOGIN，如果get的mime类型为png，则设定保存在/resources_client中
            Scanner scanner = new Scanner(System.in);
            // 这里用循环把输入其他数字的行为给屏蔽掉
            //发出对应的客户端请求
                System.out.println("请输入你的指令，GET请求请输入1，POST请求请输入2");
                String ans = scanner.next();
                if(Objects.equals(ans, "1")){
                    System.out.println("请输入你想访问的uri");
                    String uri=scanner.next();
                    doGet(uri);
                }else if(ans.equals("2")){
                    System.out.println("请输入你想访问的uri");
                    String uri=scanner.next();
                    System.out.println("请输入对应的文件路径");
                    String filePath=scanner.next();
                    doPost(uri,false,false,filePath);
                }
                                 //（2）什么时候running会false？还是说和服务端一样，客户端进程也可以始终运行，直到主机或类似终端将其主动关闭
        }
    }



    public static void connect(String ip,int port) throws IOException {
        connection = new Socket(ip,port);
        in = connection.getInputStream();
        out = connection.getOutputStream();
        connected=true;
    }

    public static void disconnect() throws IOException {
        connection.close();
        connected=false;
    }


    //TODO:发送GET请求，并处理（要求在命令行打印请求）(PNG文件要缓存在/resources_client上)
    public static void doGet(String uri) throws IOException, InterruptedException {
        if (!connected) {
            connect(ip, port);
        }
        StringBuffer sb = new StringBuffer("GET " + uri + " HTTP/1.1\r\n");
        //构建get请求头
        sb.append("Host: " + ip + ":8080\r\n");
        sb.append("User-Agent: Client\r\n");
        sb.append("Accept: */*\r\n");
        sb.append("Accept-Language: zh-cn\r\n");
        sb.append("Accept-Encoding: gzip, deflate\r\n");
        if(!if_modified_since.equals("")){
            sb.append("If-Modified-Since: "+if_modified_since+"\r\n");
        }
        if(persistent_client){
            sb.append("Connection: keep-alive"+"\r\n");
        }else{
            sb.append("Connection: close"+"\r\n");
        }
        System.out.println("指令正在发送中...");
        System.out.println(String.valueOf(sb));


        //输出
        out.write(String.valueOf(sb).getBytes(StandardCharsets.UTF_8));     //直接调用了getBytes,未用CToS
        out.flush();


        //读取请求
        byte[] buffer = new byte[2048];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int count =0;
        while ((count=in.read(buffer))>0){
            byteArrayOutputStream.write(buffer,0,count);
            if(count<2048){
                break;
            }
        }
        byte[] allInData = byteArrayOutputStream.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(allInData);
        BufferedReader reader = new BufferedReader(new InputStreamReader(byteArrayInputStream));
        String redirectlocationString="";
        System.out.println("指令正在接收中...");
        String line;
        int content_length;
        byte[] data=new byte[0];
        boolean redirection=false;
        boolean _304=false;
        boolean flag_disconnect=false;
        while ((line = reader.readLine()) != null) {
            // 打印响应
            System.out.println(line);
            //重定向处理
            if (line.contains("Location:")) {
                redirection=true;
                int locationIndex = line.indexOf("Location:");
                // 获取 "Location:" 后的字符串
                redirectlocationString = line.substring(locationIndex + "Location:".length()).trim();
            }
            if(line.contains("Content-Length:")){
                content_length=Integer.parseInt(line.split(":")[1].trim());
                data=Arrays.copyOfRange(allInData,allInData.length-content_length,allInData.length);
            }
            if(line.contains("HTTP/1.1 304 Not Modified")){
                _304=true;
            }
            if(line.matches("Connection: (keep-alive|close)")){
                if(line.contains("close")){
                    flag_disconnect=true;
                }
            }
            if(line.equals("")){
                break;
            }
        }
        if(!_304&&!redirection){
        File cache = new File("./resources_client/"+uri);
        if(cache.exists()){
            cache.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(cache);
        fos.write(data);
        fos.flush();
        fos.close();
        System.out.println("file already saved to:"+cache.getCanonicalPath());} else if (_304) {
            System.out.println("Not Modified! you can view it on "+System.getProperty("user.dir")+"/resources_client"+uri);
        } else if (redirection) {
            System.out.println("Redirecting to:"+redirectlocationString);
        }
        //如果已经认定切断连接或者没有开启“长连接模式”，则默认断开
        if(!persistent_client||flag_disconnect){
            disconnect();}
        // 信息全部接收后，正式处理重定向
        if(!redirectlocationString.isEmpty()) {
            OnRedirectHappen(redirectlocationString);
        }

    }

    //TODO:发送POST请求，并处理（要求在命令行打印请求）
    public static void doPost(String uri, boolean register, boolean login, String filePath) throws IOException, InterruptedException {
        if (!connected) {
            connect(ip, port);
        }


        // 创建http的post请求
        StringBuffer sb = new StringBuffer("POST " + uri + " HTTP/1.1\r\n");
        // 构建post请求头
        sb.append("Host: " + ip + ":8080\r\n");
        sb.append("User-Agent: Client\r\n");
        sb.append("Accept: */*\r\n");
        sb.append("Accept-Language: zh-cn\r\n");
        sb.append("Accept-Encoding: gzip, deflate\r\n");
        sb.append("Content-Type: multipart/form-data; boundary=boundary_string\r\n"); // 使用multipart/form-data格式，使用boundary_string作为JSON,文件内容这两个不同内容的分隔
        if(persistent_client){
            sb.append("Connection: keep-alive\r\n");
        }else{
            sb.append("Connection: close\r\n");
        }


        // 添加文件内容
            // 读取文件内容
            String fileContent = readFileContent(filePath);
            sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + filePath + "\"\r\n");
            sb.append("Content-Type: application/octet-stream\r\n");     //使用了这个type，直接表示二进制文件流
        // 设置整体Content-Length
            sb.append("Content-Length: " + fileContent.length() + "\r\n\r\n");
            sb.append(fileContent); // 文件内容
        System.out.println("指令正在发送中...");
        System.out.println(String.valueOf(sb));
        //输出
        out.write(String.valueOf(sb).getBytes(StandardCharsets.UTF_8));
        out.flush();
        //读取请求
        byte[] buffer = new byte[2048];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int count =0;
        while ((count=in.read(buffer))>0){
            byteArrayOutputStream.write(buffer,0,count);
            if(count<2048){
                break;
            }
        }
        byte[] allInData = byteArrayOutputStream.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(allInData);
        BufferedReader reader = new BufferedReader(new InputStreamReader(byteArrayInputStream));
        String redirectlocationString="";
        System.out.println("指令正在接收中...");
        String line;
        int content_length;
        byte[] data=new byte[0];
        boolean redirection=false;
        boolean _304=false;
        boolean flag_disconnect=false;
        while ((line = reader.readLine()) != null) {
            // 打印响应
            System.out.println(line);
            //重定向处理
            if (line.contains("Location:")) {
                redirection=true;
                int locationIndex = line.indexOf("Location:");
                // 获取 "Location:" 后的字符串
                redirectlocationString = line.substring(locationIndex + "Location:".length()).trim();
            }
            if(line.contains("Content-Length:")){
                content_length=Integer.parseInt(line.split(":")[1].trim());
                data=Arrays.copyOfRange(allInData,allInData.length-content_length,allInData.length);
            }
            if(line.contains("HTTP/1.1 304 Not Modified")){
                _304=true;
            }
            if(line.matches("Connection: (keep-alive|close)")){
                if(line.contains("close")){
                    flag_disconnect=true;
                }
            }
            if(line.equals("")){
                break;
            }
        }
        if(!_304||!redirection){
            System.out.println(new String(data));
        }
        if(!persistent_client||flag_disconnect){
            disconnect();
        }

    }

    public static String readFileContent(String filePath) throws IOException {
        FileInputStream i = new FileInputStream(new File("./resources_client/"+filePath));
        String returns=new String(i.readAllBytes());
        i.close();
        return returns;
    }

    //TODO:当重定向发生的时候做一些事情
    public static void OnRedirectHappen(String string) throws IOException, InterruptedException {

        doGet(string);

    }

}
