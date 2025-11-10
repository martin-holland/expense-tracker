// androidMain/MainActivity.kt
package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.theme.com.example.expensetracker.ThemeProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        //create a logger instance
//        AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = VERBOSE)


        setContent {
            ThemeProvider {
                AppContent()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}