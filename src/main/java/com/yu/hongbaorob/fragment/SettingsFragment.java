package com.yu.hongbaorob.fragment;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.yu.hongbaorob.R;
import com.yu.hongbaorob.service.HongBaoService;
import com.yu.hongbaorob.utils.NotificationUtil;
import com.yu.hongbaorob.utils.PermissionUtil;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

/**
 * @author donghongyu
 * @date 2019-09-02
 * @desc 设置页面
 */
public class SettingsFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    private String TAG = "SettingsFragment";
    private Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        ScrollView view = (ScrollView) inflater.inflate(R.layout.fragment_settings, container, false);

        initPermissionCheck(view);

        initAbout(view);

        return view;
    }

    private void initPermissionCheck(ScrollView view) {
        // 跳转
        View btnAccessibilityService = view.findViewById(R.id.btn_navigate_accessibility_service);
        View btnNotificationListener = view.findViewById(R.id.btn_navigate_notification_listener);
        View btnOverlays = view.findViewById(R.id.btn_navigate_overlays);

        btnAccessibilityService.setOnClickListener(v -> PermissionUtil.requestAccessSetting(getContext()));

        btnNotificationListener.setOnClickListener(v -> PermissionUtil.requestNotificationListenerSetting(getContext()));

        btnOverlays.setOnClickListener(v -> PermissionUtil.requestDrawOverlays(getContext()));

        // 检查权限
        CircularProgressButton btnCheckPermission = view.findViewById(R.id.btn_check_permission);
        btnCheckPermission.setOnClickListener(v -> {
            btnCheckPermission.performAccessibilityAction(AccessibilityEvent.TYPE_VIEW_CLICKED, null);
            btnCheckPermission.startAnimation();
            ViewGroup llPermission = view.findViewById(R.id.ll_permission);
            llPermission.removeAllViews();
            llPermission.addView((View) btnCheckPermission.getParent());

            boolean accessibilityServiceSettingEnabled =
                    PermissionUtil.isAccessibilityServiceSettingEnabled(getContext(), HongBaoService.class.getCanonicalName());
            boolean notificationListenerSettingEnabled =
                    PermissionUtil.isNotificationListenerSettingEnabled(getContext());

            handler.postDelayed(() -> {
                addView(llPermission,
                        "辅助功能状态",
                        accessibilityServiceSettingEnabled ? null : " --请尝试重新打开开关",
                        accessibilityServiceSettingEnabled,
                        v1 -> PermissionUtil.requestAccessSetting(getContext()));
                if (notificationListenerSettingEnabled)
                    NotificationUtil.sendNotification(getActivity(), "检测结果", "通知通道正常");
            }, 500);

            handler.postDelayed(() ->
                    addView(llPermission,
                            "通知监听服务状态",
                            notificationListenerSettingEnabled ? null : " --请尝试重新打开开关",
                            notificationListenerSettingEnabled,
                            v1 -> PermissionUtil.requestNotificationListenerSetting(getActivity())), 1000);

            handler.postDelayed(() ->
                    addView(llPermission,
                            "悬浮窗权限",
                            null,
                            PermissionUtil.canDrawOverlays(getContext()),
                            v1 -> PermissionUtil.requestDrawOverlays(getContext())), 1500);

            handler.postDelayed(() ->
                    addView(llPermission,
                            "外部文件访问权限",
                            null,
                            PermissionUtil.canWrite(getContext()),
                            v1 -> PermissionUtil.requestWriteExternalStorage(getActivity())), 2000);

            handler.postDelayed(() -> {
                if (PermissionUtil.canDrawOverlays(getContext()) &&
                        PermissionUtil.canWrite(getContext()) &&
                        accessibilityServiceSettingEnabled &&
                        notificationListenerSettingEnabled)
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
