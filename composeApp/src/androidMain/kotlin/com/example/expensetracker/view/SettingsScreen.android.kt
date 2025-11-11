package com.example.expensetracker.view

//import android.Manifest
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.platform.LocalContext
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.expensetracker.viewmodel.SettingsViewModel
//
//// androidMain/SettingsScreen.android.kt
//@Composable
//fun AndroidSettingsScreen(
//    viewModel: SettingsViewModel = viewModel()
//) {
//    val context = LocalContext.current
//    val activity = context as ComponentActivity
//
//    val permissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted) {
//            println("✅ Permission granted!")
//            viewModel.toggleVoiceInput(true)
//        } else {
//            println("❌ Permission denied!")
//            viewModel.toggleVoiceInput(false)
//        }
//    }
//
//    // Use the common SettingsScreen but provide Android-specific permission handler
//    SettingsScreen(
//        viewModel = viewModel,
//        onPermissionRequest = {
//            println("🎤 Launching system permission dialog")
//            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
//        }
//    )
//}