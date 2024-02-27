package Util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;

public class json {
    /**
     * @return 将用户名与密码传入以后转化为json类
    * */
    public static String sealAsJson(String uname,String password){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uname",uname);
        jsonObject.put("password",password);
        return jsonObject.toJSONString();
    }
    /**
     * @param jstring 传入的格式化的json字符串
     * @return 返回一个以[用户名,密码]为组织的字符串数组
    * */
    public static String[] parseFromJson(String jstring){
        JSONObject job = JSON.parseObject(jstring);
        return new String[]{job.getString("uname"), job.getString("password")};
    }


}
