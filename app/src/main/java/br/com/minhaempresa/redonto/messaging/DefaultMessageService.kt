package br.com.minhaempresa.redonto.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import br.com.minhaempresa.redonto.MainActivity
import br.com.minhaempresa.redonto.R
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class DefaultMessageService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Verificar se a notificação possui dados
        remoteMessage.data.isNotEmpty().let {
            // Extrair dados da notificação
            val titulo = remoteMessage.notification?.title
            val mensagem = remoteMessage.notification?.body

            if(mensagem == "Um paciente te aceitou como doutor. Corra para ver o chamado!"){

                val channelId = getString(R.string.default_notification_channel_id)
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_check)
                    .setContentTitle(titulo)
                    .setContentText(mensagem)
                    .setAutoCancel(true)

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Se necessário, crie o canal de notificação
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        getString(R.string.default_notification_channel_id),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                notificationManager.notify(0, notificationBuilder.build())
            }
            else{
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )

                val channelId = getString(R.string.default_notification_channel_id)
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_check)
                    .setContentTitle(titulo)
                    .setContentText(mensagem)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Se necessário, crie o canal de notificação
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        getString(R.string.default_notification_channel_id),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                notificationManager.notify(0, notificationBuilder.build())
            }
        }
    }
}