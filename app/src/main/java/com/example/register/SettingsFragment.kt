package com.example.register

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSeekBars()
        setupSaveButton()
        loadSavedSettings()
    }

    private fun setupSeekBars() {
        setupSeekBarListener(view?.findViewById(R.id.seekBarSpeed), view?.findViewById(R.id.tvSpeedValue))
        setupSeekBarListener(view?.findViewById(R.id.seekBarMaxCockroaches), view?.findViewById(R.id.tvMaxCockroachesValue))
        setupSeekBarListener(view?.findViewById(R.id.seekBarBonusInterval), view?.findViewById(R.id.tvBonusIntervalValue))
        setupSeekBarListener(view?.findViewById(R.id.seekBarRoundDuration), view?.findViewById(R.id.tvRoundDurationValue))
    }

    private fun setupSeekBarListener(seekBar: SeekBar?, textView: TextView?) {
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView?.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupSaveButton() {
        view?.findViewById<Button>(R.id.btnSaveSettings)?.setOnClickListener {
            saveSettings()
            android.widget.Toast.makeText(requireContext(), "Настройки сохранены!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSettings() {
        val sharedPref = requireActivity().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("game_speed", view?.findViewById<SeekBar>(R.id.seekBarSpeed)?.progress ?: 5)
            putInt("max_cockroaches", view?.findViewById<SeekBar>(R.id.seekBarMaxCockroaches)?.progress ?: 10)
            putInt("bonus_interval", view?.findViewById<SeekBar>(R.id.seekBarBonusInterval)?.progress ?: 30)
            putInt("round_duration", view?.findViewById<SeekBar>(R.id.seekBarRoundDuration)?.progress ?: 60)
            apply()
        }
    }

    private fun loadSavedSettings() {
        val sharedPref = requireActivity().getSharedPreferences("game_settings", Context.MODE_PRIVATE)

        setSeekBarProgress(R.id.seekBarSpeed, sharedPref.getInt("game_speed", 5), R.id.tvSpeedValue)
        setSeekBarProgress(R.id.seekBarMaxCockroaches, sharedPref.getInt("max_cockroaches", 10), R.id.tvMaxCockroachesValue)
        setSeekBarProgress(R.id.seekBarBonusInterval, sharedPref.getInt("bonus_interval", 30), R.id.tvBonusIntervalValue)
        setSeekBarProgress(R.id.seekBarRoundDuration, sharedPref.getInt("round_duration", 60), R.id.tvRoundDurationValue)
    }

    private fun setSeekBarProgress(seekBarId: Int, progress: Int, textViewId: Int) {
        view?.findViewById<SeekBar>(seekBarId)?.progress = progress
        view?.findViewById<TextView>(textViewId)?.text = progress.toString()
    }
}