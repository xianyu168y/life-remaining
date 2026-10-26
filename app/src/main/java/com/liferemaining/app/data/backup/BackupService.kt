package com.liferemaining.app.data.backup

import com.liferemaining.app.data.datastore.ProfileStore
import com.liferemaining.app.data.repository.GoalRepository
import com.liferemaining.app.data.repository.MilestoneRepository
import com.liferemaining.app.data.repository.ReflectionRepository
import com.liferemaining.app.data.repository.YearNoteRepository
import kotlinx.coroutines.flow.first

/**
 * 把 BackupCodec 与数据库/偏好接起来。
 * 导入时先完整解码校验，成功后才写入，避免半途失败留下混合数据。
 */
class BackupService(
    private val profileStore: ProfileStore,
    private val goalRepository: GoalRepository,
    private val milestoneRepository: MilestoneRepository,
    private val reflectionRepository: ReflectionRepository,
    private val yearNoteRepository: YearNoteRepository
) {

    suspend fun exportToJson(): String {
        val snapshot = BackupSnapshot(
            profile = profileStore.profile.first(),
            goals = goalRepository.goals.first(),
            milestones = milestoneRepository.milestones.first(),
            reflections = reflectionRepository.reflections.first(),
            yearNotes = yearNoteRepository.notes.first()
        )
        return BackupCodec.encode(snapshot)
    }

    suspend fun importFromJson(raw: String): ImportResult {
        return when (val decoded = BackupCodec.decode(raw)) {
            is DecodeResult.Failure -> ImportResult.Failure(decoded.reason)
            is DecodeResult.Success -> {
                val snapshot = decoded.snapshot
                snapshot.profile?.let { profileStore.overwrite(it) }
                goalRepository.replaceAll(snapshot.goals)
                milestoneRepository.replaceAll(snapshot.milestones)
                reflectionRepository.replaceAll(snapshot.reflections)
                yearNoteRepository.replaceAll(snapshot.yearNotes)
                ImportResult.Success(
                    goals = snapshot.goals.size,
                    milestones = snapshot.milestones.size,
                    reflections = snapshot.reflections.size,
                    yearNotes = snapshot.yearNotes.size
                )
            }
        }
    }
}
