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
import com.minnovel.weiweiyixiaohenqingcheng.utils.Logger;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainPresenterImpl extends BasePresenterImpl<IMainView> implements IMainPresenter {


    private boolean mIsRefreshing;

    public void queryBookShelf(final Boolean needRefresh) {
        if (mIsRefreshing) {
            return;
        }
        if (needRefresh)
            mView.activityRefreshView();
        Observable.create(new ObservableOnSubscribe<List<BookShelfBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BookShelfBean>> e) throws Exception {
                mIsRefreshing = true;
//                List<BookShelfBean> bookShelfes = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
                List<BookShelfBean> bookShelfes = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().loadAll();

                for (int i = 0; i < bookShelfes.size(); i++) {
                    List<BookInfoBean> temp = DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().queryBuilder().where(BookInfoBeanDao.Properties.NoteUrl.eq(bookShelfes.get(i).getNoteUrl())).limit(1).build().list();
                    if (temp != null && temp.size() > 0) {
                        BookInfoBean bookInfoBean = temp.get(0);
                        //设置章节信息
//                        bookInfoBean.setChapterlist(DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().queryBuilder().where(ChapterListBeanDao.Properties.NoteUrl.eq(bookShelfes.get(i).getNoteUrl())).orderAsc(ChapterListBeanDao.Properties.DurChapterIndex).build().list());
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
                        mIsRefreshing = false;

                        if (null != value && value.size() > 0) {
                            mView.refreshBookShelf(value);
                            if (needRefresh) {
                                startRefreshBook(value);
                            } else {
                                mView.refreshFinish();

                            }

                            for (BookShelfBean bean : value) {
                                Log.d("mmmm", "查询到的书名：" + bean.getBookInfoBean().getName() + " logo：" + bean.getBookInfoBean().getCoverUrl());

                            }

                        } else {
                            //从本地加载
                            loadNovelsFromAssets();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_ANALY));
                        mIsRefreshing = false;
                    }
                });
    }

    /**
     * 从assets加载小说
     */
    @Override
    public void loadNovelsFromAssets() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                List<BookShelfBean> savedBookShelfes = getAllBookShelfItem();
                //查看数据库中是否已经有数据
                if (savedBookShelfes != null && savedBookShelfes.size() > 0) {//数据库中已加载数据，则不用加载了
                    queryBookShelf(false);//查询之后显示
                    return;
                }

                //从asset中的数据库文件加载信息

                List<NovelBean> novels = getDataInfoFromDB();
                if (novels == null) {
                    return;
                }
                for (NovelBean novel : novels) {
                    String name = novel.name;
                    String path = name;
                    String novelPath = FileUtil.getNovelPath(mView.getContext(), name);
                    Logger.i("tt", "缓存路径：" + novelPath + " asset路径：" + path);
                    if (novelPath == null) {
                        return;
                    }
//                    FileUtil.copyFileFromAssets(mView.getContext(), path, novelPath);
                    Logger.i("tt", "copy之后的文件大小：" + new File(novelPath).length());
                    novel.path = novelPath;
                    ImportBookModelImpl.getInstance().importBook(novel)
                            .subscribe(new SimpleObserver<LocBookShelfBean>() {
                                @Override
                                public void onNext(LocBookShelfBean value) {
                                    if (value.getNew())
                                        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);//新加入

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
     *
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
                Log.d("wyl", "获得小说数据：" + bookName + " " + novelBean.name + " 封面数据：" + logo);

            }
            return noves;
        }
        return null;

    }


    /**
     * 从数据库查询所有的书架上的item
     *
     * @return
     */
    private List<BookShelfBean> getAllBookShelfItem() {
        List<BookShelfBean> bookShelfes = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().loadAll();
        return bookShelfes;
    }

    /**
     * 判断小说是否已经准备好，比如说拷贝数据和解析章节信息
     * @return
     */
    private boolean isBookReady(String bookPath) {
        if (TextUtils.isEmpty(bookPath)) {//咩有获取到小说的信息
            //TODO 据名字获取小说的路径
            return false;
        }
        File bookFile = new File(bookPath);
        if (bookFile.length() <= 0) {//小说还没从asset拷贝到内存
            return false;
        }
        return true;

    }

    /**
     * 真正的拷贝和解析小说章节信息
     */
    @Override
    public void parseBookInfo(String bookPath) {
        if (TextUtils.isEmpty(bookPath)) {
            Logger.i("tt", "parseBookInfo 小说路径为空");
            return;
        }

        Logger.i("tt", "parseBookInfo 小说路径:" + bookPath);

        if (isBookReady(bookPath)) {
            return;

        }
        //拷贝小说从asset到内存
        String txtName = FileUtil.getFileName(bookPath);
        Logger.i("tt", "parseBookInfo 获取到的文件名字：" + txtName);
        FileUtil.copyFileFromAssets(mView.getContext(), txtName, bookPath);

        File tem = new File(bookPath);
        Logger.i("tt", "parseBookInfo 拷贝后的数据大小：" + tem.length());

        //解析章节信息
        try {
            ImportBookModelImpl.getInstance().saveChapter(new File(bookPath), bookPath);//将md5换成了小说的路径
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.i("tt", "parseBookInfo 小说信息解析完成");

    }
}
