package com.upn.myoyichan.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.upn.myoyichan.R
import com.upn.myoyichan.ui.main.MainActivity

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "medicamento_reminder_channel"
        const val EXTRA_MEDICAMENTO_NOMBRE = "medicamento_nombre"
        const val EXTRA_MEDICAMENTO_ID = "medicamento_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Respetar el switch de notificaciones de la pantalla de Ajustes
        if (!PreferencesManager(context).notificationsEnabled) return

        val medicamentoNombre = intent.getStringExtra(EXTRA_MEDICAMENTO_NOMBRE) ?: "Medicamento"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        createNotificationChannel(context)
        showNotification(context, medicamentoNombre, notificationId)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.recordatorio_canal_nombre)
            val descriptionText = context.getString(R.string.recordatorio_canal_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, medicamentoNombre: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(context.getString(R.string.recordatorio_notif_titulo))
            .setContentText(context.getString(R.string.recordatorio_notif_texto, medicamentoNombre))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
