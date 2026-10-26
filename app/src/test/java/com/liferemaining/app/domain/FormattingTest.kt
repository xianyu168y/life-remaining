package com.liferemaining.app.domain

import com.liferemaining.app.util.Format
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalDate

class FormattingTest {

    @Test
    fun `grouped numbers use comma separators`() {
        assertEquals("0", Format.grouped(0))
        assertEquals("999", Format.grouped(999))
        assertEquals("1,000", Format.grouped(1000))
        assertEquals("18,420", Format.grouped(18420))
    }

    @Test
    fun `percent has one decimal`() {
        assertEquals("35.4%", Format.percent(35.4))
        assertEquals("100.0%", Format.percent(100.0))
    }

    @Test
    fun `days until phrasing`() {
        assertEquals("就是今天", Format.daysUntil(0))
        assertEquals("明天", Format.daysUntil(1))
        assertEquals("还有 42 天", Format.daysUntil(42))
        assertEquals("已过去 3 天", Format.daysUntil(-3))
    }

    @Test
    fun `iso date is stable`() {
        assertEquals("2026-10-26", Format.isoDate(LocalDate.of(2026, 10, 26)))
    }

    @Test
    fun `duration short`() {
        assertEquals("0 分钟", Format.durationShort(Duration.ofSeconds(30)))
        assertEquals("45 分钟", Format.durationShort(Duration.ofMinutes(45)))
        assertEquals("2 小时 5 分", Format.durationShort(Duration.ofMinutes(125)))
    }
}
