package com.liferemaining.app.domain.model

import java.time.LocalDateTime

/** 挂在某一年的回忆或事件，用于时间轴的年度详情。 */
data class YearNote(
    val id: Long = 0L,
    val year: Int,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
