package com.liferemaining.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 二级页容器：统一避开状态栏、导航栏与手势区。
 *
 * 只用在没有自己的 Scaffold 的页面上。已经用了 Scaffold 的页面（目标列表、目标编辑、
 * 重要日期列表、设置）由 Scaffold 处理 insets，再套一层会多出一倍留白。
 */
@Composable
fun SecondaryPage(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        content()
    }
}
