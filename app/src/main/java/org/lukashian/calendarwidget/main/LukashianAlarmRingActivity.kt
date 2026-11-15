package org.lukashian.calendarwidget.main

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.PendingIntent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import org.lukashian.calendarwidget.ACTION_STOP_ALARM_SOUND
import org.lukashian.calendarwidget.Logger
import org.lukashian.calendarwidget.REQUEST_CODE_STOP_ALARM_SOUND_FROM_RING_ACTIVITY
import org.lukashian.calendarwidget.databinding.LukashianAlarmRingBinding
import org.lukashian.calendarwidget.pi

class LukashianAlarmRingActivity : AppCompatActivity() {
    private val logger = Logger(LukashianAlarmRingActivity::class)

    private lateinit var binding: LukashianAlarmRingBinding

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        logger.info("Alarm Ring Activity Created")

        binding = LukashianAlarmRingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //The time of the alarm
        binding.alarmTime.text = "5000" //TODO: replace with number of currently set alarm

        //The pulsating dismiss text
        val scaleX = ObjectAnimator.ofFloat(binding.tapToDismiss, "scaleX", 1.0f, 1.3f).apply {
            duration = 600
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        val scaleY = ObjectAnimator.ofFloat(binding.tapToDismiss, "scaleY", 1.0f, 1.3f).apply {
            duration = 600
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        //The global tap intent to turn off the alarm
        binding.root.setOnClickListener {
            val stopSoundPI = pi(PendingIntent::getService, applicationContext,
                iReceiverClass = AlarmService::class.java,
                iAction = ACTION_STOP_ALARM_SOUND,
                piRequestCode = REQUEST_CODE_STOP_ALARM_SOUND_FROM_RING_ACTIVITY
            )
            stopSoundPI.send()
            finishAndRemoveTask()
        }
    }
}
