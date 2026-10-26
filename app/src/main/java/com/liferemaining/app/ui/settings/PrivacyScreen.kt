package com.liferemaining.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.liferemaining.app.ui.components.LifeCard
import com.liferemaining.app.ui.components.SectionHeader
import com.liferemaining.app.ui.theme.Spacing

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg)
            .padding(top = Spacing.md, bottom = Spacing.xxl)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "隐私与数据",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(Spacing.lg))
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "这个 App 没有服务器。",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = "出生日期、预期寿命、目标、重要日期和每日记录全部保存在你手机的本地数据库中。没有账号，没有云端同步，没有广告与第三方统计 SDK。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(Spacing.lg))
        SectionHeader(title = "具体做法")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            PrivacyItem("需要的权限", "只有通知权限（用于可选的每日提醒）与开机广播（用于恢复提醒）。不申请通讯录、位置、短信、存储等权限。")
            Divider()
            PrivacyItem("网络", "App 运行时不发起网络请求。除你在设置里点开「开源许可」外链。")
            Divider()
            PrivacyItem("导出", "只在你主动点击「导出数据」时生成本地 JSON 文件，写入你通过系统文件选择器指定的位置。")
            Divider()
            PrivacyItem("导入", "先完整校验版本、结构与日期格式。任何不合法数据都会被拒绝，且不会覆盖现有内容。")
            Divider()
            PrivacyItem("清除", "需要二次确认。确认后删除本机全部业务数据与偏好，并取消提醒。")
            Divider()
            PrivacyItem("备份", "Android 系统云备份与设备迁移已对本 App 关闭，数据不会被系统同步到云端。")
        }

        Spacer(Modifier.height(Spacing.lg))
        SectionHeader(title = "没有做的事")
        LifeCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "没有上传出生日期；没有上传任何记录；没有接入广告；没有接入第三方追踪；没有要求注册或登录；没有伪造「安全加密云端存储」这类未实现的能力。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = "以上描述与当前实现一致。若后续版本改变数据流向，会先更新这段说明并在更新日志中注明。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PrivacyItem(title: String, body: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(2.dp))
    Text(
        text = body,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Divider() {
    Spacer(Modifier.height(Spacing.md))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(Modifier.height(Spacing.md))
}
