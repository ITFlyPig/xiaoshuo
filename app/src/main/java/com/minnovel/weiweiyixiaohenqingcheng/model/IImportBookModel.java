//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.minnovel.weiweiyixiaohenqingcheng.model;

import com.minnovel.weiweiyixiaohenqingcheng.bean.LocBookShelfBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.LocBookShelfBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.NovelBean;

import java.io.File;
import io.reactivex.Observable;

public interface IImportBookModel {

    Observable<LocBookShelfBean> importBook(File book);
    Observable<LocBookShelfBean> importBook(NovelBean novelBean);
}
