package com.hxy.kanxue.util;

import java.util.HashMap;

/**
 * Created by xianyu.hxy on 2015/8/24.
 */
public class CookieStorage {
    private ObjStorage m_objStorage=null;
    private HashMap<String,String> m_cookie=null;
    public CookieStorage(ObjStorage objStorage){
        this.m_objStorage=objStorage;
        Object obj=m_objStorage.load("cookie");
        this.m_cookie=(obj==null)?new HashMap<String, String>() :(HashMap<String, String>)obj;
    }
    public void addCookie(String name,String value){
        if(name==null||value==null)return;
        if(m_cookie.containsKey(name)&&m_cookie.get(name)==value)return;
        m_cookie.put(name,value);
        m_objStorage.save("cookie",m_cookie);
    }
    public String getCookies(){
        if(this.m_cookie.size()==0)return null;
        String cookie="";
        Object[] keys=this.m_cookie.keySet().toArray();
        cookie+=(keys[0]+"="+this.m_cookie.get(keys[0]));
        for(int i=1;i<m_cookie.size();i++){
            cookie+=("; "+keys[i]+"="+this.m_cookie.get(keys[i]));
        }
        return cookie;
    }
    public boolean hasCookie(String name){
        return this.m_cookie.containsKey(name);
    }
    public void clearAll(){
        this.m_cookie.clear();
        m_objStorage.remove("cookie");
    }
    public void remove(String name) {
        if(null==name)return;
        m_cookie.remove(name);
        m_objStorage.save("cookie",m_cookie);
    }
}
