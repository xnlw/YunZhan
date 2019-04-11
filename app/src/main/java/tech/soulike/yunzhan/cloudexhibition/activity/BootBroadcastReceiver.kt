package tech.soulike.yunzhan.cloudexhibition.activity

import android.app.Activity
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager

class BootBroadcastReceiver : BroadcastReceiver() {

    companion object {
        val ACTION = "android.intent.action.BOOT_COMPLETED"
    }
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        //屏幕唤醒
        val pm:PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl: PowerManager.WakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK, "BootBroadcastReceiver")
        wl.acquire();

        //屏幕解锁
        val km =context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val kl = km.newKeyguardLock("BootBroadcastReceiver");
        kl.disableKeyguard();

        //启动APP
        if (intent.action == ACTION) {
            val intent = Intent(context, HelloActivity::class.java) // 要启动的Activity
            if (context !is Activity) {
                //如果不是在Activity中显示Activity，必须要设置FLAG_ACTIVITY_NEW_TASK标志
                intent .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent )
        }

    }
}
