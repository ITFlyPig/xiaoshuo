//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.minnovel.weiweiyixiaohenqingcheng.presenter.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.minnovel.weiweiyixiaohenqingcheng.bean.BookShelfBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.LocBookShelfBean;
import com.minnovel.weiweiyixiaohenqingcheng.dao.BookInfoBeanDao;
import com.minnovel.weiweiyixiaohenqingcheng.dao.BookShelfBeanDao;
import com.minnovel.weiweiyixiaohenqingcheng.dao.ChapterListBeanDao;
import com.minnovel.weiweiyixiaohenqingcheng.dao.DbHelper;
import com.minnovel.weiweiyixiaohenqingcheng.utils.FileUtil;
import com.monke.basemvplib.IView;
import com.monke.basemvplib.impl.BasePresenterImpl;
import com.minnovel.weiweiyixiaohenqingcheng.MApplication;
import com.minnovel.weiweiyixiaohenqingcheng.base.observer.SimpleObserver;
import com.minnovel.weiweiyixiaohenqingcheng.bean.BookInfoBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.BookShelfBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.LocBookShelfBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.NovelBean;
import com.minnovel.weiweiyixiaohenqingcheng.common.RxBusTag;
import com.minnovel.weiweiyixiaohenqingcheng.dao.AssetsDatabaseManager;
import com.minnovel.weiweiyixiaohenqingcheng.dao.BookInfoBeanDao;
import com.minnovel.weiweiyixiaohenqingcheng.dao.BookShelfBeanDao;
import com.minnovel.weiweiyixiaohenqingcheng.dao.ChapterListBeanDao;
import com.minnovel.weiweiyixiaohenqingcheng.dao.DbHelper;
import com.minnovel.weiweiyixiaohenqingcheng.listener.OnGetChapterListListener;
import com.minnovel.weiweiyixiaohenqingcheng.model.impl.ImportBookModelImpl;
import com.minnovel.weiweiyixiaohenqingcheng.model.impl.WebBookModelImpl;
import com.minnovel.weiweiyixiaohenqingcheng.presenter.IMainPresenter;
import com.minnovel.weiweiyixiaohenqingcheng.utils.FileUtil;
import com.minnovel.weiweiyixiaohenqingcheng.utils.NetworkUtil;
import com.minnovel.weiweiyixiaohenqingcheng.view.IMainView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainPresenterImpl extends BasePresenterImpl<IMainView> implements IMainPresenter {

    public void queryBookShelf(final Boolean needRefresh) {
        if (needRefresh)
            mView.activityRefreshView();
        Observable.create(new ObservableOnSubscribe<List<BookShelfBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BookShelfBean>> e) throws Exception {
                List<BookShelfBean> bookShelfes = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
                for (int i = 0; i < bookShelfes.size(); i++) {
                    List<BookInfoBean> temp = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder().where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfes.get(i).getNoteUrl())).limit(1).build().list();
                    if (temp != null && temp.size() > 0) {
                        BookInfoBean bookInfoBean = temp.get(0);
                        bookInfoBean.setChapterlist(DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().queryBuilder().where(ChapterListBeanDao.Properties.NoteUrl.eq(bookShelfes.get(i).getNoteUrl())).orderAsc(ChapterListBeanDao.Properties.DurChapterIndex).build().list());
                        bookShelfes.get(i).setBookInfoBean(bookInfoBean);
                    } else {
                        DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().delete(bookShelfes.get(i));
                        bookShelfes.remove(i);
                        i--;
                    }
                }
                e.onNext(bookShelfes == null ? new ArrayList<BookShelfBean>() : bookShelfes);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<BookShelfBean>>() {
                    @Override
                    public void onNext(List<BookShelfBean> value) {
                        if (null != value) {
                            mView.refreshBookShelf(value);
                            if (needRefresh) {
                                startRefreshBook(value);
                            } else {
                                mView.refreshFinish();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_ANALY));
                    }
                });
    }

    /**
     * 从assets加载小说
     */
    @Override
    public void loadNovelsFromAssets() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                List<NovelBean> novels = getDataInfoFromDB();
                if (novels == null) {
                    return;
                }
                for (NovelBean novel : novels) {
                    String name = novel.name;
                    String path = name;
                    String novelPath = FileUtil.getNovelPath(mView.getContext(), name);
                    Log.d("wyl", "缓存路径：" + novelPath + " asset路径：" + path);
                    if (novelPath == null) {
                        return;
                    }
                    FileUtil.copyFileFromAssets(mView.getContext(), path, novelPath);
                    Log.d("wyl", "copy之后的文件大小：" + new File(novelPath).length());
                    novel.path = novelPath;
                    ImportBookModelImpl.getInstance().importBook(novel)
                            .subscribe(new SimpleObserver<LocBookShelfBean>() {
                                @Override
                                public void onNext(LocBookShelfBean value) {
                                    if (value.getNew())
                                        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);

                                    queryBookShelf(false);


                                }

                                @Override
                                public void onError(Throwable e) {
                                }
                            });

                }

            }
        }.start();







    }

    public void startRefreshBook(List<BookShelfBean> value) {
        if (value != null && value.size() > 0) {
            mView.setRecyclerMaxProgress(value.size());
            refreshBookShelf(value, 0);
        } else {
            mView.refreshFinish();
        }
    }

    private void refreshBookShelf(final List<BookShelfBean> value, final int index) {
        if (index <= value.size() - 1) {
            WebBookModelImpl.getInstance().getChapterList(value.get(index), new OnGetChapterListListener() {
                @Override
                public void success(BookShelfBean bookShelfBean) {
                    saveBookToShelf(value, index);
                }

                @Override
                public void error() {
                    mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_NONET));
                }
            });
        } else {
            queryBookShelf(false);
        }
    }

    private void saveBookToShelf(final List<BookShelfBean> datas, final int index) {
        Observable.create(new ObservableOnSubscribe<BookShelfBean>() {
            @Override
            public void subscribe(ObservableEmitter<BookShelfBean> e) throws Exception {
                DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplaceInTx(datas.get(index).getBookInfoBean().getChapterlist());
                e.onNext(datas.get(index));
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean value) {
                        mView.refreshRecyclerViewItemAdd();
                        refreshBookShelf(datas, index + 1);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_NONET));
                    }
                });
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_ADD_BOOK),
                    @Tag(RxBusTag.HAD_REMOVE_BOOK),
                    @Tag(RxBusTag.UPDATE_BOOK_PROGRESS)
            }
    )
    public void hadddOrRemoveBook(BookShelfBean bookShelfBean) {
        queryBookShelf(false);
    }


    /**
     * 从数据库获取信息
     * @return
     */
    private List<NovelBean> getDataInfoFromDB() {
        // 获取管理对象，因为数据库需要通过管理对象才能够获取
        AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
        // 通过管理对象获取数据库
        SQLiteDatabase db = mg.getDatabase("data.db");
        // 对数据库进行操作
        if (db == null) {
            return null;
        }
        Cursor cursor = db.rawQuery("select * from data", null);
        if (cursor != null && cursor.getCount() > 0) {
            List<NovelBean> noves = new ArrayList<>();
            while (cursor.moveToNext()) {
                NovelBean novelBean = new NovelBean();
                String logo = cursor.getString(cursor.getColumnIndex("logo_id"));
                novelBean.coverName = logo;
                String txtName = cursor.getString(cursor.getColumnIndex("txt_name"));
                String bookName = cursor.getString(cursor.getColumnIndex("book_name"));
                novelBean.showName = bookName;
                novelBean.name = txtName;
                if (!TextUtils.isEmpty(novelBean.name)) {
                    novelBean.name = novelBean.name + ".txt";
                }
                noves.add(novelBean);
                Log.d("wyl", "获得小说数据：" + bookName + " " + novelBean.name);

            }
            return noves;
        }
        return null;

    }

}
