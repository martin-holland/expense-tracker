package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.expensetracker.data.database.AndroidDatabaseContext
import com.example.theme.com.example.expensetracker.ThemeProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize database context for Android
        AndroidDatabaseContext.init(this)
        
        // Initialize database early to ensure migrations run
        // This triggers database creation and applies migrations if needed
        android.util.Log.d("MainActivity", "Initializing database...")
        try {
            val database = com.example.expensetracker.data.database.getRoomDatabase()
            // Access a DAO to ensure database is fully initialized
            database.settingsDao()
            android.util.Log.d("MainActivity", "Database initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error initializing database", e)
        }
        //create a logger instance
//        AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = VERBOSE)

        setContent {
            ThemeProvider(useDarkTheme = false) {
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