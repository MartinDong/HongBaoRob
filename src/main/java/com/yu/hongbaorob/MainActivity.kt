package com.yu.hongbaorob

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


//    override fun onResume() {
//        super.onResume()
//        if (!HongBaoService.isStart()) {
//            try {
//                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
//            } catch (e: Exception) {
//                startActivity(Intent(Settings.ACTION_SETTINGS))
//                e.printStackTrace()
//            }
//        }
//    }
}
