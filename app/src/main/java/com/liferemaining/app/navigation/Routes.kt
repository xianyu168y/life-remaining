package com.liferemaining.app.navigation

/** 全部导航目标集中定义，避免字符串散落在各页面。 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val TIMELINE = "timeline"
    const val GOALS = "goals"
    const val MILESTONES = "milestones"
    const val SETTINGS = "settings"
    const val PRIVACY = "privacy"
    const val REFLECTIONS = "reflections"
    const val PROFILE_DETAIL = "profile"

    const val GOAL_DETAIL = "goal/{goalId}"
    fun goalDetail(id: Long) = "goal/$id"

    const val GOAL_EDIT = "goal-edit?goalId={goalId}"
    fun goalEdit(id: Long?) = if (id == null) "goal-edit?goalId=-1" else "goal-edit?goalId=$id"

    const val MILESTONE_EDIT = "milestone-edit?milestoneId={milestoneId}"
    fun milestoneEdit(id: Long?) =
        if (id == null) "milestone-edit?milestoneId=-1" else "milestone-edit?milestoneId=$id"

    const val REFLECTION = "reflection?date={date}"
    fun reflection(date: String) = "reflection?date=$date"

    const val PERIOD = "period/{period}"
    fun period(period: String) = "period/$period"

    const val YEAR_DETAIL = "year/{year}"
    fun yearDetail(year: Int) = "year/$year"
}
