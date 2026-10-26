package com.liferemaining.app.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun findById(id: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GoalEntity): Long

    @Update
    suspend fun update(entity: GoalEntity)

    @Delete
    suspend fun delete(entity: GoalEntity)

    @Query("DELETE FROM goals")
    suspend fun deleteAll()
}

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones ORDER BY date ASC, id ASC")
    fun observeAll(): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE id = :id")
    suspend fun findById(id: Long): MilestoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MilestoneEntity): Long

    @Update
    suspend fun update(entity: MilestoneEntity)

    @Delete
    suspend fun delete(entity: MilestoneEntity)

    @Query("DELETE FROM milestones")
    suspend fun deleteAll()
}

@Dao
interface ReflectionDao {
    @Query("SELECT * FROM reflections ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<ReflectionEntity>>

    @Query("SELECT * FROM reflections WHERE date = :date LIMIT 1")
    fun observeByDate(date: LocalDate): Flow<ReflectionEntity?>

    @Query("SELECT * FROM reflections WHERE date = :date LIMIT 1")
    suspend fun findByDate(date: LocalDate): ReflectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReflectionEntity): Long

    @Update
    suspend fun update(entity: ReflectionEntity)

    @Delete
    suspend fun delete(entity: ReflectionEntity)

    @Query("DELETE FROM reflections")
    suspend fun deleteAll()
}

@Dao
interface YearNoteDao {
    @Query("SELECT * FROM year_notes ORDER BY year DESC, id DESC")
    fun observeAll(): Flow<List<YearNoteEntity>>

    @Query("SELECT * FROM year_notes WHERE year = :year ORDER BY id ASC")
    fun observeByYear(year: Int): Flow<List<YearNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: YearNoteEntity): Long

    @Update
    suspend fun update(entity: YearNoteEntity)

    @Delete
    suspend fun delete(entity: YearNoteEntity)

    @Query("DELETE FROM year_notes")
    suspend fun deleteAll()
}
