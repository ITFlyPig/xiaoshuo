package com.minnovel.weiweiyixiaohenqingcheng.bean;

/**
 * Created by 作者：${zhangzhizhen} on 2017/6/21.
 * Email: 190042477@qq.com
 */

public  class MessageEvents {


    public Boolean isConnect;

    public Boolean getConnect() {
        return isConnect;
    }

    public void setConnect(Boolean connect) {
        isConnect = connect;
    }
    public MessageEvents(boolean isConnect){
        this.isConnect = isConnect;
    }
}
