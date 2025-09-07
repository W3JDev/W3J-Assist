package com.aura.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aura.app.models.*
import com.aura.app.ui.components.*
import com.aura.app.ui.theme.ProjectAuraTheme
import com.aura.app.ui.viewmodels.InsightPlaygroundViewModel
import kotlinx.coroutines.delay

class InsightPlaygroundActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectAuraTheme {
                InsightPlaygroundScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightPlaygroundScreen(
    onBack: () -> Unit,
    viewModel: InsightPlaygroundViewModel = viewModel()
) {
    var inputText by remember { mutableStateOf("") }
    val insights by viewModel.insights.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val conversationFlow by viewModel.conversationFlow.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Insight Playground",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.clearAll() }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear All"
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Test Conversation Scenario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text("Enter a conversation scenario to analyze...")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.analyzeScenario(inputText)
                                    keyboardController?.hide()
                                }
                            }
                        )
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.analyzeScenario(inputText)
                                    keyboardController?.hide()
                                }
                            },
                            enabled = inputText.isNotBlank() && !isProcessing,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isProcessing) "Analyzing..." else "Analyze")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                inputText = getSampleScenario()
                            }
                        ) {
                            Text("Sample")
                        }
                    }
                }
            }
            
            // Results Section
            if (insights.isNotEmpty() || conversationFlow != null) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Smart Suggestions
                    if (suggestions.isNotEmpty()) {
                        item {
                            SmartSuggestionsBar(
                                suggestions = suggestions,
                                onSuggestionClick = { suggestion ->
                                    // Handle suggestion click
                                    viewModel.applySuggestion(suggestion)
                                }
                            )
                        }
                    }
                    
                    // Insights
                    items(insights) { insight ->
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it / 4 }
                            ) + fadeIn()
                        ) {
                            InsightCard(insight = insight)
                        }
                    }
                    
                    // Conversation DNA
                    conversationFlow?.let { flow ->
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(
                                    initialOffsetY = { it / 4 }
                                ) + fadeIn()
                            ) {
                                ConversationDNAVisualization(
                                    conversationFlow = flow,
                                    expanded = true
                                )
                            }
                        }
                    }
                }
            } else if (!isProcessing) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Welcome to Insight Playground",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Enter a conversation scenario above to see AI insights, confidence ratings, and conversation flow analysis.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getSampleScenario(): String {
    val samples = listOf(
        "Let's discuss the quarterly budget allocation for the marketing team. We need to prioritize digital campaigns and consider cutting traditional advertising spend.",
        "I'm having trouble understanding this code implementation. Could you walk me through the authentication flow and explain why we're using JWT tokens?",
        "The client wants to add real-time chat functionality to the app. What are the technical considerations and timeline for implementation?",
        "We need to address the performance issues in the database queries. The page load times are unacceptable for users."
    )
    return samples.random()
}