package org.lukashian.clockwidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import org.lukashian.clockwidget.data.DEFAULT_INSTANCE
import org.lukashian.clockwidget.data.loadIndicator
import org.lukashian.clockwidget.data.loadInstance
import org.lukashian.clockwidget.data.saveIndicator
import org.lukashian.clockwidget.data.saveInstance
import org.lukashian.clockwidget.databinding.LukashianWidgetConfigureBinding
import org.lukashian.clockwidget.model.Instance.EARTH
import org.lukashian.clockwidget.model.Instance.MARS
import org.lukashian.clockwidget.rendering.updateAppWidget

class LukashianConfigureActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var binding: LukashianWidgetConfigureBinding

    private var instanceListener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
        val chosenInstance = when (checkedId) {
            R.id.radioButtonEarth -> EARTH
            R.id.radioButtonMars -> MARS
            else -> DEFAULT_INSTANCE
        }

//        Log.d("LukashianConfigureActivity", "Calendar Instance chosen: $chosenInstance")
        this.saveInstance(appWidgetId, chosenInstance)

        completeAction()
    }

    private var indicatorListener = CompoundButton.OnCheckedChangeListener { _, checked ->
//        Log.d("LukashianConfigureActivity", "Indicator chosen: $checked")
        this.saveIndicator(appWidgetId, checked)

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

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

//        Log.d("LukashianConfigureActivity", "Configure Activity Created")

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
//            Log.d("LukashianConfigureActivity", "Configuration saved")
            completeAction()
            finish()
        }

        //Set current values in input components
        val instance = this.loadInstance(appWidgetId)
//        Log.d("LukashianConfigureActivity", "Current Calendar Instance: $instance")

        val indicator = this.loadIndicator(appWidgetId)
//        Log.d("LukashianConfigureActivity", "Current Calendar Indicator: $indicator")

        when (instance) {
            EARTH -> binding.radioGroup.check(R.id.radioButtonEarth)
            MARS -> binding.radioGroup.check(R.id.radioButtonMars)
        }
        binding.radioGroup.setOnCheckedChangeListener(instanceListener)

        binding.switchIndicator.isChecked = indicator
        binding.switchIndicator.setOnCheckedChangeListener(indicatorListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Trigger the same cancel/back logic you use for the system back button
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
