package com.aikeyboard.personal.keyboard

enum class KeyboardLayer { LETTERS, NUMBERS, SYMBOLS }

enum class ShiftState { OFF, ON, CAPS_LOCK }

sealed interface KeyAction {
    data class Text(val value: String) : KeyAction
    data object Shift : KeyAction
    data object Backspace : KeyAction
    data object Space : KeyAction
    data object Enter : KeyAction
    data class SwitchLayer(val layer: KeyboardLayer) : KeyAction
    data object None : KeyAction
}

enum class KeyStyle { NORMAL, FUNCTION, ACCENT }

data class KeySpec(
    val label: String,
    val action: KeyAction,
    val weight: Float = 1f,
    val style: KeyStyle = KeyStyle.NORMAL,
    val repeatable: Boolean = false,
)
