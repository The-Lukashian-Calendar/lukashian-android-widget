package org.lukashian.data

import android.content.Context
import androidx.core.content.edit
import org.lukashian.model.CalendarInfo
import org.lukashian.model.Instance
import org.lukashian.model.Instance.EARTH
import org.lukashian.model.NUMBER_OF_OFFSETS

internal const val DEFAULT_INDICATOR = false
internal val DEFAULT_INSTANCE = EARTH

private const val WIDGET_PREFS_KEY = "org.lukashian.LukashianWidget"

private fun instance(appWidgetId: Int) = "instance_$appWidgetId"
private fun indicator(appWidgetId: Int) = "indicator_$appWidgetId"

private fun localEpoch(instance: Instance) = "localEpoch_${instance.name.lowercase()}"
private fun firstDayNumber(instance: Instance) = "firstDayNumber_${instance.name.lowercase()}"
private fun firstYearOfDayNumber(instance: Instance) = "firstYearOfDayNumber_${instance.name.lowercase()}"
private fun nextYearStartIndex(instance: Instance) = "nextYearStartIndex_${instance.name.lowercase()}"
private fun offsets(instance: Instance, index: Int) = "offsets_${instance.name.lowercase()}_$index"

internal fun Context.saveInstance(appWidgetId: Int, instance: Instance) {
    getSharedPreferences(WIDGET_PREFS_KEY, 0).edit {
        putString(instance(appWidgetId), instance.name)
    }
}

internal fun Context.loadInstance(appWidgetId: Int): Instance {
    return getSharedPreferences(WIDGET_PREFS_KEY, 0)
        .getString(instance(appWidgetId), null)
        ?.let { Instance.valueOf(it) }
        ?: DEFAULT_INSTANCE
}

internal fun Context.saveIndicator(appWidgetId: Int, indicator: Boolean) {
    getSharedPreferences(WIDGET_PREFS_KEY, 0).edit {
        putBoolean(indicator(appWidgetId), indicator)
    }
}

internal fun Context.loadIndicator(appWidgetId: Int): Boolean {
    return getSharedPreferences(WIDGET_PREFS_KEY, 0)
        .getBoolean(indicator(appWidgetId), DEFAULT_INDICATOR)
}

internal fun Context.deleteWidgetData(appWidgetId: Int) {
    getSharedPreferences(WIDGET_PREFS_KEY, 0).edit {
        remove(instance(appWidgetId))
        remove(indicator(appWidgetId))
    }
}

internal fun Context.saveCalendarInfo(instance: Instance, info: CalendarInfo) {
    getSharedPreferences(WIDGET_PREFS_KEY, 0).edit {
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
    return getSharedPreferences(WIDGET_PREFS_KEY, 0).let { prefs ->
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

internal fun Context.deleteCalendarInfo(instance: Instance) {
    getSharedPreferences(WIDGET_PREFS_KEY, 0).edit {
        remove(localEpoch(instance))
        remove(firstDayNumber(instance))
        remove(firstYearOfDayNumber(instance))
        remove(nextYearStartIndex(instance))
        (0..<NUMBER_OF_OFFSETS).forEach { i -> remove(offsets(instance, i)) }
    }
}
