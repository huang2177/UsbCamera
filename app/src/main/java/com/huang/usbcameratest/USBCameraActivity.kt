package com.huang.usbcameratest

import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.*
import butterknife.ButterKnife
import com.jiangdg.usbcamera.UVCCameraHelper
import com.jiangdg.usbcamera.utils.FileUtils
import com.serenegiant.usb.CameraDialog
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.common.AbstractUVCCameraHandler
import com.serenegiant.usb.encoder.RecordParams
import com.serenegiant.usb.widget.CameraViewInterface
import java.util.*

/**
 * UVCCamera use demo
 *
 *
 * Created by jiangdongguo on 2017/9/30.
 */

class USBCameraActivity : AppCompatActivity(), CameraDialog.CameraDialogParent, CameraViewInterface.Callback {
    var mTextureView: View? = null
    var mSeekBrightness: SeekBar? = null
    var mSeekContrast: SeekBar? = null
    var mSwitchVoice: Switch? = null

    private var mCameraHelper: UVCCameraHelper? = null
    private var mUVCCameraView: CameraViewInterface? = null
    private var mDialog: AlertDialog? = null

    private var isRequest: Boolean = false
    private var isPreview: Boolean = false

    private val listener = object : UVCCameraHelper.OnMyDevConnectListener {

        override fun onAttachDev(device: UsbDevice) {
            if (mCameraHelper == null || mCameraHelper!!.usbDeviceCount == 0) {
                showShortMsg("check no usb camera")
                return
            }
            // request open permission
            if (!isRequest) {
                isRequest = true
                if (mCameraHelper != null) {
                    mCameraHelper!!.requestPermission(0)
                }
            }
        }

        override fun onDettachDev(device: UsbDevice) {
            // close camera
            if (isRequest) {
                isRequest = false
                mCameraHelper!!.closeCamera()
                showShortMsg(device.deviceName + " is out")
            }
        }

        override fun onConnectDev(device: UsbDevice, isConnected: Boolean) {
            if (!isConnected) {
                showShortMsg("fail to connect,please check resolution params")
                isPreview = false
            } else {
                isPreview = true
                showShortMsg("connecting")
                // initialize seekbar
                // need to wait UVCCamera initialize over
                Thread(Runnable {
                    try {
                        Thread.sleep(2500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    Looper.prepare()
                    if (mCameraHelper != null && mCameraHelper!!.isCameraOpened) {
                        mSeekBrightness!!.progress = mCameraHelper!!.getModelValue(UVCCameraHelper.MODE_BRIGHTNESS)
                        mSeekContrast!!.progress = mCameraHelper!!.getModelValue(UVCCameraHelper.MODE_CONTRAST)
                    }
                    Looper.loop()
                }).start()
            }
        }

        override fun onDisConnectDev(device: UsbDevice) {
            showShortMsg("disconnecting")
        }
    }

    // example: {640x480,320x240,etc}
    private val resolutionList: List<String>?
        get() {
            val list = mCameraHelper!!.supportedPreviewSizes
            var resolutions: MutableList<String>? = null
            if (list != null && list.size != 0) {
                resolutions = ArrayList()
                for (size in list) {
                    if (size != null) {
                        resolutions.add(size.width.toString() + "x" + size.height)
                    }
                }
            }
            return resolutions
        }

    val isCameraOpened: Boolean
        get() = mCameraHelper!!.isCameraOpened

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        initView()

        // step.1 initialize UVCCameraHelper
        mUVCCameraView = mTextureView as CameraViewInterface?
        mUVCCameraView!!.setCallback(this)
        mCameraHelper = UVCCameraHelper.getInstance()
        mCameraHelper!!.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV)
        mCameraHelper!!.initUSBMonitor(this, mUVCCameraView, listener)


        mCameraHelper!!.setOnPreviewFrameListener { }
    }

    private fun initView() {
        mTextureView = findViewById(R.id.camera_view)
        mSwitchVoice = findViewById(R.id.switch_rec_voice)
        mSeekContrast = findViewById(R.id.seekbar_contrast)
        mSeekBrightness = findViewById(R.id.seekbar_brightness)

        mSeekBrightness!!.max = 100
        mSeekBrightness!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mCameraHelper != null && mCameraHelper!!.isCameraOpened) {
                    mCameraHelper!!.setModelValue(UVCCameraHelper.MODE_BRIGHTNESS, progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        mSeekContrast!!.max = 100
        mSeekContrast!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mCameraHelper != null && mCameraHelper!!.isCameraOpened) {
                    mCameraHelper!!.setModelValue(UVCCameraHelper.MODE_CONTRAST, progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    override fun onStart() {
        super.onStart()
        // step.2 register USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper!!.registerUSB()
        }
    }

    override fun onStop() {
        super.onStop()
        // step.3 unregister USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper!!.unregisterUSB()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toobar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_takepic -> {
                if (mCameraHelper == null || !mCameraHelper!!.isCameraOpened) {
                    showShortMsg("sorry,camera open failed")
                    return super.onOptionsItemSelected(item)
                }
                val picPath = (UVCCameraHelper.ROOT_PATH + System.currentTimeMillis()
                        + UVCCameraHelper.SUFFIX_JPEG)
                mCameraHelper!!.capturePicture(picPath) { path -> Log.i(TAG, "save path：$path") }
            }
            R.id.menu_recording -> {
                if (mCameraHelper == null || !mCameraHelper!!.isCameraOpened) {
                    showShortMsg("sorry,camera open failed")
                    return super.onOptionsItemSelected(item)
                }
                if (!mCameraHelper!!.isPushing) {
                    val videoPath = UVCCameraHelper.ROOT_PATH + System.currentTimeMillis()
                    FileUtils.createfile(FileUtils.ROOT_PATH + "test666.h264")
                    // if you want to record,please create RecordParams like this
                    val params = RecordParams()
                    params.recordPath = videoPath
                    params.recordDuration = 0                        // 设置为0，不分割保存
                    params.isVoiceClose = mSwitchVoice!!.isChecked    // is close voice
                    mCameraHelper!!.startPusher(params, object : AbstractUVCCameraHandler.OnEncodeResultListener {
                        override fun onEncodeResult(
                            data: ByteArray,
                            offset: Int,
                            length: Int,
                            timestamp: Long,
                            type: Int
                        ) {
                            // type = 1,h264 video stream
                            if (type == 1) {
                                FileUtils.putFileStream(data, offset, length)
                            }
                            // type = 0,aac audio stream
                            if (type == 0) {

                            }
                        }

                        override fun onRecordResult(videoPath: String) {
                            Log.i(TAG, "videoPath = $videoPath")
                        }
                    })
                    // if you only want to push stream,please call like this
                    // mCameraHelper.startPusher(listener);
                    showShortMsg("start record...")
                    mSwitchVoice!!.isEnabled = false
                } else {
                    FileUtils.releaseFile()
                    mCameraHelper!!.stopPusher()
                    showShortMsg("stop record...")
                    mSwitchVoice!!.isEnabled = true
                }
            }
            R.id.menu_resolution -> {
                if (mCameraHelper == null || !mCameraHelper!!.isCameraOpened) {
                    showShortMsg("sorry,camera open failed")
                    return super.onOptionsItemSelected(item)
                }
                showResolutionListDialog()
            }
            R.id.menu_focus -> {
                if (mCameraHelper == null || !mCameraHelper!!.isCameraOpened) {
                    showShortMsg("sorry,camera open failed")
                    return super.onOptionsItemSelected(item)
                }
                mCameraHelper!!.startCameraFoucs()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showResolutionListDialog() {
        val builder = AlertDialog.Builder(this@USBCameraActivity)
        val rootView = LayoutInflater.from(this@USBCameraActivity).inflate(R.layout.layout_dialog_list, null)
        val listView = rootView.findViewById<View>(R.id.listview_dialog) as ListView
        val adapter = ArrayAdapter(this@USBCameraActivity, android.R.layout.simple_list_item_1, resolutionList!!)
        if (adapter != null) {
            listView.adapter = adapter
        }
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            if (mCameraHelper == null || !mCameraHelper!!.isCameraOpened)
                return@OnItemClickListener
            val resolution = adapterView.getItemAtPosition(position) as String
            val tmp = resolution.split("x".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (tmp != null && tmp.size >= 2) {
                val widht = Integer.valueOf(tmp[0])
                val height = Integer.valueOf(tmp[1])
                mCameraHelper!!.updateResolution(widht, height)
            }
            mDialog!!.dismiss()
        }

        builder.setView(rootView)
        mDialog = builder.create()
        mDialog!!.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        FileUtils.releaseFile()
        // step.4 release uvc camera resources
        if (mCameraHelper != null) {
            mCameraHelper!!.release()
        }
    }

    private fun showShortMsg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun getUSBMonitor(): USBMonitor {
        return mCameraHelper!!.usbMonitor
    }

    override fun onDialogResult(canceled: Boolean) {
        if (canceled) {
            showShortMsg("取消操作")
        }
    }

    override fun onSurfaceCreated(view: CameraViewInterface, surface: Surface) {
        if (!isPreview && mCameraHelper!!.isCameraOpened) {
            mCameraHelper!!.startPreview(mUVCCameraView)
            isPreview = true
        }
    }

    override fun onSurfaceChanged(view: CameraViewInterface, surface: Surface, width: Int, height: Int) {

    }

    override fun onSurfaceDestroy(view: CameraViewInterface, surface: Surface) {
        if (isPreview && mCameraHelper!!.isCameraOpened) {
            mCameraHelper!!.stopPreview()
            isPreview = false
        }
    }

    companion object {
        private val TAG = "Debug"
    }
}
