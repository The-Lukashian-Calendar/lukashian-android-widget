package org.lukashian.calendarwidget

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.lukashian.calendarwidget.databinding.LukashianWidgetMainBinding

class LukashianMainActivity : AppCompatActivity() {
    private val logger = Logger(LukashianMainActivity::class)

    private lateinit var binding: LukashianWidgetMainBinding

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        logger.info("Main Activity Created")

        binding = LukashianWidgetMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
