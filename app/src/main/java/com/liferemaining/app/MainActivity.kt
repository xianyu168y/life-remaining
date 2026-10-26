package com.liferemaining.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.liferemaining.app.ui.LifeRemainingRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 只在 debug 构建读取测试注入参数；release 构建这条路不会命中。
        if (BuildConfig.DEBUG) {
            com.liferemaining.app.ui.TestFlags.readInstrumentationOverride()
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as LifeRemainingApplication).container
        setContent {
            LifeRemainingRoot(container)
        }
    }
}
