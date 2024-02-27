package Util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Resource {
    @Override
    public String toString() {
        return path;
    }

    public static Resource _405 = new Resource("405.html",Resource.MIME_html,2,"","");
    public static Resource _404 = new Resource("404.html",Resource.MIME_html,2,"","");

    public static Resource _500 = new Resource("500.html",Resource.MIME_html,2,"","");
    public static List<Resource> resources;
    public static final String DEFAULT_HOST_PAGES="index.html";
    public static final String MIME_PNG="image/png";
    public static final String MIME_JSON="application/json";
    public static final String MIME_html="text/html";
    public static final int GET=2;
    public static final  int POST=1;
    public static final int GET_POST=3;
    public String path;
    public String mimetype;
    public int method;
    //这里使用两位二进制表示，高位表示是否支持GET，低位表示是否支持POST
    public boolean GETSupported(){
        return method>=2;
    }
    public boolean POSTSupported(){
        return method%2==1;
    }

    public Resource(String Path,String mime,int method,String redirect1,String redirect2){
        path=Path;
        mimetype=mime;
        this.method=method;
        redirect=redirect1;
        temporary_redirect=redirect2;
    }

    public byte[] load_real() throws IOException {
        FileInputStream i = new FileInputStream(new File("resources/"+path));
        return i.readAllBytes();
    }

    public static List<Resource> getReousrcesFromCSV(){
        resources = new ArrayList<>();
        datareader dr = new datareader("reslist.csv");
        dr.form=dr.form.subList(0,14);
        for(String[] s:dr.form){
            resources.add(new Resource(s[0],s[1],Integer.parseInt(s[2]),s[3],s[4]));
        }
        return resources;
    }

    //这里要详细说明一下
    //如果redirect和temporary_redirect字段都为空的话，那么不允许重定向
    //如果redirect字段和temporary_redirect字段都不为空，则表示条件重定向（如果为true重定向至redirect）
    //如果redirect字段为空，但是temporary_redirect，则表示如果满足条件，就默认不发生重定向
    //如果temporary_redirect为空但是redirect不为空，那么就发生永久重定向
    public String redirect;
    public String temporary_redirect;

    /**
     *返回重定向的资源路径
     *@param condition 中间可以嵌套函数，比如注册登录结果是否为success，如果是302，则直接传入true
     *@return 返回重定向的资源路径
     */
    public String redirection(boolean condition){
        if(condition){
            return temporary_redirect;
        }else{
            if(redirect.equals("")){
                return path;
            }else{
                return redirect;
            }
        }
    }


}
