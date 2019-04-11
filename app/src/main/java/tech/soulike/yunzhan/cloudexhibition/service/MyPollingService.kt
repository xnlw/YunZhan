package tech.soulike.yunzhan.cloudexhibition.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.view.View
import com.google.gson.Gson
import tech.soulike.yunzhan.cloudexhibition.base.JsonData
import tech.soulike.yunzhan.cloudexhibition.base.MD5
import tech.soulike.yunzhan.cloudexhibition.base.ResourceController
import tech.soulike.yunzhan.cloudexhibition.base.ResourceData
import tech.soulike.yunzhan.cloudexhibition.util.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MyPollingService : Service() {

    private var downloadTask: DownloadTask? = null
    private lateinit var pollingData: JsonData

    companion object {
        const val ACTION = "tech.soulike.yunzhan.cloudexhibition.service.PollingService"
        const val START=1
        const val UPDATE=2
        const val stop=3
    }

    var needUpdate = false
    private val downloadListener = NewDownloadListener()
    private val resourceUrl: String? = null
    private var jsonUrl: String? = null



    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStart(intent: Intent, startId: Int) {
        val pool = Executors.newScheduledThreadPool(2)
        pool.scheduleAtFixedRate(pollingRunnable,0,5000, TimeUnit.MILLISECONDS)
    }

    private val pollingRunnable:Runnable = Runnable {
        try {
            val r = HttpUtil.postWithCookie(StringUtil.URL + "poll", HttpUtil.Param("uuid", CacheUtil.getScreenId()))
            val content = r.body().string()
            Log.d("content ====", "run: $content")
            pollingData = Gson().fromJson<JsonData>(content, JsonData::class.java)

            var code = pollingData.code

            if (code == 200){
                if (pollingData.data.bind){
                    if (pollingData.data.status){
                        val update = pollingData.data.update
                        if (update>CacheUtil.updateTime){
                            val stringList = ArrayList<String>()
                            jsonUrl = pollingData.data.url
                            stringList.add(jsonUrl!!)
                            downloadTask = DownloadTask(downloadListener, stringList)
                            downloadTask!!.execute(stringList.size)
                            CacheUtil.updateTime=update
                        }else{
                            val intent = Intent("tech.soulike.yunzhan.cloudexhibition.RECEIVE")
                            // 恢复播放
                            intent.putExtra("type", 5)
                            sendBroadcast(intent)
                        }
                    }else{
                        // 要求停止播放
                        val intent = Intent("tech.soulike.yunzhan.cloudexhibition.RECEIVE")
                        // 停止播放
                        intent.putExtra("type", 4)
                        sendBroadcast(intent)
                    }
                }else{
                    // 屏幕未绑定
                    val intent = Intent("tech.soulike.yunzhan.cloudexhibition.RECEIVE")
                    // 停止播放
                    intent.putExtra("type", 4)
                    sendBroadcast(intent)
                }
            }else if (code == 404){
                // Cookie 已经过期
                val intent = Intent("tech.soulike.yunzhan.cloudexhibition.RECEIVE")
                // 重新绑定cookie
                intent.putExtra("type", 3)
                sendBroadcast(intent)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

/*    private fun needUpdate(mD5: String): Boolean {
        if (!CacheUtil.getScreenMD5().equals(mD5, true)) {
            return true
        }
        val resourceList: List<ResourceData> = DataSupport.findAll(ResourceData::class.java)
        val files = File(FileUtil.getResourceHome() + "resource/").listFiles()
        if (files.isEmpty() && !resourceList.isEmpty()) {
            return true
        }
        if (resourceList.isEmpty()) {
            return false
        }
        for (resourceDate in resourceList) {
            var needUpdate = true
            for (file in files) {
                if (resourceDate.adMd5.equals(MD5.getFileMD5String(file), true)) {
                    needUpdate = false
                    break
                }
            }
            if (needUpdate) {
                return true
            }
        }

        return false

    }*/


    override fun onDestroy() {
        super.onDestroy()
        println("Service:onDestroy")
    }

    inner class NewDownloadListener : tech.soulike.yunzhan.cloudexhibition.listener.DownloadListener {

        override fun onProgress(vararg progresses: Int) {

        }

        override fun onSuccess(type: Int) {
            if (type == 1) {
                Thread(Runnable {
                    try {
                        val fileInputStream = FileInputStream(File(CacheUtil.getJSONPostion()))
                        val bytes = ByteArray(1024)
                        var b = 0
                        val result = StringBuilder()
                        b = fileInputStream.read(bytes)
                        try {
                            while (b > 0) {
                                result.append(String(bytes, 0, b))
                                b = fileInputStream.read(bytes)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        val jsonData: JsonData
                        jsonData = Gson().fromJson<JsonData>(
                                result.toString(),
                                JsonData::class.java
                        )
                        var pictureList = arrayListOf<String>()
                        val originMap = hashMapOf<String, String>()
                        var resourceData: ResourceData
                        jsonData.ad.forEach {
                            resourceData = ResourceData()
                            resourceData.adId = it.id
                            resourceData.adTime = it.time
                            resourceData.adUrl = it.url
                            resourceData.qrCodePosition = it.qrCodePosition.toString()
                            if (it.qrCodeUrl!=null){
                                resourceData.qrCodeUrl=it.qrCodeUrl!!
                            }
                            pictureList.add(it.url)
                            resourceData.save()
                            originMap[it.url] = it.id.toString()
//            SharedPreferenceUtil.putInt(it.ad_md5.toLowerCase(),it.ad_time)
                        }
                        pictureList = ResourceController.setPicture(originMap)
                        LogUtil.d("ad num is ",""+pictureList.size)
                        jsonData.ad.filter { it.qrCodeUrl!=null }.forEach {
                            pictureList.add(it.qrCodeUrl+"<<"+it.id)
                        }
                        val downloadTask = DownloadTask(downloadListener, pictureList)
                        downloadTask.execute(pictureList.size)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }).start()


            } else {
                FileUtil.deleteFile(File(CacheUtil.getJSONPostion()))
                val intent = Intent("tech.soulike.yunzhan.cloudexhibition.RECEIVE")
                // 提示更新
                intent.putExtra("type", 1)
                sendBroadcast(intent)
            }
        }

        override fun onFailed() {
            val intent = Intent("tech.soulike.yunzhan.cloudexhibition.RECEIVER")
            intent.putExtra("type", 2)
            sendBroadcast(intent)
        }

        override fun after() {

        }


    }
}
