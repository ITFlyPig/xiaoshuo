//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.minnovel.weiweiyixiaohenqingcheng.presenter;

import com.minnovel.weiweiyixiaohenqingcheng.bean.SearchBookBean;
import com.monke.basemvplib.IPresenter;
import com.minnovel.weiweiyixiaohenqingcheng.bean.SearchBookBean;

public interface IChoiceBookPresenter extends IPresenter{

    int getPage();

    void initPage();

    void toSearchBooks(String key);

    void addBookToShelf(final SearchBookBean searchBookBean);

    String getTitle();
}