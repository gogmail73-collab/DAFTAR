package com.daftar.study;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {
    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull @Override public Result doWork() {
        Context context = getApplicationContext();
        String title = getInputData().getString("title");
        String body = getInputData().getString("body");
        int notificationId = getInputData().getInt("notificationId", (int) (System.currentTimeMillis() & 0x7fffffff));
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return Result.failure();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("daftar_reminders", "تذكيرات دفتر", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("تذكيرات المهام والامتحانات والمحاضرات");
            manager.createNotificationChannel(channel);
        }
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, notificationId, open,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "daftar_reminders")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title == null ? "تذكير من دفتر" : title)
                .setContentText(body == null ? "لديك مهمة تحتاج إلى المتابعة." : body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body == null ? "لديك مهمة تحتاج إلى المتابعة." : body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pending);
        manager.notify(notificationId, notification.build());
        return Result.success();
    }
}
