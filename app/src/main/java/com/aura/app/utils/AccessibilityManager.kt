package com.aura.app.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.getSystemService
import com.aura.app.models.InsightType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AccessibilityManager(private val context: Context) {
    
    private var textToSpeech: TextToSpeech? = null
    private var vibrator: Vibrator? = null
    private var toneGenerator: ToneGenerator? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    
    companion object {
        // Vibration patterns for different insight types (in milliseconds)
        private val TALKING_POINT_PATTERN = longArrayOf(0, 100, 50, 100)
        private val ACTION_ITEM_PATTERN = longArrayOf(0, 200, 100, 200, 100, 200)
        private val WARNING_PATTERN = longArrayOf(0, 50, 50, 50, 50, 50, 200, 300)
        private val REFERENCE_PATTERN = longArrayOf(0, 150, 75, 150)
        private val CLARIFICATION_PATTERN = longArrayOf(0, 100, 100, 100, 100, 100)
        private val CODE_SNIPPET_PATTERN = longArrayOf(0, 75, 25, 75, 25, 75)
        
        // Audio tone frequencies for different insight types
        private const val TALKING_POINT_TONE = ToneGenerator.TONE_PROP_BEEP
        private const val ACTION_ITEM_TONE = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
        private const val WARNING_TONE = ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK
        private const val HIGH_CONFIDENCE_TONE = ToneGenerator.TONE_PROP_ACK
        private const val LOW_CONFIDENCE_TONE = ToneGenerator.TONE_PROP_NACK
    }
    
    init {
        initializeTextToSpeech()
        initializeVibrator()
        initializeToneGenerator()
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        // TTS started
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        // TTS completed
                    }
                    
                    override fun onError(utteranceId: String?) {
                        // TTS error
                    }
                })
            }
        }
    }
    
    private fun initializeVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService<VibratorManager>()
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService<Vibrator>()
        }
    }
    
    private fun initializeToneGenerator() {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50)
        } catch (e: Exception) {
            // ToneGenerator initialization failed, audio cues won't be available
        }
    }
    
    /**
     * Provide haptic feedback for different insight types
     */
    fun vibrateForInsight(insightType: InsightType, confidence: Float) {
        if (vibrator?.hasVibrator() != true) return
        
        val pattern = when (insightType) {
            InsightType.TALKING_POINT -> TALKING_POINT_PATTERN
            InsightType.ACTION_ITEM -> ACTION_ITEM_PATTERN
            InsightType.WARNING -> WARNING_PATTERN
            InsightType.REFERENCE -> REFERENCE_PATTERN
            InsightType.CLARIFICATION -> CLARIFICATION_PATTERN
            InsightType.CODE_SNIPPET -> CODE_SNIPPET_PATTERN
        }
        
        // Adjust vibration intensity based on confidence
        val amplitude = when {
            confidence >= 0.8f -> 255 // Strong vibration for high confidence
            confidence >= 0.6f -> 180 // Medium vibration
            else -> 100 // Light vibration for low confidence
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(pattern, intArrayOf(0, amplitude, 0, amplitude), -1)
            vibrator?.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }
    }
    
    /**
     * Provide audio cues for insights
     */
    fun playAudioCueForInsight(insightType: InsightType, confidence: Float) {
        scope.launch {
            try {
                val tone = when (insightType) {
                    InsightType.TALKING_POINT -> TALKING_POINT_TONE
                    InsightType.ACTION_ITEM -> ACTION_ITEM_TONE
                    InsightType.WARNING -> WARNING_TONE
                    else -> if (confidence >= 0.8f) HIGH_CONFIDENCE_TONE else LOW_CONFIDENCE_TONE
                }
                
                toneGenerator?.startTone(tone, 200) // 200ms duration
            } catch (e: Exception) {
                // Audio cue failed, continue silently
            }
        }
    }
    
    /**
     * Read insight content aloud using TTS
     */
    fun speakInsight(content: String, utteranceId: String = UUID.randomUUID().toString()) {
        textToSpeech?.speak(content, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }
    
    /**
     * Stop any ongoing TTS
     */
    fun stopSpeaking() {
        textToSpeech?.stop()
    }
    
    /**
     * Check if TTS is currently speaking
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking ?: false
    }
    
    /**
     * Provide notification feedback for important insights
     */
    fun notifyImportantInsight(insightType: InsightType, confidence: Float, content: String) {
        // Only notify for high-confidence or critical insights
        if (confidence >= 0.8f || insightType == InsightType.WARNING || insightType == InsightType.ACTION_ITEM) {
            vibrateForInsight(insightType, confidence)
            playAudioCueForInsight(insightType, confidence)
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        textToSpeech?.shutdown()
        toneGenerator?.release()
    }
}