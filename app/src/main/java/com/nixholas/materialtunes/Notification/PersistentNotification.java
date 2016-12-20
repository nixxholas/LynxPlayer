package com.nixholas.materialtunes.Notification;

import android.annotation.TargetApi;
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
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.nixholas.materialtunes.MainActivity;
import com.nixholas.materialtunes.Media.Entities.Song;
import com.nixholas.materialtunes.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import static android.os.Build.VERSION_CODES.N;
import static com.nixholas.materialtunes.MainActivity.mediaManager;
import static com.nixholas.materialtunes.UI.MediaControlUpdater.mediaControlsOnClickNext;
import static com.nixholas.materialtunes.UI.MediaControlUpdater.mediaControlsOnClickPlayPause;
import static com.nixholas.materialtunes.UI.MediaControlUpdater.mediaControlsOnClickPrevious;

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

    // Action Integers
    private static final int NOTI_PREV = 1;
    private static final int NOTI_PLAYPAUSE = 2;
    private static final int NOTI_NEXT = 3;
    private static final int NOTI_DISMISS = 4;

    // Action Intents
    // Create all the Pending Intents
    // http://www.journaldev.com/10463/android-pendingintent-and-notifications-example-tutorial
    Intent prevIntent;
    Intent pauseIntent;
    Intent nextIntent;
    Intent dismissIntent;

    static PendingIntent prevPendingIntent;
    static PendingIntent pausePendingIntent;
    static PendingIntent nextPendingIntent;
    static PendingIntent dismissPendingIntent;

    // Notification RemoteViews
    RemoteViews normalView;
    RemoteViews bigView;

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

        prevIntent = new Intent(mContext, PersistentNotification.class);
        pauseIntent = new Intent(mContext, PersistentNotification.class);
        nextIntent = new Intent(mContext, PersistentNotification.class);
        dismissIntent = new Intent(mContext, PersistentNotification.class);

        prevIntent.setAction(NOTIF_PREVIOUS);
        pauseIntent.setAction(NOTIF_PLAYPAUSE);
        nextIntent.setAction(NOTIF_NEXT);
        dismissIntent.setAction(NOTIF_DISMISS);

        prevPendingIntent = PendingIntent.getBroadcast(mContext, NOTI_PREV
                , prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        pausePendingIntent = PendingIntent.getBroadcast(mContext, NOTI_PLAYPAUSE
                , pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        nextPendingIntent = PendingIntent.getBroadcast(mContext, NOTI_NEXT
                , nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        dismissPendingIntent = PendingIntent.getBroadcast(mContext, NOTI_DISMISS
                , dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        normalView = new RemoteViews(MainActivity.getInstance().getPackageName(), R.layout.notification_normal);
        bigView = new RemoteViews(MainActivity.getInstance().getPackageName(), R.layout.notification_big);

        // Setup the BigView Static Contents
        bigView.setImageViewResource(R.id.notibig_previous, R.drawable.ic_skip_previous_black_36dp);
        bigView.setImageViewResource(R.id.notibig_next, R.drawable.ic_skip_next_black_36dp);

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
    }

    public PersistentNotification(Context mContext, View parentView) {
        //this.mContext = mContext;
        //this.parentView = parentView;

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.notify(NOTIFICATION_ID, notification); // Just for Debugging

        prevIntent = new Intent(mContext, PersistentNotification.class);
        pauseIntent = new Intent(mContext, PersistentNotification.class);
        nextIntent = new Intent(mContext, PersistentNotification.class);
        dismissIntent = new Intent(mContext, PersistentNotification.class);

        prevIntent.setAction(NOTIF_PREVIOUS);
        pauseIntent.setAction(NOTIF_PLAYPAUSE);
        nextIntent.setAction(NOTIF_NEXT);
        dismissIntent.setAction(NOTIF_DISMISS);

        prevPendingIntent = PendingIntent.getBroadcast(mContext, NOTI_PREV
                , prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        pausePendingIntent = PendingIntent.getBroadcast(mContext, NOTI_PLAYPAUSE
                , pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        nextPendingIntent = PendingIntent.getBroadcast(mContext, NOTI_NEXT
                , nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        dismissPendingIntent = PendingIntent.getBroadcast(mContext, NOTI_DISMISS
                , dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        normalView = new RemoteViews(MainActivity.getInstance().getPackageName(), R.layout.notification_normal);
        bigView = new RemoteViews(MainActivity.getInstance().getPackageName(), R.layout.notification_big);

        // Setup the BigView Static Contents
        bigView.setImageViewResource(R.id.notibig_previous, R.drawable.ic_skip_previous_black_36dp);
        bigView.setImageViewResource(R.id.notibig_next, R.drawable.ic_skip_next_black_36dp);

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
        normalView.setOnClickPendingIntent(R.id.noti_image_end,
                getPendingSelfIntent(mContext, NOTIF_DISMISS));

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

        mNotification = new Notification.Builder(mContext)
                .setSmallIcon(R.drawable.ic_app_icon)
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
        try {
            // Debugging Works
            //Log.e("FilePath", filePathIsValid("content://media/external/audio/albumart/" + currentSong.getAlbumId()) + "");
            //Log.e("Current Context", MainActivity.getInstance().getPackageName());
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            final Song currentSong = mediaManager.getCurrent();

            // Setup the bigView items
            bigView.setTextViewText(R.id.notibig_title, currentSong.getTitle());
            bigView.setTextViewText(R.id.notibig_artist, currentSong.getArtistName());
            bigView.setTextViewText(R.id.notibig_album, currentSong.getAlbumName());

            if (mediaManager.mMediaPlayer.isPlaying()) {
                bigView.setImageViewResource(R.id.notibig_playpause, R.drawable.ic_pause_black_36dp);
            } else {
                bigView.setImageViewResource(R.id.notibig_playpause, R.drawable.ic_play_arrow_black_36dp);
            }

            // Debugging Album Art
            //Log.e("FilePathIsValid", filePathIsValid("content://media/e0ternal/audio/albumart/" + currentSong.getAlbumId()) + "");

            // Album Art
            // http://stackoverflow.com/questions/7817551/how-to-check-file-exist-or-not-and-if-not-create-a-new-file-in-sdcard-in-async-t
            //if (filePathIsValid("content://media/external/audio/albumart/" + currentSong.getAlbumId())) {
                // Setup the albumArt first
                Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentSong.getAlbumId());

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    Uri sArtworkUri = Uri
                            .parse("content://media/external/audio/albumart");
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentSong.getAlbumId());

                    try {
                        // http://stackoverflow.com/questions/27394016/how-does-one-use-glide-to-download-an-image-into-a-bitmap
                        bigView.setImageViewBitmap(R.id.notibig_albumart,
                                Glide.with(mContext)
                                        .load(albumArtUri)
                                        .asBitmap()
                                        .placeholder(R.drawable.untitled_album)
                                        .fitCenter()
                                        .into(400, 400)
                                        .get());

                        Bitmap albumBitmap = Glide.with(mContext)
                                .load(albumArtUri)
                                .asBitmap()
                                .placeholder(R.drawable.untitled_album)
                                .fitCenter()
                                .into(400, 400)
                                .get();


                        if (mediaManager.mMediaPlayer.isPlaying()) {
                            // Creating a Notifcation
                            // https://developer.android.com/guide/topics/ui/notifiers/notifications.html
                            mNotification = new NotificationCompat.Builder(mContext)
                                    // Show controls on lock screen even when user hides sensitive content.
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                    .setSmallIcon(R.drawable.untitled_album)
                                    .setOngoing(true)
                                    // Add media control buttons that invoke intents in your media service
                                    .addAction(R.drawable.ic_skip_previous_black_36dp, "Previous", prevPendingIntent) // #0
                                    .addAction(R.drawable.ic_pause_black_36dp, "Pause", pausePendingIntent)  // #1
                                    .addAction(R.drawable.ic_skip_next_black_36dp, "Next", nextPendingIntent)     // #2
                                    .addAction(R.drawable.ic_close_black_36dp, "Close", dismissPendingIntent) // #3
                                    // Apply the media style template
                                    .setStyle(new NotificationCompat.MediaStyle()
                                            .setShowCancelButton(true)
                                            .setCancelButtonIntent(dismissPendingIntent)
                                            .setShowActionsInCompactView(1 /* #1: pause button */, 2, 3)
                                            .setMediaSession(mediaManager.getMediaSessionToken()))
                                    .setCustomBigContentView(bigView)
                                    // Converting albumArtUri to a Bitmap directly
                                    // http://stackoverflow.com/questions/3879992/how-to-get-bitmap-from-an-uri
                                    //.setLargeIcon(MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), albumArtUri))
                                    .setLargeIcon(albumBitmap)
                                    // http://stackoverflow.com/questions/5757997/hide-time-in-android-notification-without-using-custom-layout
                                    .setShowWhen(false) // Removes the timestamp for the notification
                                    .setContentTitle(currentSong.getTitle())
                                    .setContentText(currentSong.getArtistName())
                                    .build();
                        } else {
                            // Creating a Notifcation
                            // https://developer.android.com/guide/topics/ui/notifiers/notifications.html
                            mNotification = new NotificationCompat.Builder(mContext)
                                    // Show controls on lock screen even when user hides sensitive content.
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                    .setSmallIcon(R.drawable.untitled_album)
                                    .setOngoing(true)
                                    // Add media control buttons that invoke intents in your media service
                                    .addAction(R.drawable.ic_skip_previous_black_36dp, "Previous", prevPendingIntent) // #0
                                    .addAction(R.drawable.ic_play_arrow_black_36dp, "Play", pausePendingIntent)  // #1
                                    .addAction(R.drawable.ic_skip_next_black_36dp, "Next", nextPendingIntent)     // #2
                                    .addAction(R.drawable.ic_close_black_36dp, "Close", dismissPendingIntent) // #3
                                    // Apply the media style template
                                    .setStyle(new NotificationCompat.MediaStyle()
                                            .setShowCancelButton(true)
                                            .setCancelButtonIntent(dismissPendingIntent)
                                            .setShowActionsInCompactView(1 /* #1: pause button */, 2, 3)
                                            .setMediaSession(mediaManager.getMediaSessionToken()))
                                    .setCustomBigContentView(bigView)
                                    // Converting albumArtUri to a Bitmap directly
                                    // http://stackoverflow.com/questions/3879992/how-to-get-bitmap-from-an-uri
                                    //.setLargeIcon(MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), albumArtUri))
                                    .setLargeIcon(albumBitmap)
                                    // http://stackoverflow.com/questions/5757997/hide-time-in-android-notification-without-using-custom-layout
                                    .setShowWhen(false) // Removes the timestamp for the notification
                                    .setContentTitle(currentSong.getTitle())
                                    .setContentText(currentSong.getArtistName())
                                    .build();
                        }

                        mNotification.flags |= Notification.FLAG_AUTO_CANCEL;

                        mNotificationManager.notify(NOTIFICATION_ID, mNotification); // Notify the app to notify the system
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.execute();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @RequiresApi(24)
    public void updateNotificationNew() {
        final Song currentSong = mediaManager.getCurrent();

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


        mNotification = new Notification.Builder(mContext)
                .build();

    }

    private boolean filePathIsValid(String path) {
        return new File(path).exists();
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
        //Log.e("onReceive:", "Works" + intent.getAction());

        switch(intent.getAction()) {
            case NOTIF_PLAYPAUSE:
                // Debugging Purposes
                //Log.e("onReceive:", NOTIF_PLAYPAUSE + " Works");
                mediaControlsOnClickPlayPause();
                break;

            case NOTIF_NEXT:
                // Debugging Purposes
                //Log.e("onReceive:", NOTIF_NEXT + " Works");
                mediaControlsOnClickNext(MainActivity.getInstance().getCurrentFocus());
                break;

            case NOTIF_PREVIOUS:
                // Debugging Purposes
                //Log.e("onReceive:", NOTIF_PREVIOUS + " Works");
                mediaControlsOnClickPrevious(MainActivity.getInstance().getCurrentFocus());
                break;

            case NOTIF_DISMISS:
                // Clear all notification
                // http://stackoverflow.com/questions/4141555/how-to-use-getsystemservice-in-a-non-activity-class
                mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(255);
                mediaManager.mMediaPlayer.release();

                // Kill the whole app to give the user all the processing space we took
                // http://stackoverflow.com/questions/3105673/how-to-kill-an-application-with-all-its-activities
                android.os.Process.killProcess(android.os.Process.myPid());
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
