package com.aura.app.services

import android.app.Service
import android.content.Intent
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
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        if (Settings.canDrawOverlays(this)) {
            createOverlay()
        }
    }
    
    private fun createOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val composeView = ComposeView(this).apply {
            setContent {
                ProjectAuraTheme {
                    OverlayContent(
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
    }
}

@Composable
fun OverlayContent(
    onClose: () -> Unit
) {
    var status by remember { mutableStateOf("Ready") }
    var lastResponse by remember { mutableStateOf("") }
    var isDragging by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .size(width = 200.dp, height = 120.dp)
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