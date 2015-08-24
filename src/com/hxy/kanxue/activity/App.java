package com.hxy.kanxue.activity;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.hxy.kanxue.R;
import com.hxy.kanxue.net.Api;
import com.hxy.kanxue.net.HttpClientUtil;
import org.apache.http.cookie.Cookie;
import org.json.JSONObject;

import java.io.*;
import java.util.List;


/**
 * Created by xianyu.hxy on 2015/8/24.
 */
public class App extends Application {
    private static String LOG_FILE_NAME = "kanxue.log";
    public static final String LOGIN_STATE_CHANGE_ACTION = "com.pediy.bbs.kanxue.LOGIN_STATE_CHANGE_ACTION";
    private int m_versionCode = 0;
    private ProgressDialog m_updatePd = null;
    private String m_appSavePath = null;
    private boolean m_bCancel = false;
    private Handler m_handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
           if(m_updatePd==null)return;
            if(msg.what>=0&&msg.what!=m_updatePd.getMax()){
                m_updatePd.setProgress(msg.what);
                return;
            }
            if(m_appSavePath==null)return;
            m_updatePd.dismiss();
            if(msg.what<0)return;
            installApk(m_appSavePath);
        }
    };
    private Handler m_networkErrHandler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HttpClientUtil.NET_TIMEOUT:
                    Toast.makeText(App.this, R.string.net_timeout,Toast.LENGTH_SHORT).show();
                    break;
                case HttpClientUtil.NET_FAILED:
                    Toast.makeText(App.this, R.string.net_failed,Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }
    public Handler getNetworkHandler(){
        return m_networkErrHandler;
    }
    public void installApk(String path){
        Uri uri=Uri.fromFile(new File(path));
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri,"application/vnd.android.package-archive");
        startActivity(intent);
    }
    private void enableRecordLog(){
        String path=LOG_FILE_NAME;
        FileOutputStream fos=null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)){
            path=Environment.getExternalStorageDirectory()+"/kanxue/"+path;
            try {
                fos=new FileOutputStream(path,true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else {
            try {
                fos=openFileOutput(path,MODE_APPEND|MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        System.setErr(new PrintStream(fos));
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void checkUpdate(final Context context){
        checkUpdate(context, null);
    }
    public void checkUpdate(final Context context,final onCheckUpdate callback){
        Api.getInstance().checkUpdate(new HttpClientUtil.NetClientCallback() {
            @Override
            public void execute(int status, String response, List<Cookie> cookies) {
                if(callback!=null){
                    callback.networkComplete();
                }
                if(status!=HttpClientUtil.NET_SUCCESS){
                    m_networkErrHandler.sendEmptyMessage(status);
                    return;
                }
                final com.alibaba.fastjson.JSONObject obj= JSON.parseObject(response);
                if(obj.getInteger("version")==m_versionCode)){
                    if(callback!)
                }
            }
        });
    }
    private ProgressDialog createUpdateDialog(Context context){
        ProgressDialog updatePd=new ProgressDialog(context);
        updatePd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        updatePd.setTitle("提示");
        updatePd.setMessage("正在更新......");
        updatePd.setIndeterminate(false);
        updatePd.setCancelable(true);
        updatePd.setButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                m_bCancel=false;
            }
        });
        return updatePd;
    }
    private String versionCodeToName(int code){
        return code/100+"."+code/10%10+"."+code%100;
    }
    private String convertToSuitableSize(int size){
        if(size>=1024){
            return (size/1024.0+"").substring(0,3)+"MB";
        }
        return size+"KB";
    }
    public interface  onCheckUpdate{
        void networkComplete();
        void noUpdate();
    }
}


















