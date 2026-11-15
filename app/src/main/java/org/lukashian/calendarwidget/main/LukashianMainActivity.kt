package org.lukashian.calendarwidget.main

import android.Manifest
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.lukashian.calendarwidget.Logger
import org.lukashian.calendarwidget.databinding.LukashianMainBinding
import org.lukashian.calendarwidget.main.LukashianMainActivity.Companion.ACTION_TRIGGER_ALARM
import org.lukashian.calendarwidget.piForActivity
import org.lukashian.calendarwidget.piForBroadcast
import java.time.ZonedDateTime

class LukashianMainActivity : AppCompatActivity() {

    companion object {
        const val ACTION_TRIGGER_ALARM = "org.lukashian.calendarwidget.ACTION_TRIGGER_ALARM"
    }

    private val logger = Logger(LukashianMainActivity::class)

    private lateinit var binding: LukashianMainBinding

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        logger.info("Main Activity Created")

        binding = LukashianMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Request permissions
        listOf(
            Manifest.permission.INTERNET,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.USE_EXACT_ALARM,
            Manifest.permission.USE_FULL_SCREEN_INTENT,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
        ).filter { ContextCompat.checkSelfPermission(this, it) != PERMISSION_GRANTED }
         .takeIf { it.isNotEmpty() }
         ?.let { registerForActivityResult(RequestMultiplePermissions(), {}).launch(it.toTypedArray()) }






        //TODO: simplify intent creation (getBroadcast, getActivity, getService)??
        //TODO: layout
        /*
        -First, figure out which lukashian day the alarm will be, either today or tomo, by comparing with current beeps
        -Then, you now day and proportion of day when the alarm should go off, this then easily translates into an offset based on json data
        -This can then be translated into a UTC timestamp for the alarm
        -See WidgetRenderer.showLoading for which cases we have to handle in context of the alarm
            -We may not have to handle it in the UI, only in the triggering of the alarm?
        -Play a sound and turn it off, turning it on by the user will schedule it again
        -Manually turning it off shoudl unschedule iut
         */

        val showPI = piForActivity(LukashianMainActivity::class.java, applicationContext)
        val triggerPI = piForBroadcast(AlarmReceiver::class.java, applicationContext, action = ACTION_TRIGGER_ALARM)

        val triggerTime = ZonedDateTime.now().plusSeconds(10)
        val alarmInfo = AlarmManager.AlarmClockInfo(triggerTime.toInstant().toEpochMilli(), showPI)
        val alarmManager = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager

        alarmManager.setAlarmClock(alarmInfo, triggerPI)
    }
}

internal class AlarmReceiver : BroadcastReceiver() {
    private val logger = Logger(AlarmReceiver::class)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TRIGGER_ALARM -> {
                logger.info("Trigger Alarm received")
                //TODO: disable actual alarm
                ContextCompat.startForegroundService(context, Intent(context, AlarmService::class.java))
            }
        }
    }
}
