package org.lukashian.calendarwidget

import android.util.Log
import kotlin.reflect.KClass

class Logger(private val clazz: KClass<*>) {

    companion object {
        private const val DEBUG_ENABLED = false
        private const val INFO_ENABLED = false
    }

    fun debug(message: String) {
        if (DEBUG_ENABLED) {
            Log.d(clazz.simpleName, message)
        }
    }

    fun info(message: String) {
        if (INFO_ENABLED) {
            Log.i(clazz.simpleName, message)
        }
    }

    fun error(message: String) {
        Log.e(clazz.simpleName, message)
    }
}
