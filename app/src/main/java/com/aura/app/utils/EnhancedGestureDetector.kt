package com.aura.app.utils

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sqrt

enum class GestureType {
    TAP, DOUBLE_TAP, LONG_PRESS, SWIPE_UP, SWIPE_DOWN, SWIPE_LEFT, SWIPE_RIGHT, DRAG
}

data class SwipeGesture(
    val direction: GestureType,
    val distance: Float,
    val velocity: Float
)

class GestureHandler {
    companion object {
        private const val DOUBLE_TAP_TIMEOUT = 300L // ms
        private const val LONG_PRESS_TIMEOUT = 500L // ms
        private const val MIN_SWIPE_DISTANCE = 100f // pixels
        private const val MIN_SWIPE_VELOCITY = 200f // pixels per second
    }
    
    private var lastTapTime = 0L
    private var tapCount = 0
    
    /**
     * Detect swipe direction from drag gesture
     */
    fun detectSwipeDirection(start: Offset, end: Offset, timeMs: Long): SwipeGesture? {
        val deltaX = end.x - start.x
        val deltaY = end.y - start.y
        val distance = sqrt(deltaX * deltaX + deltaY * deltaY)
        
        if (distance < MIN_SWIPE_DISTANCE) return null
        
        val velocity = distance / (timeMs / 1000f) // pixels per second
        if (velocity < MIN_SWIPE_VELOCITY) return null
        
        val direction = when {
            abs(deltaX) > abs(deltaY) -> {
                if (deltaX > 0) GestureType.SWIPE_RIGHT else GestureType.SWIPE_LEFT
            }
            deltaY > 0 -> GestureType.SWIPE_DOWN
            else -> GestureType.SWIPE_UP
        }
        
        return SwipeGesture(direction, distance, velocity)
    }
    
    /**
     * Reset tap counting state
     */
    fun resetTapState() {
        tapCount = 0
        lastTapTime = 0L
    }
}

/**
 * Enhanced gesture detection that supports multiple gesture types
 */
fun Modifier.enhancedGestureDetection(
    onTap: (() -> Unit)? = null,
    onDoubleTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    onSwipe: ((SwipeGesture) -> Unit)? = null,
    onDrag: ((Offset, Offset) -> Unit)? = null,
    onDragStart: (() -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null
) = this.pointerInput(Unit) {
    val gestureHandler = GestureHandler()
    
    detectTapGestures(
        onTap = { offset ->
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - gestureHandler.lastTapTime < GestureHandler.DOUBLE_TAP_TIMEOUT) {
                // Double tap detected
                onDoubleTap?.invoke()
                gestureHandler.resetTapState()
            } else {
                // Single tap - wait to see if double tap follows
                gestureHandler.lastTapTime = currentTime
                kotlinx.coroutines.launch {
                    delay(GestureHandler.DOUBLE_TAP_TIMEOUT)
                    if (System.currentTimeMillis() - gestureHandler.lastTapTime >= GestureHandler.DOUBLE_TAP_TIMEOUT) {
                        onTap?.invoke()
                    }
                }
            }
        },
        onLongPress = { offset ->
            onLongPress?.invoke()
        }
    )
}.pointerInput(Unit) {
    if (onSwipe != null || onDrag != null) {
        var dragStart = Offset.Zero
        var dragStartTime = 0L
        
        detectDragGestures(
            onDragStart = { offset ->
                dragStart = offset
                dragStartTime = System.currentTimeMillis()
                onDragStart?.invoke()
            },
            onDragEnd = {
                val dragEnd = System.currentTimeMillis()
                val dragDuration = dragEnd - dragStartTime
                
                onDragEnd?.invoke()
                
                // Check if this was a swipe gesture
                onSwipe?.let { swipeCallback ->
                    // We need the end position, but detectDragGestures doesn't provide it directly
                    // We'll approximate using the last drag position
                }
            },
            onDrag = { change, _ ->
                onDrag?.invoke(dragStart, change.position)
                
                // Check for swipe during drag
                val currentTime = System.currentTimeMillis()
                val dragDuration = currentTime - dragStartTime
                
                if (dragDuration > 50) { // Minimum drag time to detect swipe
                    val swipe = gestureHandler.detectSwipeDirection(dragStart, change.position, dragDuration)
                    swipe?.let { onSwipe?.invoke(it) }
                }
            }
        )
    }
}

/**
 * Optimized gesture detection specifically for overlay interactions
 */
fun Modifier.overlayGestureDetection(
    onToggleExpanded: () -> Unit,
    onCopyContent: () -> Unit,
    onVoiceReadback: () -> Unit,
    onDismiss: () -> Unit,
    onMove: (Offset, Offset) -> Unit
) = this.enhancedGestureDetection(
    onDoubleTap = onToggleExpanded,
    onLongPress = onVoiceReadback,
    onSwipe = { swipe ->
        when (swipe.direction) {
            GestureType.SWIPE_UP -> onToggleExpanded()
            GestureType.SWIPE_DOWN -> onDismiss()
            GestureType.SWIPE_LEFT -> onCopyContent()
            GestureType.SWIPE_RIGHT -> onVoiceReadback()
            else -> {}
        }
    },
    onDrag = onMove
)