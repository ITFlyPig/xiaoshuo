//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.minnovel.weiweiyixiaohenqingcheng.view.impl;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.Purchase;
import com.android.tu.loadingdialog.LoadingDailog;
import com.minnovel.weiweiyixiaohenqingcheng.BitIntentDataManager;
import com.minnovel.weiweiyixiaohenqingcheng.MApplication;
import com.minnovel.weiweiyixiaohenqingcheng.R;
import com.minnovel.weiweiyixiaohenqingcheng.base.MBaseActivity;
import com.minnovel.weiweiyixiaohenqingcheng.bean.BookInfoBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.BookShelfBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.ChapterListBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.PayStatusEvent;
import com.minnovel.weiweiyixiaohenqingcheng.dao.AssetsDatabaseManager;
import com.minnovel.weiweiyixiaohenqingcheng.dao.ChapterListBeanDao;
import com.minnovel.weiweiyixiaohenqingcheng.dao.DbHelper;
import com.minnovel.weiweiyixiaohenqingcheng.presenter.IMainPresenter;
import com.minnovel.weiweiyixiaohenqingcheng.presenter.impl.BookDetailPresenterImpl;
import com.minnovel.weiweiyixiaohenqingcheng.presenter.impl.MainPresenterImpl;
import com.minnovel.weiweiyixiaohenqingcheng.presenter.impl.ReadBookPresenterImpl;
import com.minnovel.weiweiyixiaohenqingcheng.utils.GoogleBillingUtil;
import com.minnovel.weiweiyixiaohenqingcheng.utils.PayStatusUtil;
import com.minnovel.weiweiyixiaohenqingcheng.view.IMainView;
import com.minnovel.weiweiyixiaohenqingcheng.view.adapter.BookShelfAdapter;
import com.minnovel.weiweiyixiaohenqingcheng.view.popupwindow.DownloadListPop;
import com.minnovel.weiweiyixiaohenqingcheng.widget.refreshview.OnRefreshWithProgressListener;
import com.minnovel.weiweiyixiaohenqingcheng.widget.refreshview.RefreshRecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class MainActivity extends MBaseActivity<IMainPresenter> implements IMainView, View.OnClickListener {
    private ImageButton ibMoney;
    private ImageButton ibLibrary;
    private ImageButton ibAdd;
    private ImageButton ibDownload;

    private RefreshRecyclerView rfRvShelf;
    private BookShelfAdapter bookShelfAdapter;

    private FrameLayout flWarn;
    private ImageView ivWarnClose;

    private DownloadListPop downloadListPop;
    private static GoogleBillingUtil googleBillingUtil;
    private static MyOnStartSetupFinishedListener mOnStartSetupFinishedListener = new MyOnStartSetupFinishedListener();//启动结果回调接口
    private static MyOnPurchaseFinishedListener mOnPurchaseFinishedListener = new MyOnPurchaseFinishedListener();//购买回调接口

    private View vRemoveAd;
    private TextView tvTitle;
    private TextView tvRate;

    @Override
    protected IMainPresenter initInjector() {
        return new MainPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        // 初始化，只需要调用一次
        AssetsDatabaseManager.initManager(getApplication());
        setContentView(R.layout.activity_main);

        checkSub();

        showTip();
    }

    @Override
    protected void initData() {
        bookShelfAdapter = new BookShelfAdapter();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void bindView() {
        downloadListPop = new DownloadListPop(MainActivity.this);

        rfRvShelf = (RefreshRecyclerView) findViewById(R.id.rf_rv_shelf);

        ibMoney = (ImageButton) findViewById(R.id.ib_money);
        ibLibrary = (ImageButton) findViewById(R.id.ib_library);
        ibAdd = (ImageButton) findViewById(R.id.ib_add);
        ibDownload = (ImageButton) findViewById(R.id.ib_download);

        rfRvShelf.setRefreshRecyclerViewAdapter(bookShelfAdapter, new LinearLayoutManager(this));

        flWarn = (FrameLayout) findViewById(R.id.fl_warn);
        ivWarnClose = (ImageView) findViewById(R.id.iv_warn_close);
        vRemoveAd = findViewById(R.id.tv_remove_id);
        vRemoveAd.setOnClickListener(this);
        tvTitle = (TextView) vRemoveAd.findViewById(R.id.tv_title);
        tvRate = (TextView) findViewById(R.id.tv_rate);
        tvRate.setOnClickListener(this);

    }

    @Override
    protected void bindEvent() {
        bindRvShelfEvent();
        ibDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadListPop.showAsDropDown(ibDownload);
            }
        });
        ibMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击打赏
            }
        });
        ibLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityByAnim(new Intent(MainActivity.this, LibraryActivity.class), 0, 0);
            }
        });
        ibAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mPresenter.loadNovelsFromAssets();
                //点击更多
//                startActivityByAnim(new Intent(MainActivity.this, ImportBookActivity.class), 0, 0);
            }
        });
        bookShelfAdapter.setItemClickListener(new BookShelfAdapter.OnItemClickListener() {
            @Override
            public void toSearch() {
                //点击去选书
                startActivityByAnim(new Intent(MainActivity.this, LibraryActivity.class), 0, 0);
            }

            @Override
            public void onClick(final BookShelfBean bookShelfBean, int index) {
                if (bookShelfBean == null) {
                    return;
                }


                LoadingDailog.Builder loadBuilder = new LoadingDailog.Builder(getContext())
                        .setMessage("为您解析中...")
                        .setCancelable(true)
                        .setCancelOutside(false);
                final LoadingDailog dialog = loadBuilder.create();
                dialog.show();

                final BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
                if (bookInfoBean == null) {
                    return;
                }

                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        mPresenter.parseBookInfo(bookInfoBean.getNoteUrl());
                        //获取一次章节信息
                        if (bookInfoBean.getChapterlist() == null || bookInfoBean.getChapterlist().size() <= 0) {
                            List<ChapterListBean> chapters = DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().queryBuilder().where(ChapterListBeanDao.Properties.NoteUrl.eq(bookShelfBean.getNoteUrl())).orderAsc(ChapterListBeanDao.Properties.DurChapterIndex).build().list();
                            bookInfoBean.setChapterlist(chapters);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(MainActivity.this, ReadBookActivity.class);
                                intent.putExtra("from", ReadBookPresenterImpl.OPEN_FROM_APP);
                                String key = String.valueOf(System.currentTimeMillis());
                                intent.putExtra("data_key", key);
                                try {
                                    BitIntentDataManager.getInstance().putData(key, bookShelfBean.clone());
                                } catch (CloneNotSupportedException e) {
                                    BitIntentDataManager.getInstance().putData(key, bookShelfBean);
                                    e.printStackTrace();
                                }
                                startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
                                dialog.cancel();
                            }
                        });

                    }
                }.start();


//
//

            }

            @Override
            public void onLongClick(View animView, BookShelfBean bookShelfBean, int index) {
                Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
                intent.putExtra("from", BookDetailPresenterImpl.FROM_BOOKSHELF);
                String key = String.valueOf(System.currentTimeMillis());
                intent.putExtra("data_key", key);
                BitIntentDataManager.getInstance().putData(key, bookShelfBean);
                startActivityByAnim(intent, animView, "img_cover", android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        ivWarnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flWarn.setVisibility(View.GONE);
            }
        });
    }

    private void bindRvShelfEvent() {
        rfRvShelf.setBaseRefreshListener(new OnRefreshWithProgressListener() {
            @Override
            public int getMaxProgress() {
                return bookShelfAdapter.getBooks().size();
            }

            @Override
            public void startRefresh() {
                mPresenter.queryBookShelf(true);
            }
        });
    }

    @Override
    protected void firstRequest() {
        //通过百度API 判断是否有更新
//        try {
//            BDAutoUpdateSDK.uiUpdateAction(this, new UICheckUpdateCallback() {
//                @Override
//                public void onNoUpdateFound() {
//
//                }
//
//                @Override
//                public void onCheckComplete() {
//
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        mPresenter.queryBookShelf(false);
    }

    @Override
    public void refreshBookShelf(List<BookShelfBean> bookShelfBeanList) {
        bookShelfAdapter.replaceAll(bookShelfBeanList);
    }

    @Override
    public void activityRefreshView() {
        //执行刷新响应
        rfRvShelf.startRefresh();
    }

    @Override
    public void refreshFinish() {
        rfRvShelf.finishRefresh(false, true);
    }

    @Override
    public void refreshError(String error) {
        refreshFinish();
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refreshRecyclerViewItemAdd() {
        rfRvShelf.getRpb().setDurProgress(rfRvShelf.getRpb().getDurProgress() + 1);
    }

    @Override
    public void setRecyclerMaxProgress(int x) {
        rfRvShelf.getRpb().setMaxProgress(x);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadListPop.onDestroy();
    }

    private long exitTime = 0;

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

    /**
     * 检查有效订阅
     */
    public static void checkSub() {


        GoogleBillingUtil.cleanListener();
        if (GoogleBillingUtil.getInstance().isReady()) {
            handleSubSize();
        } else {
            googleBillingUtil = GoogleBillingUtil.getInstance()
                    .setOnStartSetupFinishedListener(mOnStartSetupFinishedListener)
                    .setOnPurchaseFinishedListener(mOnPurchaseFinishedListener)
                    .build();
        }


    }

    private static void handleSubSize() {
        int size = googleBillingUtil.getPurchasesSizeSubs();

//        Toast.makeText(MApplication.getInstance(), "有效订阅的数量：" + size, Toast.LENGTH_LONG).show();

        if (size > 0) {
            PayStatusUtil.savePaySubStatus(true);
        } else {
            PayStatusUtil.savePaySubStatus(false);
        }

        EventBus.getDefault().post(new PayStatusEvent());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_remove_id:
                Intent intent = new Intent(this, DownProApp.class);
                startActivity(intent);
                break;
            case R.id.tv_rate:
                rateApp();
                break;
        }
    }

    //服务初始化结果回调接口
    private static class MyOnStartSetupFinishedListener implements GoogleBillingUtil.OnStartSetupFinishedListener {
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
            Log.d("wyl", "已经订阅的商品：" + (subs == null ? 0 : subs.size()));

            if (subs != null) {
                for (Purchase sub : subs) {
                    Log.d("wyl", "已经订阅的商品：" + sub.getSku());
                }
            }

            handleSubSize();

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
    private static class MyOnPurchaseFinishedListener implements GoogleBillingUtil.OnPurchaseFinishedListener {
        @Override
        public void onPurchaseSuccess(List<Purchase> list) {
            //内购或者订阅成功,可以通过purchase.getSku()获取suk进而来判断是哪个商品
            Log.d("wyl", "购买商品回调接口 onPurchaseSuccess");

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
    protected void onResume() {
        super.onResume();
        checkSub();
    }

    /**
     * 给APP评分
     */
    private void rateApp() {
        //这里开始执行一个应用市场跳转逻辑，默认this为Context上下文对象
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.minnovel.weiweiyixiaohenqingcheng")); //跳转到应用市场，非Google Play市场一般情况也实现了这个接口
        if (intent.resolveActivity(getPackageManager()) != null) { //可以接收
            startActivity(intent);
        } else { //没有应用市场，我们通过浏览器跳转到Google Play
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.minnovel.weiweiyixiaohenqingcheng"));
            if (intent.resolveActivity(getPackageManager()) != null) { //有浏览器
                startActivity(intent);
            } else {
                //TODO 提示用户

            }
        }

    }


    private Dialog dialog;
    private View rating_but;
    private View watch_video_ad_but;
    private void showDialog(){
        dialog= new Dialog(getContext(),R.style.dialog);
        View view = View.inflate(getContext(), R.layout.dialogunlock, null);
        rating_but = view.findViewById(R.id.rating_but);
        watch_video_ad_but = (TextView)view.findViewById(R.id.watch_video_ad_but);
        watch_video_ad_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (dialog != null) {
                   dialog.dismiss();
               }

            }
        });
        rating_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                Intent intent = new Intent(MainActivity.this, DownProApp.class);
                startActivity(intent);
            }
        });

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.BOTTOM);
        lp.width = LinearLayout.LayoutParams.FILL_PARENT;
        lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);
        //点击dialog之外的区域禁止取消dialog
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }

    /**
     * 弹出提示框
     */
    private void showTip() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showDialog();
            }
        }, 60 * 1000);

    }
}