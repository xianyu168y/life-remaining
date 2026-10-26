package com.liferemaining.app.data.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

/** 全部按 ISO-8601 存取，跨时区/跨地区都不会歧义。 */
class Converters {
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)
}
