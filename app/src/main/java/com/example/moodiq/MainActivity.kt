package com.example.moodiq

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.moodiq.core.MoodiqApp
import com.example.moodiq.ui.navigation.MoodiqNavGraph
import com.example.moodiq.ui.theme.MoodiqTheme
import com.example.moodiq.ui.viewmodel.MainViewModel
import com.example.moodiq.workers.SmartNotificationWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleSmartReminder()

        setContent {
            val container = (application as MoodiqApp).appContainer
            val vm: MainViewModel = viewModel(factory = MainViewModel.factory(container))
            MoodiqTheme { PermissionGate(vm) }
        }
    }

    private fun scheduleSmartReminder() {
        val request = PeriodicWorkRequestBuilder<SmartNotificationWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "smart_notification",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}

@Composable
private fun PermissionGate(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val mediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val recordPermission = Manifest.permission.RECORD_AUDIO

    val mediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { mediaGranted ->
        val recordGranted = ContextCompat.checkSelfPermission(
            context,
            recordPermission
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setPermissionsGranted(mediaGranted, recordGranted)
    }

    val recordLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { recordGranted ->
        val mediaGranted = ContextCompat.checkSelfPermission(
            context,
            mediaPermission
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setPermissionsGranted(mediaGranted, recordGranted)
    }

    LaunchedEffect(Unit) {
        val mediaGranted = ContextCompat.checkSelfPermission(
            context,
            mediaPermission
        ) == PackageManager.PERMISSION_GRANTED
        val recordGranted = ContextCompat.checkSelfPermission(
            context,
            recordPermission
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setPermissionsGranted(mediaGranted, recordGranted)
    }

    LaunchedEffect(state.permissionGranted, state.recordPermissionGranted) {
        if (state.permissionGranted && !state.recordPermissionGranted) {
            recordLauncher.launch(recordPermission)
        }
    }

    if (state.permissionGranted) {
        MoodiqNavGraph(viewModel)
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { mediaLauncher.launch(mediaPermission) }) {
                Text("Grant music access")
            }
        }
    }
}
