package com.liferemaining.app.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 全 App 共享的「今天」。
 *
 * 为什么不用每条 Flow 各自 LocalDate.now()：
 * 页面在后台停留跨过午夜后，所有统计必须同时翻页，不能出现首页已换天、
 * 时间轴还是昨天的状态。所有 ViewModel 订阅同一个 StateFlow。
 */
class TodayProvider(private val clock: () -> java.time.LocalDate = { AppClock.today() }) {

    private val _today = MutableStateFlow(clock())
    val today: StateFlow<java.time.LocalDate> = _today.asStateFlow()

    fun refresh() {
        val current = clock()
        if (_today.value != current) {
            _today.value = current
        }
    }
}
