package com.aura.app.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.aura.app.models.SmartSuggestion
import com.aura.app.ui.theme.ProjectAuraTheme
import org.junit.Rule
import org.junit.Test

class SmartSuggestionsBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSmartSuggestionsBar_DisplaysSuggestions() {
        val suggestions = listOf(
            SmartSuggestion("1", "Ask follow-up", "followup", 0.8f),
            SmartSuggestion("2", "Clarify point", "clarify", 0.7f),
            SmartSuggestion("3", "Take notes", "notes", 0.9f)
        )

        var clickedSuggestion: SmartSuggestion? = null

        composeTestRule.setContent {
            ProjectAuraTheme {
                SmartSuggestionsBar(
                    suggestions = suggestions,
                    onSuggestionClick = { suggestion ->
                        clickedSuggestion = suggestion
                    }
                )
            }
        }

        // Check that all suggestions are displayed
        composeTestRule
            .onNodeWithText("Ask follow-up")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Clarify point")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Take notes")
            .assertIsDisplayed()

        // Test clicking a suggestion
        composeTestRule
            .onNodeWithText("Ask follow-up")
            .performClick()

        assert(clickedSuggestion?.text == "Ask follow-up")
        assert(clickedSuggestion?.confidence == 0.8f)
    }

    @Test
    fun testSmartSuggestionsBar_HiddenWhenEmpty() {
        composeTestRule.setContent {
            ProjectAuraTheme {
                SmartSuggestionsBar(
                    suggestions = emptyList(),
                    onSuggestionClick = { }
                )
            }
        }

        // Should not display smart suggestions header when empty
        composeTestRule
            .onNodeWithText("Smart Suggestions")
            .assertDoesNotExist()
    }

    @Test
    fun testSmartSuggestionsBar_HiddenWhenNotVisible() {
        val suggestions = listOf(
            SmartSuggestion("1", "Test", "test", 0.8f)
        )

        composeTestRule.setContent {
            ProjectAuraTheme {
                SmartSuggestionsBar(
                    suggestions = suggestions,
                    onSuggestionClick = { },
                    isVisible = false
                )
            }
        }

        // Should not display when isVisible is false
        composeTestRule
            .onNodeWithText("Test")
            .assertDoesNotExist()
    }

    @Test
    fun testQuickActionSuggestions() {
        var clickedAction: String? = null

        composeTestRule.setContent {
            ProjectAuraTheme {
                QuickActionSuggestions(
                    onAction = { action ->
                        clickedAction = action
                    }
                )
            }
        }

        // Check that quick actions are displayed
        composeTestRule
            .onNodeWithText("👍 Agree")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("❓ Clarify")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("📝 Take Note")
            .assertIsDisplayed()

        // Test clicking an action
        composeTestRule
            .onNodeWithText("👍 Agree")
            .performClick()

        assert(clickedAction == "agree")
    }
}