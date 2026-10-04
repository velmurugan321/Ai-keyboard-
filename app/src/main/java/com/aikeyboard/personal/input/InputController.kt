package com.aikeyboard.personal.input

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo

/**
 * The only class that talks to the focused text field.
 * Nothing typed is stored, logged or sent anywhere.
 */
class InputController(private val service: InputMethodService) {

    fun commitText(text: String) {
        service.currentInputConnection?.commitText(text, 1)
    }

    fun backspace() {
        val ic = service.currentInputConnection ?: return

        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
            return
        }

        val before = ic.getTextBeforeCursor(2, 0)
        if (before.isNullOrEmpty()) {
            // Some editors (web views etc.) do not expose text; use a real key event.
            service.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
        } else {
            val count = if (before.length == 2 &&
                Character.isHighSurrogate(before[0]) &&
                Character.isLowSurrogate(before[1])
            ) 2 else 1
            ic.deleteSurroundingText(count, 0)
        }
    }

    fun enter() {
        val ic = service.currentInputConnection
        val info = service.currentInputEditorInfo
        if (ic == null || info == null) {
            service.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
            return
        }
        val action = editorActionOf(info)
        if (action != null) {
            ic.performEditorAction(action)
        } else {
            service.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
    }

    fun isFieldEmpty(): Boolean {
        val before = service.currentInputConnection?.getTextBeforeCursor(1, 0)
        return before.isNullOrEmpty()
    }

    fun enterLabelFor(info: EditorInfo?): String {
        val action = info?.let { editorActionOf(it) } ?: return "↵"
        return when (action) {
            EditorInfo.IME_ACTION_GO -> "Go"
            EditorInfo.IME_ACTION_SEARCH -> "Search"
            EditorInfo.IME_ACTION_SEND -> "Send"
            EditorInfo.IME_ACTION_NEXT -> "Next"
            EditorInfo.IME_ACTION_DONE -> "Done"
            EditorInfo.IME_ACTION_PREVIOUS -> "Prev"
            else -> "↵"
        }
    }

    private fun editorActionOf(info: EditorInfo): Int? {
        if ((info.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) return null
        val action = info.imeOptions and EditorInfo.IME_MASK_ACTION
        return if (action == EditorInfo.IME_ACTION_NONE ||
            action == EditorInfo.IME_ACTION_UNSPECIFIED
        ) null else action
    }
}
