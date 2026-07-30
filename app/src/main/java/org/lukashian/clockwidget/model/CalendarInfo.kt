package org.lukashian.clockwidget.model

const val NUMBER_OF_OFFSETS = 30

data class CalendarInfo(
    val localEpoch: Int,
    val firstDayNumber: Int,
    val firstYearOfDayNumber: Int,
    val nextYearStartIndex: Int,
    val offsets: IntArray
)
