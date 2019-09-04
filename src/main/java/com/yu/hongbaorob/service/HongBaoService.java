package com.yu.hongbaorob.service;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.yu.hongbaorob.R;

import java.util.List;

import static android.view.accessibility.AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK;

/**
 * author : donghongyu
 * e-mail : 1358506549@qq.com
 * date   : 2019-09-0311:43
 * desc   : 监控正在聊天的页面
 * version: 1.0
 */
public class HongBaoService extends AccessibilityService {
    private static final int MSG_BACK = 233;

    private static final String UI_RECEIVE = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    private static final String UI_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";

    private boolean luckyClicked, hasLucky;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_BACK) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                postDelayed(() -> performGlobalAction(GLOBAL_ACTION_BACK), 1000);
            }
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();

        if (eventType == TYPE_NOTIFICATION_STATE_CHANGED) {
            unlockScreen();
            luckyClicked = false;
        }

        if (eventType == TYPE_WINDOW_CONTENT_CHANGED) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (null == rootNode) return;

            // 查找页面中的 id=com.tencent.mm:id/as8 ，微信红包，触发点击事件，唤起红包详情
            List<AccessibilityNodeInfo> listHongBao = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/as8");
            if (null != listHongBao && listHongBao.size() > 0) {
                AccessibilityNodeInfo parent = listHongBao.get(listHongBao.size() - 1);
                while (null != parent) {
                    if (parent.isClickable()) {
                        parent.performAction(ACTION_CLICK);
                        break;
                    }
                    parent = parent.getParent();
                }
            }

            // 查找页面中的 id=com.tencent.mm:id/d4h ，触发点击抢红包
            List<AccessibilityNodeInfo> listHongBaoDetial = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/d4h");
            if (null != listHongBaoDetial && listHongBaoDetial.size() > 0) {
                AccessibilityNodeInfo parent = listHongBaoDetial.get(listHongBaoDetial.size() - 1);
                while (null != parent) {
                    if (parent.isClickable() ) {
                        parent.performAction(ACTION_CLICK);
                        break;
                    }
                    parent = parent.getParent();
                }
            }

            // 查找页面中的id=com.tencent.mm:id/lb，触发返回
            List<AccessibilityNodeInfo> listBack = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/lb");
            if (null != listBack && listBack.size() > 0) {
                AccessibilityNodeInfo parent = listBack.get(listBack.size() - 1);
                while (null != parent) {
                    if (parent.isClickable()) {
                        parent.performAction(ACTION_CLICK);
                        break;
                    }
                    parent = parent.getParent();
                }
            }

        }

        if (eventType == TYPE_WINDOW_STATE_CHANGED) {
            String clazzName = event.getClassName().toString();
            if (clazzName.equals(UI_RECEIVE)) {
                traverseNode(event.getSource());
            }

            if (clazzName.equals(UI_DETAIL) && hasLucky) {
                hasLucky = false;
                handler.sendEmptyMessageDelayed(MSG_BACK, 1000);
            }
        }
    }

    private void traverseNode(AccessibilityNodeInfo node) {
        if (null == node) return;
        final int count = node.getChildCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                AccessibilityNodeInfo childNode = node.getChild(i);
                if (null != childNode && childNode.getClassName().equals("android.widget.Button") && childNode.isClickable()) {
                    childNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    hasLucky = true;
                }

                traverseNode(childNode);
            }
        }
    }

    private void unlockScreen() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("MyKeyguardLock");
        keyguardLock.disableKeyguard();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        // 创建唤醒锁
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");

        // 获得唤醒锁
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);

        // 释放唤醒锁, 如果没有其它唤醒锁存在, 设备会很快进入休眠状态
        wakeLock.release();
    }

    @Override
    public void onInterrupt() {

    }
}
