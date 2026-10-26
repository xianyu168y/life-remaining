package com.liferemaining.app

import android.content.Context
import com.liferemaining.app.data.backup.BackupService
import com.liferemaining.app.data.database.AppDatabase
import com.liferemaining.app.data.datastore.ProfileStore
import com.liferemaining.app.data.repository.GoalRepository
import com.liferemaining.app.data.repository.MilestoneRepository
import com.liferemaining.app.data.repository.ReflectionRepository
import com.liferemaining.app.data.repository.YearNoteRepository
import com.liferemaining.app.domain.model.UserProfile
import com.liferemaining.app.notifications.ReminderScheduler
import com.liferemaining.app.util.TodayProvider

/** 手工依赖容器。App 规模不需要 DI 框架，构造关系一眼可见。 */
class AppContainer(private val context: Context) {

    val database: AppDatabase = AppDatabase.get(context)

    val todayProvider: TodayProvider = TodayProvider()

    val profileStore: ProfileStore = ProfileStore(context)

    val goalRepository: GoalRepository = GoalRepository(database.goalDao())

    val milestoneRepository: MilestoneRepository = MilestoneRepository(database.milestoneDao())

    val reflectionRepository: ReflectionRepository = ReflectionRepository(database.reflectionDao())

    val yearNoteRepository: YearNoteRepository = YearNoteRepository(database.yearNoteDao())

    val backupService: BackupService = BackupService(
        database = database,
        profileStore = profileStore,
        goalRepository = goalRepository,
        milestoneRepository = milestoneRepository,
        reflectionRepository = reflectionRepository,
        yearNoteRepository = yearNoteRepository
    )

    fun scheduleReminder(profile: UserProfile): Boolean =
        ReminderScheduler.sync(context, profile)

    fun cancelReminder() = ReminderScheduler.cancel(context)

    fun hasNotificationPermission(): Boolean = ReminderScheduler.hasPermission(context)

    /** 「清除全部数据」：业务表 + 偏好一起清空。 */
    suspend fun clearEverything() {
        goalRepository.clear()
        milestoneRepository.clear()
        reflectionRepository.clear()
        yearNoteRepository.clear()
        profileStore.clearAll()
    }
}
