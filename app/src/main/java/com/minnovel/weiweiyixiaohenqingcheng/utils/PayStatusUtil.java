package com.minnovel.weiweiyixiaohenqingcheng.utils;

import android.content.Context;
import android.util.Log;

public class PayStatusUtil {

    /**
     * 保存订阅的状态
     *
     * @param isOk
     */
    public static void savePaySubStatus(boolean isOk) {
        SharedPreferenceUtil.setBooleanDataIntoSP(Constant.Name_sp.BUY, Constant.Key_sp.SUB_STATUS, isOk);

    }

    /**
     * 获取订阅的状态
     *
     * @return
     */
    public static boolean isSubAvailable() {
        boolean isSubAvailable = SharedPreferenceUtil.getBooleanValueFromSP(Constant.Name_sp.BUY, Constant.Key_sp.SUB_STATUS, false);
        Log.d("wyl", "订阅是否有效：" + isSubAvailable);
        return isSubAvailable;
    }


}
