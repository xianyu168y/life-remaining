package com.liferemaining.app.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    val category: String,
    val createdAt: LocalDate,
    val dueDate: LocalDate?,
    val status: String,
    val priority: String,
    val isImportant: Boolean
)

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val date: LocalDate,
    val category: String,
    val note: String,
    val repeatsAnnually: Boolean
)

@Entity(
    tableName = "reflections",
    indices = [Index(value = ["date"], unique = true)]
)
data class ReflectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: LocalDate,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

@Entity(
    tableName = "year_notes",
    indices = [Index(value = ["year"])]
)
data class YearNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val year: Int,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
