package com.liferemaining.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.liferemaining.app.R
import com.liferemaining.app.data.datastore.ProfileStore
import com.liferemaining.app.domain.model.ReminderTone
import com.liferemaining.app.domain.model.UserProfile
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * 每日提醒调度。
 *
 * 采用 WorkManager 的 PeriodicWorkRequest（24h 周期）。系统会把任务放进
 * Doze / App Standby 的窗口里执行，因此不承诺精确到某一分钟送达 —— UI 文案
 * 也这样表述，并在权限被拒绝时给出明确提示。
 *
 * 时间变更（时区/手动改时间）和配置更新都走 [sync]：先取消，再按最新配置重排。
 */
object ReminderScheduler {
    const val CHANNEL_ID = "daily_reminder"
    const val WORK_NAME = "daily_reminder_work"

    fun messageFor(tone: ReminderTone, seed: Int): String =
        ReminderPolicy.messageFor(tone, seed)

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 依据当前偏好重排提醒。
     * @return true 表示已排程；false 表示为关闭状态或被系统权限拦下（调用方提示用户）。
     */
    fun sync(context: Context, profile: UserProfile): Boolean {
        val workManager = WorkManager.getInstance(context)
        if (!profile.dailyReminderEnabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return false
        }
        if (!hasPermission(context)) {
            workManager.cancelUniqueWork(WORK_NAME)
            return false
        }
        createChannel(context)

        val delay = initialDelayUntil(profile.reminderTime, LocalDateTime.now())
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay.toMinutes().coerceAtLeast(0), TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .addTag(WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
        return true
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun initialDelayUntil(time: LocalTime, now: LocalDateTime): Duration =
        ReminderPolicy.initialDelayUntil(time, now)

    class ReminderWorker(
        appContext: Context,
        params: WorkerParameters
    ) : CoroutineWorker(appContext, params) {
        override suspend fun doWork(): Result {
            if (!hasPermission(applicationContext)) return Result.success()

            // 语气每次都从 DataStore 读最新值：用户在设置里改完语气后，
            // 不需要重新排程，下一条通知就是新语气。
            val tone = runCatching {
                ProfileStore(applicationContext).profile.first().reminderTone
            }.getOrElse { ReminderTone.GENTLE }
            val body = messageFor(tone, LocalDateTime.now().dayOfYear)

            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(applicationContext.getString(R.string.notification_title))
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build()

            return try {
                NotificationManagerCompat.from(applicationContext)
                    .notify(NOTIFICATION_ID, notification)
                Result.success()
            } catch (e: SecurityException) {
                Result.success()
            }
        }

        companion object {
            const val NOTIFICATION_ID = 1001
        }
    }
}
