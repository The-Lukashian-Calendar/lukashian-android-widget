package org.lukashian.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.lukashian.model.CalendarInfo
import org.lukashian.model.Instance.EARTH
import org.lukashian.model.Instance.MARS
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET

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
        .build()
        .create(LukashianApi::class.java)
}

internal class CalendarInfoUpdater(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val earthInfo = lukashianClient.getEarthCalendarInfo()
            val marsInfo = lukashianClient.getMarsCalendarInfo()

            if (
                earthInfo.isSuccessful && earthInfo.body() != null &&
                marsInfo.isSuccessful && marsInfo.body() != null
            ) {
                applicationContext.saveCalendarInfo(EARTH, earthInfo.body()!!)
                applicationContext.saveCalendarInfo(MARS, marsInfo.body()!!)
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
