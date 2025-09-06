package com.aura.app.services

import ai.picovoice.porcupine.*
import android.app.*
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.aura.app.AuraApplication
import com.aura.app.MainActivity
import com.aura.app.utils.GeminiClient
import com.aura.app.utils.VoiceCommandProcessor
import kotlinx.coroutines.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WakeWordService : Service() {
    
    private var porcupine: Porcupine? = null
    private var audioRecord: AudioRecord? = null
    private var isListening = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val geminiClient = GeminiClient()
    private lateinit var voiceCommandProcessor: VoiceCommandProcessor
    private var isPaused = false
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "wake_word_channel"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        voiceCommandProcessor = VoiceCommandProcessor(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        
        when (intent?.action) {
            "RESUME_LISTENING" -> {
                resumeListening()
            }
            else -> {
                initializePorcupine()
                startListening()
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Wake Word Detection",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background service for wake word detection"
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Project Aura")
            .setContentText("Listening for wake word...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun initializePorcupine() {
        try {
            // Create a simple wake word detection
            // Note: For production, you should use actual Porcupine keyword files
            val keywordPath = createPorcupineKeywordFile()
            
            porcupine = Porcupine.Builder()
                .setAccessKey("YOUR_PICOVOICE_ACCESS_KEY") // Replace with actual key
                .setKeywordPath(keywordPath)
                .setSensitivity(0.5f)
                .build(applicationContext)
                
        } catch (e: PorcupineException) {
            // Handle error - fall back to alternative wake word detection
            e.printStackTrace()
        }
    }
    
    private fun createPorcupineKeywordFile(): String {
        // This is a placeholder - in production you would use actual .ppn files
        // For now, we'll simulate wake word detection
        val file = File(filesDir, "aura_keyword.ppn")
        if (!file.exists()) {
            file.createNewFile()
        }
        return file.absolutePath
    }
    
    private fun startListening() {
        if (isListening) return
        
        scope.launch {
            try {
                val sampleRate = 16000
                val channelConfig = AudioFormat.CHANNEL_IN_MONO
                val audioFormat = AudioFormat.ENCODING_PCM_16BIT
                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )
                
                audioRecord?.startRecording()
                isListening = true
                
                val buffer = ShortArray(512)
                
                while (isListening) {
                    val numRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    
                    if (numRead > 0) {
                        // Simple wake word detection (placeholder)
                        if (detectWakeWord(buffer)) {
                            handleWakeWordDetected()
                        }
                        
                        // If using Porcupine, uncomment this:
                        /*
                        porcupine?.let { porcupine ->
                            val keywordIndex = porcupine.process(buffer)
                            if (keywordIndex >= 0) {
                                handleWakeWordDetected()
                            }
                        }
                        */
                    }
                    
                    delay(10) // Small delay to prevent excessive CPU usage
                }
            } catch (e: Exception) {
                e.printStackTrace()
                stopSelf()
            }
        }
    }
    
    private fun detectWakeWord(buffer: ShortArray): Boolean {
        // Placeholder wake word detection logic
        // In production, this would be handled by Porcupine
        
        // Simple energy-based detection (very basic)
        val energy = buffer.map { it * it }.average()
        return energy > 1000000 // Arbitrary threshold
    }
    
    private fun handleWakeWordDetected() {
        if (isPaused) return
        
        scope.launch {
            // Start recording for a few seconds to capture the command
            val command = recordAudioCommand()
            if (command.isNotEmpty()) {
                // Check if it's a voice command first
                val response = if (voiceCommandProcessor.isVoiceCommand(command)) {
                    val result = voiceCommandProcessor.processVoiceCommand(command)
                    
                    // Handle pause command specially
                    if (command.lowercase().contains("pause")) {
                        isPaused = true
                        // Send broadcast to OverlayService to show paused status
                        sendBroadcast(Intent("com.aura.app.PAUSE_STATUS").apply {
                            putExtra("isPaused", true)
                        })
                    }
                    
                    result
                } else {
                    // Process with AI if not a voice command
                    geminiClient.processText(command)
                }
                
                // Save to database
                val database = AuraApplication.instance.database
                val savedResponse = com.aura.app.models.SavedResponse(
                    timestamp = System.currentTimeMillis(),
                    inputText = command,
                    aiResponse = response,
                    inputType = "audio"
                )
                database.savedResponseDao().insertResponse(savedResponse)
                
                // Send broadcast to OverlayService to show response
                sendBroadcast(Intent("com.aura.app.VOICE_RESPONSE").apply {
                    putExtra("response", response)
                    putExtra("command", command)
                })
            }
        }
    }
    
    /**
     * Resume voice recognition from pause
     */
    fun resumeListening() {
        isPaused = false
        sendBroadcast(Intent("com.aura.app.PAUSE_STATUS").apply {
            putExtra("isPaused", false)
        })
    }
    
    private suspend fun recordAudioCommand(): String {
        // Placeholder for audio recording and speech-to-text conversion
        // In production, this would record audio for a few seconds and convert to text
        return "Sample voice command"
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isListening = false
        audioRecord?.stop()
        audioRecord?.release()
        porcupine?.delete()
        voiceCommandProcessor.cleanup()
        scope.cancel()
    }
}