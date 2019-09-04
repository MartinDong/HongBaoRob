package com.yu.hongbaorob.service;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

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

            // 查找页面中的 id=com.tencent.mm:id/ar0 ，微信红包，触发点击事件，唤起红包详情
            List<AccessibilityNodeInfo> listHongBao = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ar0");
            if (null != listHongBao && listHongBao.size() > 0) {
                for (AccessibilityNodeInfo accessibilityNodeInfo : listHongBao) {
                    // 判断红包是否已经被领取了
                    List<AccessibilityNodeInfo> status = accessibilityNodeInfo.findAccessibilityNodeInfosByText("已领取");
                    List<AccessibilityNodeInfo> status2 = accessibilityNodeInfo.findAccessibilityNodeInfosByText("已被领完");
                    if (null == status || status.size() > 0 || null == status2 || status2.size() > 0) {
                        continue;
                    }

                    // 判断是否是微信红包
                    List<AccessibilityNodeInfo> status3 = accessibilityNodeInfo.findAccessibilityNodeInfosByText("微信红包");
                    if (null == status3 || status3.size() == 0) {
                        continue;
                    }

                    // 执行点击事件
                    while (null != accessibilityNodeInfo) {
                        if (accessibilityNodeInfo.isClickable()) {
                            accessibilityNodeInfo.performAction(ACTION_CLICK);
                            break;
                        }
                        accessibilityNodeInfo = accessibilityNodeInfo.getParent();
                    }
                }
            }

            // 查找页面中的 id=com.tencent.mm:id/d4h ，触发点击抢红包
            List<AccessibilityNodeInfo> listHongBaoDetial = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/d4h");
            if (null != listHongBaoDetial && listHongBaoDetial.size() > 0) {
                AccessibilityNodeInfo parentHongBaoDetial = listHongBaoDetial.get(listHongBaoDetial.size() - 1);
                while (null != parentHongBaoDetial) {
                    if (parentHongBaoDetial.isClickable()) {
                        parentHongBaoDetial.performAction(ACTION_CLICK);
                        luckyClicked = true;
                        break;
                    }
                    parentHongBaoDetial = parentHongBaoDetial.getParent();
                }
            }

            // 查找页面中的id=com.tencent.mm:id/lb，触发返回
            List<AccessibilityNodeInfo> listBack = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/lb");
            if (null != listBack && listBack.size() > 0) {
                AccessibilityNodeInfo parentlistBack = listBack.get(listBack.size() - 1);
                while (null != parentlistBack) {
                    if (parentlistBack.isClickable() && luckyClicked) {
                        parentlistBack.performAction(ACTION_CLICK);
                        luckyClicked = false;
                        break;
                    }
                    parentlistBack = parentlistBack.getParent();
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
