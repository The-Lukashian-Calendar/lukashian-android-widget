package org.lukashian.calendarwidget.main

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.lukashian.calendarwidget.Logger
import org.lukashian.calendarwidget.R

class AlarmService : Service() {

    companion object {
        const val ALARM_CHANNEL_ID = "alarm_channel"
        const val ALARM_NOTIFICATION_ID = 1001
        const val ACTION_STOP_ALARM_SOUND = "org.lukashian.calendarwidget.ACTION_STOP_ALARM_SOUND"
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var audioManager: AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private val logger = Logger(AlarmService::class)

    override fun onCreate() {
        super.onCreate()

        logger.info("AlarmService Created")

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.info("AlarmService Started")

        if (intent?.action == ACTION_STOP_ALARM_SOUND) {
            logger.info("Stop Alarm Sound Requested")
            stopAlarm()
            return START_NOT_STICKY
        }

        logger.info("Start Alarm Sound Requested")

        //Create Notification Channel
        val channel = NotificationChannel(ALARM_CHANNEL_ID, "Alarms", IMPORTANCE_HIGH).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)

        // Create the Intent for full-screen Ring Activity
        val ringRequestCode = LukashianMainAlarmRingActivity::class.java.name.hashCode()
        val ringIntent = Intent(applicationContext, LukashianMainAlarmRingActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .setPackage(applicationContext.packageName)
        val ringPendingIntent = PendingIntent.getActivity(applicationContext, ringRequestCode, ringIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //Create the Intent for dismissing the Alarm
        val stopSoundRequestCode = AlarmService::class.java.name.hashCode()
        val stopSoundIntent = Intent(applicationContext, AlarmService::class.java)
            .setAction(ACTION_STOP_ALARM_SOUND)
            .setPackage(applicationContext.packageName)
        val stopSoundPendingIntent = PendingIntent.getService(applicationContext, stopSoundRequestCode, stopSoundIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification with the full-screen intent attached
        val builder = NotificationCompat.Builder(applicationContext, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.alarm_ring)
            .setContentTitle("Lukashian Alarm - 5000 beeps") //TODO: replace with number of currently set alarm
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(ringPendingIntent, true)
            .setAutoCancel(false)
            .setSound(null)
            .setDeleteIntent(stopSoundPendingIntent) //Swipe away
            .addAction(R.drawable.alarm_off, "Dismiss", stopSoundPendingIntent) //Dismiss button

        //Show the notification to the user
        startForeground(ALARM_NOTIFICATION_ID, builder.build(), FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)

        //Initialize Audio Attributes
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        //Request Audio Focus
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(false)
            .build()
        audioManager.requestAudioFocus(audioFocusRequest!!)

        //Sound the Alarm
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(audioAttributes)
            setDataSource(applicationContext,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            )
            isLooping = true
            prepare()
            start()
        }

        return START_STICKY
    }

    private fun stopAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        logger.info("AlarmService Destroyed")

        stopAlarm()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
