package org.lukashian.calendarwidget.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.lukashian.calendarwidget.Logger
import org.lukashian.calendarwidget.databinding.LukashianAlarmRingBinding

class LukashianMainAlarmRingActivity : AppCompatActivity() {
    private val logger = Logger(LukashianMainAlarmRingActivity::class)

    private lateinit var binding: LukashianAlarmRingBinding

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        logger.info("Alarm Ring Activity Created")

        binding = LukashianAlarmRingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    //TODO: layout with option to dismiss (button with AlarmService stopsound intent)
    //What happens if you press it when you've already dismissed it in the notification bar?
    //When you press it, dismiss the whole window
}
