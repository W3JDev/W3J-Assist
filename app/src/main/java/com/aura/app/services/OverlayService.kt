package com.aura.app.services

import android.app.Service
import android.content.*
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.aura.app.AuraApplication
import com.aura.app.ui.theme.ProjectAuraTheme

class OverlayService : Service() {
    
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private lateinit var voiceResponseReceiver: BroadcastReceiver
    private lateinit var pauseStatusReceiver: BroadcastReceiver
    
    // Mutable state for the overlay content
    private val _status = mutableStateOf("Ready")
    private val _lastResponse = mutableStateOf("")
    private val _isPaused = mutableStateOf(false)
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        if (Settings.canDrawOverlays(this)) {
            createOverlay()
            setupBroadcastReceivers()
        }
    }
    
    private fun setupBroadcastReceivers() {
        // Receiver for voice responses
        voiceResponseReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val response = intent?.getStringExtra("response") ?: ""
                val command = intent?.getStringExtra("command") ?: ""
                _status.value = "Response"
                _lastResponse.value = response
                
                // Auto-clear after 10 seconds
                android.os.Handler(mainLooper).postDelayed({
                    _status.value = if (_isPaused.value) "Paused" else "Ready"
                    _lastResponse.value = ""
                }, 10000)
            }
        }
        
        // Receiver for pause status
        pauseStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val isPaused = intent?.getBooleanExtra("isPaused", false) ?: false
                _isPaused.value = isPaused
                _status.value = if (isPaused) "Paused" else "Ready"
            }
        }
        
        // Register receivers
        val voiceFilter = IntentFilter("com.aura.app.VOICE_RESPONSE")
        val pauseFilter = IntentFilter("com.aura.app.PAUSE_STATUS")
        registerReceiver(voiceResponseReceiver, voiceFilter)
        registerReceiver(pauseStatusReceiver, pauseFilter)
    }
    
    private fun createOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val composeView = ComposeView(this).apply {
            setContent {
                ProjectAuraTheme {
                    OverlayContent(
                        status = _status.value,
                        lastResponse = _lastResponse.value,
                        isPaused = _isPaused.value,
                        onClose = { stopSelf() }
                    )
                }
            }
        }
        
        // Wrap ComposeView in a FrameLayout for proper overlay behavior
        val frameLayout = FrameLayout(this).apply {
            addView(composeView)
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }
        
        overlayView = frameLayout
        windowManager?.addView(frameLayout, params)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { view ->
            windowManager?.removeView(view)
        }
        // Unregister broadcast receivers
        try {
            unregisterReceiver(voiceResponseReceiver)
            unregisterReceiver(pauseStatusReceiver)
        } catch (e: Exception) {
            // Receivers might not be registered
        }
    }
}

@Composable
fun OverlayContent(
    status: String,
    lastResponse: String,
    isPaused: Boolean,
    onClose: () -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .size(width = 250.dp, height = if (lastResponse.isNotEmpty()) 150.dp else 100.dp)
            .background(
                Color.Black.copy(alpha = 0.8f), 
                RoundedCornerShape(12.dp)
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false }
                ) { _, _ ->
                    // Handle drag movement
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with status and close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aura",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            // Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            when {
                                isPaused -> Color.Red
                                status == "Ready" -> Color.Green
                                status == "Response" -> Color.Blue
                                else -> Color.Yellow
                            },
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = status,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
            
            // Response text (if available)
            if (lastResponse.isNotEmpty()) {
                Text(
                    text = lastResponse,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Aura",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onClose() }
                )
            }
            
            Column {
                Text(
                    text = status,
                    color = when (status) {
                        "Listening..." -> Color.Green
                        "Processing..." -> Color.Yellow
                        else -> Color.White
                    },
                    fontSize = 12.sp
                )
                
                if (lastResponse.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lastResponse.take(50) + if (lastResponse.length > 50) "..." else "",
                        color = Color.White,
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}