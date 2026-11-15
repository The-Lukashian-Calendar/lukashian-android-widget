package org.lukashian.calendarwidget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

internal fun pi(
    creator: (context: Context, requestCode: Int, intent: Intent, flags: Int) -> PendingIntent,
    context: Context,
    iReceiverClass: Class<*>,
    iAction: String? = null,
    iFlags: Int? = null,
    iExtras: List<Pair<String, IntArray>> = emptyList(),
    piRequestCode: Int
): PendingIntent {
    val intent = Intent(context, iReceiverClass).setPackage(context.packageName)
    iAction?.let { intent.setAction(it) }
    iFlags?.let { intent.addFlags(it) }
    iExtras.forEach { (name, value) -> intent.putExtra(name, value) }

    val piFlags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    return creator(context, piRequestCode, intent, piFlags)
}
