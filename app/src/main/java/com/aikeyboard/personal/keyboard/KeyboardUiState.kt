package com.aikeyboard.personal.keyboard

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class KeyboardUiState {
    var layer by mutableStateOf(KeyboardLayer.LETTERS)
    var shift by mutableStateOf(ShiftState.OFF)
    var enterLabel by mutableStateOf("↵")
}
