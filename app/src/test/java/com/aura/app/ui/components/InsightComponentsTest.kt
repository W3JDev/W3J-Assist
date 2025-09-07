package com.aura.app.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.aura.app.models.*
import com.aura.app.ui.theme.ProjectAuraTheme
import org.junit.Rule
import org.junit.Test

class InsightComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testConfidenceMeter_HighConfidence() {
        composeTestRule.setContent {
            ProjectAuraTheme {
                ConfidenceMeter(confidence = 0.9f)
            }
        }

        composeTestRule
            .onNodeWithText("90%")
            .assertIsDisplayed()
    }

    @Test
    fun testConfidenceMeter_LowConfidence() {
        composeTestRule.setContent {
            ProjectAuraTheme {
                ConfidenceMeter(confidence = 0.3f)
            }
        }

        composeTestRule
            .onNodeWithText("30%")
            .assertIsDisplayed()
    }

    @Test
    fun testInsightCard_TalkingPoint() {
        val insight = Insight(
            timestamp = System.currentTimeMillis(),
            content = "This is a talking point",
            confidence = 0.85f,
            type = InsightType.TALKING_POINT
        )

        composeTestRule.setContent {
            ProjectAuraTheme {
                InsightCard(insight = insight)
            }
        }

        composeTestRule
            .onNodeWithText("Talking point")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("This is a talking point")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("85%")
            .assertIsDisplayed()
    }

    @Test
    fun testInsightCard_AllTypes() {
        val insightTypes = listOf(
            InsightType.TALKING_POINT to "Talking point",
            InsightType.CODE_SNIPPET to "Code snippet",
            InsightType.CLARIFICATION to "Clarification",
            InsightType.ACTION_ITEM to "Action item",
            InsightType.REFERENCE to "Reference",
            InsightType.WARNING to "Warning"
        )

        insightTypes.forEach { (type, expectedText) ->
            val insight = Insight(
                timestamp = System.currentTimeMillis(),
                content = "Test content",
                confidence = 0.75f,
                type = type
            )

            composeTestRule.setContent {
                ProjectAuraTheme {
                    InsightCard(insight = insight)
                }
            }

            composeTestRule
                .onNodeWithText(expectedText)
                .assertIsDisplayed()
        }
    }

    @Test
    fun testInsightTypeIcon_AllTypes() {
        val insightTypes = InsightType.values()

        insightTypes.forEach { type ->
            composeTestRule.setContent {
                ProjectAuraTheme {
                    InsightTypeIcon(type = type)
                }
            }

            composeTestRule
                .onNodeWithContentDescription(type.name)
                .assertIsDisplayed()
        }
    }
}