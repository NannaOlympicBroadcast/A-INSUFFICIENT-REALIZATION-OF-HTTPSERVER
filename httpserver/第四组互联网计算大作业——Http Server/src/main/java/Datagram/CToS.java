package Datagram;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

//客户端向服务器发送请求
public class CToS extends MessageHeader{
    public boolean keep_alive;
    public String uri;
    public String operation;
    //public String If_Not_Modified_Since;//设置是否304


    //TODO:把客户端请求封装成二进制报文的形式
    @Override
    public byte[] SealAsBytes() {

        StringBuilder resStringBuilder = new StringBuilder();
        resStringBuilder.append(operation);
        resStringBuilder.append(' ');
        resStringBuilder.append(uri);
        resStringBuilder.append(' ');
        resStringBuilder.append(version);
        resStringBuilder.append("\r\n");
        resStringBuilder.append("Connection:");
        resStringBuilder.append(keep_alive?"keep-alive":"close");
        resStringBuilder.append("\r\n");
        resStringBuilder.append("Date:"+Date);
        resStringBuilder.append("\r\n");
        resStringBuilder.append("Content-Length:"+contentLength);
        resStringBuilder.append("\r\n");
        for (String key: headerFields.keySet()
        ) {
            resStringBuilder.append(key);
            resStringBuilder.append(": ");
            resStringBuilder.append(headerFields.get(key));
            resStringBuilder.append("\r\n");
        }
        resStringBuilder.append("\r\n");
        if (operation.equals("POST")){
            resStringBuilder.append(new String(dataFields));
        }
        allInBytes = resStringBuilder.toString().getBytes();
        return allInBytes;
    }

    //TODO：将报文解析为报文对象
    @Override
    public MessageHeader parseAsHeader() {
        try {
            //由报文构造输入流
            reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(allInBytes)));

            //读取报文首行，即请求行，得到操作，uri及使用的协议版本等
            String requestLine = reader.readLine();
            String[] elements = requestLine.split("\\s+");
            operation = elements[0];
            uri = elements[1];
            version = elements[2];

            //逐行读取首部信息，构造headerFields
            String header = reader.readLine();
            while (!(header==null||header.equals(""))) {
                String[] temp = header.split(":");
                String key = temp[0].trim();
                String value = header.substring(header.indexOf(":")+2).trim();
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
                } else{
                    writeItem(key,value);
                }
                header = reader.readLine();
            }

            //获取数据部分，构造dataFields
            int lenOfAll = allInBytes.length;
            dataFields = Arrays.copyOfRange(allInBytes, lenOfAll - (int) contentLength, lenOfAll);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }


}
