package com.aura.app.services

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.aura.app.AuraApplication
import com.aura.app.models.*
import com.aura.app.ui.components.*
import com.aura.app.ui.theme.ProjectAuraTheme
import kotlinx.coroutines.delay

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
    var isExpanded by remember { mutableStateOf(false) }
    var liveInsights by remember { mutableStateOf(listOf<Insight>()) }
    var suggestions by remember { mutableStateOf(listOf<SmartSuggestion>()) }
    var showLiveMode by remember { mutableStateOf(false) }
    
    // Simulate live insights for demo
    LaunchedEffect(showLiveMode) {
        if (showLiveMode) {
            delay(1000)
            liveInsights = listOf(
                Insight(
                    timestamp = System.currentTimeMillis(),
                    content = "Active conversation detected",
                    confidence = 0.95f,
                    type = InsightType.TALKING_POINT
                )
            )
            
            delay(2000)
            suggestions = listOf(
                SmartSuggestion("1", "Ask follow-up", "followup", 0.8f),
                SmartSuggestion("2", "Clarify point", "clarify", 0.7f)
            )
        }
    }
    
    AnimatedContent(
        targetState = isExpanded,
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { if (targetState) it else -it }
            ) with slideOutHorizontally(
                targetOffsetX = { if (targetState) -it else it }
            )
        },
        label = "overlay_expansion"
    ) { expanded ->
        if (expanded) {
            // Expanded Live Insight Mode
            Card(
                modifier = Modifier
                    .size(width = 320.dp, height = 480.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        RoundedCornerShape(16.dp)
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
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Live Insights",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Live Insights",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { showLiveMode = !showLiveMode },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (showLiveMode) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (showLiveMode) "Pause" else "Start",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            IconButton(
                                onClick = { isExpanded = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Minimize,
                                    contentDescription = "Minimize",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Status and confidence
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = status,
                            color = when (status) {
                                "Listening..." -> Color(0xFF4CAF50)
                                "Processing..." -> Color(0xFFFF9800)
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (liveInsights.isNotEmpty()) {
                            ConfidenceMeter(
                                confidence = liveInsights.maxOfOrNull { it.confidence } ?: 0f,
                                modifier = Modifier
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Smart Suggestions
                    if (suggestions.isNotEmpty()) {
                        SmartSuggestionsBar(
                            suggestions = suggestions,
                            onSuggestionClick = { suggestion ->
                                // Handle suggestion click
                                lastResponse = "Applied: ${suggestion.text}"
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Live Insights List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(liveInsights) { insight ->
                            InsightCard(
                                insight = insight,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        if (liveInsights.isEmpty() && !showLiveMode) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Start Live Mode",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            text = "Tap play to start Live Insight Mode",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Quick Actions
                    if (showLiveMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        QuickActionSuggestions(
                            onAction = { action ->
                                lastResponse = "Action: $action"
                            }
                        )
                    }
                }
            }
        } else {
            // Compact Mode
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Aura",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (liveInsights.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            Color(0xFF4CAF50),
                                            androidx.compose.foundation.shape.CircleShape
                                        )
                                )
                            }
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = "Expand",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { isExpanded = true }
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
                    }
                    
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = status,
                                color = when (status) {
                                    "Listening..." -> Color.Green
                                    "Processing..." -> Color.Yellow
                                    else -> Color.White
                                },
                                fontSize = 12.sp
                            )
                            
                            if (liveInsights.isNotEmpty()) {
                                Text(
                                    text = "${liveInsights.size}",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 10.sp,
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF4CAF50).copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
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
    }
}