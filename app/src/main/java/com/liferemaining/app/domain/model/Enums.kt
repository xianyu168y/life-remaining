package com.liferemaining.app.domain.model

/** 外观主题。默认跟随系统。 */
enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色");

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

/** 低饱和度强调色。键值用于 DataStore 持久化，避免存颜色值。 */
enum class AccentKey(val storageKey: String, val label: String) {
    TEAL("teal", "青绿"),
    MOSS("moss", "苔绿"),
    BLUE("blue", "雾蓝"),
    AMBER("amber", "赭石");

    companion object {
        fun fromStorage(value: String?): AccentKey =
            entries.firstOrNull { it.storageKey == value } ?: TEAL
    }
}

/** 每日提醒的语气。通知文案随语气变化，但不制造恐惧。 */
enum class ReminderTone(val label: String) {
    GENTLE("温和"),
    NEUTRAL("中性"),
    DIRECT("直接")
}

enum class GoalCategory(val label: String) {
    HEALTH("健康与生活"),
    FAMILY("家庭与关系"),
    LEARNING("学习与成长"),
    TRAVEL("旅行与体验"),
    CAREER("事业与创造"),
    OTHER("其他");

    companion object {
        fun fromStorage(value: String?): GoalCategory =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}

enum class GoalStatus(val label: String) {
    ACTIVE("进行中"),
    COMPLETED("已完成");

    companion object {
        fun fromStorage(value: String?): GoalStatus =
            entries.firstOrNull { it.name == value } ?: ACTIVE
    }
}

enum class GoalPriority(val label: String) {
    LOW("低"),
    MEDIUM("中"),
    HIGH("高");

    companion object {
        fun fromStorage(value: String?): GoalPriority =
            entries.firstOrNull { it.name == value } ?: MEDIUM
    }
}

enum class MilestoneCategory(val label: String) {
    BIRTHDAY("生日"),
    ANNIVERSARY("纪念日"),
    TRAVEL("旅行"),
    DEADLINE("截止日期"),
    OTHER("其他");

    companion object {
        fun fromStorage(value: String?): MilestoneCategory =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}
