package Datagram;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public abstract class MessageHeader {

    public String Date;
    public String version = "HTTP/1.1";

    public HashMap<String, String> headerFields = new HashMap<>();
    //可变的报文头比如Location If-Modified-Since
    public byte[] dataFields; // 报文数据段
    public long contentLength = 0;
    public BufferedReader reader;
    public byte[] allInBytes;
    //注意：这里是所有的内容（包括头和数据内容）的二进制形式
    public void writeItem(String key, String value){
        headerFields.put(key, value);
    }
    public void CountContentLength(){
        if(dataFields==null){
            contentLength=0;
        }else{
        contentLength = dataFields.length;
        }

    }


    public abstract byte[] SealAsBytes() throws IOException;
    public void GetDate() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        this.Date = sdf.format(now);
    }


    public abstract MessageHeader parseAsHeader();


}
