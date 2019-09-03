package com.yu.hongbaorob.service;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * author : donghongyu
 * e-mail : 1358506549@qq.com
 * date   : 2019-09-0311:43
 * desc   : 监控正在聊天的页面
 * version: 1.0
 */
public class HongBaoService extends AccessibilityService {
    private final String TAG = getClass().getName();

    public static HongBaoService mService;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mService = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.e(TAG, "--onAccessibilityEvent--" + accessibilityEvent);

    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "--onInterrupt--" + "(；′⌒`)\r\n红包功能被迫中断");
        mService = null;
    }

    /**
     * 辅助功能是否启动
     */
    public static boolean isStart() {
        return mService != null;
    }
}
