package com.aura.app.ui.viewmodels

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.aura.app.models.InsightType

@ExperimentalCoroutinesApi
class InsightPlaygroundViewModelTest {

    private lateinit var viewModel: InsightPlaygroundViewModel
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = InsightPlaygroundViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun testAnalyzeScenario_BudgetText() = runBlockingTest {
        // Given
        val budgetText = "Let's discuss the quarterly budget allocation for the marketing team."

        // When
        viewModel.analyzeScenario(budgetText)
        
        // Wait for the coroutine to complete
        advanceTimeBy(3000) // Total processing time
        runCurrent()

        // Then
        val insights = viewModel.insights.value
        val suggestions = viewModel.suggestions.value

        assert(insights.isNotEmpty()) { "Should generate insights for budget discussion" }
        assert(suggestions.isNotEmpty()) { "Should generate suggestions for budget discussion" }

        // Check for specific budget-related insights
        val hasBudgetInsight = insights.any { 
            it.content.contains("Financial discussion", ignoreCase = true) 
        }
        assert(hasBudgetInsight) { "Should contain financial discussion insight" }

        // Check for budget-specific suggestions
        val hasBudgetSuggestion = suggestions.any {
            it.text.contains("breakdown", ignoreCase = true)
        }
        assert(hasBudgetSuggestion) { "Should contain budget breakdown suggestion" }
    }

    @Test
    fun testAnalyzeScenario_CodeText() = runBlockingTest {
        // Given
        val codeText = "I'm having trouble understanding this code implementation."

        // When
        viewModel.analyzeScenario(codeText)
        
        // Wait for processing
        advanceTimeBy(3000)
        runCurrent()

        // Then
        val insights = viewModel.insights.value
        val suggestions = viewModel.suggestions.value

        assert(insights.isNotEmpty()) { "Should generate insights for code discussion" }
        
        // Should have code snippet type insight
        val hasCodeInsight = insights.any { 
            it.type == InsightType.CODE_SNIPPET 
        }
        assert(hasCodeInsight) { "Should contain code snippet type insight" }

        // Should have code-specific suggestions
        val hasCodeSuggestion = suggestions.any {
            it.text.contains("example", ignoreCase = true)
        }
        assert(hasCodeSuggestion) { "Should contain code example suggestion" }
    }

    @Test
    fun testAnalyzeScenario_PerformanceText() = runBlockingTest {
        // Given
        val performanceText = "We need to address the performance issues in the database."

        // When
        viewModel.analyzeScenario(performanceText)
        
        // Wait for processing
        advanceTimeBy(3000)
        runCurrent()

        // Then
        val insights = viewModel.insights.value

        // Should have warning type insight for performance issues
        val hasWarningInsight = insights.any { 
            it.type == InsightType.WARNING 
        }
        assert(hasWarningInsight) { "Should contain warning type insight for performance issues" }

        // Should have high confidence for performance detection
        val hasHighConfidenceInsight = insights.any {
            it.confidence >= 0.9f && it.content.contains("Performance", ignoreCase = true)
        }
        assert(hasHighConfidenceInsight) { "Should have high confidence performance insight" }
    }

    @Test
    fun testApplySuggestion() = runBlockingTest {
        // Given
        val suggestion = com.aura.app.models.SmartSuggestion(
            "1", "Ask follow-up", "followup", 0.8f
        )
        val initialInsightCount = viewModel.insights.value.size

        // When
        viewModel.applySuggestion(suggestion)
        runCurrent()

        // Then
        val insights = viewModel.insights.value
        assert(insights.size == initialInsightCount + 1) { "Should add new insight when applying suggestion" }
        
        val newInsight = insights.last()
        assert(newInsight.content.contains("Applied suggestion: Ask follow-up")) { "Should contain applied suggestion content" }
        assert(newInsight.type == InsightType.ACTION_ITEM) { "Applied suggestion should be action item type" }
        assert(newInsight.confidence == 0.8f) { "Should preserve suggestion confidence" }
    }

    @Test
    fun testClearAll() = runBlockingTest {
        // Given - add some insights first
        viewModel.analyzeScenario("Test scenario")
        advanceTimeBy(3000)
        runCurrent()
        
        assert(viewModel.insights.value.isNotEmpty()) { "Should have insights before clearing" }

        // When
        viewModel.clearAll()
        runCurrent()

        // Then
        assert(viewModel.insights.value.isEmpty()) { "Should clear all insights" }
        assert(viewModel.suggestions.value.isEmpty()) { "Should clear all suggestions" }
        assert(viewModel.conversationFlow.value == null) { "Should clear conversation flow" }
    }

    @Test
    fun testProcessingState() = runBlockingTest {
        // Given
        assert(!viewModel.isProcessing.value) { "Should not be processing initially" }

        // When
        val job = launch {
            viewModel.analyzeScenario("Test scenario")
        }

        // Check processing state during analysis
        advanceTimeBy(500) // Partial time
        assert(viewModel.isProcessing.value) { "Should be processing during analysis" }

        // Complete the analysis
        advanceTimeBy(3000) // Complete processing time
        runCurrent()

        // Then
        assert(!viewModel.isProcessing.value) { "Should not be processing after completion" }
        
        job.cancel()
    }
}