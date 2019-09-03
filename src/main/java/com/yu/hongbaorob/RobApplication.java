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

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
    }
}
