package com.focuscity.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.focuscity.MainActivity
import java.util.Calendar

object NotificationHelper {

    const val FOCUS_CHANNEL_ID = "focus_session"
    const val REMINDER_CHANNEL_ID = "daily_reminder"
    const val FOCUS_NOTIFICATION_ID = 1
    const val COMPLETE_NOTIFICATION_ID = 2
    const val REMINDER_NOTIFICATION_ID = 3

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val focusChannel = NotificationChannel(
            FOCUS_CHANNEL_ID,
            "Focus Session",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Active focus session progress"
            setShowBadge(false)
        }

        val reminderChannel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Daily Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily study reminders"
        }

        manager.createNotificationChannel(focusChannel)
        manager.createNotificationChannel(reminderChannel)
    }

    fun buildFocusNotification(
        context: Context,
        timeText: String,
        isTimer: Boolean,
        coinsEarned: Int = 0
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val label = if (isTimer) "Timer" else "Stopwatch"
        return NotificationCompat.Builder(context, FOCUS_CHANNEL_ID)
            .setContentTitle("Focusing... \uD83C\uDFAF")
            .setContentText("$label: $timeText • \uD83E\uDE99 $coinsEarned")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()
    }

    fun showSessionCompleteNotification(context: Context, coins: Int, minutes: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, FOCUS_CHANNEL_ID)
            .setContentTitle("Session Complete! \uD83C\uDF89")
            .setContentText("$minutes min focused • +$coins coins earned")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(COMPLETE_NOTIFICATION_ID, notification)
    }

    fun scheduleDailyReminder(context: Context, hour: Int = 18, minute: Int = 0) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REMINDER_NOTIFICATION_ID, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REMINDER_NOTIFICATION_ID, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }
}
