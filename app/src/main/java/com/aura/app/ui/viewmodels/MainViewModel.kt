package com.aura.app.ui.viewmodels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.app.AuraApplication
import com.aura.app.models.SavedResponse
import com.aura.app.services.OverlayService
import com.aura.app.services.WakeWordService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    
    private val database = AuraApplication.instance.database
    
    private val _savedResponses = MutableLiveData<List<SavedResponse>>()
    val savedResponses: LiveData<List<SavedResponse>> = _savedResponses
    
    private val _isOverlayEnabled = MutableLiveData(false)
    val isOverlayEnabled: LiveData<Boolean> = _isOverlayEnabled
    
    private val _isListening = MutableLiveData(false)
    val isListening: LiveData<Boolean> = _isListening
    
    init {
        loadSavedResponses()
    }
    
    private fun loadSavedResponses() {
        viewModelScope.launch {
            database.savedResponseDao().getAllResponses().collectLatest { responses ->
                _savedResponses.value = responses
            }
        }
    }
    
    fun toggleOverlay(context: Context) {
        val isEnabled = _isOverlayEnabled.value ?: false
        if (isEnabled) {
            context.stopService(Intent(context, OverlayService::class.java))
            _isOverlayEnabled.value = false
        } else {
            context.startService(Intent(context, OverlayService::class.java))
            _isOverlayEnabled.value = true
        }
    }
    
    fun toggleListening(context: Context) {
        val isCurrentlyListening = _isListening.value ?: false
        if (isCurrentlyListening) {
            context.stopService(Intent(context, WakeWordService::class.java))
            _isListening.value = false
        } else {
            context.startForegroundService(Intent(context, WakeWordService::class.java))
            _isListening.value = true
        }
    }
    
    fun startOCRMode(context: Context) {
        val intent = Intent(context, com.aura.app.OCRActivity::class.java)
        context.startActivity(intent)
    }
    
    fun startInsightPlayground(context: Context) {
        val intent = Intent(context, com.aura.app.InsightPlaygroundActivity::class.java)
        context.startActivity(intent)
    }
    
    fun deleteResponse(response: SavedResponse) {
        viewModelScope.launch {
            database.savedResponseDao().deleteResponse(response)
        }
    }
    
    fun saveResponse(inputText: String, aiResponse: String, inputType: String) {
        viewModelScope.launch {
            val response = SavedResponse(
                timestamp = System.currentTimeMillis(),
                inputText = inputText,
                aiResponse = aiResponse,
                inputType = inputType
            )
            database.savedResponseDao().insertResponse(response)
        }
    }
    
    /**
     * Resume voice recognition from pause
     */
    fun resumeVoiceRecognition(context: Context) {
        val intent = Intent(context, WakeWordService::class.java)
        intent.action = "RESUME_LISTENING"
        context.startService(intent)
    }
    
    /**
     * Manual trigger for voice commands via UI
     */
    fun processVoiceCommand(context: Context, command: String) {
        viewModelScope.launch {
            val voiceCommandProcessor = com.aura.app.utils.VoiceCommandProcessor(context)
            val response = voiceCommandProcessor.processVoiceCommand(command)
            
            // Save the interaction
            val savedResponse = SavedResponse(
                timestamp = System.currentTimeMillis(),
                inputText = command,
                aiResponse = response,
                inputType = "manual_voice"
            )
            database.savedResponseDao().insertResponse(savedResponse)
            
            voiceCommandProcessor.cleanup()
        }
    }
}