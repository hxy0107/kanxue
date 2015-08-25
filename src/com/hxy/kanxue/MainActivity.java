package com.hxy.kanxue;


        import android.app.TabActivity;
        import android.content.Intent;
        import android.os.Bundle;
        import android.view.KeyEvent;
        import android.view.View;
        import android.widget.ImageView;
        import android.widget.TabHost;
        import android.widget.TabHost.TabSpec;
        import android.widget.Toast;
        import com.hxy.kanxue.activity.*;
        import com.hxy.kanxue.net.Api;

public class MainActivity extends TabActivity {
    private long m_exitTime = 0;
    private String[] m_tabTitle = new String[] { "����", "��ҳ", "��ȫ��Ѷ", "����" };

    private Class<?>[] m_tabIntent = new Class<?>[] { ForumDisplayPage.class,
            ForumHomePage.class, ForumDisplayPage.class, SettingPage.class };

    private int[] m_tabIcon = new int[] { R.drawable.collections_view_as_list,
            R.drawable.collections_view_as_grid, R.drawable.coffee,
            R.drawable.action_settings };

    private Bundle[] m_data = new Bundle[] {
            createBundle(Api.NEW_FORUM_ID, "����", true), null,
            createBundle(Api.SECURITY_FORUM_ID, "��ȫ��Ѷ", true), null };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);


        TabHost tabHost = getTabHost();
        for (int i = 0; i < this.m_tabTitle.length; i++) {
            String title = this.m_tabTitle[i];
            Intent intent = new Intent(this, m_tabIntent[i]);
            if (m_data[i] != null) {
                intent.putExtras(m_data[i]);
            }
            View tab = getLayoutInflater().inflate(R.layout.forum_tab, null);
            ImageView imgView = (ImageView) tab.findViewById(R.id.tabIcon);
            imgView.setImageResource(m_tabIcon[i]);
            TabSpec spec = tabHost.newTabSpec(title).setIndicator(tab)
                    .setContent(intent);
            tabHost.addTab(spec);
        }
        // ÿ�ν��������
        App app = (App) this.getApplication();
        app.checkUpdate(this);

    }

    // �����·��ؼ��˳�����tabActivity�в�����
	/*
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
	 * if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() ==
	 * KeyEvent.ACTION_DOWN) { if((System.currentTimeMillis() - m_exitTime) >
	 * 2000) { //System.currentTimeMillis()���ۺ�ʱ���ã��϶�����2000 Toast.makeText(this,
	 * "�ٰ�һ���˳�����",Toast.LENGTH_SHORT).show(); m_exitTime =
	 * System.currentTimeMillis(); } else { finish(); System.exit(0); } return
	 * true; } return super.onKeyDown(keyCode, event); }
	 */

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - m_exitTime) > 2000) { // System.currentTimeMillis()���ۺ�ʱ���ã��϶�����2000
                Toast.makeText(this, "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();
                m_exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private Bundle createBundle(int id, String title, boolean bHideBackBtn) {
        Bundle data = new Bundle();
        data.putInt("id", id);
        data.putString("title", title);
        data.putBoolean("isHideBackBtn", bHideBackBtn);
        return data;
    }
}
