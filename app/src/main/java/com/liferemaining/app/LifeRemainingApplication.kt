package com.liferemaining.app

import android.app.Application
import com.liferemaining.app.notifications.ReminderScheduler

class LifeRemainingApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderScheduler.createChannel(this)
    }
}
