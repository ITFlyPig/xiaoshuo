//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.minnovel.weiweiyixiaohenqingcheng.view;

import com.minnovel.weiweiyixiaohenqingcheng.bean.SearchBookBean;
import com.minnovel.weiweiyixiaohenqingcheng.view.adapter.ChoiceBookAdapter;
import com.monke.basemvplib.IView;
import com.minnovel.weiweiyixiaohenqingcheng.bean.SearchBookBean;
import com.minnovel.weiweiyixiaohenqingcheng.view.adapter.ChoiceBookAdapter;
import java.util.List;

public interface IChoiceBookView extends IView{

    void refreshSearchBook(List<SearchBookBean> books);

    void loadMoreSearchBook(List<SearchBookBean> books);

    void refreshFinish(Boolean isAll);

    void loadMoreFinish(Boolean isAll);

    void searchBookError();

    void addBookShelfSuccess(List<SearchBookBean> searchBooks);

    void addBookShelfFailed(int code);

    ChoiceBookAdapter getSearchBookAdapter();

    void updateSearchItem(int index);

    void startRefreshAnim();
}
