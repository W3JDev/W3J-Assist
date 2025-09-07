package com.aura.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircularShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.app.models.Insight
import com.aura.app.models.InsightType
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ConfidenceMeter(
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val animatedConfidence by animateFloatAsState(
        targetValue = confidence,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "confidence_animation"
    )
    
    val confidenceColor = when {
        confidence >= 0.8f -> Color(0xFF4CAF50) // Green
        confidence >= 0.6f -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(
            modifier = Modifier.size(24.dp)
        ) {
            drawConfidenceArc(animatedConfidence, confidenceColor)
        }
        
        Text(
            text = "${(confidence * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = confidenceColor
        )
    }
}

private fun DrawScope.drawConfidenceArc(
    confidence: Float,
    color: Color
) {
    val strokeWidth = 3.dp.toPx()
    val sweepAngle = 270f * confidence
    val startAngle = 135f
    
    // Background arc
    drawArc(
        color = Color.Gray.copy(alpha = 0.3f),
        startAngle = startAngle,
        sweepAngle = 270f,
        useCenter = false,
        size = Size(size.width - strokeWidth, size.height - strokeWidth),
        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        )
    )
    
    // Progress arc
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        size = Size(size.width - strokeWidth, size.height - strokeWidth),
        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        )
    )
}

@Composable
fun InsightTypeIcon(
    type: InsightType,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (type) {
        InsightType.TALKING_POINT -> Icons.Default.Chat to Color(0xFF2196F3)
        InsightType.CODE_SNIPPET -> Icons.Default.Code to Color(0xFF9C27B0)
        InsightType.CLARIFICATION -> Icons.Default.Help to Color(0xFFFF9800)
        InsightType.ACTION_ITEM -> Icons.Default.Assignment to Color(0xFF4CAF50)
        InsightType.REFERENCE -> Icons.Default.Link to Color(0xFF607D8B)
        InsightType.WARNING -> Icons.Default.Warning to Color(0xFFF44336)
    }
    
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircularShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type.name,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun InsightCard(
    insight: Insight,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InsightTypeIcon(type = insight.type)
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = insight.type.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                ConfidenceMeter(confidence = insight.confidence)
            }
            
            Text(
                text = insight.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}