package com.aura.app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Runtime

class PerformanceMonitor(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private val powerManager = context.getSystemService<PowerManager>()
    
    // Performance metrics
    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()
    
    private val _isLowPowerMode = MutableStateFlow(false)
    val isLowPowerMode: StateFlow<Boolean> = _isLowPowerMode.asStateFlow()
    
    private val _memoryUsage = MutableStateFlow(0.0)
    val memoryUsage: StateFlow<Double> = _memoryUsage.asStateFlow()
    
    private val _processingQuality = MutableStateFlow(ProcessingQuality.HIGH)
    val processingQuality: StateFlow<ProcessingQuality> = _processingQuality.asStateFlow()
    
    // Performance thresholds
    companion object {
        private const val LOW_BATTERY_THRESHOLD = 20
        private const val CRITICAL_BATTERY_THRESHOLD = 10
        private const val HIGH_MEMORY_USAGE_THRESHOLD = 0.8 // 80%
        private const val CRITICAL_MEMORY_USAGE_THRESHOLD = 0.9 // 90%
        private const val MEMORY_CHECK_INTERVAL = 30000L // 30 seconds
    }
    
    enum class ProcessingQuality {
        HIGH,      // Full processing, all features enabled
        MEDIUM,    // Reduced processing, some features disabled
        LOW,       // Minimal processing, essential features only
        CRITICAL   // Emergency mode, minimal functionality
    }
    
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val batteryPercentage = (level * 100) / scale
                    
                    _batteryLevel.value = batteryPercentage
                    updateProcessingQuality()
                }
                Intent.ACTION_POWER_SAVE_MODE_CHANGED -> {
                    _isLowPowerMode.value = powerManager?.isPowerSaveMode ?: false
                    updateProcessingQuality()
                }
            }
        }
    }
    
    init {
        startMonitoring()
    }
    
    private fun startMonitoring() {
        // Register battery monitoring
        val batteryFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_SAVE_MODE_CHANGED)
        }
        context.registerReceiver(batteryReceiver, batteryFilter)
        
        // Start memory monitoring
        startMemoryMonitoring()
        
        // Initial state
        updateProcessingQuality()
    }
    
    private fun startMemoryMonitoring() {
        scope.launch {
            while (true) {
                updateMemoryUsage()
                kotlinx.coroutines.delay(MEMORY_CHECK_INTERVAL)
            }
        }
    }
    
    private fun updateMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        
        val memoryUsagePercentage = usedMemory.toDouble() / maxMemory.toDouble()
        _memoryUsage.value = memoryUsagePercentage
        
        updateProcessingQuality()
    }
    
    private fun updateProcessingQuality() {
        val batteryLevel = _batteryLevel.value
        val isLowPower = _isLowPowerMode.value
        val memoryUsage = _memoryUsage.value
        
        val newQuality = when {
            // Critical conditions
            batteryLevel <= CRITICAL_BATTERY_THRESHOLD || 
            memoryUsage >= CRITICAL_MEMORY_USAGE_THRESHOLD -> ProcessingQuality.CRITICAL
            
            // Low performance conditions
            batteryLevel <= LOW_BATTERY_THRESHOLD || 
            isLowPower || 
            memoryUsage >= HIGH_MEMORY_USAGE_THRESHOLD -> ProcessingQuality.LOW
            
            // Medium performance conditions
            batteryLevel <= 50 || 
            memoryUsage >= 0.6 -> ProcessingQuality.MEDIUM
            
            // High performance (default)
            else -> ProcessingQuality.HIGH
        }
        
        _processingQuality.value = newQuality
    }
    
    /**
     * Get processing configuration based on current performance state
     */
    fun getProcessingConfig(): ProcessingConfig {
        return when (_processingQuality.value) {
            ProcessingQuality.HIGH -> ProcessingConfig(
                updateInterval = 100L, // 100ms - very responsive
                confidenceThreshold = 0.5f,
                enableAnimations = true,
                enableHapticFeedback = true,
                enableAudioCues = true,
                maxConcurrentInsights = 10,
                enableRealTimeProcessing = true
            )
            
            ProcessingQuality.MEDIUM -> ProcessingConfig(
                updateInterval = 250L, // 250ms - good responsiveness
                confidenceThreshold = 0.6f,
                enableAnimations = true,
                enableHapticFeedback = true,
                enableAudioCues = false,
                maxConcurrentInsights = 7,
                enableRealTimeProcessing = true
            )
            
            ProcessingQuality.LOW -> ProcessingConfig(
                updateInterval = 500L, // 500ms - reduced responsiveness
                confidenceThreshold = 0.7f,
                enableAnimations = false,
                enableHapticFeedback = false,
                enableAudioCues = false,
                maxConcurrentInsights = 5,
                enableRealTimeProcessing = false
            )
            
            ProcessingQuality.CRITICAL -> ProcessingConfig(
                updateInterval = 1000L, // 1s - minimal responsiveness
                confidenceThreshold = 0.8f,
                enableAnimations = false,
                enableHapticFeedback = false,
                enableAudioCues = false,
                maxConcurrentInsights = 3,
                enableRealTimeProcessing = false
            )
        }
    }
    
    /**
     * Check if feature should be enabled based on current performance
     */
    fun shouldEnableFeature(feature: PerformanceFeature): Boolean {
        val quality = _processingQuality.value
        
        return when (feature) {
            PerformanceFeature.ANIMATIONS -> quality in listOf(ProcessingQuality.HIGH, ProcessingQuality.MEDIUM)
            PerformanceFeature.HAPTIC_FEEDBACK -> quality in listOf(ProcessingQuality.HIGH, ProcessingQuality.MEDIUM)
            PerformanceFeature.AUDIO_CUES -> quality == ProcessingQuality.HIGH
            PerformanceFeature.REAL_TIME_PROCESSING -> quality in listOf(ProcessingQuality.HIGH, ProcessingQuality.MEDIUM)
            PerformanceFeature.BACKGROUND_PROCESSING -> quality != ProcessingQuality.CRITICAL
            PerformanceFeature.VOICE_RECOGNITION -> true // Always enabled but quality may be reduced
        }
    }
    
    /**
     * Get recommended update interval based on performance
     */
    fun getUpdateInterval(): Long = getProcessingConfig().updateInterval
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Receiver may not be registered
        }
    }
}

data class ProcessingConfig(
    val updateInterval: Long,
    val confidenceThreshold: Float,
    val enableAnimations: Boolean,
    val enableHapticFeedback: Boolean,
    val enableAudioCues: Boolean,
    val maxConcurrentInsights: Int,
    val enableRealTimeProcessing: Boolean
)

enum class PerformanceFeature {
    ANIMATIONS,
    HAPTIC_FEEDBACK,
    AUDIO_CUES,
    REAL_TIME_PROCESSING,
    BACKGROUND_PROCESSING,
    VOICE_RECOGNITION
}