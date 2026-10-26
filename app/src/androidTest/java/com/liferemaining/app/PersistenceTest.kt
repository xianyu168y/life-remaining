package com.liferemaining.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.liferemaining.app.data.database.AppDatabase
import com.liferemaining.app.data.database.GoalEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * 校验数据真的写到了磁盘：插入 → 关闭数据库 → 重新打开 → 仍然存在。
 * 这条测试对应产品要求里的「重启后数据依然存在」。
 */
@RunWith(AndroidJUnit4::class)
class PersistenceTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val dbName = "persistence-test.db"
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        context.deleteDatabase(dbName)
        db = Room.databaseBuilder(context, AppDatabase::class.java, dbName).build()
    }

    @After
    fun tearDown() {
        db.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun goalSurvivesDatabaseReopen() = runBlocking {
        val id = db.goalDao().insert(
            GoalEntity(
                title = "跑一次半马",
                description = "从五公里开始",
                category = "HEALTH",
                createdAt = LocalDate.of(2026, 10, 26),
                dueDate = LocalDate.of(2027, 4, 1),
                status = "ACTIVE",
                priority = "HIGH",
                isImportant = true
            )
        )
        assertTrue(id > 0)

        db.close()
        db = Room.databaseBuilder(context, AppDatabase::class.java, dbName).build()

        val restored = db.goalDao().findById(id)
        assertNotNull(restored)
        assertEquals("跑一次半马", restored!!.title)
        assertEquals(LocalDate.of(2027, 4, 1), restored.dueDate)
        assertEquals(true, restored.isImportant)
    }

    @Test
    fun reflectionIsUniquePerDate() = runBlocking {
        val dao = db.reflectionDao()
        val date = LocalDate.of(2026, 10, 26)
        val now = java.time.LocalDateTime.of(2026, 10, 26, 22, 0)
        dao.insert(
            com.liferemaining.app.data.database.ReflectionEntity(
                date = date,
                content = "第一版",
                createdAt = now,
                updatedAt = now
            )
        )
        val existing = dao.findByDate(date)
        assertNotNull(existing)
        dao.update(existing!!.copy(content = "第二版"))
        val all = dao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals("第二版", all[0].content)
    }

    @Test
    fun clearAllRemovesEveryTable() = runBlocking {
        db.milestoneDao().insert(
            com.liferemaining.app.data.database.MilestoneEntity(
                title = "生日",
                date = LocalDate.of(1990, 12, 1),
                category = "BIRTHDAY",
                note = "",
                repeatsAnnually = true
            )
        )
        db.yearNoteDao().insert(
            com.liferemaining.app.data.database.YearNoteEntity(
                year = 2026,
                content = "记录",
                createdAt = java.time.LocalDateTime.now(),
                updatedAt = java.time.LocalDateTime.now()
            )
        )

        db.goalDao().deleteAll()
        db.milestoneDao().deleteAll()
        db.reflectionDao().deleteAll()
        db.yearNoteDao().deleteAll()

        assertEquals(0, db.goalDao().observeAll().first().size)
        assertEquals(0, db.milestoneDao().observeAll().first().size)
        assertEquals(0, db.reflectionDao().observeAll().first().size)
        assertEquals(0, db.yearNoteDao().observeAll().first().size)
    }
}
