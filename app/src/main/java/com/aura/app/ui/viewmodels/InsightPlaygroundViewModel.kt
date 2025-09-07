package com.aura.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.app.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class InsightPlaygroundViewModel : ViewModel() {
    
    private val _insights = MutableStateFlow<List<Insight>>(emptyList())
    val insights: StateFlow<List<Insight>> = _insights.asStateFlow()
    
    private val _suggestions = MutableStateFlow<List<SmartSuggestion>>(emptyList())
    val suggestions: StateFlow<List<SmartSuggestion>> = _suggestions.asStateFlow()
    
    private val _conversationFlow = MutableStateFlow<ConversationFlow?>(null)
    val conversationFlow: StateFlow<ConversationFlow?> = _conversationFlow.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    fun analyzeScenario(text: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            
            // Clear previous results
            _insights.value = emptyList()
            _suggestions.value = emptyList()
            _conversationFlow.value = null
            
            // Simulate AI processing delay
            delay(1500)
            
            // Generate mock insights based on the input
            val generatedInsights = generateMockInsights(text)
            val generatedSuggestions = generateMockSuggestions(text)
            val generatedFlow = generateMockConversationFlow(text, generatedInsights)
            
            // Animate insights appearing one by one
            generatedInsights.forEachIndexed { index, insight ->
                delay(300)
                _insights.value = _insights.value + insight
            }
            
            // Add suggestions after insights
            delay(500)
            _suggestions.value = generatedSuggestions
            
            // Add conversation flow last
            delay(300)
            _conversationFlow.value = generatedFlow
            
            _isProcessing.value = false
        }
    }
    
    fun applySuggestion(suggestion: SmartSuggestion) {
        viewModelScope.launch {
            // Create a new insight based on the suggestion
            val newInsight = Insight(
                timestamp = System.currentTimeMillis(),
                content = "Applied suggestion: ${suggestion.text}",
                confidence = suggestion.confidence,
                type = InsightType.ACTION_ITEM,
                sourceText = suggestion.action
            )
            
            _insights.value = _insights.value + newInsight
        }
    }
    
    fun clearAll() {
        _insights.value = emptyList()
        _suggestions.value = emptyList()
        _conversationFlow.value = null
    }
    
    private fun generateMockInsights(text: String): List<Insight> {
        val insights = mutableListOf<Insight>()
        val currentTime = System.currentTimeMillis()
        
        // Analyze text content and generate relevant insights
        when {
            text.contains("budget", ignoreCase = true) || text.contains("cost", ignoreCase = true) -> {
                insights.add(
                    Insight(
                        timestamp = currentTime,
                        content = "Financial discussion detected. Consider requesting specific budget numbers and ROI metrics.",
                        confidence = 0.9f,
                        type = InsightType.TALKING_POINT,
                        sourceText = text
                    )
                )
                insights.add(
                    Insight(
                        timestamp = currentTime + 100,
                        content = "Action needed: Create budget allocation spreadsheet with Q4 projections.",
                        confidence = 0.8f,
                        type = InsightType.ACTION_ITEM,
                        sourceText = text
                    )
                )
            }
            
            text.contains("code", ignoreCase = true) || text.contains("implementation", ignoreCase = true) -> {
                insights.add(
                    Insight(
                        timestamp = currentTime,
                        content = "Technical discussion identified. Prepare code examples and documentation references.",
                        confidence = 0.85f,
                        type = InsightType.CODE_SNIPPET,
                        sourceText = text
                    )
                )
                insights.add(
                    Insight(
                        timestamp = currentTime + 100,
                        content = "Consider asking about error handling and edge cases in the implementation.",
                        confidence = 0.75f,
                        type = InsightType.CLARIFICATION,
                        sourceText = text
                    )
                )
            }
            
            text.contains("performance", ignoreCase = true) || text.contains("slow", ignoreCase = true) -> {
                insights.add(
                    Insight(
                        timestamp = currentTime,
                        content = "Performance concerns detected. Monitor for specific metrics and bottlenecks.",
                        confidence = 0.92f,
                        type = InsightType.WARNING,
                        sourceText = text
                    )
                )
                insights.add(
                    Insight(
                        timestamp = currentTime + 100,
                        content = "Reference: Performance monitoring best practices documentation",
                        confidence = 0.7f,
                        type = InsightType.REFERENCE,
                        sourceText = text
                    )
                )
            }
            
            else -> {
                // Generic insights
                insights.add(
                    Insight(
                        timestamp = currentTime,
                        content = "Key topic identified. Consider asking follow-up questions for clarity.",
                        confidence = 0.7f,
                        type = InsightType.TALKING_POINT,
                        sourceText = text
                    )
                )
                insights.add(
                    Insight(
                        timestamp = currentTime + 100,
                        content = "This discussion may require documentation or action items.",
                        confidence = 0.65f,
                        type = InsightType.CLARIFICATION,
                        sourceText = text
                    )
                )
            }
        }
        
        return insights
    }
    
    private fun generateMockSuggestions(text: String): List<SmartSuggestion> {
        return when {
            text.contains("budget", ignoreCase = true) -> listOf(
                SmartSuggestion("1", "Request breakdown", "budget_breakdown", 0.9f),
                SmartSuggestion("2", "Ask about timeline", "timeline", 0.8f),
                SmartSuggestion("3", "Clarify priorities", "priorities", 0.75f)
            )
            
            text.contains("code", ignoreCase = true) -> listOf(
                SmartSuggestion("1", "Show example", "code_example", 0.85f),
                SmartSuggestion("2", "Explain flow", "explain_flow", 0.8f),
                SmartSuggestion("3", "Check tests", "tests", 0.7f)
            )
            
            text.contains("performance", ignoreCase = true) -> listOf(
                SmartSuggestion("1", "Check metrics", "metrics", 0.9f),
                SmartSuggestion("2", "Profile code", "profile", 0.85f),
                SmartSuggestion("3", "Monitor logs", "logs", 0.8f)
            )
            
            else -> listOf(
                SmartSuggestion("1", "Ask questions", "questions", 0.7f),
                SmartSuggestion("2", "Take notes", "notes", 0.8f),
                SmartSuggestion("3", "Set follow-up", "followup", 0.75f)
            )
        }
    }
    
    private fun generateMockConversationFlow(text: String, insights: List<Insight>): ConversationFlow {
        val currentTime = System.currentTimeMillis()
        
        val keyMoments = listOf(
            ConversationMoment(
                timestamp = currentTime - 3000,
                type = MomentType.TOPIC_CHANGE,
                description = "New topic introduced",
                importance = 0.8f
            ),
            ConversationMoment(
                timestamp = currentTime - 2000,
                type = MomentType.QUESTION_ASKED,
                description = "Clarification requested",
                importance = 0.6f
            ),
            ConversationMoment(
                timestamp = currentTime - 1000,
                type = MomentType.INSIGHT_GENERATED,
                description = "AI insight generated",
                importance = 0.9f
            ),
            ConversationMoment(
                timestamp = currentTime,
                type = MomentType.ACTION_IDENTIFIED,
                description = "Action item identified",
                importance = 0.85f
            )
        )
        
        return ConversationFlow(
            id = "playground_${Random.nextInt(1000)}",
            timestamp = currentTime,
            insights = insights,
            keyMoments = keyMoments
        )
    }
}