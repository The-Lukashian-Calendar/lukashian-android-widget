package org.lukashian.calendarwidget.data

import android.content.Context
import androidx.core.content.edit
import org.lukashian.calendarwidget.model.CalendarInfo
import org.lukashian.calendarwidget.model.Instance
import org.lukashian.calendarwidget.model.Instance.EARTH
import org.lukashian.calendarwidget.model.NUMBER_OF_OFFSETS

internal const val DEFAULT_WIDGET_INDICATOR = false
internal val DEFAULT_WIDGET_INSTANCE = EARTH

internal const val DEFAULT_ALARM_BEEPS = 0
internal val DEFAULT_ALARM_INSTANCE = EARTH
internal const val DEFAULT_ALARM_ENABLED = false

const val PREFS_KEY = "org.lukashian.calendarwidget.LukashianWidget"

private fun instance(appWidgetId: Int) = "instance_$appWidgetId"
private fun indicator(appWidgetId: Int) = "indicator_$appWidgetId"

private fun alarmBeeps() = "alarm_beeps"
private fun alarmInstance() = "alarm_instance"
fun alarmEnabled() = "alarm_enabled"

private fun localEpoch(instance: Instance) = "localEpoch_${instance.name.lowercase()}"
private fun firstDayNumber(instance: Instance) = "firstDayNumber_${instance.name.lowercase()}"
private fun firstYearOfDayNumber(instance: Instance) = "firstYearOfDayNumber_${instance.name.lowercase()}"
private fun nextYearStartIndex(instance: Instance) = "nextYearStartIndex_${instance.name.lowercase()}"
private fun offsets(instance: Instance, index: Int) = "offsets_${instance.name.lowercase()}_$index"

internal fun Context.saveInstance(appWidgetId: Int, instance: Instance) {
    getSharedPreferences(PREFS_KEY, 0).edit {
        putString(instance(appWidgetId), instance.name)
    }
}

internal fun Context.loadInstance(appWidgetId: Int): Instance {
    return getSharedPreferences(PREFS_KEY, 0)
        .getString(instance(appWidgetId), null)
        ?.let { Instance.valueOf(it) }
        ?: DEFAULT_WIDGET_INSTANCE
}

internal fun Context.saveIndicator(appWidgetId: Int, indicator: Boolean) {
    getSharedPreferences(PREFS_KEY, 0).edit {
        putBoolean(indicator(appWidgetId), indicator)
    }
}

internal fun Context.loadIndicator(appWidgetId: Int): Boolean {
    return getSharedPreferences(PREFS_KEY, 0)
        .getBoolean(indicator(appWidgetId), DEFAULT_WIDGET_INDICATOR)
}

internal fun Context.deleteWidgetData(appWidgetId: Int) {
    getSharedPreferences(PREFS_KEY, 0).edit {
        remove(instance(appWidgetId))
        remove(indicator(appWidgetId))
    }
}

internal fun Context.saveAlarmBeeps(beeps: Int) {
    getSharedPreferences(PREFS_KEY, 0).edit {
        putInt(alarmBeeps(), beeps)
    }
}

internal fun Context.loadAlarmBeeps(): Int {
    return getSharedPreferences(PREFS_KEY, 0)
        .getInt(alarmBeeps(), DEFAULT_ALARM_BEEPS)
}

internal fun Context.saveAlarmInstance(instance: Instance) {
    getSharedPreferences(PREFS_KEY, 0).edit {
        putString(alarmInstance(), instance.name)
    }
}

internal fun Context.loadAlarmInstance(): Instance {
    return getSharedPreferences(PREFS_KEY, 0)
        .getString(alarmInstance(), null)
        ?.let { Instance.valueOf(it) }
        ?: DEFAULT_ALARM_INSTANCE
}

internal fun Context.saveAlarmEnabled(enabled: Boolean) {
    getSharedPreferences(PREFS_KEY, 0).edit {
        putBoolean(alarmEnabled(), enabled)
    }
}

internal fun Context.loadAlarmEnabled(): Boolean {
    return getSharedPreferences(PREFS_KEY, 0)
        .getBoolean(alarmEnabled(), DEFAULT_ALARM_ENABLED)
}

internal fun Context.saveCalendarInfo(instance: Instance, info: CalendarInfo) {
    getSharedPreferences(PREFS_KEY, 0).edit {
        putInt(localEpoch(instance), info.localEpoch)
        putInt(firstDayNumber(instance), info.firstDayNumber)
        putInt(firstYearOfDayNumber(instance), info.firstYearOfDayNumber)
        putInt(nextYearStartIndex(instance), info.nextYearStartIndex)
        for (i in 0..<NUMBER_OF_OFFSETS) {
            putInt(offsets(instance, i), info.offsets[i])
        }
    }
}

internal fun Context.loadCalendarInfo(instance: Instance): CalendarInfo? {
    return getSharedPreferences(PREFS_KEY, 0).let { prefs ->
        if (
            prefs.contains(localEpoch(instance)) &&
            prefs.contains(firstDayNumber(instance)) &&
            prefs.contains(firstYearOfDayNumber(instance)) &&
            prefs.contains(nextYearStartIndex(instance)) &&
            (0..<NUMBER_OF_OFFSETS).map { i -> prefs.contains(offsets(instance, i)) }.toBooleanArray().all { it }
        ) {
            CalendarInfo(
                prefs.getInt(localEpoch(instance), 0),
                prefs.getInt(firstDayNumber(instance), 0),
                prefs.getInt(firstYearOfDayNumber(instance), 0),
                prefs.getInt(nextYearStartIndex(instance), 0),
                (0..<NUMBER_OF_OFFSETS).map { i -> prefs.getInt(offsets(instance, i), 0) }.toIntArray()
            )
        } else null
    }
}
