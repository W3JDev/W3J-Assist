package com.aura.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.app.models.ConversationFlow
import com.aura.app.models.ConversationMoment
import com.aura.app.models.MomentType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

@Composable
fun ConversationDNAVisualization(
    conversationFlow: ConversationFlow,
    modifier: Modifier = Modifier,
    expanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(expanded) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Conversation DNA",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Conversation DNA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${conversationFlow.insights.size} insights • ${conversationFlow.keyMoments.size} key moments",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // DNA visualization
            ConversationDNAChart(
                moments = conversationFlow.keyMoments,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversationFlow.keyMoments) { moment ->
                        ConversationMomentItem(moment = moment)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationDNAChart(
    moments: List<ConversationMoment>,
    modifier: Modifier = Modifier
) {
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 2000,
            easing = EaseOutCubic
        ),
        label = "dna_animation"
    )
    
    Canvas(modifier = modifier) {
        drawConversationDNA(moments, animationProgress)
    }
}

private fun DrawScope.drawConversationDNA(
    moments: List<ConversationMoment>,
    progress: Float
) {
    if (moments.isEmpty()) return
    
    val width = size.width
    val height = size.height
    val centerY = height / 2
    
    val strokeWidth = 3.dp.toPx()
    val amplitude = height * 0.3f
    
    // Draw base line
    drawLine(
        color = Color.Gray.copy(alpha = 0.3f),
        start = Offset(0f, centerY),
        end = Offset(width, centerY),
        strokeWidth = strokeWidth / 2
    )
    
    // Draw DNA strands
    val path = Path()
    val momentPositions = mutableListOf<Pair<Offset, ConversationMoment>>()
    
    for (i in moments.indices) {
        val x = (i.toFloat() / (moments.size - 1).coerceAtLeast(1)) * width * progress
        val importance = moments[i].importance
        val y = centerY + sin(i * 0.8f) * amplitude * importance
        
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
        
        momentPositions.add(Offset(x, y) to moments[i])
    }
    
    // Draw the DNA path
    drawPath(
        path = path,
        color = MaterialTheme.colorScheme.primary,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        )
    )
    
    // Draw moment indicators
    momentPositions.forEach { (position, moment) ->
        val momentColor = getMomentColor(moment.type)
        drawCircle(
            color = momentColor,
            radius = 6.dp.toPx() * moment.importance,
            center = position
        )
        drawCircle(
            color = momentColor.copy(alpha = 0.3f),
            radius = 12.dp.toPx() * moment.importance,
            center = position
        )
    }
}

@Composable
private fun ConversationMomentItem(
    moment: ConversationMoment,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val momentColor = getMomentColor(moment.type)
    val momentIcon = getMomentIcon(moment.type)
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(momentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = momentIcon,
                contentDescription = moment.type.name,
                tint = momentColor,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = moment.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = timeFormatter.format(Date(moment.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Importance indicator
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < (moment.importance * 3).toInt()) 
                            momentColor 
                        else 
                            Color.Gray.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

private fun getMomentColor(type: MomentType): Color {
    return when (type) {
        MomentType.TOPIC_CHANGE -> Color(0xFF2196F3)
        MomentType.KEY_DECISION -> Color(0xFF4CAF50)
        MomentType.QUESTION_ASKED -> Color(0xFFFF9800)
        MomentType.INSIGHT_GENERATED -> Color(0xFF9C27B0)
        MomentType.ACTION_IDENTIFIED -> Color(0xFFF44336)
    }
}

private fun getMomentIcon(type: MomentType): ImageVector {
    return when (type) {
        MomentType.TOPIC_CHANGE -> Icons.Default.TrendingUp
        MomentType.KEY_DECISION -> Icons.Default.CheckCircle
        MomentType.QUESTION_ASKED -> Icons.Default.HelpOutline
        MomentType.INSIGHT_GENERATED -> Icons.Default.Lightbulb
        MomentType.ACTION_IDENTIFIED -> Icons.Default.Assignment
    }
}