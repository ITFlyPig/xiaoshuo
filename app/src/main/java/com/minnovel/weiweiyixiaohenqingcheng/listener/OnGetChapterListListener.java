//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.minnovel.weiweiyixiaohenqingcheng.listener;

import com.minnovel.weiweiyixiaohenqingcheng.bean.BookShelfBean;
import com.minnovel.weiweiyixiaohenqingcheng.bean.BookShelfBean;

public interface OnGetChapterListListener {
    public void success(BookShelfBean bookShelfBean);
    public void error();
}
