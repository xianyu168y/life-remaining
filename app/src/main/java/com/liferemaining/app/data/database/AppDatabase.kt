package com.liferemaining.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        GoalEntity::class,
        MilestoneEntity::class,
        ReflectionEntity::class,
        YearNoteEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun reflectionDao(): ReflectionDao
    abstract fun yearNoteDao(): YearNoteDao

    companion object {
        private const val DB_NAME = "life_remaining.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }

        /**
         * 迁移策略：schema 必须随版本一起提交到 app/schemas。
         * 升级时在这里追加 MIGRATION_n_m，禁止 fallbackToDestructiveMigration()。
         * 当前版本 1，尚无历史 schema，因此没有迁移对象；新增版本时补齐。
         */
        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .build()

        /** 仅供测试与「清除全部数据」后重建使用。 */
        fun closeAndReset() {
            instance?.close()
            instance = null
        }
    }
}
