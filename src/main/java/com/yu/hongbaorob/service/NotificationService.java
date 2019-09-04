package com.yu.hongbaorob.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * author : donghongyu
 * e-mail : 1358506549@qq.com
 * date   : 2019-09-0315:59
 * desc   : 监听通知
 * version: 1.0
 */
public class NotificationService extends NotificationListenerService {
    private final String TAG = getClass().getName();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if (null == notification) return;

        Bundle extras = notification.extras;
        if (null == extras) return;

        List<String> textList = new ArrayList<>();
        String title = extras.getString("android.title");
        if (!isEmpty(title)) textList.add(title);

        String detailText = extras.getString("android.text");
        if (!isEmpty(detailText)) textList.add(detailText);

        if (textList.size() == 0) return;
        for (String text : textList) {
            if (!isEmpty(text) && text.contains("[微信红包]")) {
                Log.e(TAG,"---红包来啦🧧---");
                final PendingIntent pendingIntent = notification.contentIntent;
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }
}
