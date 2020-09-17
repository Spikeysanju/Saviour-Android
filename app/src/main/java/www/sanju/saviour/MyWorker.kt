package www.sanju.saviour

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters


class MyWorker(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {


    override fun doWork(): Result {

        // get accidents result for every 15mins
        showAccidentNotification()

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    private fun showAccidentNotification() {

        val repository = Repo(applicationContext)
        val result = repository.fetchLatestAccident()

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
                applicationContext, 0, intent,
                PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = applicationContext.getString(R.string.default_notification_channel_id)
        val channelName = applicationContext.getString(R.string.default_notification_channel_name)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_saviour_notification)
                .setContentTitle("Accident Detected")
                .setContentText("Hey $USER_NAME, New accident near you $result")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)


        val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For android OREO notification channel is needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }


    companion object {
        const val USER_NAME = "Bharath"
    }

}
