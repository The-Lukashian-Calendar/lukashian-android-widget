package org.lukashian.calendarwidget.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.lukashian.calendarwidget.Logger
import org.lukashian.calendarwidget.model.CalendarInfo
import org.lukashian.calendarwidget.model.Instance.EARTH
import org.lukashian.calendarwidget.model.Instance.MARS
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

private val logger = Logger(CalendarInfoUpdater::class)

internal interface LukashianApi {
    @GET("earth")
    suspend fun getEarthCalendarInfo(): Response<CalendarInfo>

    @GET("mars")
    suspend fun getMarsCalendarInfo(): Response<CalendarInfo>
}

private const val BASE_URL = "https://www.lukashian.org/api/watchinfo/"

private val lukashianClient: LukashianApi by lazy {
    Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LukashianApi::class.java)
}

internal class CalendarInfoUpdater(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            logger.info("Retrieving Earth Calendar Info")
            val earthInfo = lukashianClient.getEarthCalendarInfo()

            logger.info("Retrieving Mars Calendar Info")
            val marsInfo = lukashianClient.getMarsCalendarInfo()

            if (
                earthInfo.isSuccessful && earthInfo.body() != null &&
                marsInfo.isSuccessful && marsInfo.body() != null
            ) {
                logger.info("Saving Calendar Info")
                applicationContext.saveCalendarInfo(EARTH, earthInfo.body()!!)
                applicationContext.saveCalendarInfo(MARS, marsInfo.body()!!)
                Result.success()
            } else {
                logger.error("Retrieval failed.\nEarth result: ${earthInfo.body()}\nMars result: ${marsInfo.body()}")
                Result.failure()
            }
        } catch (e: Exception) {
            logger.error("Error during retrieval")
            e.printStackTrace()
            Result.retry()
        }
    }
}

internal fun scheduleCalendarInfoUpdate(context: Context) {
    logger.info("Scheduling CalendarInfo update")

    val immediateUpdate = OneTimeWorkRequestBuilder<CalendarInfoUpdater>()
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .addTag("immediateCalendarInfoUpdate")
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "immediateCalendarInfoUpdate",
        ExistingWorkPolicy.REPLACE,
        immediateUpdate
    )

    val periodicUpdate = PeriodicWorkRequestBuilder<CalendarInfoUpdater>(1, TimeUnit.DAYS)
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .addTag("periodicCalendarInfoUpdate")
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "periodicCalendarInfoUpdate",
        ExistingPeriodicWorkPolicy.REPLACE,
        periodicUpdate
    )
}

internal fun unscheduleCalendarInfoUpdate(context: Context) {
    logger.info("Unscheduling CalendarInfo update")
    WorkManager.getInstance(context).cancelAllWorkByTag("periodicCalendarInfoUpdate")
}
