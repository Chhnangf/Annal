package com.chhangf.annal.worker

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.chhangf.annal.MainActivity
import com.chhangf.annal.R


class ReminderWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        const val TAG = "ReminderWorker"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ServiceCast")
    override fun doWork(): Result {

        Log.d(TAG, "ReminderWorker: doWork() called")

        // 从inputData中提取通知所需的参数，如ID、标题和消息。
        val notificationId = inputData.getInt("NOTIFICATION_ID", 0)
        val title = inputData.getString("TITLE") ?: ""
        val message = inputData.getString("MESSAGE") ?: ""

        Log.d(
            TAG,
            "ReminderWorker: Creating notification with ID $notificationId, Title: $title, Message: $message"
        )

        // 获取到NotificationManager服务实例，并使用getString()方法获取预定义的通知频道ID。
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = applicationContext.getString(R.string.notification_channel_id)

        // 创建一个新的通知频道。
        val channel = NotificationChannel(
            notificationChannelId, // 用于标识通知渠道的唯一字符串ID
            "Reminders", // 通知渠道的显示名称，用户将在系统通知设置中看到这个名字。
            NotificationManager.IMPORTANCE_DEFAULT // 一个枚举值，用来指定这个通知渠道的重要程度
        ).apply {
            enableVibration(false) // 根据需求配置
            lightColor = Color.BLUE // 设置呼吸灯颜色，如果设备支持的话
            setBypassDnd(true) // 允许绕过勿扰模式
            setShowBadge(true) // 显示角标，可选
            description = "Reminder notifications"
            enableLights(true) // 开启指示灯，可选
//            enableVibration(true)
//            vibrationPattern = longArrayOf(0L, 500L, 250L, 500L) // 添加这行来设置自定义震动模式

        }
        notificationManager.createNotificationChannel(channel)

        // 4-17增加气泡提醒
        val mainIntent = Intent(applicationContext, MainActivity::class.java)
        mainIntent.putExtra("EXTRA_KEY_TITLE", title)
        mainIntent.putExtra("EXTRA_KEY_MESSAGE", message)

        val pendingIntentFlags =
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId, // 这里可以使用一个唯一的整数作为通知的请求码
            mainIntent,
            pendingIntentFlags
        )
        // 4-17气泡提醒end

        // 设置相关属性，包括图标、标题、正文、优先级以及是否自动取消通知
        val notificationBuilder =
            NotificationCompat.Builder(applicationContext, notificationChannelId)
                .setSmallIcon(coil.base.R.drawable.abc_vector_test) // 临时图标
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent) // 添加这行来关联PendingIntent与通知

        // 添加气泡元数据（仅当API级别大于等于Android 10时）
        val bubbleIntent = Intent(applicationContext, MainActivity::class.java)
        bubbleIntent.putExtra("EXTRA_KEY_TITLE", title)
        bubbleIntent.putExtra("EXTRA_KEY_MESSAGE", message)

        val bubblePendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            bubbleIntent,
            pendingIntentFlags
        )

        // 检查气泡元数据构建过程是否发生异常
        val bubbleData = try {
            NotificationCompat.BubbleMetadata.Builder(
                bubblePendingIntent,
                androidx.core.graphics.drawable.IconCompat.createWithResource(
                    applicationContext,
                    androidx.appcompat.resources.R.drawable.abc_vector_test // 替换为有效的资源ID
                )
            )
                .setDesiredHeight(600)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "ReminderWorker: Error creating Bubble Metadata: ", e)
            null // 如果构建失败，则不添加气泡元数据
        }

        if (bubbleData != null) {

            notificationBuilder.setBubbleMetadata(bubbleData)

            Log.d(TAG, "ReminderWorker: Bubble metadata created successfully for notification with ID $notificationId:")
            Log.d(TAG, "\tBubble Target Intent: $bubbleIntent")
            notificationBuilder.setBubbleMetadata(bubbleData)

        } else {
            Log.w(TAG, "ReminderWorker: Failed to create Bubble Metadata, will send regular notification instead.")
        }



        // 设置Heads-up Notification的行为
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
        // 尽管DecoratedCustomViewStyle不直接影响heads-up通知，但如果后续要自定义视图，可以设置
        val headsUpBehavior = NotificationCompat.DecoratedCustomViewStyle()
        notificationBuilder.setStyle(headsUpBehavior)

//        val vibrationPattern = longArrayOf(0L, 500L, 250L, 500L)
//        notificationBuilder.setVibrate(vibrationPattern)

        // 发送通知
        val notification = notificationBuilder.build()
        notificationManager.notify(notificationId, notification)

        // 日志只记录通知已发送
        Log.d(TAG, "ReminderWorker: Notification sent successfully with ID $notificationId.")

        // 当然，如果有需要，也可以添加一个简单的错误检查
        try {
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "ReminderWorker: Notification sent successfully with ID $notificationId.")
        } catch (e: Exception) {
            Log.e(TAG, "ReminderWorker: Failed to send the notification with ID $notificationId.", e)
        }

        return Result.success()
    }
}