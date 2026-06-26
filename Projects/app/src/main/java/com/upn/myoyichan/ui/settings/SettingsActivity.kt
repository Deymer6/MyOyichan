package com.upn.myoyichan.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.SeekBar
import com.upn.myoyichan.R
import com.upn.myoyichan.databinding.ActivitySettingsBinding
import com.upn.myoyichan.ui.BaseActivity
import com.upn.myoyichan.utils.PreferencesManager
import com.upn.myoyichan.utils.VoiceManager

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: PreferencesManager
    private lateinit var voiceManager: VoiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferencesManager(this)
        voiceManager = VoiceManager(this)

        binding.btnBack.setOnClickListener { finish() }

        cargarValores()
        configurarListeners()
    }

    private fun cargarValores() {
        binding.seekVoiceSpeed.progress = (prefs.voiceSpeed * 100).toInt()
        binding.seekVoicePitch.progress = (prefs.voicePitch * 100).toInt()
        binding.switchVoice.isChecked = prefs.voiceAssistantEnabled
        binding.switchNotifications.isChecked = prefs.notificationsEnabled
        binding.spFontSize.setSelection(prefs.fontSizeIndex)
    }

    private fun configurarListeners() {
        binding.seekVoiceSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                prefs.voiceSpeed = (binding.seekVoiceSpeed.progress / 100f).coerceIn(0.5f, 2.0f)
                previsualizarVoz()
            }
        })

        binding.seekVoicePitch.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                prefs.voicePitch = (binding.seekVoicePitch.progress / 100f).coerceIn(0.5f, 2.0f)
                previsualizarVoz()
            }
        })

        binding.switchVoice.setOnCheckedChangeListener { _, isChecked ->
            prefs.voiceAssistantEnabled = isChecked
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.notificationsEnabled = isChecked
        }

        binding.spFontSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Solo recreamos si realmente cambió, para evitar bucles con la selección inicial.
                if (position != prefs.fontSizeIndex) {
                    prefs.fontSizeIndex = position
                    recreate() // Aplica el nuevo tamaño de fuente al instante
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun previsualizarVoz() {
        voiceManager.setSpeechRate(prefs.voiceSpeed)
        voiceManager.setSpeechPitch(prefs.voicePitch)
        voiceManager.hablar(getString(R.string.settings_voz_preview))
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.destroy()
    }
}
