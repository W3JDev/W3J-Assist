package com.aura.app.services
import android.app.Service
import android.content.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.aura.app.AuraApplication
import com.aura.app.models.*
import com.aura.app.ui.components.*
import com.aura.app.ui.theme.ProjectAuraTheme
import com.aura.app.utils.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

class OverlayService : Service() {
    
    private var windowManager: WindowManager?  null
    private var overlayView: View?  null
    private lateinit var voiceResponseReceiver: BroadcastReceiver
    private lateinit var pauseStatusReceiver: BroadcastReceiver
    
    // Mutable state for the overlay content
    private val _status  mutableStateOf("Ready")
    private val _lastResponse  mutableStateOf("")
    private val _isPaused  mutableStateOf(false)
    
    // Enhanced accessibility and performance systems
    private lateinit var accessibilityManager: AccessibilityManager
    private lateinit var performanceMonitor: PerformanceMonitor
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Enhanced state management for real-time updates
    private val _liveInsights = mutableStateOf<List<Insight>>(emptyList())
    private val _isExpanded = mutableStateOf(false)
    private val _currentProcessingConfig = mutableStateOf(ProcessingConfig(
        updateInterval = 100L,
        confidenceThreshold = 0.5f,
        enableAnimations = true,
        enableHapticFeedback = true,
        enableAudioCues = true,
        maxConcurrentInsights = 10,
        enableRealTimeProcessing = true
    ))
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize accessibility and performance systems
        accessibilityManager = AccessibilityManager(this)
        performanceMonitor = PerformanceMonitor(this)
        
        // Monitor performance changes and adapt
        serviceScope.launch {
            performanceMonitor.processingQuality.collect { quality ->
                _currentProcessingConfig.value = performanceMonitor.getProcessingConfig()
            }
        }
        
        if (Settings.canDrawOverlays(this)) {
            createOverlay()
            setupBroadcastReceivers()
            startPerformanceOptimizedUpdates()
        }
    }
    
    private fun setupBroadcastReceivers() {
        // Receiver for voice responses
        voiceResponseReceiver  object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val response = intent?.getStringExtra("response") ?: ""
                val command = intent?.getStringExtra("command") ?: ""
                val insightType = intent?.getStringExtra("insightType")?.let { 
                    InsightType.valueOf(it) 
                } ?: InsightType.TALKING_POINT
                val confidence = intent?.getFloatExtra("confidence", 0.8f) ?: 0.8f
                
                _status.value  "Response"
                _lastResponse.value  response
                
                // Add new insight to live insights with performance optimization
                val currentInsights = _liveInsights.value.toMutableList()
                val config = _currentProcessingConfig.value
                
                if (currentInsights.size >= config.maxConcurrentInsights) {
                    currentInsights.removeFirst() // Remove oldest insight
                }
                
                val newInsight = Insight(
                    timestamp = System.currentTimeMillis(),
                    content = response,
                    confidence = confidence,
                    type = insightType
                )
                
                currentInsights.add(newInsight)
                _liveInsights.value = currentInsights
                
                // Provide accessibility feedback if enabled
                if (performanceMonitor.shouldEnableFeature(PerformanceFeature.HAPTIC_FEEDBACK)) {
                    accessibilityManager.vibrateForInsight(insightType, confidence)
                }
                
                if (performanceMonitor.shouldEnableFeature(PerformanceFeature.AUDIO_CUES)) {
                    accessibilityManager.playAudioCueForInsight(insightType, confidence)
                }
                
                // Smart notification for important insights
                accessibilityManager.notifyImportantInsight(insightType, confidence, response)
                
                // Auto-clear with adaptive timing
                android.os.Handler(mainLooper).postDelayed({
                    _status.value  if (_isPaused.value) "Paused" else "Ready"
                    _lastResponse.value  ""
                }, config.updateInterval * 100) // Adaptive clear timing
            }
        }
        
        // Receiver for pause status
        pauseStatusReceiver  object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val isPaused  intent?.getBooleanExtra("isPaused", false) ?: false
                _isPaused.value  isPaused
                _status.value  if (isPaused) "Paused" else "Ready"
            }
        }
        
        // Register receivers
        val voiceFilter  IntentFilter("com.aura.app.VOICE_RESPONSE")
        val pauseFilter  IntentFilter("com.aura.app.PAUSE_STATUS")
        registerReceiver(voiceResponseReceiver, voiceFilter)
        registerReceiver(pauseStatusReceiver, pauseFilter)
    }
    
    private fun createOverlay() {
        windowManager  getSystemService(WINDOW_SERVICE) as WindowManager
        
        val composeView  ComposeView(this).apply {
            setContent {
                ProjectAuraTheme {
                    OverlayContent(
                        status  _status.value,
                        lastResponse  _lastResponse.value,
                        isPaused  _isPaused.value,
                        onClose  { stopSelf() }
                    )
                }
            }
        }
        
        // Wrap ComposeView in a FrameLayout for proper overlay behavior
        val frameLayout  FrameLayout(this).apply {
            addView(composeView)
        }
        
        val params  WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity  Gravity.TOP or Gravity.START
            x  100
            y  100
        }
        
        overlayView  frameLayout
        windowManager?.addView(frameLayout, params)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { view -
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
    onClose: () - Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var liveInsights by remember { mutableStateOf(listOf<Insight>()) }
    var suggestions by remember { mutableStateOf(listOf<SmartSuggestion>()) }
    var showLiveMode by remember { mutableStateOf(false) }
    
    // Clipboard manager for copy functionality
    val clipboardManager = LocalClipboardManager.current
    
    // Enhanced gesture handlers
    val handleToggleExpanded = { isExpanded = !isExpanded }
    val handleCopyContent = {
        val contentToCopy = if (lastResponse.isNotEmpty()) lastResponse else "No content to copy"
        clipboardManager.setText(AnnotatedString(contentToCopy))
    }
    val handleVoiceReadback = {
        // Voice readback functionality - would integrate with AccessibilityManager
        // For now, just toggle show live mode as demo
        showLiveMode = !showLiveMode
    }
    val handleDismiss = {
        isExpanded = false
        onClose()
    }
    val handleMove = { startPos: Offset, currentPos: Offset ->
        // Drag movement - this would update overlay position
        isDragging = true
    }
    
 
    // Simulate live insights for demo
    LaunchedEffect(showLiveMode) {
        if (showLiveMode) {
            delay(1000)
            liveInsights  listOf(
                Insight(
                    timestamp  System.currentTimeMillis(),
                    content  "Active conversation detected",
                    confidence  0.95f,
                    type  InsightType.TALKING_POINT
                )
            )
            
            delay(2000)
            suggestions  listOf(
                SmartSuggestion("1", "Ask follow-up", "followup", 0.8f),
                SmartSuggestion("2", "Clarify point", "clarify", 0.7f)
            )
        }
    }
    
    AnimatedContent(
        targetState  isExpanded,
        transitionSpec  {
            slideInHorizontally(
                initialOffsetX  { if (targetState) it else -it }
            ) with slideOutHorizontally(
                targetOffsetX  { if (targetState) -it else it }
            )
        },
        label  "overlay_expansion"
    ) { expanded -
        if (expanded) {
            // Expanded Live Insight Mode
            Card(
                modifier  Modifier
                    .size(width  320.dp, height  480.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha  0.95f),
                        RoundedCornerShape(16.dp)
                    )
                    .overlayGestureDetection(
                        onToggleExpanded = handleToggleExpanded,
                        onCopyContent = handleCopyContent,
                        onVoiceReadback = handleVoiceReadback,
                        onDismiss = handleDismiss,
                        onMove = handleMove
                    ),
                colors  CardDefaults.cardColors(
                    containerColor  MaterialTheme.colorScheme.surface.copy(alpha  0.95f)
                ),
                shape  RoundedCornerShape(16.dp)

    Card(
        modifier  Modifier
            .size(width  250.dp, height  if (lastResponse.isNotEmpty()) 150.dp else 100.dp)
            .background(
                Color.Black.copy(alpha  0.8f), 
                RoundedCornerShape(12.dp)
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart  { isDragging  true },
                    onDragEnd  { isDragging  false }
                ) { _, _ -
                    // Handle drag movement
                }
            },
        colors  CardDefaults.cardColors(
            containerColor  Color.Black.copy(alpha  0.8f)
        ),
        shape  RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier  Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement  Arrangement.SpaceBetween
        ) {
            // Header with status and close button
            Row(
                modifier  Modifier.fillMaxWidth(),
                horizontalArrangement  Arrangement.SpaceBetween,
                verticalAlignment  Alignment.CenterVertically
            ) {
                Text(
                    text  "Aura",
                    color  Color.White,
                    fontSize  12.sp,
                    fontWeight  FontWeight.Bold
                )
                IconButton(
                    onClick  onClose,
                    modifier  Modifier.size(20.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription  "Close",
                        tint  Color.White,
                        modifier  Modifier.size(12.dp)
                    )
                }
            }
            
            // Status indicator
            Row(
                verticalAlignment  Alignment.CenterVertically
            ) {
                Box(
                    modifier  Modifier
                        .size(8.dp)
                        .background(
                            when {
                                isPaused - Color.Red
                                status  "Ready" - Color.Green
                                status  "Response" - Color.Blue
                                else - Color.Yellow
                            },
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
                Spacer(modifier  Modifier.width(8.dp))
                Text(
                    text  status,
                    color  Color.White,
                    fontSize  11.sp
                )
            }
            
            // Response text (if available)
            if (lastResponse.isNotEmpty()) {
                Text(
                    text  lastResponse,
                    color  Color.White.copy(alpha  0.9f),
                    fontSize  10.sp,
                    maxLines  3,
                    overflow  TextOverflow.Ellipsis,
                    modifier  Modifier.padding(top  4.dp)
                )
            }
        }
    }
}
                .padding(12.dp),
            verticalArrangement  Arrangement.SpaceBetween
        ) {
            Row(
                modifier  Modifier.fillMaxWidth(),
                horizontalArrangement  Arrangement.SpaceBetween,
                verticalAlignment  Alignment.Top
 
            ) {
                Column(
                    modifier  Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier  Modifier.fillMaxWidth(),
                        horizontalArrangement  Arrangement.SpaceBetween,
                        verticalAlignment  Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment  Alignment.CenterVertically,
                            horizontalArrangement  Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector  Icons.Default.Psychology,
                                contentDescription  "Live Insights",
                                tint  MaterialTheme.colorScheme.primary,
                                modifier  Modifier.size(20.dp)
                            )
                            Text(
                                text  "Live Insights",
                                style  MaterialTheme.typography.titleMedium,
                                fontWeight  FontWeight.Bold
                            )
                        }
                        
                        Row(
                            horizontalArrangement  Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick  { showLiveMode  !showLiveMode },
                                modifier  Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector  if (showLiveMode) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription  if (showLiveMode) "Pause" else "Start",
                                    modifier  Modifier.size(16.dp)
                                )
                            }
                            
                            IconButton(
                                onClick  { isExpanded  false },
                                modifier  Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector  Icons.Default.Minimize,
                                    contentDescription  "Minimize",
                                    modifier  Modifier.size(16.dp)
                                )
                            }
                            
                            IconButton(
                                onClick  onClose,
                                modifier  Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector  Icons.Default.Close,
                                    contentDescription  "Close",
                                    modifier  Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier  Modifier.height(12.dp))
                    
                    // Status and confidence
                    Row(
                        horizontalArrangement  Arrangement.spacedBy(12.dp),
                        verticalAlignment  Alignment.CenterVertically
                    ) {
                        Text(
                            text  status,
                            color  when (status) {
                                "Listening..." - Color(0xFF4CAF50)
                                "Processing..." - Color(0xFFFF9800)
                                else - MaterialTheme.colorScheme.onSurface
                            },
                            style  MaterialTheme.typography.bodyMedium,
                            fontWeight  FontWeight.Medium
                        )
                        
                        if (liveInsights.isNotEmpty()) {
                            ConfidenceMeter(
                                confidence  liveInsights.maxOfOrNull { it.confidence } ?: 0f,
                                modifier  Modifier
                            )
                        }
                    }
                    
                    Spacer(modifier  Modifier.height(12.dp))
                    
                    // Smart Suggestions
                    if (suggestions.isNotEmpty()) {
                        SmartSuggestionsBar(
                            suggestions  suggestions,
                            onSuggestionClick  { suggestion -
                                // Handle suggestion click
                                lastResponse  "Applied: ${suggestion.text}"
                            }
                        )
                        
                        Spacer(modifier  Modifier.height(12.dp))
                    }
                    
                    // Live Insights List
                    LazyColumn(
                        modifier  Modifier.weight(1f),
                        verticalArrangement  Arrangement.spacedBy(8.dp)
                    ) {
                        items(liveInsights) { insight -
                            InsightCard(
                                insight  insight,
                                modifier  Modifier.fillMaxWidth()
                            )
                        }
                        
                        if (liveInsights.isEmpty() && !showLiveMode) {
                            item {
                                Box(
                                    modifier  Modifier.fillMaxWidth().padding(vertical  32.dp),
                                    contentAlignment  Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment  Alignment.CenterHorizontally,
                                        verticalArrangement  Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector  Icons.Default.PlayArrow,
                                            contentDescription  "Start Live Mode",
                                            tint  MaterialTheme.colorScheme.primary,
                                            modifier  Modifier.size(32.dp)
                                        )
                                        Text(
                                            text  "Tap play to start Live Insight Mode",
                                            style  MaterialTheme.typography.bodySmall,
                                            color  MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Quick Actions
                    if (showLiveMode) {
                        Spacer(modifier  Modifier.height(8.dp))
                        QuickActionSuggestions(
                            onAction  { action -
                                lastResponse  "Action: $action"
                            }
                        )
                    }
                }
            }
        } else {
            // Compact Mode
            Card(
                modifier  Modifier
                    .size(width  200.dp, height  120.dp)
                    .background(
                        Color.Black.copy(alpha  0.8f),
                        RoundedCornerShape(12.dp)
                    )
                    .overlayGestureDetection(
                        onToggleExpanded = handleToggleExpanded,
                        onCopyContent = handleCopyContent,
                        onVoiceReadback = handleVoiceReadback,
                        onDismiss = handleDismiss,
                        onMove = handleMove
                    ),
                colors  CardDefaults.cardColors(
                    containerColor  Color.Black.copy(alpha  0.8f)
                ),
                shape  RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier  Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement  Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier  Modifier.fillMaxWidth(),
                        horizontalArrangement  Arrangement.SpaceBetween,
                        verticalAlignment  Alignment.Top
                    ) {
                        Row(
                            verticalAlignment  Alignment.CenterVertically,
                            horizontalArrangement  Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text  "Aura",
                                color  Color.White,
                                fontSize  14.sp,
                                fontWeight  FontWeight.Bold
                            )
                            if (liveInsights.isNotEmpty()) {
                                Box(
                                    modifier  Modifier
                                        .size(6.dp)
                                        .background(
                                            Color(0xFF4CAF50),
                                            androidx.compose.foundation.shape.CircleShape
                                        )
                                )
                            }
                        }
                        
                        Row(
                            horizontalArrangement  Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector  Icons.Default.OpenInFull,
                                contentDescription  "Expand",
                                tint  Color.White,
                                modifier  Modifier
                                    .size(16.dp)
                                    .clickable { isExpanded  true }
                            )
                            Icon(
                                imageVector  Icons.Default.Close,
                                contentDescription  "Close",
                                tint  Color.White,
                                modifier  Modifier
                                    .size(16.dp)
                                    .clickable { onClose() }
                            )
                        }
                    }
                    
                    Column {
                        Row(
                            horizontalArrangement  Arrangement.spacedBy(8.dp),
                            verticalAlignment  Alignment.CenterVertically
                        ) {
                            Text(
                                text  status,
                                color  when (status) {
                                    "Listening..." - Color.Green
                                    "Processing..." - Color.Yellow
                                    else - Color.White
                                },
                                fontSize  12.sp
                            )
                            
                            if (liveInsights.isNotEmpty()) {
                                Text(
                                    text  "${liveInsights.size}",
                                    color  Color(0xFF4CAF50),
                                    fontSize  10.sp,
                                    modifier  Modifier
                                        .background(
                                            Color(0xFF4CAF50).copy(alpha  0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal  4.dp, vertical  2.dp)
                                )
                            }
                        }
                        
                        if (lastResponse.isNotEmpty()) {
                            Spacer(modifier  Modifier.height(4.dp))
                            Text(
                                text  lastResponse.take(50) + if (lastResponse.length  50) "..." else "",
                                color  Color.White,
                                fontSize  10.sp,
                                lineHeight  12.sp
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Start performance-optimized real-time updates
     */
    private fun startPerformanceOptimizedUpdates() {
        serviceScope.launch {
            while (true) {
                val config = _currentProcessingConfig.value
                
                if (config.enableRealTimeProcessing && _liveInsights.value.isNotEmpty()) {
                    // Simulate real-time insight updates with performance-aware timing
                    delay(config.updateInterval)
                    
                    // Optional: Update insight confidence scores or add new insights
                    // This would be connected to real AI processing in production
                } else {
                    // Use longer intervals when real-time processing is disabled
                    delay(config.updateInterval * 5)
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Cleanup enhanced systems
        accessibilityManager.cleanup()
        performanceMonitor.cleanup()
        
        // Cleanup existing functionality
        try {
            unregisterReceiver(voiceResponseReceiver)
            unregisterReceiver(pauseStatusReceiver)
        } catch (e: Exception) {
            // Receivers may not be registered
        }
        
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                // View may not be attached
            }
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}