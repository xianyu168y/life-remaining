package com.liferemaining.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/** 每天最多一条记录；同一天再次保存视为编辑。 */
data class DailyReflection(
    val id: Long = 0L,
    val date: LocalDate,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
