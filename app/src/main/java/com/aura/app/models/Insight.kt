package com.aura.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insights")
data class Insight(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val content: String,
    val confidence: Float, // 0.0 to 1.0
    val type: InsightType,
    val sourceText: String = "",
    val conversationId: String = ""
)

enum class InsightType {
    TALKING_POINT,
    CODE_SNIPPET,
    CLARIFICATION,
    ACTION_ITEM,
    REFERENCE,
    WARNING
}

data class SmartSuggestion(
    val id: String,
    val text: String,
    val action: String,
    val confidence: Float
)

data class ConversationFlow(
    val id: String,
    val timestamp: Long,
    val insights: List<Insight>,
    val keyMoments: List<ConversationMoment>
)

data class ConversationMoment(
    val timestamp: Long,
    val type: MomentType,
    val description: String,
    val importance: Float // 0.0 to 1.0
)

enum class MomentType {
    TOPIC_CHANGE,
    KEY_DECISION,
    QUESTION_ASKED,
    INSIGHT_GENERATED,
    ACTION_IDENTIFIED
}