package com.aikeyboard.personal

import android.inputmethodservice.InputMethodService
import android.os.SystemClock
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.aikeyboard.personal.input.InputController
import com.aikeyboard.personal.keyboard.KeyAction
import com.aikeyboard.personal.keyboard.KeyboardLayer
import com.aikeyboard.personal.keyboard.KeyboardUiState
import com.aikeyboard.personal.keyboard.ShiftState
import com.aikeyboard.personal.ui.KeyboardScreen

class AiKeyboardService :
    InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    // Compose needs these owners because an IME has no Activity.
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    private val uiState = KeyboardUiState()
    private lateinit var input: InputController
    private var lastShiftTapMs = 0L

    override fun onCreate() {
        super.onCreate()
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        input = InputController(this)
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KeyboardScreen(state = uiState, onKey = ::onKey)
            }
        }

        window?.window?.decorView?.let { decor ->
            ViewTreeLifecycleOwner.set(decor, this)
            ViewTreeViewModelStoreOwner.set(decor, this)
            ViewTreeSavedStateRegistryOwner.set(decor, this)
        }

        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        return composeView
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        uiState.enterLabel = input.enterLabelFor(info)
        if (restarting) return

        val inputType = info?.inputType ?: 0
        uiState.layer = when (inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE -> KeyboardLayer.NUMBERS
            else -> KeyboardLayer.LETTERS
        }

        uiState.shift = when {
            !input.isFieldEmpty() -> ShiftState.OFF
            (inputType and InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS) != 0 -> ShiftState.CAPS_LOCK
            (inputType and InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) != 0 ||
                (inputType and InputType.TYPE_TEXT_FLAG_CAP_WORDS) != 0 -> ShiftState.ON
            else -> ShiftState.OFF
        }
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        store.clear()
        super.onDestroy()
    }

    private fun onKey(action: KeyAction) {
        when (action) {
            is KeyAction.Text -> {
                input.commitText(action.value)
                if (uiState.shift == ShiftState.ON) uiState.shift = ShiftState.OFF
            }
            KeyAction.Shift -> handleShift()
            KeyAction.Backspace -> input.backspace()
            KeyAction.Space -> input.commitText(" ")
            KeyAction.Enter -> input.enter()
            is KeyAction.SwitchLayer -> uiState.layer = action.layer
            KeyAction.None -> Unit
        }
    }

    private fun handleShift() {
        val now = SystemClock.uptimeMillis()
        val quick = now - lastShiftTapMs < DOUBLE_TAP_MS
        uiState.shift = when (uiState.shift) {
            ShiftState.OFF -> if (quick) ShiftState.CAPS_LOCK else ShiftState.ON
            ShiftState.ON -> if (quick) ShiftState.CAPS_LOCK else ShiftState.OFF
            ShiftState.CAPS_LOCK -> ShiftState.OFF
        }
        lastShiftTapMs = now
    }

    private companion object {
        const val DOUBLE_TAP_MS = 350L
    }
}
