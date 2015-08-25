package com.hxy.kanxue.activity;

/**
 * Created by xianyu.hxy on 2015/8/25.
 */
import java.util.List;

import com.hxy.kanxue.R;
import com.hxy.kanxue.net.Api;
import com.hxy.kanxue.net.HttpClientUtil;
import org.apache.http.cookie.Cookie;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class NewThreadPage extends Activity {
    private int m_id;
    private ProgressDialog m_pd;
    private EditText m_kxReward;
    private EditText m_subject;
    private int m_currentTopic;

    private Handler m_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HttpClientUtil.NET_TIMEOUT:
                    Toast.makeText(NewThreadPage.this, R.string.net_timeout, Toast.LENGTH_SHORT).show();
                    break;
                case HttpClientUtil.NET_FAILED:
                    Toast.makeText(NewThreadPage.this, R.string.net_failed, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

            //�ر�ProgressDialog
            m_pd.dismiss();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.new_thread_page);
       // AseoZdpAseo.init(this,AseoZdpAseo.SCREEN_TYPE);
        Bundle data = this.getIntent().getExtras();
        m_id = data.getInt("id");
        m_subject = (EditText)this.findViewById(R.id.newThreadSubject);

        if (m_id == Api.HELP_FORUM_ID) {
            m_kxReward = (EditText)this.findViewById(R.id.newThreadKxReward);
            m_kxReward.setVisibility(View.VISIBLE);
            return;
        } else if (m_id == Api.GET_JOB_FORUM_ID) {
            m_subject.setText("����Ƹ��");
            return;
        }

        View v = this.findViewById(R.id.newThreadTopic);
        v.setVisibility(View.VISIBLE);
        Spinner spinner = (Spinner)this.findViewById(R.id.newThreadTopicSelect);
        final String[] topics = getResources().getStringArray(R.array.topic_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, topics);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int index, long arg3) {
                if (index == 0)
                    return;
                m_currentTopic = index;
                String tmp = m_subject.getText().toString();
                tmp = topics[index] + tmp;
                m_subject.setText(tmp);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO �Զ����ɵķ������

            }

        });
    }

    public void onBackBtnClick(View v) {
        this.finish();
    }

    public void onSubmitBtnClick(View v) {
        EditText message = (EditText)this.findViewById(R.id.newThreadMessage);
        final String subjectText = m_subject.getText().toString();
        String msgText = message.getText().toString();

        if (m_subject.length() == 0) {
            Toast.makeText(NewThreadPage.this, "���ⲻ��Ϊ��", Toast.LENGTH_SHORT).show();
            m_subject.requestFocus();
            return;
        }

        if (m_id == Api.HELP_FORUM_ID) {
            if (m_kxReward.length() == 0) {
                Toast.makeText(NewThreadPage.this, "���ͽ���Ϊ��", Toast.LENGTH_SHORT).show();
                m_kxReward.requestFocus();
                return;
            } else {
                int kx = Integer.parseInt(m_kxReward.getText().toString());
                if (kx < 10 || kx > 100) {
                    Toast.makeText(NewThreadPage.this, "���ͽ����޶���Χ", Toast.LENGTH_SHORT).show();
                    m_kxReward.requestFocus();
                    return;
                }
            }
        } else {
            if (m_id != Api.GET_JOB_FORUM_ID) {
                if (m_currentTopic == 0) {
                    Toast.makeText(NewThreadPage.this, "��ѡ����", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        if (message.length() == 0) {
            Toast.makeText(NewThreadPage.this, "���ݲ���Ϊ��", Toast.LENGTH_SHORT).show();
            message.requestFocus();
            return;
        } else if (message.length() < Api.POST_CONTENT_SIZE_MIN) {
            Toast.makeText(NewThreadPage.this, "���ݳ��Ȳ���С��"+Api.POST_CONTENT_SIZE_MIN+"���ַ�", Toast.LENGTH_SHORT).show();
            message.requestFocus();
            return;
        }

        HttpClientUtil.NetClientCallback callback = new HttpClientUtil.NetClientCallback() {

            @Override
            public void execute(int status, final String response,
                                List<Cookie> cookies) {
                System.out.println("post new thread return:"+response);

                m_handler.sendEmptyMessage(status);
                if (status != HttpClientUtil.NET_SUCCESS)
                    return;
                m_handler.post(new Runnable() {

                    @Override
                    public void run() {
                        JSONObject jsonObj = JSON.parseObject(response);
                        int result = jsonObj.getInteger("result");

                        switch(result) {
                            case Api.NEW_POST_SUCCESS:
                                Toast.makeText(NewThreadPage.this, R.string.new_post_success, Toast.LENGTH_SHORT).show();
                                break;
                            case Api.NEW_POST_FAIL_WITHIN_THIRTY_SECONDS:
                                Toast.makeText(NewThreadPage.this, R.string.new_post_fail_within_thirty_seconds, Toast.LENGTH_SHORT).show();
                                return;
                            case Api.NEW_POST_FAIL_WITHIN_FIVE_MINUTES:
                                Toast.makeText(NewThreadPage.this, R.string.new_post_fail_within_five_minutes, Toast.LENGTH_SHORT).show();
                                return;
                            case Api.NEW_POST_FAIL_NOT_ENOUGH_KX:
                                Toast.makeText(NewThreadPage.this, R.string.new_post_fail_not_enough_kx, Toast.LENGTH_SHORT).show();
                                return;
                            default:
                                break;
                        }
                        Bundle data = new Bundle();
                        data.putString("subject", subjectText);
                        int id = jsonObj.getInteger("threadid");
                        data.putInt("id", id);
                        Intent intent = NewThreadPage.this.getIntent();
                        intent.putExtras(data);
                        NewThreadPage.this.setResult(1, intent);
                        NewThreadPage.this.finish();
                    }

                });

            }

        };

        m_pd = ProgressDialog.show(this, null, "�����������ݣ����Ժ󡭡�", true, true);
        if (m_id == Api.HELP_FORUM_ID) {
            Api.getInstance().newThread(this.m_id, subjectText, m_kxReward.getText().toString(), msgText, callback);
            return;
        }
        Api.getInstance().newThread(this.m_id, subjectText, msgText, callback);

    }
}

