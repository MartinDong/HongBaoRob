package com.yu.hongbaorob

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.yu.hongbaorob.service.HongBaoService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvContent.text = "开启辅助，打开wx聊天界可看到提示，如没有提示请一键清理所有app或重启手机再试"
    }


    override fun onResume() {
        super.onResume()
        if (!HongBaoService.isStart()) {
            try {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
                e.printStackTrace()
            }
        }
    }
}
