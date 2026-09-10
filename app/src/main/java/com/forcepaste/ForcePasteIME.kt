package com.forcepaste

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class ForcePasteIME : InputMethodService() {

    private var pasteJob: Job? = null
    private var pasteButton: Button? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private fun dp(value: Int) = (value * resources.displayMetrics.density).roundToInt()

    override fun onCreateInputView(): View {
        cancelPaste()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF303030.toInt())
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        pasteButton = Button(this).apply {
            text = getString(R.string.paste_button)
            textSize = 18f
            setBackgroundColor(0xFF6200EE.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setOnClickListener {
                if (pasteJob != null) {
                    cancelPaste()
                    feedback(R.string.paste_cancelled)
                } else {
                    pasteClipboard()
                }
            }
        }
        layout.addView(pasteButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
        return layout
    }

    private fun feedback(message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun pasteClipboard() {
        // Keep this target for the whole operation; never follow focus to another field.
        val inputConnection = currentInputConnection
        if (inputConnection == null) {
            feedback(R.string.no_input_connection)
            return
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = try {
            clipboard.primaryClip
        } catch (_: RuntimeException) {
            feedback(R.string.clipboard_unavailable)
            return
        }
        if (clip == null || clip.itemCount == 0) {
            feedback(R.string.empty_clipboard)
            return
        }
        // Inline text only: coercing URI/Intent clips can synchronously call a provider.
        val text = clip.getItemAt(0).text?.toString()
        if (text == null) {
            feedback(R.string.unsupported_clipboard)
            return
        }
        if (text.isEmpty()) {
            feedback(R.string.empty_clipboard)
            return
        }

        pasteButton?.setText(R.string.cancel_paste)
        pasteJob = scope.launch {
            try {
                val completed = pasteText(text) { inputConnection.commitText(it, 1) }
                feedback(if (completed) R.string.paste_sent else R.string.paste_failed)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: RuntimeException) {
                feedback(R.string.paste_failed)
            } finally {
                if (pasteJob === coroutineContext[Job]) {
                    pasteJob = null
                    pasteButton?.setText(R.string.paste_button)
                }
            }
        }
    }

    private fun cancelPaste() {
        pasteJob?.cancel()
        pasteJob = null
        pasteButton?.setText(R.string.paste_button)
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        cancelPaste()
        super.onStartInput(attribute, restarting)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        cancelPaste()
        super.onFinishInputView(finishingInput)
    }

    override fun onFinishInput() {
        cancelPaste()
        super.onFinishInput()
    }

    override fun onDestroy() {
        cancelPaste()
        scope.cancel()
        pasteButton = null
        super.onDestroy()
    }
}
