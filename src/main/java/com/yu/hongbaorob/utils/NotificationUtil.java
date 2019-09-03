package com.yu.hongbaorob.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import com.yu.hongbaorob.R;

/**
 * author : donghongyu
 * e-mail : 1358506549@qq.com
 * date   : 2019-09-0319:57
 * desc   :
 * version: 1.0
 */
public class NotificationUtil {
    public static void sendNotification(FragmentActivity activity, String title, String content) {
        if (activity == null)
            return;
        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1", "1", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(activity, "1")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setDefaults(Notification.FLAG_ONLY_ALERT_ONCE)
                .build();

        manager.notify(12, notification);
    }
}
