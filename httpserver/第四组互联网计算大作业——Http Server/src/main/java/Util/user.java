package Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class user {
    static List<user> users;
    public static List<user> getUsers(){
        if(users==null){
            return (users=new ArrayList<user>());
        }else{
            return users;
        }
    }
    public String uname;
    public String password;
    public user(String name,String pwd){
        uname=name;
        password=pwd;
    }
    public static List<user> GetUsersByCsv(){
        List<user> returns = new ArrayList<>();
        datareader reader = new datareader("users.csv");
        for (String[] j:reader.form){
            returns.add(new user(j[0],j[1]));
        }
        return users=returns;
    }
    public static void dump_users(){
        List<String[]> csv_data=new ArrayList<>();
        List<user> uL = getUsers();
        for (user u:uL){
           csv_data.add(new String[]{u.uname,u.password});
        }
        datadumper dd = new datadumper("users.csv",csv_data);
    }

    public static RegisterResult register(String name,String password){
        if(name.contains(",")||name.isEmpty()||name.isBlank()){
            return RegisterResult.IllegalUsername;
        }else{
            for (user u:users){
                if(u.uname.equals(name)){
                    return RegisterResult.UnameAlreadyExists;
                }
            }
            if(password.contains(",")||password.isEmpty()||password.isBlank()){
                return RegisterResult.IllegalPassword;
            }else{
                users.add(new user(name,password));
                dump_users();
                return RegisterResult.Successful;//写直达策略
            }
        }
    }

    public static LoginResult login(String uname,String password){
        for (user u:users){
            if(u.uname.equals(uname)){
                return u.password.equals(password)? LoginResult.LoginSuccessful:LoginResult.WrongPassword;
            }
        }
        return LoginResult.InvalidUname;
    }




}

