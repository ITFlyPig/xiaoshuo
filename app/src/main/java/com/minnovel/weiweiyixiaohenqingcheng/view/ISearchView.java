//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.minnovel.weiweiyixiaohenqingcheng.view;

import android.widget.EditText;

import com.minnovel.weiweiyixiaohenqingcheng.bean.SearchBookBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.SearchHistoryBean;
import com.minnovel.weiweiyixiaohenqingcheng.view.adapter.SearchBookAdapter;
import com.monke.basemvplib.IView;
import com.minnovel.weiweiyixiaohenqingcheng.bean.SearchBookBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.SearchHistoryBean;
import com.minnovel.weiweiyixiaohenqingcheng.view.adapter.SearchBookAdapter;
import java.util.List;

public interface ISearchView extends IView{

    /**
     * 成功 新增查询记录
     * @param searchHistoryBean
     */
    void insertSearchHistorySuccess(SearchHistoryBean searchHistoryBean);

    /**
     * 成功搜索 搜索记录
     * @param datas
     */
    void querySearchHistorySuccess(List<SearchHistoryBean> datas);

    /**
     * 首次查询成功 更新UI
     * @param books
     */
    void refreshSearchBook(List<SearchBookBean> books);

    /**
     * 加载更多书籍成功 更新UI
     * @param books
     */
    void loadMoreSearchBook(List<SearchBookBean> books);

    /**
     * 刷新成功
     * @param isAll
     */
    void refreshFinish(Boolean isAll);

    /**
     * 加载成功
     * @param isAll
     */
    void loadMoreFinish(Boolean isAll);

    /**
     * 搜索失败
     * @param isRefresh
     */
    void searchBookError(Boolean isRefresh);

    /**
     * 获取搜索内容EditText
     * @return
     */
    EditText getEdtContent();

    /**
     * 添加书籍失败
     * @param code
     */
    void addBookShelfFailed(int code);

    SearchBookAdapter getSearchBookAdapter();

    void updateSearchItem(int index);

    /**
     * 判断书籍是否已经在书架上
     * @param searchBookBean
     * @return
     */
    Boolean checkIsExist(SearchBookBean searchBookBean);
}
