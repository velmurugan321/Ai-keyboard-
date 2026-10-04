package com.aikeyboard.personal.keyboard

/**
 * All key layouts live here. Future stages (Tamil, Tanglish, emoji, ...)
 * add new layers / layout providers next to these.
 */
object KeyboardLayouts {

    fun rows(
        layer: KeyboardLayer,
        shift: ShiftState,
        enterLabel: String,
    ): List<List<KeySpec>> = when (layer) {
        KeyboardLayer.LETTERS -> letters(shift, enterLabel)
        KeyboardLayer.NUMBERS -> numbers(enterLabel)
        KeyboardLayer.SYMBOLS -> symbols(enterLabel)
    }

    private fun textKeys(chars: String): List<KeySpec> =
        chars.map { KeySpec(it.toString(), KeyAction.Text(it.toString())) }

    private fun spacer(weight: Float) = KeySpec("", KeyAction.None, weight)

    private fun backspaceKey() = KeySpec(
        label = "⌫",
        action = KeyAction.Backspace,
        weight = 1.5f,
        style = KeyStyle.FUNCTION,
        repeatable = true,
    )

    private fun bottomRow(
        switchLabel: String,
        target: KeyboardLayer,
        enterLabel: String,
    ): List<KeySpec> = listOf(
        KeySpec(switchLabel, KeyAction.SwitchLayer(target), 1.5f, KeyStyle.FUNCTION),
        KeySpec(",", KeyAction.Text(",")),
        KeySpec("space", KeyAction.Space, 5f),
        KeySpec(".", KeyAction.Text(".")),
        KeySpec(enterLabel, KeyAction.Enter, 1.5f, KeyStyle.ACCENT),
    )

    private fun letters(shift: ShiftState, enterLabel: String): List<List<KeySpec>> {
        val upper = shift != ShiftState.OFF
        fun letterRow(chars: String): List<KeySpec> = chars.map { c ->
            val s = if (upper) c.uppercaseChar().toString() else c.toString()
            KeySpec(s, KeyAction.Text(s))
        }

        val shiftKey = KeySpec(
            label = if (shift == ShiftState.CAPS_LOCK) "⇪" else "⇧",
            action = KeyAction.Shift,
            weight = 1.5f,
            style = if (shift == ShiftState.OFF) KeyStyle.FUNCTION else KeyStyle.ACCENT,
        )

        return listOf(
            letterRow("qwertyuiop"),
            listOf(spacer(0.5f)) + letterRow("asdfghjkl") + listOf(spacer(0.5f)),
            listOf(shiftKey) + letterRow("zxcvbnm") + listOf(backspaceKey()),
            bottomRow("?123", KeyboardLayer.NUMBERS, enterLabel),
        )
    }

    private fun numbers(enterLabel: String): List<List<KeySpec>> = listOf(
        textKeys("1234567890"),
        textKeys("@#$" + "_&-+()/"),
        listOf(KeySpec("=\\<", KeyAction.SwitchLayer(KeyboardLayer.SYMBOLS), 1.5f, KeyStyle.FUNCTION)) +
            textKeys("*\"':;!?") + listOf(backspaceKey()),
        bottomRow("ABC", KeyboardLayer.LETTERS, enterLabel),
    )

    private fun symbols(enterLabel: String): List<List<KeySpec>> = listOf(
        textKeys("[]{}#%^*+="),
        textKeys("_\\|~<>€£¥•"),
        listOf(KeySpec("123", KeyAction.SwitchLayer(KeyboardLayer.NUMBERS), 1.5f, KeyStyle.FUNCTION)) +
            textKeys(".,?!'\"`") + listOf(backspaceKey()),
        bottomRow("ABC", KeyboardLayer.LETTERS, enterLabel),
    )
}
