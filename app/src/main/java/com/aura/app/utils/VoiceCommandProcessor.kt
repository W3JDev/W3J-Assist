package com.aura.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.speech.tts.TextToSpeech
import com.aura.app.AuraApplication
import com.aura.app.models.SavedResponse
import kotlinx.coroutines.flow.firstOrNull
import java.util.*

class VoiceCommandProcessor(private val context: Context) {
    
    private var textToSpeech: TextToSpeech? = null
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val database = AuraApplication.instance.database
    private val integrationManager = IntegrationManager(context)
    
    companion object {
        // Voice command keywords
        const val SHOW_INSIGHTS = "show insights"
        const val READ_LATEST = "read latest"
        const val COPY_COMMAND = "copy"
        const val SAVE_COMMAND = "save"
        const val PAUSE_COMMAND = "pause"
        const val SHARE_SLACK = "share slack"
        const val SHARE_DISCORD = "share discord"
        const val SAVE_NOTION = "save notion"
        const val SAVE_OBSIDIAN = "save obsidian"
        const val CALENDAR_CONTEXT = "calendar context"
        const val PIP_MODE = "pip mode"
    }
    
    init {
        initializeTextToSpeech()
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
            }
        }
    }
    
    /**
     * Processes voice command and returns response message
     */
    suspend fun processVoiceCommand(command: String): String {
        val normalizedCommand = command.lowercase().trim()
        
        return when {
            normalizedCommand.contains(SHOW_INSIGHTS) -> handleShowInsights()
            normalizedCommand.contains(READ_LATEST) -> handleReadLatest()
            normalizedCommand.contains(COPY_COMMAND) -> handleCopy()
            normalizedCommand.contains(SAVE_NOTION) -> handleSaveToNotion(command)
            normalizedCommand.contains(SAVE_OBSIDIAN) -> handleSaveToObsidian(command)
            normalizedCommand.contains(SHARE_SLACK) -> handleShareToSlack()
            normalizedCommand.contains(SHARE_DISCORD) -> handleShareToDiscord()
            normalizedCommand.contains(CALENDAR_CONTEXT) -> handleCalendarContext()
            normalizedCommand.contains(PIP_MODE) -> handlePiPMode()
            normalizedCommand.contains(SAVE_COMMAND) -> handleSave(command)
            normalizedCommand.contains(PAUSE_COMMAND) -> handlePause()
            else -> processWithAI(command) // Default to AI processing
        }
    }
    
    private suspend fun handleShowInsights(): String {
        val responses = database.savedResponseDao().getAllResponsesOnce()
        return if (responses.isNotEmpty()) {
            val recentResponses = responses.takeLast(3)
            val insights = recentResponses.joinToString("\n\n") { 
                "Input: ${it.inputText}\nResponse: ${it.aiResponse}"
            }
            "Recent insights:\n$insights"
        } else {
            "No insights available yet."
        }
    }
    
    private suspend fun handleReadLatest(): String {
        val latestResponse = database.savedResponseDao().getLatestResponse()
        return if (latestResponse != null) {
            // Use text-to-speech to read the latest response
            textToSpeech?.speak(
                latestResponse.aiResponse,
                TextToSpeech.QUEUE_FLUSH,
                null,
                null
            )
            "Reading latest response: ${latestResponse.aiResponse}"
        } else {
            "No responses available to read."
        }
    }
    
    private suspend fun handleCopy(): String {
        val latestResponse = database.savedResponseDao().getLatestResponse()
        return if (latestResponse != null) {
            val clip = ClipData.newPlainText("Aura Response", latestResponse.aiResponse)
            clipboardManager.setPrimaryClip(clip)
            "Latest response copied to clipboard."
        } else {
            "No response available to copy."
        }
    }
    
    private suspend fun handleSave(command: String): String {
        // Extract content to save from the command
        val contentToSave = command.substringAfter(SAVE_COMMAND).trim()
        return if (contentToSave.isNotEmpty()) {
            val savedResponse = SavedResponse(
                timestamp = System.currentTimeMillis(),
                inputText = "Manual save",
                aiResponse = contentToSave,
                inputType = "voice_save"
            )
            database.savedResponseDao().insertResponse(savedResponse)
            "Content saved successfully."
        } else {
            "Please specify what to save after the save command."
        }
    }
    
    private fun handlePause(): String {
        // This will be handled by the calling service to pause listening
        return "Pausing voice recognition."
    }
    
    private suspend fun processWithAI(command: String): String {
        val geminiClient = GeminiClient()
        return geminiClient.processText(command)
    }
    
    private suspend fun handleShareToSlack(): String {
        val latestResponse = database.savedResponseDao().getLatestResponse()
        return if (latestResponse != null) {
            val success = integrationManager.shareToSlack(
                latestResponse.aiResponse,
                "Aura AI Insight"
            )
            if (success) "Shared latest insight to Slack" else "Unable to share to Slack - using generic share"
        } else {
            "No insights available to share"
        }
    }
    
    private suspend fun handleShareToDiscord(): String {
        val latestResponse = database.savedResponseDao().getLatestResponse()
        return if (latestResponse != null) {
            val success = integrationManager.shareToDiscord(
                latestResponse.aiResponse,
                "Aura AI Insight"
            )
            if (success) "Shared latest insight to Discord" else "Unable to share to Discord - using generic share"
        } else {
            "No insights available to share"
        }
    }
    
    private suspend fun handleSaveToNotion(command: String): String {
        val latestResponse = database.savedResponseDao().getLatestResponse()
        return if (latestResponse != null) {
            val success = integrationManager.saveToNotion(
                latestResponse.aiResponse,
                "Aura AI Insight"
            )
            if (success) "Saved latest insight to Notion format" else "Saved insight to local file"
        } else {
            "No insights available to save"
        }
    }
    
    private suspend fun handleSaveToObsidian(command: String): String {
        val latestResponse = database.savedResponseDao().getLatestResponse()
        return if (latestResponse != null) {
            val success = integrationManager.saveToObsidian(
                latestResponse.aiResponse,
                "Aura AI Insight"
            )
            if (success) "Saved latest insight to Obsidian format" else "Unable to save to Obsidian"
        } else {
            "No insights available to save"
        }
    }
    
    private fun handleCalendarContext(): String {
        return integrationManager.getCalendarContext()
    }
    
    private fun handlePiPMode(): String {
        return integrationManager.triggerPiPMode()
    }
    
    fun isVoiceCommand(command: String): Boolean {
        val normalizedCommand = command.lowercase().trim()
        return normalizedCommand.contains(SHOW_INSIGHTS) ||
                normalizedCommand.contains(READ_LATEST) ||
                normalizedCommand.contains(COPY_COMMAND) ||
                normalizedCommand.contains(SAVE_COMMAND) ||
                normalizedCommand.contains(PAUSE_COMMAND) ||
                normalizedCommand.contains(SHARE_SLACK) ||
                normalizedCommand.contains(SHARE_DISCORD) ||
                normalizedCommand.contains(SAVE_NOTION) ||
                normalizedCommand.contains(SAVE_OBSIDIAN) ||
                normalizedCommand.contains(CALENDAR_CONTEXT) ||
                normalizedCommand.contains(PIP_MODE)
    }
    
    fun cleanup() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}