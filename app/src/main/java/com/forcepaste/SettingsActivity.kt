package com.forcepaste

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ScrollView
import kotlin.math.roundToInt
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private fun dp(value: Int) = (value * resources.displayMetrics.density).roundToInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        val title = TextView(this).apply {
            text = getString(R.string.settings_title)
            textSize = 24f
            setPadding(0, 0, 0, dp(16))
        }

        val instructions = TextView(this).apply {
            text = getString(R.string.settings_instructions)
            textSize = 16f
            setPadding(0, 0, 0, dp(24))
        }

        val enableButton = Button(this).apply {
            text = getString(R.string.enable_keyboard)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }

        layout.addView(title)
        layout.addView(instructions)
        layout.addView(enableButton)

        setContentView(ScrollView(this).apply { addView(layout) })
    }
}
