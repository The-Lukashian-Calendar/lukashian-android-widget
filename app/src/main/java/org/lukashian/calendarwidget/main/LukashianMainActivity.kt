package org.lukashian.calendarwidget.main

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import org.lukashian.calendarwidget.ACTION_TRIGGER_ALARM
import org.lukashian.calendarwidget.Logger
import org.lukashian.calendarwidget.REQUEST_CODE_SHOW_MAIN_ACTIVITY
import org.lukashian.calendarwidget.REQUEST_CODE_TRIGGER_ALARM
import org.lukashian.calendarwidget.data.PREFS_KEY
import org.lukashian.calendarwidget.data.alarmEnabled
import org.lukashian.calendarwidget.data.loadAlarmEnabled
import org.lukashian.calendarwidget.data.saveAlarmEnabled
import org.lukashian.calendarwidget.databinding.LukashianMainBinding
import org.lukashian.calendarwidget.pi
import java.time.ZonedDateTime

class LukashianMainActivity : AppCompatActivity() {

    private val logger = Logger(LukashianMainActivity::class)

    private lateinit var prefs: SharedPreferences
    private lateinit var alarmEnabledSwitch: SwitchCompat

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == alarmEnabled()) {
            alarmEnabledSwitch.isChecked = loadAlarmEnabled()
        }
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        logger.info("Main Activity Created")

        prefs = getSharedPreferences(PREFS_KEY, MODE_PRIVATE)

        val binding = LukashianMainBinding.inflate(layoutInflater)
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



        alarmEnabledSwitch = binding.alarmEnabledSwitch
        alarmEnabledSwitch.isChecked = loadAlarmEnabled()
        prefs.registerOnSharedPreferenceChangeListener(prefListener)

        //TODO: layout (make sure to set and unset the alarm whenever the user changes any of the three inputs,
        // in addition to setting the datamanager)
        /*
        -First, figure out which lukashian day the alarm will be, either today or tomo, by comparing with current beeps
        -Then, you now day and proportion of day when the alarm should go off, this then easily translates into an offset based on json data
        -This can then be translated into a UTC timestamp for the alarm
        -Botton screen notification: "Alarm scheduled for xxxx beeps"
        -See WidgetRenderer.showLoading for which cases we have to handle in context of the alarm
            -We may not have to handle it in the UI, only in the triggering of the alarm?
            -Use bottom screen notification "Cannot set alarm because up-to-date calendar information could not be retrieved"
            -Do this while kicking off reload, just like widget renderer
         */

        val showPI = pi(PendingIntent::getActivity, applicationContext,
            iReceiverClass = LukashianMainActivity::class.java,
            piRequestCode = REQUEST_CODE_SHOW_MAIN_ACTIVITY
        )
        val triggerPI = pi(PendingIntent::getBroadcast, applicationContext,
            iReceiverClass = AlarmReceiver::class.java,
            iAction = ACTION_TRIGGER_ALARM,
            piRequestCode = REQUEST_CODE_TRIGGER_ALARM
        )

        val triggerTime = ZonedDateTime.now().plusSeconds(10)
        val alarmInfo = AlarmManager.AlarmClockInfo(triggerTime.toInstant().toEpochMilli(), showPI)
        val alarmManager = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager

        applicationContext.saveAlarmEnabled(true)

        alarmManager.setAlarmClock(alarmInfo, triggerPI)
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
    }
}

internal class AlarmReceiver : BroadcastReceiver() {
    private val logger = Logger(AlarmReceiver::class)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TRIGGER_ALARM -> {
                logger.info("Trigger Alarm received")
                context.saveAlarmEnabled(false)
                ContextCompat.startForegroundService(context, Intent(context, AlarmService::class.java))
            }
        }
    }
}
