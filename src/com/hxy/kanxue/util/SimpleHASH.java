package com.hxy.kanxue.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by xianyu.hxy on 2015/8/24.
 */
public class SimpleHASH {
    private static String convertToHex(byte[] data){
        StringBuilder buf=new StringBuilder();
        for(byte b:data){
            int halfbyte=(b>>>4)&0x0f;
            int two_halfs=0;
            do{
                buf.append((0<=halfbyte)&&(halfbyte<=9)?(char)('0'+halfbyte):(char)('a'+(halfbyte-10)));
                halfbyte=b&0x0f;
            }while (two_halfs++<1);
        }
        return buf.toString();
    }
    public static String sha1(String text){
        return convertToHex(hash("SHA-1", text));
    }
    public static String md5(String text){
        return convertToHex(hash("MD5",text));
    }
    private static byte[] hash(String hashType,String input){
        MessageDigest md=null;

        try {
            md=MessageDigest.getInstance(hashType);
            if(md==null)return null;
            md.update(input.getBytes("iso-8859-1"),0,input.length());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return md.digest();
    }
}
