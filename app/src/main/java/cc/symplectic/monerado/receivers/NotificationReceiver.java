package cc.symplectic.monerado.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.impl.background.systemalarm.SystemAlarmService;
import androidx.work.impl.foreground.SystemForegroundService;

import cc.symplectic.monerado.MainActivity;
import cc.symplectic.monerado.R;

public class NotificationReceiver extends BroadcastReceiver {
    private int l33t = 1337;

    @Override
    public void onReceive(Context context, Intent intent) {
        String Title = intent.getStringExtra("Title");
        String MessagePreface = intent.getStringExtra("ContentText");
        String Message = intent.getStringExtra("Message");


        /*
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.NOTIFICATION_NAME)
                .setSmallIcon(R.drawable.monerado_mountain_small)
                .setContentTitle(Title)
                .setContentText(MessagePreface)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(Message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(l33t, builder.build());
*/
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Worker Status";
            String description = "Workers dead and alive";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("31337", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "31337")
                .setSmallIcon(R.drawable.monerado_mountain_small)
                .setContentTitle(Title)
                .setContentText(MessagePreface)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(Message))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());


    }

}
