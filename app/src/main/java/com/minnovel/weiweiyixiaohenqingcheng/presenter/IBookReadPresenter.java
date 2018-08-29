//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.minnovel.weiweiyixiaohenqingcheng.presenter;

import android.app.Activity;

import com.minnovel.weiweiyixiaohenqingcheng.bean.BookShelfBean;
import com.minnovel.weiweiyixiaohenqingcheng.presenter.impl.ReadBookPresenterImpl;
import com.minnovel.weiweiyixiaohenqingcheng.widget.contentswitchview.BookContentView;
import com.monke.basemvplib.IPresenter;
import com.minnovel.weiweiyixiaohenqingcheng.bean.BookShelfBean;
import com.minnovel.weiweiyixiaohenqingcheng.presenter.impl.ReadBookPresenterImpl;
import com.minnovel.weiweiyixiaohenqingcheng.widget.contentswitchview.BookContentView;

public interface IBookReadPresenter extends IPresenter{

    int getOpen_from();

    BookShelfBean getBookShelf();

    void initContent();

    void loadContent(BookContentView bookContentView, long bookTag, final int chapterIndex, final int page);

    void updateProgress(int chapterIndex, int pageIndex);

    void saveProgress();

    String getChapterTitle(int chapterIndex);

    void setPageLineCount(int pageLineCount);

    void addToShelf(final ReadBookPresenterImpl.OnAddListner addListner);

    Boolean getAdd();

    void initData(Activity activity);

    void openBookFromOther(Activity activity);
}
