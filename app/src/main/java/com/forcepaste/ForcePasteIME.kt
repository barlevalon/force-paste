package com.forcepaste

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.coroutines.*

class ForcePasteIME : InputMethodService() {

    private var pasteJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateInputView(): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF303030.toInt())
            setPadding(16, 16, 16, 16)
        }

        val pasteButton = Button(this).apply {
            text = getString(R.string.paste_button)
            textSize = 18f
            setBackgroundColor(0xFF6200EE.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(32, 48, 32, 48)

            setOnClickListener {
                pasteClipboard()
            }
        }

        layout.addView(pasteButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        return layout
    }

    private fun pasteClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip

        if (clip == null || clip.itemCount == 0) {
            Toast.makeText(this, R.string.empty_clipboard, Toast.LENGTH_SHORT).show()
            return
        }

        val text = clip.getItemAt(0).coerceToText(this).toString()
        if (text.isEmpty()) {
            Toast.makeText(this, R.string.empty_clipboard, Toast.LENGTH_SHORT).show()
            return
        }

        val inputConnection = currentInputConnection ?: return

        // Cancel any existing paste job
        pasteJob?.cancel()

        pasteJob = scope.launch {
            for (char in text) {
                if (!isActive) break
                inputConnection.commitText(char.toString(), 1)
                delay(5) // 5ms delay between characters
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pasteJob?.cancel()
        scope.cancel()
    }
}
