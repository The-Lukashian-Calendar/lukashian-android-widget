package org.lukashian

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import org.lukashian.databinding.LukashianWidgetConfigureBinding

class LukashianWidgetConfigureActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var binding: LukashianWidgetConfigureBinding

    private var instanceListener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
        val chosenInstance = when (checkedId) {
            R.id.radioButtonEarth -> this.resources.getString(R.string.earthInstance)
            R.id.radioButtonMars -> this.resources.getString(R.string.marsInstance)
            else -> this.resources.getString(R.string.defaultInstance)
        }

        println("Calendar Instance chosen: $chosenInstance")
        saveCalendarInstance(this, appWidgetId, chosenInstance)

        completeAction()
    }

    private var indicatorListener = CompoundButton.OnCheckedChangeListener { _, checked ->
        println("Indicator chosen: $checked")
        saveCalendarIndicator(this, appWidgetId, checked)

        completeAction()
    }

    private fun completeAction() {
        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(this)
        updateAppWidget(this, appWidgetManager, appWidgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        println("Configure Activity Created")

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        binding = LukashianWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Find the widget id from the intent
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Enable the back arrow in the upper left
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // Configure the save button
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            println("Configuration saved")
            completeAction()
            finish()
        }

        //Set current values in input components
        val calendarInstance = loadCalendarInstance(this@LukashianWidgetConfigureActivity, appWidgetId)
        println("Current Calendar Instance: $calendarInstance")

        val indicator = loadCalendarIndicator(this@LukashianWidgetConfigureActivity, appWidgetId)
        println("Current Calendar Indicator: $indicator")

        when (calendarInstance) {
            getString(R.string.earthInstance) -> binding.radioGroup.check(R.id.radioButtonEarth)
            getString(R.string.marsInstance) -> binding.radioGroup.check(R.id.radioButtonMars)
            else -> binding.radioGroup.clearCheck()
        }
        binding.radioGroup.setOnCheckedChangeListener(instanceListener)

        binding.switchIndicator.isChecked = indicator
        binding.switchIndicator.setOnCheckedChangeListener(indicatorListener)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Trigger the same cancel/back logic you use for the system back button
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

private const val WIDGET_PREFS_KEY = "org.lukashian.LukashianWidget"
private const val INSTANCE_PREFIX = "calendar_instance_"
private const val INDICATOR_PREFIX = "calendar_indicator_"

internal fun saveCalendarInstance(context: Context, appWidgetId: Int, text: String) {
    context.getSharedPreferences(WIDGET_PREFS_KEY, 0).edit {
        putString(INSTANCE_PREFIX + appWidgetId, text)
    }
}

internal fun loadCalendarInstance(context: Context, appWidgetId: Int): String {
    return context.getSharedPreferences(WIDGET_PREFS_KEY, 0).getString(INSTANCE_PREFIX + appWidgetId, null) ?:
           context.getString(R.string.defaultInstance)
}

internal fun deleteCalendarInstance(context: Context, appWidgetId: Int) {
    context.getSharedPreferences(WIDGET_PREFS_KEY, 0).edit {
        remove(INSTANCE_PREFIX + appWidgetId)
    }
}

internal fun saveCalendarIndicator(context: Context, appWidgetId: Int, indicator: Boolean) {
    context.getSharedPreferences(WIDGET_PREFS_KEY, 0).edit {
        putBoolean(INDICATOR_PREFIX + appWidgetId, indicator)
    }
}

internal fun loadCalendarIndicator(context: Context, appWidgetId: Int): Boolean {
    return context.getSharedPreferences(WIDGET_PREFS_KEY, 0).getBoolean(INDICATOR_PREFIX + appWidgetId, false)
}

internal fun deleteCalendarIndicator(context: Context, appWidgetId: Int) {
    context.getSharedPreferences(WIDGET_PREFS_KEY, 0).edit {
        remove(INDICATOR_PREFIX + appWidgetId)
    }
}
