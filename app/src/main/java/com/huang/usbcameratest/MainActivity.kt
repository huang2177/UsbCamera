package com.huang.usbcameratest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.jiangdg.usbcamera.UVCCameraHelper

class MainActivity : AppCompatActivity() {
    private var mCameraHelper: UVCCameraHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCameraHelper = UVCCameraHelper.getInstance()
        mCameraHelper!!.initUSBMonitor(this, null, null)
    }
}
