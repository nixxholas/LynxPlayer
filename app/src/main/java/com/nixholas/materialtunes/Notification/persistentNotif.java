package com.nixholas.materialtunes.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.nixholas.materialtunes.MainActivity;
import com.nixholas.materialtunes.R;

/**
 * The Generic Notification Object for MaterialTunes.
 *
 * Created by nixho on 14-Nov-16.
 *
 * http://www.laurivan.com/android-display-a-notification/
 */

public class PersistentNotif implements Runnable {

    private static final int NOTIFICATION_ID = 255;
    private Context mContext;
    private NotificationManager mNotificationManager;

    public PersistentNotif(Context mContext) {
        this.mContext = mContext;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void run() {
        makeNotification(mContext);
    }

    private void makeNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("Notification Title")
                .setContentText("Sample Notification Content")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_app_icon)
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                ;

        Notification n = builder.build(); // Build the Notification

        // http://www.laurivan.com/android-make-your-notification-sticky/
        n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        mNotificationManager.notify(NOTIFICATION_ID, n);
    }
}
