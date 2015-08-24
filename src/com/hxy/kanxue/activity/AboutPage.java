package com.hxy.kanxue.activity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.hxy.kanxue.R;

/**
 * Created by xianyu.hxy on 2015/8/24.
 */
public class AboutPage extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_page);
        PackageInfo pinfo;

        try {
            pinfo=getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
            TextView tv=(TextView)this.findViewById(R.id.aboutPageVerText);
            tv.setText("v"+pinfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }
    public void onBackBtnClick(View v){
        finish();
    }
}
