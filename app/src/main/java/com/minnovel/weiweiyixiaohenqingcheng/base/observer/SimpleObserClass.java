//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.minnovel.weiweiyixiaohenqingcheng.base.observer;

import com.minnovel.weiweiyixiaohenqingcheng.utils.NetworkUtil;

public class SimpleObserClass<T> {
    private int code;
    private T t;

    public SimpleObserClass(T t){
        this(t,NetworkUtil.SUCCESS);
    }

    public SimpleObserClass(T t,int code){
        this.t = t;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Boolean success(){
        return code == NetworkUtil.SUCCESS;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }
}
