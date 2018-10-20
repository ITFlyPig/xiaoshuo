package com.minnovel.weiweiyixiaohenqingcheng.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.minnovel.weiweiyixiaohenqingcheng.MApplication;
import com.monke.basemvplib.BaseApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Created by TJbaobao on 2017/11/2.
 * CSDN:http://blog.csdn.net/u013640004/article/details/78257536
 * <p>
 * 当前版本:V1.1.5
 * 更新日志:
 * <p>
 * v1.1.5 2018/07/13
 * 优化-尽可能处理了一些可能造成的内存泄漏的问题。
 * 修改-查询成功接口增加一个String skuType，参数，各位在查询的时候需要判断skuType
 * 增加-增加两处接口为Null的Log提示，tag为GoogleBillingUtil。
 * <p>
 * V1.1.4 2018/01/03
 * 修改-实现单例模式，避免多实例导致的谷歌接口回调错乱问题。
 * <p>
 * V1.1.3 2017/12/19
 * 修复-服务启动失败时导致的空指针错误。
 * <p>
 * V1.1.2    2017/12/18
 * 修复-修复内购未被消耗的BUG。
 * 增加-每次启动都获取一次历史内购订单，并且全部消耗。
 * 增加-可以通过设置isAutoConsumeAsync来确定内购是否每次自动消耗。
 * 增加-将consumeAsync改为public，你可以手动调用消耗。
 * <p>
 * V1.1.1  2017/11/2
 * 升级-内购API版本为google最新版本。compile 'com.android.billingclient:billing:1.0'
 * 特性-不需要key了，不需要IInAppBillingService.aidl了，不需要那一大堆Utils了，创建新实例的时候必须要传入购买回调接口。
 * <p>
 * V1.0.3 2017/10/27
 * 增加-支持内购
 * <p>
 * V1.0.2  2017/09/11
 * 修复-修复BUG
 * <p>
 * v1.0.1 2017/07/29
 * 初始版本
 */


@SuppressWarnings("ALL")
public class GoogleBillingUtil {

    private static final String TAG = "GoogleBillingUtil";

    public static String[] inAppSKUS = new String[]{};//内购ID,必填
    public static String[] subsSKUS = new String[]{"minnovel_subscription_01"};//订阅ID,必填

    public static final String BILLING_TYPE_INAPP = BillingClient.SkuType.INAPP;//内购
    public static final String BILLING_TYPE_SUBS = BillingClient.SkuType.SUBS;//订阅

    private static BillingClient mBillingClient;
    private static BillingClient.Builder builder;
    private static OnPurchaseFinishedListener mOnPurchaseFinishedListener;
    private static OnStartSetupFinishedListener mOnStartSetupFinishedListener;
    private static OnQueryFinishedListener mOnQueryFinishedListener;

    private boolean isAutoConsumeAsync = true;//是否在购买成功后自动消耗商品

    private static final GoogleBillingUtil mGoogleBillingUtil = new GoogleBillingUtil();

    private static final String BASE_64_ENCODED_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjv6yx9SARcJw9Ji23YX9Ql7dazJVvhtLvnpIvZHueqRA/QT21Oh11IraRiE3WgOHKvXqg0C18zfaPFmwkv/3181LoRztfor5hhybrt5lV7XKLI+JUG390ZqfvlHGOxWbgibgEsdTKObz2K6M6s/yU5PrnqKNxKLnU81zg3GnC7strKduKDjL8A2+uOzaO45vSF+hjPvsMvh3CQpDBGqWsNzlM4LaGXUWVoTmwurK9LtgdRqy/oGqHiIKBIfCbEcmdh+lNsWcNM+sbhKiu6mcqHiS2dyj+QU54UqJGxlp/6dKNfGYJNPBRPILW9s/m9EY7PExSsDh+NsBTS895pW93QIDAQAB";

    private GoogleBillingUtil() {

    }

    public static GoogleBillingUtil getInstance() {
        cleanListener();
        return mGoogleBillingUtil;
    }

    public GoogleBillingUtil build() {
        if (mBillingClient == null) {
            synchronized (mGoogleBillingUtil) {
                Log.d("tt", "BaseApplication.getInstance(): " + MApplication.getInstance());
                if (mBillingClient == null) {
                    if (isGooglePlayServicesAvailable(MApplication.getInstance())) {
                        builder = BillingClient.newBuilder(MApplication.getInstance());
                        mBillingClient = builder.setListener(mGoogleBillingUtil.new MyPurchasesUpdatedListener()).build();
                    }
                } else {
                    builder.setListener(mGoogleBillingUtil.new MyPurchasesUpdatedListener());
                }
            }
        } else {
            builder.setListener(mGoogleBillingUtil.new MyPurchasesUpdatedListener());
        }
        synchronized (mGoogleBillingUtil) {
            if (mGoogleBillingUtil.startConnection()) {
                mGoogleBillingUtil.queryInventoryInApp();
                mGoogleBillingUtil.queryInventorySubs();
                mGoogleBillingUtil.queryPurchasesInApp();
            }
        }
        return mGoogleBillingUtil;
    }

    public boolean startConnection() {
        if (mBillingClient == null) {
            return false;
        }
        if (!mBillingClient.isReady()) {
            mBillingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                    if (billingResponseCode == BillingClient.BillingResponse.OK) {
                        queryInventoryInApp();
                        queryInventorySubs();
                        queryPurchasesInApp();
                        if (mOnStartSetupFinishedListener != null) {
                            mOnStartSetupFinishedListener.onSetupSuccess();
                        }
                    } else {
                        if (mOnStartSetupFinishedListener != null) {
                            mOnStartSetupFinishedListener.onSetupFail(billingResponseCode);
                        }
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    if (mOnStartSetupFinishedListener != null) {
                        mOnStartSetupFinishedListener.onSetupError();
                    }
                }
            });
            return false;
        } else {
            return true;
        }
    }

    /**
     * Google购买商品回调接口(订阅和内购都走这个接口)
     */
    private class MyPurchasesUpdatedListener implements PurchasesUpdatedListener {
        @Override
        public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> list) {
            if (mOnPurchaseFinishedListener == null) {
                Log.e(TAG, "警告:接收到购买回调，但购买商品接口为Null，请设置购买接口。eg:setOnPurchaseFinishedListener()");
                return;
            }
            if (responseCode == BillingClient.BillingResponse.OK && list != null) {
                if (isAutoConsumeAsync) {
                    //消耗商品
                    for (Purchase purchase : list) {
                        String sku = purchase.getSku();
                        if (sku != null) {
                            String skuType = getSkuType(sku);
                            if (skuType != null) {
                                if (skuType.equals(BillingClient.SkuType.INAPP)) {
                                    consumeAsync(purchase.getPurchaseToken());
                                }
                            }
                        }
                    }
                }
                mOnPurchaseFinishedListener.onPurchaseSuccess(list);
            } else {
                mOnPurchaseFinishedListener.onPurchaseFail(responseCode);
            }
        }
    }

    /**
     * 查询内购商品信息
     */
    public void queryInventoryInApp() {
        queryInventory(BillingClient.SkuType.INAPP);
    }

    /**
     * 查询订阅商品信息
     */
    public void queryInventorySubs() {
        queryInventory(BillingClient.SkuType.SUBS);
    }

    private void queryInventory(final String skuType) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBillingClient == null) {
                    if (mOnQueryFinishedListener != null) {
                        mOnQueryFinishedListener.onQueryError();
                    }
                    return;
                }
                ArrayList<String> skuList = new ArrayList<>();
                if (skuType.equals(BillingClient.SkuType.INAPP)) {
                    Collections.addAll(skuList, inAppSKUS);
                } else if (skuType.equals(BillingClient.SkuType.SUBS)) {
                    Collections.addAll(skuList, subsSKUS);
                }
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(skuType);
                mBillingClient.querySkuDetailsAsync(params.build(), new MySkuDetailsResponseListener(mOnQueryFinishedListener, skuType));
            }
        };
        executeServiceRequest(runnable);
    }

    /**
     * Google查询商品信息回调接口
     */
    private class MySkuDetailsResponseListener implements SkuDetailsResponseListener {
        private OnQueryFinishedListener mOnQueryFinishedListener;
        private String skuType;

        public MySkuDetailsResponseListener(OnQueryFinishedListener onQueryFinishedListener, String skuType) {
            mOnQueryFinishedListener = onQueryFinishedListener;
            this.skuType = skuType;
        }

        @Override
        public void onSkuDetailsResponse(int responseCode, List<SkuDetails> list) {
            if (mOnQueryFinishedListener == null) {
                Log.e(TAG, "警告:接收到查询商品回调，但查询商品接口为Null，请设置购买接口。eg:setOnQueryFinishedListener()");
                return;
            }
            if (responseCode == BillingClient.BillingResponse.OK && list != null) {
                mOnQueryFinishedListener.onQuerySuccess(skuType, list);
                Logger.i("wyl", "GoogleBillingUtil 内购数：" + list.size());
            } else {
                mOnQueryFinishedListener.onQueryFail(responseCode);
                Logger.i("wyl", "GoogleBillingUtil 内购查询失败");
            }
        }

    }

    /**
     * 发起内购
     *
     * @param skuId
     * @return
     */
    public void purchaseInApp(Activity activity, String skuId) {
        purchase(activity, skuId, BillingClient.SkuType.INAPP);
    }

    /**
     * 发起订阅
     *
     * @param skuId
     * @return
     */
    public void purchaseSubs(Activity activity, String skuId) {
        purchase(activity, skuId, BillingClient.SkuType.SUBS);
    }

    private void purchase(Activity activity, final String skuId, final String skuType) {
        if (mBillingClient == null) {
            if (mOnPurchaseFinishedListener != null) {
                mOnPurchaseFinishedListener.onPurchaseError();
            }
            return;
        }
        if (startConnection()) {
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSku(skuId)
                    .setType(skuType)
                    .build();
            mBillingClient.launchBillingFlow(activity, flowParams);
        } else {
            if (mOnPurchaseFinishedListener != null) {
                mOnPurchaseFinishedListener.onPurchaseError();
            }
        }
    }

    /**
     * 消耗商品
     *
     * @param purchaseToken
     */
    public void consumeAsync(String purchaseToken) {
        if (mBillingClient == null) {
            return;
        }
        mBillingClient.consumeAsync(purchaseToken, new MyConsumeResponseListener());
    }

    /**
     * Googlg消耗商品回调
     */
    private class MyConsumeResponseListener implements ConsumeResponseListener {
        @Override
        public void onConsumeResponse(int responseCode, String s) {
            if (responseCode == BillingClient.BillingResponse.OK) {

            }
        }
    }


    /**
     * 获取已经内购的商品
     *
     * @return
     */
    public List<Purchase> queryPurchasesInApp() {
        return queryPurchases(BillingClient.SkuType.INAPP);
    }

    /**
     * 获取已经订阅的商品
     *
     * @return
     */
    public List<Purchase> queryPurchasesSubs() {
        return queryPurchases(BillingClient.SkuType.SUBS);
    }

    private List<Purchase> queryPurchases(String skuType) {
        if (mBillingClient == null) {
            return null;
        }
        if (!mBillingClient.isReady()) {
            startConnection();
        } else {
            Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(skuType);
            if (purchasesResult != null) {
                if (purchasesResult.getResponseCode() == BillingClient.BillingResponse.OK) {
                    List<Purchase> purchaseList = purchasesResult.getPurchasesList();
                    if (isAutoConsumeAsync) {
                        if (purchaseList != null) {
                            for (Purchase purchase : purchaseList) {
                                if (skuType.equals(BillingClient.SkuType.INAPP)) {
                                    consumeAsync(purchase.getPurchaseToken());
                                }
                            }
                        }
                    }
                    return purchaseList;
                }
            }

        }
        return null;
    }

    /**
     * 获取有效订阅的数量
     *
     * @return -1查询失败，0没有有效订阅，>0具有有效的订阅
     */
    public int getPurchasesSizeSubs() {
        List<Purchase> list = queryPurchasesSubs();
        if (list != null) {
            return list.size();
        }
        return -1;
    }

    /**
     * 通过sku获取订阅商品序号
     *
     * @param sku
     * @return
     */
    public int getSubsPositionBySku(String sku) {
        return getPositionBySku(sku, BillingClient.SkuType.SUBS);
    }

    /**
     * 通过sku获取内购商品序号
     *
     * @param sku
     * @return 成功返回需要 失败返回-1
     */
    public int getInAppPositionBySku(String sku) {
        return getPositionBySku(sku, BillingClient.SkuType.INAPP);
    }

    private int getPositionBySku(String sku, String skuType) {

        if (skuType.equals(BillingClient.SkuType.INAPP)) {
            int i = 0;
            for (String s : inAppSKUS) {
                if (s.equals(sku)) {
                    return i;
                }
                i++;
            }
        } else if (skuType.equals(BillingClient.SkuType.SUBS)) {
            int i = 0;
            for (String s : subsSKUS) {
                if (s.equals(sku)) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    private void executeServiceRequest(final Runnable runnable) {
        if (startConnection()) {
            runnable.run();
        }
    }

    /**
     * 通过序号获取订阅sku
     *
     * @param position
     * @return
     */
    public String getSubsSkuByPosition(int position) {
        if (position >= 0 && position < subsSKUS.length) {
            return subsSKUS[position];
        } else {
            return null;
        }
    }

    /**
     * 通过序号获取内购sku
     *
     * @param position
     * @return
     */
    public String getInAppSkuByPosition(int position) {
        if (position >= 0 && position < inAppSKUS.length) {
            return inAppSKUS[position];
        } else {
            return null;
        }
    }

    /**
     * 通过sku获取商品类型(订阅获取内购)
     *
     * @param sku
     * @return inapp内购，subs订阅
     */
    private String getSkuType(String sku) {
        if (Arrays.asList(inAppSKUS).contains(sku)) {
            return BillingClient.SkuType.INAPP;
        } else if (Arrays.asList(subsSKUS).contains(sku)) {
            return BillingClient.SkuType.SUBS;
        }
        return null;
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        if (googleApiAvailability != null) {
            int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
            return resultCode == ConnectionResult.SUCCESS;
        }
        return false;
    }

    public GoogleBillingUtil setOnQueryFinishedListener(OnQueryFinishedListener onQueryFinishedListener) {
        mOnQueryFinishedListener = onQueryFinishedListener;
        return mGoogleBillingUtil;
    }

    public GoogleBillingUtil setOnPurchaseFinishedListener(OnPurchaseFinishedListener onPurchaseFinishedListener) {
        mOnPurchaseFinishedListener = onPurchaseFinishedListener;
        return mGoogleBillingUtil;
    }

    public OnStartSetupFinishedListener getOnStartSetupFinishedListener() {
        return mOnStartSetupFinishedListener;
    }

    public GoogleBillingUtil setOnStartSetupFinishedListener(OnStartSetupFinishedListener onStartSetupFinishedListener) {
        mOnStartSetupFinishedListener = onStartSetupFinishedListener;
        return mGoogleBillingUtil;
    }

    /**
     * 本工具查询回调接口
     */
    public interface OnQueryFinishedListener {
        //Inapp和sub都走这个接口查询的时候一定要判断skuType
        public void onQuerySuccess(String skuType, List<SkuDetails> list);

        public void onQueryFail(int responseCode);

        public void onQueryError();
    }

    /**
     * 本工具购买回调接口(内购与订阅都走这接口)
     */
    public interface OnPurchaseFinishedListener {

        public void onPurchaseSuccess(List<Purchase> list);

        public void onPurchaseFail(int responseCode);

        public void onPurchaseError();

    }

    /**
     * google服务启动接口
     */
    public interface OnStartSetupFinishedListener {
        public void onSetupSuccess();

        public void onSetupFail(int responseCode);

        public void onSetupError();
    }

    public boolean isReady() {
        return mBillingClient != null && mBillingClient.isReady();
    }

    public boolean isAutoConsumeAsync() {
        return isAutoConsumeAsync;
    }

    public void setIsAutoConsumeAsync(boolean isAutoConsumeAsync) {
        this.isAutoConsumeAsync = isAutoConsumeAsync;
    }

    /**
     * 清除所有监听器，防止内存泄漏
     * 如果有多个页面使用了支付，需要确保上个页面的cleanListener在下一个页面的GoogleBillingUtil.getInstance()前使用。
     * 所以不建议放在onDestory里调用
     */
    public static void cleanListener() {
        mOnPurchaseFinishedListener = null;
        mOnQueryFinishedListener = null;
        mOnStartSetupFinishedListener = null;
        if (builder != null) {
            builder.setListener(null);
        }
    }

    /**
     * 断开连接google服务
     * 注意！！！一般情况不建议调用该方法，让google保留连接是最好的选择。
     */
    public static void endConnection() {
        //注意！！！一般情况不建议调用该方法，让google保留连接是最好的选择。
        if (mBillingClient != null) {
            if (mBillingClient.isReady()) {
                mBillingClient.endConnection();
                mBillingClient = null;
            }
        }
    }


    /**
     * 验证一个购买到的商品
     * @param purchase
     * @return
     */
    public boolean handlePurchase(Purchase purchase) {
        if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
            Log.i(TAG, "Got a purchase: " + purchase + "; but signature is bad. Skipping...");
            return false;
        }

        Log.d(TAG, "Got a verified purchase: " + purchase);

        return true;
    }


    //验证签名
    private boolean verifyValidSignature(String signedData, String signature) {
        // Some sanity checks to see if the developer (that's you!) really followed the
        // instructions to run this sample (don't put these checks on your app!)
        if (BASE_64_ENCODED_PUBLIC_KEY.contains("CONSTRUCT_YOUR")) {
            throw new RuntimeException("Please update your app's public key at: "
                    + "BASE_64_ENCODED_PUBLIC_KEY");
        }

        try {
            return Security.verifyPurchase(BASE_64_ENCODED_PUBLIC_KEY, signedData, signature);
        } catch (IOException e) {
            Log.e(TAG, "Got an exception trying to validate a purchase: " + e);
            return false;
        }
    }
}
