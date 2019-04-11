package tech.soulike.yunzhan.cloudexhibition.view

import android.graphics.*
import org.litepal.crud.DataSupport
import tech.soulike.yunzhan.cloudexhibition.base.MD5
import tech.soulike.yunzhan.cloudexhibition.base.MyApplication
import tech.soulike.yunzhan.cloudexhibition.base.ResourceData
import tech.soulike.yunzhan.cloudexhibition.util.FileUtil
import tech.soulike.yunzhan.cloudexhibition.util.LogUtil
import tech.soulike.yunzhan.cloudexhibition.util.SharedPreferenceUtil
import tech.soulike.yunzhan.cloudexhibition.util.StringUtil
import java.io.File
import java.util.ArrayList
import android.graphics.Bitmap
import android.util.Log


/**
 * Created by thunder on 18-3-7.
 */
class ResourceFactory {
    private lateinit var bitmapPaint: Paint
    private lateinit var bitmap: Bitmap
    var start: Int = 0
        set(value) {
            field = value
            SharedPreferenceUtil.putInt(StringUtil.START_PICTURE, start)
        }
    var end: Int = 0
        set(value)  {
            field = value
            SharedPreferenceUtil.putInt(StringUtil.END_PICTURE, field)
        }
    var current: Int = 0
        set(value) =  if (value >= bitmapList.size) field=0 else field=value
    private lateinit var integers: MutableList<Int>
    private var bitmapList: MutableList<Bitmap?> = ArrayList()
    private lateinit var timeOrder: MutableList<Int>
    private lateinit var files: Array<File>
    fun initBitmapList() {
        bitmapPaint = Paint()
        val i = 0
            integers = ArrayList()
            bitmapList = ArrayList()
            timeOrder = ArrayList()


        val file = File(FileUtil.getResourceHome() + "resource/")

        files = file.listFiles()
        val resourceDataList = DataSupport.findAll(ResourceData::class.java)
        for (file1 in files) {
            if (picFilter(file1)){
                    val resourceData = resourceDataList.firstOrNull{
                        it.adId.toString()==file1.name.substring(0,file1.name.lastIndexOf("."))
                    }
                    if (resourceData!=null) {
                        timeOrder.add(resourceData.adTime)
                        val bitmapOld = android.graphics.BitmapFactory.decodeFile(file1.absolutePath)
                        val bitmapBase = Bitmap.createBitmap(MyApplication.context.resources.displayMetrics.widthPixels, MyApplication.context.resources.displayMetrics.heightPixels, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmapBase)
                        val fileQr = File(FileUtil.getResourceHome()+"qrcode/qrcode_"+resourceData.adId)

                        canvas.drawColor(Color.BLACK)

                        val widthOld = bitmapOld.width
                        val heightOld = bitmapOld.height
                        val widthNew = MyApplication.context.resources.displayMetrics.widthPixels
                        val heightNew = MyApplication.context.resources.displayMetrics.heightPixels
                        Log.d("resource", "wo:$widthOld     ho:$heightOld        wn:$widthNew       hn:$heightNew")
                        val scaleWidth = widthNew.toFloat() / widthOld
                        val scaleHeight = heightNew.toFloat() / heightOld
                        val matrix = Matrix()
                        val min = if (scaleHeight < scaleWidth) scaleHeight else scaleWidth
                        matrix.postScale(min, min)
                        val newBm = Bitmap.createBitmap(bitmapOld, 0, 0, widthOld, heightOld, matrix, true)
                        canvas.drawBitmap(newBm, (widthNew - widthOld * min) / 2, (heightNew - heightOld * min) / 2, Paint())
                        if (fileQr.exists()){
                            val bitmapQr = android.graphics.BitmapFactory.decodeFile(fileQr.absolutePath)
//                            matrix.postScale(100f/bitmapQr.width,100f/bitmapQr.height)
//
//                            val newBitmapQr = Bitmap.createBitmap(bitmapQr, 0, 0, bitmapQr.width, bitmapQr.height, matrix, true)
//                            LogUtil.d(bitmapQr.width.toString(),bitmapQr.height.toString())
//
//                            LogUtil.d(newBitmapQr.width.toString(),newBitmapQr.height.toString())
                            val newBitmapQr = zoomImage(bitmapQr,100.0,100.0)
                            canvas.drawBitmap(newBitmapQr,widthNew/10.toFloat(),heightNew/10.toFloat(), Paint())
                        }
                        bitmapList.add(bitmapBase)

                    }else{
                        FileUtil.deleteFile(file1)
                    }
            }else if(camFilter(file1)){
                // 视频类
                bitmapList.add(null)
            }

        }
        SharedPreferenceUtil.putInt(StringUtil.END_PICTURE, bitmapList.size)
        start = 0
        end = bitmapList.size
        this.current = SharedPreferenceUtil.getInt(StringUtil.CURRENT_PICTURE, start)
    }
    private fun camFilter(file: File):Boolean{
        val list:List<ResourceData> = DataSupport.where("adId = ?",file.name.substring(0,file.name.lastIndexOf("."))) .find(ResourceData::class.java)
        return list.isNotEmpty() &&list[0].type==1
    }
    private fun picFilter(file:File):Boolean{
//        val filePost = listOf(".jpg",".png")
//        return filePost.contains(file.name.substring(file.name.lastIndexOf('.')))
        val list:List<ResourceData> = DataSupport.where("adId = ?",file.name.substring(0,file.name.lastIndexOf("."))) .find(ResourceData::class.java)
        return list.isNotEmpty() &&list[0].type==0
    }

    fun addBitmap(path: String) {
        bitmapList.add(android.graphics.BitmapFactory.decodeFile(path))
        end++
        SharedPreferenceUtil.putInt(StringUtil.END_PICTURE, end)
    }

    private fun createBitmap() :Boolean {
        if (bitmapList.size > 0)
            bitmap = bitmapList[current] ?: return false
        return true
    }

    fun getTime(): Int {
        return timeOrder[current]
    }
    fun getTime(current: Int):Int{
        return timeOrder[current]
    }
    internal fun onDraw(canvas: Canvas) : Boolean {
        return if (createBitmap()){
            canvas.drawBitmap(bitmap, 0f, 0f, bitmapPaint)
            SharedPreferenceUtil.putInt(StringUtil.CURRENT_PICTURE, current+1)

       true
        } else {
            false
        }
    }

    fun zoomImage(bgimage: Bitmap, newWidth: Double,
                  newHeight: Double): Bitmap {
        // 获取这个图片的宽和高
        val width = bgimage.width.toFloat()
        val height = bgimage.height.toFloat()
        // 创建操作图片用的matrix对象
        val matrix = Matrix()
        // 计算宽高缩放率
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight)
        LogUtil.d(scaleHeight.toString(),scaleWidth.toString())
        return Bitmap.createBitmap(bgimage, 0, 0, width.toInt(),
                height.toInt(), matrix, true)
    }
    fun getVideoLoc():List<File>{
        val file = File(FileUtil.getResourceHome() + "resource/")
        return file.listFiles().filter {
            camFilter(file)
        }
    }

}