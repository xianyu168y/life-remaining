package com.liferemaining.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.liferemaining.app.ui.LifeRemainingRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as LifeRemainingApplication).container
        setContent {
            LifeRemainingRoot(container)
        }
    }
}
