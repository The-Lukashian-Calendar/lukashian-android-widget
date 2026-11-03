package org.lukashian.calendarwidget.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.lukashian.calendarwidget.Logger
import org.lukashian.calendarwidget.databinding.LukashianMainBinding

class LukashianMainActivity : AppCompatActivity() {
    private val logger = Logger(LukashianMainActivity::class)

    private lateinit var binding: LukashianMainBinding

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        logger.info("Main Activity Created")

        binding = LukashianMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /*

First, figure out which lukashian day the alarm will be, either today or tomo, by comparing with current beeps
Then, you now day and proportion of day when the alarm should go off, this then easily translates into an offset based on json data
This can then be translated into a UTC timestamp for the alarm

     */
}
