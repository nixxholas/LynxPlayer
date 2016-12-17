package com.nixholas.materialtunes.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.nixholas.materialtunes.MainActivity;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.os.Build.VERSION_CODES.N;
import static com.nixholas.materialtunes.MainActivity.mediaManager;
import static com.nixholas.materialtunes.Media.MediaNotifUpdater.mediaNotifOnClickNext;
import static com.nixholas.materialtunes.Media.MediaNotifUpdater.mediaNotifOnClickPrevious;
import static com.nixholas.materialtunes.UI.MediaControlUpdater.mediaControlsOnClickPlayPause;

/**
 * The Generic Notification Object for MaterialTunes.
 *
 * Created by nixho on 14-Nov-16.
 *
 * http://www.laurivan.com/android-display-a-notification/
 */

public class PersistentNotification extends BroadcastReceiver implements Runnable {
    // Notification Tags
    private static final String NOTIF_PREVIOUS = "NOTI_PREVIOUS";
    private static final String NOTIF_PLAYPAUSE = "NOTI_PLAYPAUSE";
    private static final String NOTIF_NEXT = "NOTI_NEXT";
    private static final String NOTIF_DISMISS = "NOTI_DISMISS";
    private static final String NOTIF_LAUNCH = "NOTI_LAUNCH";

    private static final int NOTIFICATION_ID = 255;
    private final Context mContext = MainActivity.getInstance();
    private NotificationManager mNotificationManager;
    private Notification mNotification;

    // NormalView Widgets

    public PersistentNotification() {
    }

    public PersistentNotification(Context mContext) {
        //this.mContext = mContext;

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.notify(NOTIFICATION_ID, notification); // Just for Debugging
    }

    public PersistentNotification(Context mContext, View parentView) {
        //this.mContext = mContext;
        //this.parentView = parentView;

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.notify(NOTIFICATION_ID, notification); // Just for Debugging
    }

    @Override
    public void run() {
        makeNotification(mContext);
    }

    private void makeNotification(Context context) {
        mNotification = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_app_icon) // It is a requirement to have a default small icon for notifications
                .setContentTitle("MaterialTunes")
                .build();
    }

    public void createNotification() {
        // Debugging Works
        //Log.e("FilePath", filePathIsValid("content://media/external/audio/albumart/" + currentSong.getAlbumId()) + "");
        //Log.e("Current Context", MainActivity.getInstance().getPackageName());
        Song currentSong = mediaManager.getCurrent();

        RemoteViews normalView = new RemoteViews(MainActivity.getInstance().getPackageName(), R.layout.notification_normal);
        RemoteViews bigView = new RemoteViews(MainActivity.getInstance().getPackageName(), R.layout.notification_big);

        // Setup the normalView items
        normalView.setTextViewText(R.id.noti_title, currentSong.getTitle());
        normalView.setTextViewText(R.id.noti_artist, currentSong.getArtistName());

        // Setup the bigView items
        bigView.setTextViewText(R.id.notibig_title, currentSong.getTitle());
        bigView.setTextViewText(R.id.notibig_artist, currentSong.getArtistName());
        bigView.setTextViewText(R.id.notibig_album, currentSong.getAlbumName());
        bigView.setImageViewResource(R.id.notibig_previous, R.drawable.ic_skip_previous_black_36dp);
        bigView.setImageViewResource(R.id.notibig_next, R.drawable.ic_skip_next_black_36dp);

        if (mediaManager.mMediaPlayer.isPlaying()) {
            bigView.setImageViewResource(R.id.notibig_playpause, R.drawable.ic_pause_black_36dp);
        } else {
            bigView.setImageViewResource(R.id.notibig_playpause, R.drawable.ic_play_arrow_white_36dp);
        }

        // http://stackoverflow.com/questions/13472990/implementing-onclick-listener-for-app-widget
        bigView.setOnClickPendingIntent(R.id.notibig_playpause,
                getPendingSelfIntent(mContext, NOTIF_PLAYPAUSE));
        bigView.setOnClickPendingIntent(R.id.notibig_previous,
                getPendingSelfIntent(mContext, NOTIF_PREVIOUS));
        bigView.setOnClickPendingIntent(R.id.notibig_next,
                getPendingSelfIntent(mContext, NOTIF_NEXT));
        bigView.setOnClickPendingIntent(R.id.notibig_dismiss,
                getPendingSelfIntent(mContext, NOTIF_DISMISS));
        bigView.setOnClickPendingIntent(R.id.notibig_layout,
                getPendingSelfIntent(mContext, NOTIF_LAUNCH));

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

        mNotification = new NotificationCompat.Builder(mContext)
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

        mNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(NOTIFICATION_ID, mNotification); // Notify the app to notify the system
    }

    public void updateNotification() {
        // Debugging Works
        //Log.e("FilePath", filePathIsValid("content://media/external/audio/albumart/" + currentSong.getAlbumId()) + "");
        //Log.e("Current Context", MainActivity.getInstance().getPackageName());
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Song currentSong = mediaManager.getCurrent();

        RemoteViews normalView = new RemoteViews(MainActivity.getInstance().getPackageName(), R.layout.notification_normal);
        RemoteViews bigView = new RemoteViews(MainActivity.getInstance().getPackageName(), R.layout.notification_big);

        // Setup the normalView items
        normalView.setTextViewText(R.id.noti_title, currentSong.getTitle());
        normalView.setTextViewText(R.id.noti_artist, currentSong.getArtistName());

        // Setup the bigView items
        bigView.setTextViewText(R.id.notibig_title, currentSong.getTitle());
        bigView.setTextViewText(R.id.notibig_artist, currentSong.getArtistName());
        bigView.setTextViewText(R.id.notibig_album, currentSong.getAlbumName());
        bigView.setImageViewResource(R.id.notibig_previous, R.drawable.ic_skip_previous_black_36dp);
        bigView.setImageViewResource(R.id.notibig_next, R.drawable.ic_skip_next_black_36dp);

        if (mediaManager.mMediaPlayer.isPlaying()) {
            bigView.setImageViewResource(R.id.notibig_playpause, R.drawable.ic_pause_black_36dp);
        } else {
            bigView.setImageViewResource(R.id.notibig_playpause, R.drawable.ic_play_arrow_black_36dp);
        }

        // http://stackoverflow.com/questions/13472990/implementing-onclick-listener-for-app-widget
        bigView.setOnClickPendingIntent(R.id.notibig_playpause,
                getPendingSelfIntent(mContext, NOTIF_PLAYPAUSE));
        bigView.setOnClickPendingIntent(R.id.notibig_previous,
                getPendingSelfIntent(mContext, NOTIF_PREVIOUS));
        bigView.setOnClickPendingIntent(R.id.notibig_next,
                getPendingSelfIntent(mContext, NOTIF_NEXT));
        bigView.setOnClickPendingIntent(R.id.notibig_dismiss,
                getPendingSelfIntent(mContext, NOTIF_DISMISS));
        bigView.setOnClickPendingIntent(R.id.notibig_layout,
                getPendingSelfIntent(mContext, NOTIF_LAUNCH));

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

        if (Build.VERSION.SDK_INT != N) {
            mNotification = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_app_icon)
                    .setCustomContentView(normalView)
                    .setCustomBigContentView(bigView)
                    //.setLargeIcon(uriToBmp(albumArtUri))
                    // http://stackoverflow.com/questions/5757997/hide-time-in-android-notification-without-using-custom-layout
                    //.setShowWhen(false) // Removes the timestamp for the notification
                    // http://stackoverflow.com/questions/27343202/changing-notification-icon-background-on-lollipop
                    //.setColor(Color.parseColor("303F9F"))
                    .setOngoing(true)
                    .build();
        } else {

        }

        mNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(NOTIFICATION_ID, mNotification); // Notify the app to notify the system
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
        Intent intent = new Intent(context, PersistentNotification.class);
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
                //Log.e("onReceive:", NOTIF_PLAYPAUSE + " Works");
                mediaControlsOnClickPlayPause();
                updateNotification();
                break;

            case NOTIF_NEXT:
                // Debugging Purposes
                //Log.e("onReceive:", NOTIF_NEXT + " Works");
                //mediaControlsOnClickNext();
                mediaNotifOnClickNext();
                updateNotification();
                break;

            case NOTIF_PREVIOUS:
                // Debugging Purposes
                //Log.e("onReceive:", NOTIF_PREVIOUS + " Works");
                //mediaControlsOnClickPrevious();
                mediaNotifOnClickPrevious();
                updateNotification();
                break;

            case NOTIF_DISMISS:
                // Clear all notification
                // http://stackoverflow.com/questions/4141555/how-to-use-getsystemservice-in-a-non-activity-class
                mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(255);
                mediaManager.mMediaPlayer.reset();

                // mediaManager.mMediaPlayer.release(); // http://stackoverflow.com/questions/3692562/how-does-one-remove-a-mediaplayer
                // https://developer.android.com/guide/topics/media/mediaplayer.html
                //mediaManager.mMediaPlayer = null; // Good Practice to nullify our player
                //getInstance().finish();
                break;

            case NOTIF_LAUNCH:
                Intent mainIntent = new Intent(context, MainActivity.getInstance().getClass());
                // http://stackoverflow.com/questions/5029354/how-can-i-programmatically-open-close-notifications-in-android
                Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // http://stackoverflow.com/questions/3689581/calling-startactivity-from-outside-of-an-activity
                context.sendBroadcast(closeIntent);
                context.startActivity(mainIntent);
                break;

            default:
                break;
        }
    }
}