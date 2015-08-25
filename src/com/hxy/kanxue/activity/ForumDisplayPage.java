package com.hxy.kanxue.activity;

/**
 * Created by xianyu.hxy on 2015/8/25.
 */
        import java.net.MalformedURLException;
        import java.net.URL;
        import java.util.List;

        import com.hxy.kanxue.R;
        import com.hxy.kanxue.net.Api;
        import com.hxy.kanxue.net.HttpClientUtil;
        import com.hxy.kanxue.widget.ImageViewWithCache;
        import com.hxy.kanxue.widget.RefreshActionBtn;
        import com.hxy.kanxue.widget.XListView;
        import org.apache.http.cookie.Cookie;

        import android.app.Activity;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Message;
        import android.text.Html;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.AdapterView;
        import android.widget.AdapterView.OnItemClickListener;
        import android.widget.BaseAdapter;
        import android.widget.ProgressBar;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.alibaba.fastjson.JSON;
        import com.alibaba.fastjson.JSONArray;
        import com.alibaba.fastjson.JSONObject;


public class ForumDisplayPage extends Activity implements XListView.IXListViewListener,
        OnItemClickListener {
    private int m_id = 0;
    private int m_currentPage = 1;
    private int m_totalPage = 0;
    private XListView m_listView;
    private ForumDisplayAdapter m_adapter;
    private ProgressBar m_pBar;
    private RefreshActionBtn m_refreshBtn;
    private JSONArray m_model = null;
    private long m_lastUpdateTime = 0;
    private Recv m_recv = null;

    private Handler m_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HttpClientUtil.NET_SUCCESS:
                    m_adapter.notifyDataSetChanged();
                    // ֻ��listview��������֮�������pull load
                    if (m_currentPage == 2 && m_totalPage != 0) {
                        m_listView.setPullLoadEnable(true);
                    }
                    break;
                case HttpClientUtil.NET_TIMEOUT:
                    Toast.makeText(ForumDisplayPage.this, R.string.net_timeout,
                            Toast.LENGTH_SHORT).show();
                    break;
                case HttpClientUtil.NET_FAILED:
                    Toast.makeText(ForumDisplayPage.this, R.string.net_failed,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

            if (m_refreshBtn.isRefreshing()) {
                m_listView.setSelection(1);
                m_refreshBtn.endRefresh();
            }

            // ֹͣ��������ʱ��processBar
            if (m_listView.getPullLoading()) {
                // ���ص����һҳʱ����pull load
                if (m_currentPage > m_totalPage) {
                    m_listView.setPullLoadEnable(false);
                } else {
                    m_listView.stopLoadMore();
                }
            }

            if (m_listView.getPullRefreshing()) {
                m_listView.stopRefresh();
            }
            m_pBar.setVisibility(View.GONE);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.forum_display_page);
       // AseoZdpAseo.init(this,AseoZdpAseo.INSERT_TYPE);
        m_listView = (XListView) this.findViewById(R.id.forumDisplayListView);
        m_listView.setPullLoadEnable(false);
        m_listView.setPullRefreshEnable(true);
        m_listView.setXListViewListener(this);
        m_listView.setOnItemClickListener(this);
        m_adapter = new ForumDisplayAdapter();
        m_listView.setAdapter(m_adapter);

        m_refreshBtn = (RefreshActionBtn) this
                .findViewById(R.id.forumDisplayRefreshBtn);
        m_pBar = (ProgressBar) this.findViewById(R.id.forumDisplayProgressBar);

        Bundle data = this.getIntent().getExtras();
        TextView tv = (TextView) this.findViewById(R.id.forumDisplayPageTitle);
        tv.setText(data.getString("title"));
        // ������鲻�ܷ��������ط�����ť
        m_id = data.getInt("id");
        if (m_id == Api.NEW_FORUM_ID) {
            this.findViewById(R.id.forumDisplayNewThreadBtn).setVisibility(
                    View.GONE);
        }

        if (data.getBoolean("isHideBackBtn")) {
            this.findViewById(R.id.forumDisplayBackBtn)
                    .setVisibility(View.GONE);
            this.findViewById(R.id.forumDisplaySeg).setVisibility(View.GONE);
            this.findViewById(R.id.forumDisplayKanxueTitle).setVisibility(
                    View.VISIBLE);
        }

        m_recv = new Recv();
        IntentFilter filter = new IntentFilter();
        filter.addAction(App.LOGIN_STATE_CHANGE_ACTION);
        this.registerReceiver(m_recv, filter);

        loadModel(m_currentPage++);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(m_recv);
    }

    private void loadModel(final int page) {
        Api.getInstance().getForumDisplayPage(m_id, page,
                new HttpClientUtil.NetClientCallback() {

                    @Override
                    public void execute(int status, String response,
                                        List<Cookie> cookies) {
                        if (status == HttpClientUtil.NET_SUCCESS) {
                            JSONObject obj = JSON.parseObject(response);
                            JSONArray arr = obj.getJSONArray("threadList");
                            if (page == 1) {
                                m_lastUpdateTime = obj.getLong("time");
                                m_totalPage = obj.getInteger("pagenav");
                                m_model = arr;
                            } else {
                                for (int i = 0; i < arr.size(); i++) {
                                    m_model.add(arr.getJSONObject(i));
                                }
                            }
                        }
                        m_handler.sendEmptyMessage(status);
                    }
                });
    }

    public void forceRefresh() {
        m_currentPage = 1;
        loadModel(m_currentPage++);
    }

    public void onBackBtnClick(View v) {
        this.finish();
    }

    public void onPageTitleClick(View v) {
        // ����listView������
        m_listView.setSelection(0);
    }

    public void onNewThreadBtnClick(View v) {
        if (!Api.getInstance().isLogin()) {
            this.startActivity(new Intent(this, LoginPage.class));
            return;
        }
        if (this.m_model == null) {
            Toast.makeText(ForumDisplayPage.this, R.string.forum_display_null,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle data = new Bundle();
        data.putInt("id", this.m_id);
        Intent intent = new Intent(this, NewThreadPage.class);
        intent.putExtras(data);
        this.startActivityForResult(intent, 0);
    }

    private void refresh() {
        // ˢ��֮ǰ�ж���������
        Api.getInstance().checkNewPostInForumDisplayPage(m_id,
                m_lastUpdateTime, new HttpClientUtil.NetClientCallback() {

                    @Override
                    public void execute(int status, String response,
                                        List<Cookie> cookies) {
                        if (status != HttpClientUtil.NET_SUCCESS) {
                            m_handler.sendEmptyMessage(status);
                            return;
                        }
                        final JSONObject obj = JSON.parseObject(response);
                        if (obj.getInteger("result") == 0) {
                            m_handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(ForumDisplayPage.this,
                                            "������", Toast.LENGTH_SHORT).show();
                                }

                            });
                            m_handler.sendEmptyMessage(0);
                            return;
                        }
                        m_currentPage = 1;
                        loadModel(m_currentPage++);
                    }

                });
    }

    private void safeRefresh() {
        // �����ʼ���紫��ʧ���ˣ�ǿ������ˢ��
        if (this.m_model == null) {
            forceRefresh();
        } else {
            refresh();
        }
    }

    public void onRefreshBtnClick(View v) {
        if (m_refreshBtn.isRefreshing())
            return;
        m_refreshBtn.startRefresh();
        m_listView.setSelection(0);
        m_listView.pullRefreshing();
        safeRefresh();
    }

    @Override
    public void onRefresh() {
        m_refreshBtn.startRefresh();
        safeRefresh();
    }

    @Override
    public void onLoadMore() {
        loadModel(m_currentPage++);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
        // ����header����������
        if (position < 1)
            return;

        JSONObject obj = m_model.getJSONObject(position - 1);
        Bundle data = new Bundle();
        // listview��header,position����һ����
        data.putInt("id", obj.getInteger("threadid"));
        data.putInt("open", obj.getInteger("open"));
        Intent intent = new Intent(this, ShowThreadPage.class);
        intent.putExtras(data);
        this.startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 1:
                Bundle bunde = data.getExtras();
                JSONObject item = new JSONObject();
                item.put("threadtitle", bunde.getString("subject"));
                item.put("threadid", bunde.getInt("id"));
                item.put("postusername",
                        Html.fromHtml(Api.getInstance().getLoginUserName()));
                item.put("postuserid", Api.getInstance().getLoginUserId());
                item.put("avatar", Api.getInstance().getIsAvatar());
                item.put("avatardateline", "");
                item.put("lastpostdate", "");
                item.put("views", 1);
                item.put("replycount", 0);
                item.put("globalsticky", 0);
                item.put("sticky", 0);
                item.put("goodnees", 0);

                int index = this.getLastTopPostIndex();
                this.m_model.add(index, item);
                this.m_adapter.notifyDataSetChanged();
                m_listView.setSelection(index + 1);
                break;
            default:
                break;
        }
    }

    private int getLastTopPostIndex() {
        for (int i = 0; i < this.m_model.size(); i++) {
            JSONObject item = this.m_model.getJSONObject(i);
            if (item.getInteger("globalsticky") == -1
                    || item.getInteger("sticky") != 0)
                continue;
            return i;
        }
        return -1;
    }

    // ���ǳ����ߵ�½״̬�����仯ʱ���յ�ˢ����Ϣ
    private class Recv extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ForumDisplayPage.this.forceRefresh();
        }

    }

    class ForumDisplayAdapter extends BaseAdapter {

        public ForumDisplayAdapter() {
            super();
        }

        @Override
        public int getCount() {
            return (m_model == null) ? 0 : m_model.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.forum_display_item, null);
            }
            TextView title = (TextView) convertView
                    .findViewById(R.id.forumDisplayTitle);
            TextView replyCnt = (TextView) convertView
                    .findViewById(R.id.replyCnt);
            TextView viewsCnt = (TextView) convertView
                    .findViewById(R.id.viewsCnt);
            TextView userName = (TextView) convertView
                    .findViewById(R.id.postUserName);
            ImageViewWithCache img = (ImageViewWithCache) convertView
                    .findViewById(R.id.headImg);

            JSONObject item = m_model.getJSONObject(position);
            String threadTitle = item.getString("threadtitle");
            // �����ǰΪ������飬�����ö�����ʾ��Ϣ
            if (m_id != Api.NEW_FORUM_ID) {
                if (item.getInteger("globalsticky") == Api.GLOBAL_TOP_FORUM) {
                    threadTitle = "<font color=\"red\">[�ܶ�] </font>"
                            + threadTitle;
                } else if (item.getInteger("globalsticky") == Api.AREA_TOP_FORUM) {
                    threadTitle = "<font color=\"red\">[����] </font>"
                            + threadTitle;
                } else if (item.getInteger("sticky") == Api.TOP_FORUM) {
                    threadTitle = "<font color=\"red\">[�ö�] </font>"
                            + threadTitle;
                }
            }
            title.setText(Html.fromHtml(threadTitle));

            replyCnt.setText(item.getString("replycount"));
            viewsCnt.setText(item.getString("views"));
            userName.setText(Html.fromHtml(item.getString("postusername")));
            if (item.getInteger("avatar") == 1) {
                try {
                    img.setImageUrl(new URL(Api.getInstance()
                            .getUserHeadImageUrl(item.getInteger("postuserid"))));
                } catch (MalformedURLException e) {
                    // TODO �Զ����ɵ� catch ��
                    e.printStackTrace();
                }
            } else {
                img.setImageResource(R.drawable.default_user_head_img);
            }

            convertView.findViewById(R.id.forumDisplayLock).setVisibility(
                    (item.getInteger("open") == 0) ? View.VISIBLE : View.GONE);

            return convertView;
        }
    }
}
