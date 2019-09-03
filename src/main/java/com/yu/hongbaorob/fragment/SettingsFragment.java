package com.yu.hongbaorob.fragment;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.yu.hongbaorob.R;
import com.yu.hongbaorob.RobApplication;
import com.yu.hongbaorob.service.HongBaoService;
import com.yu.hongbaorob.widget.MySwitchCompat;

import java.io.File;
import java.util.Date;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import ezy.assist.compat.SettingsCompat;

/**
 * @author donghongyu
 * @date 2019-09-02
 * @desc 设置页面
 */
public class SettingsFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    private String TAG = "SettingsFragment";
    private Handler handler = new Handler();
    private long clickTime = new Date().getTime();
    private int clicks = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        ScrollView view = (ScrollView) inflater.inflate(R.layout.fragment_settings, container, false);

        initPermissionCheck(view);

        initSettings(view);

        initAbout(view);

        return view;
    }

    private void initSettings(ScrollView view) {
        // 设置
        ((MySwitchCompat) view.findViewById(R.id.switch_show_all_qq_messages))
                .setAttr(RobApplication.class, "isShowAllQQMessages")
                .setOnCheckedChangeListener((compoundButton, b) -> RobApplication.layoutHeight = -1);
        ((MySwitchCompat) view.findViewById(R.id.switch_we_chat_auto_login))
                .setAttr(RobApplication.class, "isWeChatAutoLogin");
        ((MySwitchCompat) view.findViewById(R.id.switch_swipe_remove_on))
                .setAttr(RobApplication.class, "isSwipeRemoveOn");
        ((MySwitchCompat) view.findViewById(R.id.switch_check_update_only_on_wifi))
                .setAttr(RobApplication.class, "isCheckUpdateOnlyOnWiFi");
    }

    private void initPermissionCheck(ScrollView view) {
        // 跳转
        View btnAccessibilityService = view.findViewById(R.id.btn_navigate_accessibility_service);
        View btnNotificationListener = view.findViewById(R.id.btn_navigate_notification_listener);
        View btnOverlays = view.findViewById(R.id.btn_navigate_overlays);

        btnAccessibilityService.setOnClickListener(v -> jumpToAccessSetting());

        btnNotificationListener.setOnClickListener(v -> jumpToNotificationListenerSetting());

        btnOverlays.setOnClickListener(v -> SettingsCompat.manageDrawOverlays(getActivity()));

        // 检查权限
        CircularProgressButton btnCheckPermission = view.findViewById(R.id.btn_check_permission);
        btnCheckPermission.setOnClickListener(v -> {
            btnCheckPermission.performAccessibilityAction(AccessibilityEvent.TYPE_VIEW_CLICKED, null);
            btnCheckPermission.startAnimation();
            ViewGroup llPermission = view.findViewById(R.id.ll_permission);
            llPermission.removeAllViews();
            llPermission.addView((View) btnCheckPermission.getParent());

            boolean accessibilityServiceSettingEnabled = isAccessibilityServiceSettingEnabled();
            boolean notificationListenerSettingEnabled = isNotificationListenerSettingEnabled();

            handler.postDelayed(() -> {
                addView(llPermission,
                        "辅助功能状态",
                        accessibilityServiceSettingEnabled && !isAccessibilityServiceWork() ? " --请尝试重新打开开关" : null,
                        isAccessibilityServiceWork(),
                        v1 -> jumpToAccessSetting());
                if (notificationListenerSettingEnabled)
                    sendNotification();
            }, 500);

            handler.postDelayed(() ->
                    addView(llPermission,
                            "通知监听服务状态",
                            notificationListenerSettingEnabled && !isNotificationListenerWork() ? " --请尝试重新打开开关" : null,
                            isNotificationListenerWork(),
                            v1 -> jumpToNotificationListenerSetting()), 1000);

            handler.postDelayed(() ->
                    addView(llPermission,
                            "悬浮窗权限",
                            null,
                            checkFloatingPermission(),
                            v1 -> SettingsCompat.manageDrawOverlays(getActivity())), 1500);

            handler.postDelayed(() ->
                    addView(llPermission,
                            "外部文件访问权限",
                            null,
                            checkWriteExternalStoragePermission(),
                            v1 -> requestWriteExternalStorage()), 2000);
            handler.postDelayed(() -> {
                if (checkFloatingPermission() && checkWriteExternalStoragePermission() && accessibilityServiceSettingEnabled &&
                        isAccessibilityServiceWork() && notificationListenerSettingEnabled && isNotificationListenerWork())
                    btnCheckPermission.doneLoadingAnimation(
                            getResources().getColor(R.color.colorCorrect),
                            getBitmap(R.drawable.ic_accept));
                else btnCheckPermission.doneLoadingAnimation(
                        getResources().getColor(R.color.colorError),
                        getBitmap(R.drawable.ic_cancel));
            }, 2500);

            handler.postDelayed(btnCheckPermission::revertAnimation, 5000);
        });
    }

    private void jumpToNotificationListenerSetting() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void jumpToAccessSetting() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void requestWriteExternalStorage() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    private boolean checkFloatingPermission() {
        return SettingsCompat.canDrawOverlays(getContext());
    }

    private boolean checkWriteExternalStoragePermission() {
        return (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    public boolean isAccessibilityServiceSettingEnabled() {
        if (getContext() == null)
            return false;
        final String service = getContext().getPackageName() + "/" + HongBaoService.class.getCanonicalName();
        int accessibilityEnabled = Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0);
        if (accessibilityEnabled != 1)
            return false;
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        String settingValue = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        mStringColonSplitter.setString(settingValue);
        while (mStringColonSplitter.hasNext()) {
            String accessibilityService = mStringColonSplitter.next();
            if (accessibilityService.equalsIgnoreCase(service)) {
                return true;
            }
        }

        return false;
    }

    private boolean isAccessibilityServiceWork() {
//        Log.w(TAG, "isAccessibilityServiceWork: clickTime: " + (new Date().getTime() - RobApplication.timeCheckAccessibilityServiceIsWorking));
//        return (new Date().getTime() - RobApplication.timeCheckAccessibilityServiceIsWorking) < 5000;
        return HongBaoService.isStart();
    }

    private boolean isNotificationListenerSettingEnabled() {
        if (getContext() == null)
            return false;
        String notificationEnabled = Settings.Secure.getString(getContext().getContentResolver(), "enabled_notification_listeners");
        if (TextUtils.isEmpty(notificationEnabled))
            return false;
        for (String name : notificationEnabled.split(":")) {
            ComponentName cn = ComponentName.unflattenFromString(name);
            if (cn != null) {
                if (TextUtils.equals(getContext().getPackageName(), cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNotificationListenerWork() {
//        Log.d(TAG, "isNotificationListenerWork: clickTime: " + (new Date().getTime() - RobApplication.timeCheckNotificationListenerServiceIsWorking));
//
//        FragmentActivity activity = getActivity();
//        if (activity == null)
//            return false;
//        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
//        if (manager == null)
//            return false;
//        manager.cancel(12);
//
//        return (new Date().getTime() - RobApplication.timeCheckNotificationListenerServiceIsWorking) < 5000;
        return true;
    }

    private void sendNotification() {
        FragmentActivity activity = getActivity();
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
                .setContentTitle("检测结果")
                .setContentText("通知通道正常")
                .setDefaults(Notification.FLAG_ONLY_ALERT_ONCE)
                .build();

        manager.notify(12, notification);
    }

    private void initAbout(ScrollView view) {
        // 关于
        View btnCheckUpdate = view.findViewById(R.id.btn_check_update);
        TextView tvLocalVersion = view.findViewById(R.id.tv_local_version);
        TextView tvRemoteVersion = view.findViewById(R.id.tv_remote_version);
        btnCheckUpdate.setOnClickListener((v) -> {
        });

        // 当前版本号
        try {
            tvLocalVersion.setText("v" + getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        initLogViewer(view);

    }

    // TODO: 2018/7/2 查看日志
    private void initLogViewer(ScrollView view) {
        view.findViewById(R.id.copyright).setOnClickListener(v -> {
            long clickTime = new Date().getTime();
            if (clickTime - this.clickTime > 1000)
                clicks = 1;
            else
                clicks++;
            this.clickTime = clickTime;
            if (clicks == 5) {
                File file = new File(getActivity().getExternalFilesDir("logs"), "Anti-recall-06-14.log");
                Log.i(TAG, "onCreateView: file: " + file);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri uri = FileProvider.getUriForFile(getActivity(), "com.qsboy.provider", file.getParentFile());
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri, "*/*");
                } else {
                    intent.setDataAndType(Uri.fromFile(file), "*/*");
                }
                startActivity(intent);
            }
//            public static void shareFile(Context context, Uri uri) {
            // File file = new File("\sdcard\android123.cwj"); //附件文件地址
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra("subject", ""); //
            intent.putExtra("body", ""); // 正文
//                intent.putExtra(Intent.EXTRA_STREAM, uri); // 添加附件，附件为file对象
//                if (uri.toString().endsWith(".gz")) {
//                    intent.setType("application/x-gzip"); // 如果是gz使用gzip的mime
//                } else if (uri.toString().endsWith(".txt")) {
//                    intent.setType("text/plain"); // 纯文本则用text/plain的mime
//                } else {
            intent.setType("application/octet-stream"); // 其他的均使用流当做二进制数据来发送
//                }
            startActivity(intent); // 调用系统的mail客户端进行发送
//            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getContext(), "该权限能查看撤回的图片", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void addView(ViewGroup mainView, String content, String hint, boolean isChecked, View.OnClickListener onClickListener) {

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.item_check_permission, mainView, false);
        LinearLayout llPermissioin = view.findViewById(R.id.ll_permission);
        TextView tvPermission = view.findViewById(R.id.tv_permission_content);
        ImageView ivChecked = view.findViewById(R.id.iv_checked);
        ImageView ivFix = view.findViewById(R.id.iv_fix);

        tvPermission.setText(content);
        if (hint != null) {
            TextView tvPermissionHint = new TextView(getContext());
            tvPermissionHint.setText(hint);
            llPermissioin.addView(tvPermissionHint);
        }
        if (isChecked) {
            ivChecked.setImageResource(R.drawable.ic_accept);
            ivChecked.setColorFilter(0xCC22DD22);
            ivFix.setVisibility(View.GONE);
        } else {
            ivChecked.setImageResource(R.drawable.ic_cancel);
            ivChecked.setColorFilter(0xAADD2222);
        }

        if (!isChecked)
            view.setOnClickListener(onClickListener);
        mainView.addView(view);
    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = VectorDrawableCompat.create(getResources(), drawableRes, null);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
