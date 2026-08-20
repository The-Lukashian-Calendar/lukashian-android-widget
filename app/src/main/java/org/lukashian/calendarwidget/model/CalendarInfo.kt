package org.lukashian.calendarwidget.model

import com.google.gson.annotations.SerializedName

const val NUMBER_OF_OFFSETS = 30

data class CalendarInfo(
    @SerializedName("localEpoch") val localEpoch: Int,
    @SerializedName("firstDayNumber") val firstDayNumber: Int,
    @SerializedName("firstYearOfDayNumber") val firstYearOfDayNumber: Int,
    @SerializedName("nextYearStartIndex") val nextYearStartIndex: Int,
    @SerializedName("offsets") val offsets: IntArray
)
