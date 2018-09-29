package com.minnovel.weiweiyixiaohenqingcheng.utils;

import android.content.Context;

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
        return SharedPreferenceUtil.getBooleanValueFromSP(Constant.Name_sp.BUY, Constant.Key_sp.SUB_STATUS, false);
    }


}
