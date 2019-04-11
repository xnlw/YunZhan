package tech.soulike.yunzhan.cloudexhibition.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.VideoView
import tech.soulike.yunzhan.cloudexhibition.R
import tech.soulike.yunzhan.cloudexhibition.base.MyApplication
import tech.soulike.yunzhan.cloudexhibition.listener.ImageChangeListener
import tech.soulike.yunzhan.cloudexhibition.view.BaseImage
import tech.soulike.yunzhan.cloudexhibition.service.MyPollingService
import tech.soulike.yunzhan.cloudexhibition.util.*
import java.io.IOException
import android.support.v4.media.session.MediaControllerCompat.setMediaController
import android.os.Environment.getExternalStorageDirectory
import android.widget.MediaController
import android.media.MediaPlayer
import android.view.WindowManager
import tech.soulike.yunzhan.cloudexhibition.view.CustomerVideoView


class PlayerActivity : AppCompatActivity() {
    private lateinit var baseImage: BaseImage
    lateinit var frameLayout : FrameLayout
    private lateinit var videoView: CustomerVideoView
    private var status = false
    private var current = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val broadcastReceiver = MyReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("tech.soulike.yunzhan.cloudexhibition.RECEIVE")
        registerReceiver(broadcastReceiver, intentFilter)
        baseImage = BaseImage(this@PlayerActivity)


        setContentView(R.layout.activity_player)
        startService(Intent(this,MyPollingService::class.java))
        frameLayout = findViewById<View>(R.id.container) as FrameLayout
        videoView=frameLayout.findViewById(R.id.play_video)
        frameLayout.addView(baseImage)
        status = true
        baseImage.setImageChangeListener(object : ImageChangeListener{
            override fun onBitmapChange(currentId: Int) {

            }

            override fun onBitmapPause(){
                baseImage.visibility=View.INVISIBLE
                videoView.visibility=View.VISIBLE
//                本地的视频  需要在手机SD卡根目录添加一个 fl1234.mp4 视频
                current++
                if (current>=baseImage.getVideoLoc().size){
                    current=0
                }
                val videoUrl1 = baseImage.getVideoLoc()[current].absolutePath

                val uri = Uri.parse(videoUrl1)

                //设置视频控制器
                videoView.setMediaController(MediaController(this@PlayerActivity))

                //播放完成回调
                videoView.setOnCompletionListener(MyPlayerOnCompletionListener())

                //设置视频路径
                videoView.setVideoURI(uri)

                //开始播放视频
                videoView.start()

            }

        })


    }

    internal inner class MyPlayerOnCompletionListener : MediaPlayer.OnCompletionListener {

        override fun onCompletion(mp: MediaPlayer) {
            videoView.visibility=View.GONE
            baseImage.visibility=View.VISIBLE
            baseImage.goOn()

        }
    }

    private inner class MyReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val type = intent.getIntExtra("type", 0)
            if (type == 1) {
                Toast.makeText(this@PlayerActivity, "正在更新资源包", Toast.LENGTH_LONG).show()
                if (status)
                    baseImage.restart()
                else{
                    baseImage = BaseImage(this@PlayerActivity)
                    frameLayout.addView(baseImage)
                    status = true
                }

            } else if (type == 2) {
                Toast.makeText(this@PlayerActivity, "资源包验证失败", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@PlayerActivity, HelloActivity::class.java))
                finish()
            } else if (type ==3){
                createScreen()
                if (status){
                    frameLayout.removeView(baseImage)
                    status = false
                }

            } else if (type == 4){
                if (status){
                    frameLayout.removeView(baseImage)
                    status = false
                }
            }else if (type ==5){
                if (!status){
                    frameLayout.addView(baseImage)
                    baseImage.restart()
                    status=true
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this,MyPollingService::class.java))
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            val decorView = window.decorView
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
    private fun getHasVirtualKey(): Int {
        var dpi = 0
        val display = windowManager.defaultDisplay
        val dm = DisplayMetrics()
        val c: Class<*>
        try {
            c = Class.forName("android.view.Display")
            val method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
            method.invoke(display, dm)
            dpi = dm.heightPixels
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return dpi
    }
    private fun createScreen() {
        Thread {
            try {
                runOnUiThread { Toast.makeText(this, "正在创建屏幕", Toast.LENGTH_SHORT).show() }
                val widthNumber = MyApplication.context.resources.displayMetrics.widthPixels
                val heightNumber = getHasVirtualKey()
                val screenResolution: String = heightNumber.toString() + " * " + widthNumber.toString()
                // this function should send location information
                val response = HttpUtil.post(StringUtil.URL + "create", HttpUtil.Param("uuid", CacheUtil.getScreenId()), HttpUtil.Param("screen_resolution", screenResolution))
                val cookie = response.header("Set-Cookie")
                Log.d("", "createScreen: response:" + response.body().string()+"cookie = "+cookie)
                CacheUtil.saveCookie(cookie)
                CacheUtil.saveScreen(true)
            } catch (e: IOException) {
                runOnUiThread { Toast.makeText(this, "无法连接到服务器，请检查网络", Toast.LENGTH_LONG).show() }
                e.printStackTrace()
            }
        }.start()
    }

}
