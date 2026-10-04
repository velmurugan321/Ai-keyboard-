package com.aikeyboard.personal.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aikeyboard.personal.keyboard.KeyAction
import com.aikeyboard.personal.keyboard.KeySpec
import com.aikeyboard.personal.keyboard.KeyStyle
import com.aikeyboard.personal.keyboard.KeyboardLayouts
import com.aikeyboard.personal.keyboard.KeyboardUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val KeyHeight = 46.dp

private data class Palette(
    val background: Color,
    val key: Color,
    val keyPressed: Color,
    val function: Color,
    val functionPressed: Color,
    val accent: Color,
    val accentPressed: Color,
    val text: Color,
    val accentText: Color,
)

private val LightPalette = Palette(
    background = Color(0xFFE8EAED),
    key = Color(0xFFFFFFFF),
    keyPressed = Color(0xFFC9CDD2),
    function = Color(0xFFCDD1D6),
    functionPressed = Color(0xFFAEB3BA),
    accent = Color(0xFF1A73E8),
    accentPressed = Color(0xFF1558B0),
    text = Color(0xFF202124),
    accentText = Color(0xFFFFFFFF),
)

private val DarkPalette = Palette(
    background = Color(0xFF1F2124),
    key = Color(0xFF3C4043),
    keyPressed = Color(0xFF5F6368),
    function = Color(0xFF2B2D30),
    functionPressed = Color(0xFF45484C),
    accent = Color(0xFF8AB4F8),
    accentPressed = Color(0xFF6A9BE8),
    text = Color(0xFFE8EAED),
    accentText = Color(0xFF202124),
)

@Composable
fun KeyboardScreen(
    state: KeyboardUiState,
    onKey: (KeyAction) -> Unit,
) {
    val palette = if (isSystemInDarkTheme()) DarkPalette else LightPalette
    val rows = KeyboardLayouts.rows(state.layer, state.shift, state.enterLabel)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.background)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 4.dp, vertical = 6.dp),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                row.forEach { spec ->
                    Key(
                        spec = spec,
                        palette = palette,
                        modifier = Modifier.weight(spec.weight),
                        onKey = onKey,
                    )
                }
            }
        }
    }
}

@Composable
private fun Key(
    spec: KeySpec,
    palette: Palette,
    modifier: Modifier,
    onKey: (KeyAction) -> Unit,
) {
    if (spec.action is KeyAction.None) {
        Spacer(modifier = modifier.height(KeyHeight))
        return
    }

    var pressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val currentSpec by rememberUpdatedState(spec)
    val currentOnKey by rememberUpdatedState(onKey)

    val background = when (spec.style) {
        KeyStyle.NORMAL -> if (pressed) palette.keyPressed else palette.key
        KeyStyle.FUNCTION -> if (pressed) palette.functionPressed else palette.function
        KeyStyle.ACCENT -> if (pressed) palette.accentPressed else palette.accent
    }
    val textColor = if (spec.style == KeyStyle.ACCENT) palette.accentText else palette.text

    Box(
        modifier = modifier
            .height(KeyHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    pressed = true
                    var repeatJob: kotlinx.coroutines.Job? = null
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        currentOnKey(currentSpec.action)
                        if (currentSpec.repeatable) {
                            repeatJob = scope.launch {
                                delay(400)
                                while (true) {
                                    currentOnKey(currentSpec.action)
                                    delay(50)
                                }
                            }
                        }
                        waitForUpOrCancellation()
                    } finally {
                        repeatJob?.cancel()
                        pressed = false
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val isSingle = spec.label.length == 1
        Text(
            text = spec.label,
            color = textColor,
            fontSize = if (isSingle) 22.sp else 14.sp,
            fontWeight = if (isSingle) FontWeight.Normal else FontWeight.Medium,
        )
    }
}
