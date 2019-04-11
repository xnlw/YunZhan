package tech.soulike.yunzhan.cloudexhibition.util

import android.text.BoringLayout
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by thunder on 18-3-6.
 *
 */
object CacheUtil {
    private fun iGetCookie(): String {
        return SharedPreferenceUtil.getString("cookie", "")
    }

    private fun iSaveCookie(paramString: String) {
        SharedPreferenceUtil.putString("cookie", paramString)
    }

    fun getCookie(): String = iGetCookie()



    fun saveCookie(cookie: String) = iSaveCookie(cookie)

    private fun iGetScreenId(): String {
        return SharedPreferenceUtil.getString("screen_id", "")
    }

    private fun iSaveScreenId(screenId: String) {
        SharedPreferenceUtil.putString("screen_id", screenId)
    }

    fun getScreenId(): String= iGetScreenId()

    fun saveScreenId(screenId: String) = iSaveScreenId(screenId)

    private fun iGetAccount(): String {
        return SharedPreferenceUtil.getString("account")
    }

    private fun iSaveAccount(account: String) {
        SharedPreferenceUtil.putString("account", account)
    }

    fun getAccount(): String = iGetAccount()
    
    fun saveAccount(account: String) = iSaveAccount(account)

    fun isAutoStart(): Boolean {
        return SharedPreferenceUtil.getBoolean("autoStart", false)
    }

    fun setAutoStart(value: Boolean) {
        SharedPreferenceUtil.putBoolean("autoStart", value)
    }

    fun setAutoTime(time: String) {
        SharedPreferenceUtil.putString("autoTime", time)
    }

    fun getAutoTime(): String {
        var result = SharedPreferenceUtil.getString("autoTime")
        if (result == "") {
            result = "自启动时间"
        } else {
            var date: Date? = null
            val ft = SimpleDateFormat("yyyy-MM-dd HH:mm")
            try {
                date = ft.parse(result)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (date != null) {
                if (date.time < System.currentTimeMillis()) {
                    result = "自启动时间"
                }
            }
        }
        return result

    }

    /**
     * Indicates whether the resource information has changed
     */
    fun getScreenMD5(): String {
        return SharedPreferenceUtil.getString("screen_md5")
    }

    fun setScreenMD5(md5: String) {
        SharedPreferenceUtil.putString("screen_md5", md5)
    }

    fun getPullTime(): Int {
        return SharedPreferenceUtil.getInt("screen_time", 30)
    }

    fun setPullTime(time: Int) {
        SharedPreferenceUtil.putInt("screen_time", time)
    }

    fun setJOSNPosition(position: String) {
        SharedPreferenceUtil.putString("json_position", position)
    }

    fun getJSONPostion(): String {
        return SharedPreferenceUtil.getString("json_position")
    }
    val isCreateScreen : Boolean
    get()  = SharedPreferenceUtil.getBoolean("screen_create")
    fun saveScreen(b: Boolean)= SharedPreferenceUtil.putBoolean("screen_create",b)
    var updateTime:Long
    get() = SharedPreferenceUtil.getLong("update_time")
    set(value) {
        SharedPreferenceUtil.putLong("update_time",value)
    }
}