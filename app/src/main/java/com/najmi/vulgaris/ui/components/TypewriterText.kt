package com.najmi.vulgaris.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    charDelayMs: Int = 15,
    onComplete: () -> Unit = {}
) {
    var visibleCharCount by remember(text) { mutableIntStateOf(0) }
    
    LaunchedEffect(text) {
        if (text.isNotEmpty()) {
            animate(
                initialValue = 0f,
                targetValue = text.length.toFloat(),
                animationSpec = tween(
                    durationMillis = text.length * charDelayMs,
                    easing = LinearEasing
                )
            ) { value, _ ->
                visibleCharCount = value.toInt()
            }
            onComplete()
        }
    }
    
    Text(
        text = text.take(visibleCharCount),
        modifier = modifier,
        style = style,
        color = color
    )
}

@Composable
fun TypewriterTextImmediate(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    animate: Boolean = true,
    charDelayMs: Int = 8,
    onComplete: () -> Unit = {}
) {
    if (!animate) {
        Text(
            text = text,
            modifier = modifier,
            style = style,
            color = color
        )
        return
    }
    
    TypewriterText(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        charDelayMs = charDelayMs,
        onComplete = onComplete
    )
}
