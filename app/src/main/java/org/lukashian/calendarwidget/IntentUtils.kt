package org.lukashian.calendarwidget

import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

internal fun <A: Activity> piForActivity(
    activityClass: Class<A>,
    context: Context,
    action: String? = null,
    iFlags: Int? = null,
    piRequestCode: Int = activityClass.hashCode(),
    piFlags: Int = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
): PendingIntent {
    val intent = createIntent(context, activityClass, action, iFlags)
    return PendingIntent.getActivity(context, piRequestCode, intent, piFlags)
}

internal fun <B: BroadcastReceiver> piForBroadcast(
    receiverClass: Class<B>,
    context: Context,
    action: String? = null,
    iFlags: Int? = null,
    iExtras: List<Pair<String, IntArray>> = emptyList(),
    piRequestCode: Int = receiverClass.hashCode(),
    piFlags: Int = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
): PendingIntent {
    val intent = createIntent(context, receiverClass, action, iFlags)
    iExtras.forEach { (name, value) -> intent.putExtra(name, value) }
    return PendingIntent.getBroadcast(context, piRequestCode, intent, piFlags)
}

internal fun <S: Service> piForService(
    serviceClass: Class<S>,
    context: Context,
    action: String? = null,
    iFlags: Int? = null,
    piRequestCode: Int = serviceClass.hashCode(),
    piFlags: Int = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
): PendingIntent {
    val intent = createIntent(context, serviceClass, action, iFlags)
    return PendingIntent.getService(context, piRequestCode, intent, piFlags)
}

private fun createIntent(context: Context, clazz: Class<*>, action: String?, iFlags: Int?): Intent {
    val intent = Intent(context, clazz).setPackage(context.packageName)
    action?.let { intent.setAction(it) }
    iFlags?.let { intent.addFlags(it) }
    return intent
}
