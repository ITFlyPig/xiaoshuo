package com.minnovel.weiweiyixiaohenqingcheng.view.impl;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.minnovel.weiweiyixiaohenqingcheng.R;
import com.minnovel.weiweiyixiaohenqingcheng.bean.MessageEvents;
import com.minnovel.weiweiyixiaohenqingcheng.bean.PayStatusEvent;
import com.minnovel.weiweiyixiaohenqingcheng.utils.GoogleBillingUtil;
import com.minnovel.weiweiyixiaohenqingcheng.utils.PayStatusUtil;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class DownProApp extends Activity {
    RelativeLayout rl_down;

    private GoogleBillingUtil googleBillingUtil;
    private MyOnPurchaseFinishedListener mOnPurchaseFinishedListener = new MyOnPurchaseFinishedListener();//购买回调接口
    private MyOnQueryFinishedListener mOnQueryFinishedListener = new MyOnQueryFinishedListener();//查询回调接口
    private MyOnStartSetupFinishedListener mOnStartSetupFinishedListener = new MyOnStartSetupFinishedListener();//启动结果回调接口


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_pro_app);
        init();


        GoogleBillingUtil.cleanListener();
        googleBillingUtil = GoogleBillingUtil.getInstance()
                .setOnPurchaseFinishedListener(mOnPurchaseFinishedListener)
                .setOnQueryFinishedListener(mOnQueryFinishedListener)
                .setOnStartSetupFinishedListener(mOnStartSetupFinishedListener)
                .build();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvents event) {
        /* Do something */
        if (event.getConnect() == true || PayStatusUtil.isSubAvailable()) {
//            getDate();
        } else {
//            mUIListView.setVisibility(View.GONE);
//            mUIllempty.setVisibility(View.VISIBLE);
        }
    }

    ;

    public void DownClick(View v) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.SelfTourGuidePro.singapore")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.SelfTourGuidePro.singapore")));
        }
    }

    public void init() {
        rl_down = findViewById(R.id.rl_down);
        rl_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                 try {
//                     startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.SelfTourGuidePro.singapore")));
//                 } catch (android.content.ActivityNotFoundException anfe) {
//                     startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.SelfTourGuidePro.singapore")));
//                 }

                googleBillingUtil.purchaseSubs(DownProApp.this, GoogleBillingUtil.subsSKUS[0]);
            }
        });
    }

    public void onClickBack(View v) {
        finish();
    }


    //查询商品信息回调接口
    private class MyOnQueryFinishedListener implements GoogleBillingUtil.OnQueryFinishedListener {
        @Override
        public void onQuerySuccess(String skuType, List<SkuDetails> list) {
            Log.d("wyl", "查询商品信息回调接口 onQuerySuccess");
            if (list != null) {
                for (SkuDetails skuDetails : list) {
                    String log = "";
                    if (skuType == BillingClient.SkuType.INAPP) {
                        log += "内购的商品:";
                    } else if (skuType == BillingClient.SkuType.SUBS) {
                        log += "订阅的商品:";
                    }
                    Log.d("wyl", log + skuDetails.getTitle() + " 序列号：" + skuDetails.getSku() + " 价格：" + skuDetails.getPrice());
                }
            }


            //查询成功，返回商品列表，
            //skuDetails.getPrice()获得价格(文本)
            //skuDetails.getType()获得类型 sub或者inapp,因为sub和inapp的查询结果都走这里，所以需要判断。
            //googleBillingUtil.getSubsPositionBySku(skuDetails.getSku())获得当前subs sku的序号
            //googleBillingUtil.getInAppPositionBySku(skuDetails.getSku())获得当前inapp suk的序号
        }

        @Override
        public void onQueryFail(int responseCode) {
            Log.d("wyl", "查询商品信息回调接口 onQueryFail");
            //查询失败
        }

        @Override
        public void onQueryError() {
            //查询错误
            Log.d("wyl", "查询商品信息回调接口 onQueryError");
        }
    }

    //服务初始化结果回调接口
    private class MyOnStartSetupFinishedListener implements GoogleBillingUtil.OnStartSetupFinishedListener {
        @Override
        public void onSetupSuccess() {
            Log.d("wyl", "服务初始化结果回调接口 onSetupSuccess");


            Log.d("wyl", "开始查询已经购买商品");
            List<Purchase> inapps = googleBillingUtil.queryPurchasesInApp();
            if (inapps != null) {
                for (Purchase inapp : inapps) {
                    Log.d("wyl", "已经购买的商品：" + inapp.getSku());
                }
            }
            Log.d("wyl", "开始查询已经订阅商品");
            List<Purchase> subs = googleBillingUtil.queryPurchasesSubs();
            if (subs != null) {
                for (Purchase sub : subs) {
                    Log.d("wyl", "已经订阅的商品：" + sub.getSku());
                }
            }

            int size = googleBillingUtil.getPurchasesSizeSubs();
            Log.d("wyl", "获取有效订阅的数量：" + size);

//            Toast.makeText(BaseApplication.getInstance(), "DownPro 有效订阅的数量：" + size + ":::" + (subs == null ? 0 :subs.size()), Toast.LENGTH_LONG).show();

        }

        @Override
        public void onSetupFail(int responseCode) {
            Log.d("wyl", "服务初始化结果回调接口 onSetupFail");

        }

        @Override
        public void onSetupError() {
            Log.d("wyl", "服务初始化结果回调接口 onSetupError");

        }
    }

    //购买商品回调接口
    private class MyOnPurchaseFinishedListener implements GoogleBillingUtil.OnPurchaseFinishedListener {
        @Override
        public void onPurchaseSuccess(List<Purchase> list) {
            //内购或者订阅成功,可以通过purchase.getSku()获取suk进而来判断是哪个商品
            Log.d("wyl", "购买商品回调接口 onPurchaseSuccess");
            if (list != null) {
                for (Purchase purchase : list) {
                    String sku = purchase.getSku();
                    if (!TextUtils.isEmpty(sku) && TextUtils.equals(sku, GoogleBillingUtil.subsSKUS[0])) {//订阅商品成功，记录
                        PayStatusUtil.savePaySubStatus(true);
                    }

                    String log = "";
                    if (googleBillingUtil.handlePurchase(purchase)) {
                        log = log + "商品序列号：" + purchase.getSku();
                        Log.d("wyl", " 尚明" + "购买的商品通过验证：" + purchase.getSignature());
                    } else {
                        log = log + "商品序列号：" + purchase.getSku();
                        Log.d("wyl", "购买的商品未通过验证：" + purchase.getSignature());
                    }
                    Log.d("wyl", "购买或者订阅成功：" + log);
                }
            }
        }

        @Override
        public void onPurchaseFail(int responseCode) {
            Log.d("wyl", "购买商品回调接口 onPurchaseFail：" + responseCode);

        }

        @Override
        public void onPurchaseError() {
            Log.d("wyl", "购买商品回调接口 onPurchaseError");

        }


    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.checkSub();//每做一次购买操作就查询一次


    }

    public static int count = 0;

    private void test() {
        count++;
        if (count % 2 == 0) {
            Log.d("wyl", " test 订阅有效");
            PayStatusUtil.savePaySubStatus(true);
        } else {
            PayStatusUtil.savePaySubStatus(false);
            Log.d("wyl", "test 订阅无效");
        }
        EventBus.getDefault().post(new PayStatusEvent());
    }
}
