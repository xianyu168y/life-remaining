# 余生 · Life Remaining

一款参考 KernelSU Manager 界面语言的 Android 原生「生命倒计时」应用。
它不显示「还剩多少天」这一个数字，而是把时间拆成可观察、可行动的三层：

1. **生命进度** —— 剩余天数、年龄、进度环、预计终点；
2. **周期进度** —— 年 / 月 / 周的真实日历进度；
3. **可执行的事** —— 目标、重要日期、每日记录。

视觉上借鉴 KernelSU / Miuix 的工具型布局：分层卡片、清晰的状态标识、
克制的低饱和强调色、整齐的列表与设置分组。没有复制其品牌标识、图标或 Root 功能。

## 状态

本仓库通过 GitHub Actions 在云端构建与测试，不在本地构建。

| 检查 | 命令 |
| --- | --- |
| 单元测试 | `./gradlew testDebugUnitTest` |
| Debug 构建 | `./gradlew assembleDebug` |
| 仪器测试 | `./gradlew connectedDebugAndroidTest` |

工作流文件：`.github/workflows/android.yml`
- `build-and-test`：JDK 17 + Android SDK 35，执行 64 个 JVM 单元测试、`assembleDebug`、lint（仅报告），
  上传 `unit-test-report` 与 `app-debug-apk`。
- `instrumented-test`：API 34 模拟器运行 17 个 `connectedDebugAndroidTest`（含 4 个真机渲染烟测），
  上传 `instrumented-test-report`。

APK 从对应 GitHub Actions run 的 **Artifacts → app-debug-apk** 下载。
测试报告在 **Artifacts → unit-test-report / instrumented-test-report**。

## 功能

- **首页**：生命主卡片、进度环、年/月/周进度、今日行动、目标预览、重要日期预览、下拉刷新；
- **时间轴**：年 / 月 / 周三视图，年份节点按过去/今年/未来分状态，可进入年度详情写回忆；
- **目标**：新增、编辑、完成、删除、筛选、排序、优先级、重要人生目标标记；
- **重要日期**：年度重复按真实日历计算下一次（2 月 29 日在平年落到 2 月 28 日）；
- **每日记录**：按日期保存、编辑、删除、浏览历史；
- **设置**：主题（跟随系统/浅色/深色）、强调色、动态颜色、生命参数、倒计时开关、
  隐藏精确数字、每日提醒（时间与语气）、导入导出、清除数据二次确认、隐私说明。

## 计算口径（唯一标准）

代码位置：`app/src/main/java/com/liferemaining/app/domain/calculator/LifeCountdownCalculator.kt`

- 全部使用 `LocalDate` 日历日运算，不用毫秒差、不用固定 365 天、不把月份当 30 天；
- 已度过天数 = `DAYS.between(出生日期, 今天)`，出生当天为 0 天；
- 预计终点 = `出生日期.plusYears(预期寿命)`；2 月 29 日在平年回退到 2 月 28 日（Java Time 规则）；
- 剩余天数 = `DAYS.between(今天, 预计终点)`，夹到 ≥ 0；到期当天为 0，之后不会出现负数；
- 生命进度 = 已度过 / 总天数，夹到 0–100；
- 预期寿命合法区间 1–120；非法值视为未配置，不显示伪造数字。

以上是数学估算，不是医学预测。

## 数据与隐私

- 无账号、无服务器、无广告、无第三方追踪 SDK；
- 出生日期、目标、重要日期、记录全部保存在本机 Room 数据库与 DataStore；
- 自身只声明通知（可选提醒）与开机广播（恢复提醒）；WorkManager 合并 WAKE_LOCK / 网络状态查询用于调度提醒，不发送任何数据；不申请通讯录、位置、短信、存储；
- 导出为本地 JSON（系统文件选择器）；导入先完整校验，失败不覆盖现有数据；
- 清除数据需要二次确认；
- 系统云备份与设备迁移已对本 App 关闭（`data_extraction_rules.xml`）。

## 技术栈

Kotlin · Jetpack Compose · Material 3 · Navigation Compose · ViewModel + StateFlow ·
Room · DataStore · WorkManager · kotlinx.serialization · JUnit4 · Compose UI Test

## 目录

```
app/src/main/java/com/liferemaining/app/
├── MainActivity.kt
├── LifeRemainingApplication.kt
├── AppContainer.kt
├── navigation/Routes.kt
├── ui/
│   ├── LifeRemainingRoot.kt      导航图
│   ├── MainScaffold.kt           底部四个入口
│   ├── theme/                    颜色、排版、圆角
│   ├── components/               卡片、列表、进度环、周格、日期选择
│   ├── dashboard/                首页、周期详情
│   ├── timeline/                 时间轴、年度详情
│   ├── goals/                    列表、详情、编辑
│   ├── milestones/               列表、编辑
│   ├── reflection/               每日记录、历史
│   ├── settings/                 设置、生命参数、隐私
│   └── onboarding/               五步首次配置
├── domain/
│   ├── model/                    不可变数据模型
│   └── calculator/               生命 / 周期 / 重要日期计算
├── data/
│   ├── database/                 Room 实体、DAO、数据库
│   ├── datastore/                偏好
│   ├── repository/               仓库与 mapper
│   └── backup/                   JSON 编解码与导入导出
├── notifications/                提醒调度、开机恢复
└── util/                         日期格式、行动文案、今天提供者
```

## 测试覆盖

`app/src/test/`
- `LifeCountdownCalculatorTest`：空参数、出生当天、普通日期、闰年 2 月 29 日、到期日、
  逾期、进度边界、非法寿命、未来出生日期、跨月、跨年；
- `TimeProgressCalculatorTest`：年/月/周分母、周一起点、周格状态、elapsed + remaining = total；
- `MilestoneCalculatorTest`：年度重复、跨年、闰日回退、一次性事件已过去、排序与去重；
- `BackupCodecTest`：往返一致、非法 JSON、错误 app 标识、未来版本、非法寿命、错误日期、空标题；
- `ReminderScheduleTest` / `FormattingTest` / `PromptSeedTest` / `ProfileDefaultsTest`。

`app/src/androidTest/`（17 个）
- `DashboardUiTest`：核心数字可见、页面可滚动、空状态可点击、隐藏数字；
- `PersistenceTest`：关闭并重开数据库后目标仍在、每日记录按日期唯一、清空生效；
- `NavigationUiTest`：首次启动进入 Onboarding；四个底部入口真实切换；
- `LocalizationTest`：深浅主题 × 四种强调色的排版回归；
- `RenderSmokeTest`：在真实设备渲染深色/浅色/苔绿/空状态首页，断言实际绘制出非空画面。
