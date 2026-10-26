package com.liferemaining.app.data.repository

import com.liferemaining.app.data.database.GoalDao
import com.liferemaining.app.data.database.GoalEntity
import com.liferemaining.app.data.database.MilestoneDao
import com.liferemaining.app.data.database.MilestoneEntity
import com.liferemaining.app.data.database.ReflectionDao
import com.liferemaining.app.data.database.ReflectionEntity
import com.liferemaining.app.data.database.YearNoteDao
import com.liferemaining.app.data.database.YearNoteEntity
import com.liferemaining.app.domain.model.DailyReflection
import com.liferemaining.app.domain.model.GoalCategory
import com.liferemaining.app.domain.model.GoalPriority
import com.liferemaining.app.domain.model.GoalStatus
import com.liferemaining.app.domain.model.LifeGoal
import com.liferemaining.app.domain.model.Milestone
import com.liferemaining.app.domain.model.MilestoneCategory
import com.liferemaining.app.domain.model.YearNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime

class GoalRepository(private val dao: GoalDao) {

    val goals: Flow<List<LifeGoal>> = dao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun find(id: Long): LifeGoal? = dao.findById(id)?.toModel()

    suspend fun add(goal: LifeGoal): Long = dao.insert(goal.toEntity())

    suspend fun update(goal: LifeGoal) {
        require(goal.id > 0L) { "更新目标需要已存在的 id" }
        dao.update(goal.toEntity())
    }

    suspend fun delete(goal: LifeGoal) = dao.delete(goal.toEntity())

    suspend fun clear() = dao.deleteAll()

    suspend fun replaceAll(goals: List<LifeGoal>) {
        dao.deleteAll()
        goals.forEach { dao.insert(it.copy(id = 0).toEntity()) }
    }
}

class MilestoneRepository(private val dao: MilestoneDao) {

    val milestones: Flow<List<Milestone>> = dao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun find(id: Long): Milestone? = dao.findById(id)?.toModel()

    suspend fun add(milestone: Milestone): Long = dao.insert(milestone.toEntity())

    suspend fun update(milestone: Milestone) {
        require(milestone.id > 0L) { "更新重要日期需要已存在的 id" }
        dao.update(milestone.toEntity())
    }

    suspend fun delete(milestone: Milestone) = dao.delete(milestone.toEntity())

    suspend fun clear() = dao.deleteAll()

    suspend fun replaceAll(milestones: List<Milestone>) {
        dao.deleteAll()
        milestones.forEach { dao.insert(it.copy(id = 0).toEntity()) }
    }
}

class ReflectionRepository(private val dao: ReflectionDao) {

    val reflections: Flow<List<DailyReflection>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    fun observe(date: LocalDate): Flow<DailyReflection?> =
        dao.observeByDate(date).map { it?.toModel() }

    suspend fun find(date: LocalDate): DailyReflection? =
        dao.findByDate(date)?.toModel()

    /** 同一天只保留一条：已有则覆盖内容并更新 updatedAt。 */
    suspend fun save(date: LocalDate, content: String): DailyReflection {
        val now = LocalDateTime.now()
        val existing = dao.findByDate(date)
        return if (existing == null) {
            val entity = ReflectionEntity(
                date = date,
                content = content,
                createdAt = now,
                updatedAt = now
            )
            val id = dao.insert(entity)
            entity.copy(id = id).toModel()
        } else {
            val updated = existing.copy(content = content, updatedAt = now)
            dao.update(updated)
            updated.toModel()
        }
    }

    suspend fun delete(reflection: DailyReflection) = dao.delete(reflection.toEntity())

    suspend fun clear() = dao.deleteAll()

    suspend fun replaceAll(reflections: List<DailyReflection>) {
        dao.deleteAll()
        reflections.forEach { dao.insert(it.copy(id = 0).toEntity()) }
    }
}

class YearNoteRepository(private val dao: YearNoteDao) {

    val notes: Flow<List<YearNote>> = dao.observeAll().map { list -> list.map { it.toModel() } }

    fun observeYear(year: Int): Flow<List<YearNote>> =
        dao.observeByYear(year).map { list -> list.map { it.toModel() } }

    suspend fun add(year: Int, content: String): YearNote {
        val now = LocalDateTime.now()
        val entity = YearNoteEntity(year = year, content = content, createdAt = now, updatedAt = now)
        val id = dao.insert(entity)
        return entity.copy(id = id).toModel()
    }

    suspend fun update(note: YearNote) = dao.update(note.copy(updatedAt = LocalDateTime.now()).toEntity())

    suspend fun delete(note: YearNote) = dao.delete(note.toEntity())

    suspend fun clear() = dao.deleteAll()

    suspend fun replaceAll(notes: List<YearNote>) {
        dao.deleteAll()
        notes.forEach { dao.insert(it.copy(id = 0).toEntity()) }
    }
}

// ---------- mappers ----------

internal fun GoalEntity.toModel() = LifeGoal(
    id = id,
    title = title,
    description = description,
    category = GoalCategory.fromStorage(category),
    createdAt = createdAt,
    dueDate = dueDate,
    status = GoalStatus.fromStorage(status),
    priority = GoalPriority.fromStorage(priority),
    isImportant = isImportant
)

internal fun LifeGoal.toEntity() = GoalEntity(
    id = id,
    title = title,
    description = description,
    category = category.name,
    createdAt = createdAt,
    dueDate = dueDate,
    status = status.name,
    priority = priority.name,
    isImportant = isImportant
)

internal fun MilestoneEntity.toModel() = Milestone(
    id = id,
    title = title,
    date = date,
    category = MilestoneCategory.fromStorage(category),
    note = note,
    repeatsAnnually = repeatsAnnually
)

internal fun Milestone.toEntity() = MilestoneEntity(
    id = id,
    title = title,
    date = date,
    category = category.name,
    note = note,
    repeatsAnnually = repeatsAnnually
)

internal fun ReflectionEntity.toModel() = DailyReflection(
    id = id,
    date = date,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun DailyReflection.toEntity() = ReflectionEntity(
    id = id,
    date = date,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun YearNoteEntity.toModel() = YearNote(
    id = id,
    year = year,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun YearNote.toEntity() = YearNoteEntity(
    id = id,
    year = year,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)
