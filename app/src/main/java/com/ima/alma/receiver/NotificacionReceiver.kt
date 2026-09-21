package com.ima.alma.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ima.alma.MainActivity
import com.ima.alma.R
import com.ima.alma.util.AlarmaManagerHelper
import com.ima.alma.util.FrasesProvider

class NotificacionReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "alma_notificaciones_diarias"
        const val NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context, intent: Intent) {
        mostrarNotificacion(context)
        // Reprogramar automáticamente para las 5:00 PM del día siguiente
        AlarmaManagerHelper.programarNotificacionDiaria(context)
    }

    private fun mostrarNotificacion(context: Context) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // Crear NotificationChannel para Android 8.0+ (API 26+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Reflexiones Diarias ALMA",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Canal para notificaciones filosóficas diarias a las 5:00 PM"
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Obtener una frase aleatoria de FrasesProvider
            val frase = FrasesProvider.obtenerFraseAleatoria(context)
            val textoNotificacion = "«$frase»"

            val intentMainActivity = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intentMainActivity,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("ALMA")
                .setContentText(textoNotificacion)
                .setStyle(NotificationCompat.BigTextStyle().bigText(textoNotificacion))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
