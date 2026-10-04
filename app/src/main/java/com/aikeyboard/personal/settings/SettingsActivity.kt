package com.aikeyboard.personal.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement

class SettingsActivity : ComponentActivity() {

    private var refresh by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SettingsScreen(refresh)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh++
    }
}

private fun isKeyboardEnabled(context: Context): Boolean {
    val imm = context.getSystemService(InputMethodManager::class.java) ?: return false
    return imm.enabledInputMethodList.any { it.packageName == context.packageName }
}

private fun isKeyboardSelected(context: Context): Boolean {
    val current = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.DEFAULT_INPUT_METHOD,
    )
    return current?.startsWith(context.packageName + "/") == true
}

@Composable
private fun SettingsScreen(refresh: Int) {
    val context = LocalContext.current
    val enabled = remember(refresh) { isKeyboardEnabled(context) }
    val selected = remember(refresh) { isKeyboardSelected(context) }
    var testText by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("AI Keyboard", style = MaterialTheme.typography.headlineMedium)
            Text("Stage 1: basic English keyboard (personal use).")

            Text(if (enabled) "✔ 1. Keyboard is enabled" else "✘ 1. Keyboard is not enabled yet")
            Text(if (selected) "✔ 2. AI Keyboard is the current keyboard" else "✘ 2. AI Keyboard is not the current keyboard")

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                },
            ) { Text("Open keyboard settings") }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                onClick = {
                    context.getSystemService(InputMethodManager::class.java)
                        ?.showInputMethodPicker()
                },
            ) { Text("Choose AI Keyboard") }

            OutlinedTextField(
                value = testText,
                onValueChange = { testText = it },
                label = { Text("Tap here to test the keyboard") },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                "Privacy: AI Keyboard does not record, store or send what you type. " +
                    "It has no internet permission, no ads and no analytics.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
