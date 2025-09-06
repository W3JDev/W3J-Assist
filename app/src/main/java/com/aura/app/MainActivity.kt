package com.aura.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aura.app.services.OverlayService
import com.aura.app.services.WakeWordService
import com.aura.app.ui.theme.ProjectAuraTheme
import com.aura.app.ui.viewmodels.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* Handle overlay permission result */ }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectAuraTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val savedResponses by viewModel.savedResponses.observeAsState(emptyList())
    val isOverlayEnabled by viewModel.isOverlayEnabled.observeAsState(false)
    val isListening by viewModel.isListening.observeAsState(false)
    
    // Request multiple permissions
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )
    
    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App title
        Text(
            text = "Project Aura",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Permission status
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Permissions Status:", style = MaterialTheme.typography.titleMedium)
                
                PermissionStatusItem(
                    name = "Camera",
                    granted = permissionsState.permissions.find { 
                        it.permission == Manifest.permission.CAMERA 
                    }?.hasPermission == true
                )
                
                PermissionStatusItem(
                    name = "Microphone", 
                    granted = permissionsState.permissions.find { 
                        it.permission == Manifest.permission.RECORD_AUDIO 
                    }?.hasPermission == true
                )
                
                PermissionStatusItem(
                    name = "Overlay",
                    granted = Settings.canDrawOverlays(context)
                )
            }
        }
        
        // Controls
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Controls:", style = MaterialTheme.typography.titleMedium)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            if (!Settings.canDrawOverlays(context)) {
                                // Request overlay permission
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } else {
                                viewModel.toggleOverlay(context)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isOverlayEnabled) "Disable Overlay" else "Enable Overlay")
                    }
                    
                    Button(
                        onClick = { viewModel.toggleListening(context) },
                        enabled = permissionsState.permissions.find { 
                            it.permission == Manifest.permission.RECORD_AUDIO 
                        }?.hasPermission == true,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isListening) "Stop Listening" else "Start Listening")
                    }
                }
                
                Button(
                    onClick = { viewModel.startOCRMode(context) },
                    enabled = permissionsState.permissions.find { 
                        it.permission == Manifest.permission.CAMERA 
                    }?.hasPermission == true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Text("OCR Mode")
                    }
                }
            }
        }
        
        // Saved responses
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Saved Notes:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (savedResponses.isEmpty()) {
                    Text(
                        "No notes saved yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savedResponses) { response ->
                            SavedResponseItem(
                                response = response,
                                onDelete = { viewModel.deleteResponse(response) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionStatusItem(name: String, granted: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Text(
            text = "$name: ${if (granted) "Granted" else "Denied"}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SavedResponseItem(
    response: com.aura.app.models.SavedResponse,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = response.inputText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = response.aiResponse,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}