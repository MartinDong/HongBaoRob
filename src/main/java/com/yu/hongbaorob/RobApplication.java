package com.yu.hongbaorob;

import android.app.Application;

/**
 * author : donghongyu
 * e-mail : 1358506549@qq.com
 * date   : 2019-09-0312:41
 * desc   :
 * version: 1.0
 */
public class RobApplication extends Application {

    public static RobApplication mApplication;
    // 用于调整两个RecyclerView高度
    public static int layoutHeight = -1;
    // 检查权限按钮的点击时间
    public static long timeCheckAccessibilityServiceIsWorking = 0;
    public static long timeCheckNotificationListenerServiceIsWorking = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
    }
}
