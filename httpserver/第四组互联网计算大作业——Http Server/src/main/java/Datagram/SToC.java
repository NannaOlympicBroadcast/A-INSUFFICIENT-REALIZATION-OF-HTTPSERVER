package Datagram;

import java.io.*;
import java.util.Arrays;

//服务器向客户端发送报文请求
public class SToC extends MessageHeader{
    public boolean keep_alive;
    public int statuscode;
    public String phrase; // 服务端 状态代码后面的短语
    //TODO:把服务器做出的应答封装成二进制报文的形式
    @Override
    public byte[] SealAsBytes() throws IOException {
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        StringBuilder resStringBuilder = new StringBuilder();
        resStringBuilder.append(version);
        resStringBuilder.append(' ');
        resStringBuilder.append(statuscode);
        resStringBuilder.append(' ');
        resStringBuilder.append(phrase);
        resStringBuilder.append("\r\n");

        for (String key: headerFields.keySet()) {
            resStringBuilder.append(key);
            resStringBuilder.append(": ");
            resStringBuilder.append(headerFields.get(key));
            resStringBuilder.append("\r\n");
        }
        resStringBuilder.append("Date:");
        resStringBuilder.append(Date);
        resStringBuilder.append("\r\n");
        resStringBuilder.append("Connection:");
        resStringBuilder.append(keep_alive?"keep-alive":"close");
        resStringBuilder.append("\r\n");
        resStringBuilder.append("Content-Length:");
        resStringBuilder.append(contentLength);
        resStringBuilder.append("\r\n");
        resStringBuilder.append("\r\n");
        bos.write(resStringBuilder.toString().getBytes());
        if(contentLength>0){
        bos.write(dataFields);}
        allInBytes = bos.toByteArray();
        return allInBytes;
    }

    //TODO:访问reader得到的二进制流，并转化为一个报文对象
    @Override
    public MessageHeader parseAsHeader() {
        try {
            //构造输入流
            reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(allInBytes)));

            //读取首行，即状态行，得到协议版本，状态码，描述短语等
            String statusLine = reader.readLine();
            String[] elements = statusLine.split("\\s+");
            version = elements[0];
            statuscode = Integer.parseInt(elements[1]);
            int len =elements.length;
            String[] phrases = Arrays.copyOfRange(elements,2,len);
            phrase = String.join(" ",phrases);

            //逐行读取首部，写入headerFields
            String header = reader.readLine();
            while(!header.equals("")){
                String[] temp = header.split(":");
                String key = temp[0].trim();
                String value = temp[1].trim();
                if(key.equals("Content-Length")){
                    contentLength = Long.parseLong(value);
                }else if(key.equals("Connection")){
                    if(value.equals("keep-alive")){
                        keep_alive = true;
                    }else{
                        keep_alive = false;
                    }
                }else if(key.equals("Date")){
                    Date = value;
                }else{
                    writeItem(key,value);
                }
                header = reader.readLine();
            }

            //获取数据部分，构造dataFields
            int lenOfAll = allInBytes.length;
            dataFields = Arrays.copyOfRange(allInBytes,lenOfAll - (int)contentLength,lenOfAll);
        }catch(Exception e){
            e.printStackTrace();
        }
        return this;
    }
}
