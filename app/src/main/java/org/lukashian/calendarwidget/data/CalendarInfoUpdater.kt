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
import org.lukashian.calendarwidget.model.CalendarInfo
import org.lukashian.calendarwidget.model.Instance.EARTH
import org.lukashian.calendarwidget.model.Instance.MARS
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

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
//            Log.d("CalendarInfoUpdater", "Retrieving Earth Calendar Info")
            val earthInfo = lukashianClient.getEarthCalendarInfo()

//            Log.d("CalendarInfoUpdater", "Retrieving Mars Calendar Info")
            val marsInfo = lukashianClient.getMarsCalendarInfo()

            if (
                earthInfo.isSuccessful && earthInfo.body() != null &&
                marsInfo.isSuccessful && marsInfo.body() != null
            ) {
//                Log.d("CalendarInfoUpdater", "Saving Calendar Info")
                applicationContext.saveCalendarInfo(EARTH, earthInfo.body()!!)
                applicationContext.saveCalendarInfo(MARS, marsInfo.body()!!)
                Result.success()
            } else {
//                Log.e("CalendarInfoUpdater", "Retrieval failed.\nEarth result: ${earthInfo.body()}\nMars result: ${marsInfo.body()}")
                Result.failure()
            }
        } catch (e: Exception) {
//            Log.e("CalendarInfoUpdater", "Error during retrieval")
//            e.printStackTrace()
            Result.retry()
        }
    }
}

internal fun scheduleCalendarInfoUpdate(context: Context) {
//    Log.d("CalendarInfoUpdater", "Scheduling CalendarInfo update")

    val immediateUpdate = OneTimeWorkRequestBuilder<CalendarInfoUpdater>()
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "immediateUpdate",
        ExistingWorkPolicy.REPLACE,
        immediateUpdate
    )

    val periodicUpdate = PeriodicWorkRequestBuilder<CalendarInfoUpdater>(1, TimeUnit.DAYS)
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .addTag("periodicUpdate")
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "periodicUpdate",
        ExistingPeriodicWorkPolicy.REPLACE,
        periodicUpdate
    )
}
