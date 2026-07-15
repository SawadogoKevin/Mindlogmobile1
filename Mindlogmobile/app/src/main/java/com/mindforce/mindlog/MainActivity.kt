package com.mindforce.mindlog

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mindforce.mindlog.ui.navigation.MindForceNavGraph
import com.mindforce.mindlog.ui.theme.MindForceTheme
import com.mindforce.mindlog.ui.theme.MindWhite

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MindForceApp

        setContent {
            MindForceTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MindWhite) {
                    MindForceNavGraph(app = app)
                }
            }
        }
    }
}
