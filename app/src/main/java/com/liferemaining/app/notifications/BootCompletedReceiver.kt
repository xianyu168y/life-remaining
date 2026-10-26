package com.liferemaining.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.liferemaining.app.LifeRemainingApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 重启、时区变化、App 更新后重新对齐提醒。
 * WorkManager 本身能持久化周期任务，但时区变化会让「每天 08:30」发生偏移，
 * 因此这里按最新偏好重新排一次。
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_TIMEZONE_CHANGED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val profileStore = (context.applicationContext as? LifeRemainingApplication)?.container?.profileStore
            ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val profile = profileStore.profile.first()
                ReminderScheduler.sync(context.applicationContext, profile)
            } finally {
                pending.finish()
            }
        }
    }
}
