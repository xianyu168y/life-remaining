package com.liferemaining.app.ui

/**
 * 只服务于仪器测试的进程内开关。
 *
 * NavigationUiTest 的「首次进入 Onboarding」与「已配置后进入首页」互相冲突 ——
 * DataStore 是同进程共享的，前一个用例会污染后一个。改为由 MainActivity 在启动时
 * 调用 [readInstrumentationOverride]，读取 `-e app_test_start ONBOARDING|DASHBOARD`，
 * 覆盖 startDestination。并发安全（纯静态赋值），不依赖 JUnit 执行顺序。
 */
object TestFlags {
    @Volatile
    var startDestinationOverride: String? = null

    fun readInstrumentationOverride() {
        startDestinationOverride = try {
            val cls = Class.forName("android.app.Instrumentation")
            val m = cls.getDeclaredMethod("waitForInstrumentation")
            val inst = m.invoke(null)
            val getArgs = inst.javaClass.getMethod("getArguments")
            val args = getArgs.invoke(inst) as? android.os.Bundle
            args?.getString("app_test_start")
        } catch (_: Throwable) {
            null
        }
    }
}
