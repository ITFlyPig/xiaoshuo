//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.minnovel.weiweiyixiaohenqingcheng;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDex;

import com.minnovel.weiweiyixiaohenqingcheng.BuildConfig;
import com.minnovel.weiweiyixiaohenqingcheng.R;
import com.minnovel.weiweiyixiaohenqingcheng.service.DownloadService;
import com.umeng.analytics.MobclickAgent;

public class MApplication extends Application {

    private static MApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.IS_RELEASE) {
            String channel = "debug";
            try {
                ApplicationInfo appInfo = getPackageManager()
                        .getApplicationInfo(getPackageName(),
                                PackageManager.GET_META_DATA);
                channel = appInfo.metaData.getString("UMENG_CHANNEL_VALUE");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(this, getString(R.string.umeng_key), channel, MobclickAgent.EScenarioType.E_UM_NORMAL, true));
        }
        instance = this;
        startService(new Intent(this, DownloadService.class));
    }

    public static MApplication getInstance() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this) ;
    }
}
