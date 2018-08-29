//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.minnovel.weiweiyixiaohenqingcheng.model;

import com.minnovel.weiweiyixiaohenqingcheng.bean.BookContentBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.BookShelfBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.SearchBookBean;
import com.minnovel.weiweiyixiaohenqingcheng.listener.OnGetChapterListListener;
import com.minnovel.weiweiyixiaohenqingcheng.bean.BookContentBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.BookShelfBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.SearchBookBean;
import com.minnovel.weiweiyixiaohenqingcheng.listener.OnGetChapterListListener;
import java.util.List;
import io.reactivex.Observable;

public interface IStationBookModel {

    /**
     * 搜索书籍
     */
    Observable<List<SearchBookBean>> searchBook(String content, int page);

    /**
     * 网络请求并解析书籍信息
     */
    Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean);

    /**
     * 网络解析图书目录
     */
    void getChapterList(final BookShelfBean bookShelfBean, OnGetChapterListListener getChapterListListener);

    /**
     * 章节缓存
     */
    Observable<BookContentBean> getBookContent(final String durChapterUrl, final int durChapterIndex);
}
