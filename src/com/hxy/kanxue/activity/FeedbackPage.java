package com.hxy.kanxue.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hxy.kanxue.R;
import com.hxy.kanxue.net.Api;
import com.hxy.kanxue.net.HttpClientUtil;
import org.apache.http.cookie.Cookie;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xianyu.hxy on 2015/8/25.
 */
public class FeedbackPage extends Activity {
    private EditText m_name;
    private EditText m_email;
    private EditText m_message;
    private Handler m_handler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HttpClientUtil.NET_TIMEOUT:
                    Toast.makeText(FeedbackPage.this, R.string.net_timeout,Toast.LENGTH_SHORT).show();
                    break;
                case HttpClientUtil.NET_FAILED:
                    Toast.makeText(FeedbackPage.this, R.string.net_failed,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // AseoZdpAseo.init(this,AseoZdpAseo.SCREEN_TYPE);
        this.setContentView(R.layout.feedback_page);
        this.m_name = (EditText) this.findViewById(R.id.feedbackName);
        this.m_name.setText(Html.fromHtml(Api.getInstance().getLoginUserName()));
        this.m_email = (EditText) this.findViewById(R.id.feedbackEmail);
        this.m_email.setText(Api.getInstance().getEmail());
        this.m_message = (EditText) this.findViewById(R.id.feedbackMessage);
    }
    public void onBackBtnClick(View v) {
        this.finish();
    }
    public void onSendBtnClick(View v){
        String name=m_name.getText().toString();
        if (name.length() == 0) {
            Toast.makeText(FeedbackPage.this, "��������Ϊ��", Toast.LENGTH_SHORT)
                    .show();
            this.m_name.requestFocus();
            return;
        }

        String email = m_email.getText().toString();
        if (email.length() == 0) {
            Toast.makeText(FeedbackPage.this, "Email����Ϊ��", Toast.LENGTH_SHORT)
                    .show();
            this.m_email.requestFocus();
            return;
        } else {
            Pattern pattern = Pattern.compile(
                    "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(email);
            if (!matcher.matches()) {
                Toast.makeText(FeedbackPage.this, "������Ч��Email��ַ",
                        Toast.LENGTH_SHORT).show();
                this.m_email.requestFocus();
                return;
        }
    }
        String msg = m_message.getText().toString();
        if (msg.length() == 0) {
            Toast.makeText(FeedbackPage.this, "��Ϣ����Ϊ��", Toast.LENGTH_SHORT)
                    .show();
            this.m_message.requestFocus();
            return;
        }
        final ProgressDialog pd = ProgressDialog.show(this, null,
                "�ύ�����У����Ժ󡭡�", true, true);
        Api.getInstance().feedback(name, email, msg, new HttpClientUtil.NetClientCallback() {

            @Override
            public void execute(final int status, final String response,
                                List<Cookie> cookies) {
                System.out.println("feedback:" + response);

                m_handler.sendEmptyMessage(status);
                m_handler.post(new Runnable() {

                    @Override
                    public void run() {
                        pd.dismiss();
                        switch (status) {
                            case HttpClientUtil.NET_SUCCESS:
                                if (response == null) {
                                    Toast.makeText(FeedbackPage.this, "�ύ����ʧ��",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                JSONObject jsonObj = JSON.parseObject(response);
                                if (jsonObj == null) {
                                    Toast.makeText(FeedbackPage.this, "���ݴ���",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                int ret = jsonObj.getInteger("result");
                                if (ret != 0) {
                                    Toast.makeText(FeedbackPage.this, "�ύ����ʧ��",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Toast.makeText(FeedbackPage.this, "�ύ�����ɹ�",
                                        Toast.LENGTH_SHORT).show();
                                FeedbackPage.this.finish();
                                break;
                            default:
                                break;
                        }

                    }

                });

            }

        });
    }

}
































