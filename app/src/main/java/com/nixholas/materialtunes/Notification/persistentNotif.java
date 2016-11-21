package com.nixholas.materialtunes.Notification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.nixholas.materialtunes.MainActivity;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.Media.MediaManager;
import com.nixholas.materialtunes.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.nixholas.materialtunes.MainActivity.mediaControlsOnClickNext;
import static com.nixholas.materialtunes.MainActivity.mediaControlsOnClickPlayPause;
import static com.nixholas.materialtunes.MainActivity.mediaControlsOnClickPrevious;

/**
 * The Generic Notification Object for MaterialTunes.
 *
 * Created by nixho on 14-Nov-16.
 *
 * http://www.laurivan.com/android-display-a-notification/
 */

public class PersistentNotif extends BroadcastReceiver implements Runnable {
    // Notification Tags
    private static final String NOTIF_PREVIOUS = "NOTI_PREVIOUS";
    private static final String NOTIF_PLAYPAUSE = "NOTI_PLAYPAUSE";
    private static final String NOTIF_NEXT = "NOTI_NEXT";

    private static final int NOTIFICATION_ID = 255;
    private Context mContext;
    private NotificationManager mNotificationManager;
    private Notification notification;
    private View parentView;
    //private static RemoteViews normalView, bigView;

    // NormalView Widgets

    public PersistentNotif() {
    }

    public PersistentNotif(Context mContext) {
        this.mContext = mContext;

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.notify(NOTIFICATION_ID, notification); // Just for Debugging
    }

    public PersistentNotif(Context mContext, View parentView) {
        this.mContext = mContext;
        this.parentView = parentView;

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.notify(NOTIFICATION_ID, notification); // Just for Debugging
    }

    @Override
    public void run() {
        makeNotification(mContext);
    }

    private void makeNotification(Context context) {
        notification = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_app_icon) // It is a requirement to have a default small icon for notifications
                .setContentTitle("MaterialTunes")
                .build();
    }

    public void updateNotification(Song currentSong) {
        // Debugging Works
        //Log.e("FilePath", filePathIsValid("content://media/external/audio/albumart/" + currentSong.getAlbumId()) + "");

        RemoteViews normalView = new RemoteViews(mContext.getPackageName(), R.layout.notification_normal);
        RemoteViews bigView = new RemoteViews(mContext.getPackageName(), R.layout.notification_big);

        // Setup the normalView items
        normalView.setTextViewText(R.id.noti_title, currentSong.getTitle());
        normalView.setTextViewText(R.id.noti_artist, currentSong.getArtistName());

        // Setup the bigView items
        bigView.setTextViewText(R.id.notibig_title, currentSong.getTitle());
        bigView.setTextViewText(R.id.notibig_artist, currentSong.getArtistName());
        bigView.setTextViewText(R.id.notibig_album, currentSong.getAlbumName());
        bigView.setImageViewResource(R.id.notibig_previous, R.drawable.ic_skip_previous_black_36dp);
        bigView.setImageViewResource(R.id.notibig_playpause, R.drawable.ic_pause_black_36dp);
        bigView.setImageViewResource(R.id.notibig_next, R.drawable.ic_skip_next_black_36dp);

        // http://stackoverflow.com/questions/13472990/implementing-onclick-listener-for-app-widget
        bigView.setOnClickPendingIntent(R.id.notibig_playpause,
                                        getPendingSelfIntent(mContext, NOTIF_PLAYPAUSE));
        bigView.setOnClickPendingIntent(R.id.notibig_previous,
                getPendingSelfIntent(mContext, NOTIF_PREVIOUS));
        bigView.setOnClickPendingIntent(R.id.notibig_next,
                getPendingSelfIntent(mContext, NOTIF_NEXT));

        // Debugging Album Art
        // Somehow doesn't work yet
        Log.e("FilePathIsValid", filePathIsValid("content://media/external/audio/albumart/" + currentSong.getAlbumId()) + "");

        // Album Art
        // http://stackoverflow.com/questions/7817551/how-to-check-file-exist-or-not-and-if-not-create-a-new-file-in-sdcard-in-async-t
        if (filePathIsValid("content://media/external/audio/albumart/" + currentSong.getAlbumId())) {
            // Setup the albumArt first
            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentSong.getAlbumId());

            normalView.setImageViewUri(R.id.noti_albumart, albumArtUri);
            bigView.setImageViewUri(R.id.notibig_albumart, albumArtUri);
        } else {
            normalView.setImageViewResource(R.id.noti_albumart, R.drawable.untitled_album);
            bigView.setImageViewResource(R.id.notibig_albumart, R.drawable.untitled_album);
        }

        notification = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_app_icon)
                .setCustomContentView(normalView)
                .setCustomBigContentView(bigView)
                //.setLargeIcon(uriToBmp(albumArtUri))
                // http://stackoverflow.com/questions/5757997/hide-time-in-android-notification-without-using-custom-layout
                .setShowWhen(false) // Removes the timestamp for the notification
                // http://stackoverflow.com/questions/27343202/changing-notification-icon-background-on-lollipop
                //.setColor(Color.parseColor("303F9F"))
                .setOngoing(true)
                .build();

        mNotificationManager.notify(NOTIFICATION_ID, notification); // Notify the app to notify the system
    }

    private boolean filePathIsValid(String path) {
        return new File(path).exists();
    }

    private Bitmap uriToBmp(Uri input) {
        Bitmap bmp;
        // http://stackoverflow.com/questions/13859769/how-to-compress-uri-image-to-bitmap
        InputStream imageStream = null;
        try {
            imageStream = mContext.getContentResolver().openInputStream(
                    input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // http://stackoverflow.com/questions/15255611/how-to-convert-a-drawable-image-from-resources-to-a-bitmap
            bmp = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.untitled_album);
            return bmp;
        }

        try {
            bmp = BitmapFactory.decodeStream(imageStream);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            stream.close();
            stream = null;
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
            // http://stackoverflow.com/questions/15255611/how-to-convert-a-drawable-image-from-resources-to-a-bitmap
            bmp = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.untitled_album);
            return bmp;
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, PersistentNotif.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    /**
     * This guy helped me abit but I figured out the way to "plug" them (Intent and Broadcast) together
     * http://stackoverflow.com/questions/12372654/how-to-trigger-broadcast-receiver-from-notification
     * @param context
     * @param intent
     */
    public void onReceive(Context context, Intent intent) {
        //Log.e("onReceive:", "Works");

        switch(intent.getAction()) {
            case NOTIF_PLAYPAUSE:
                // Debugging Purposes
                Log.e("onReceive:", NOTIF_PLAYPAUSE + " Works");
                mediaControlsOnClickPlayPause();
                break;
            case NOTIF_NEXT:
                // Debugging Purposes
                Log.e("onReceive:", NOTIF_NEXT + " Works");
                mediaControlsOnClickNext();
                break;
            case NOTIF_PREVIOUS:
                // Debugging Purposes
                Log.e("onReceive:", NOTIF_PREVIOUS + " Works");
                mediaControlsOnClickPrevious();
                break;
            default:
                break;
        }
    }
}
