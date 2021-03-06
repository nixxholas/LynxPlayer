package com.nixholas.lynx.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.widget.RemoteViews;

import com.nixholas.lynx.R;
import com.nixholas.lynx.media.entities.Song;
import com.nixholas.lynx.ui.activities.MainActivity;

import java.io.File;

import static com.nixholas.lynx.ui.MediaControlUpdater.mediaControlsOnClickNext;
import static com.nixholas.lynx.ui.MediaControlUpdater.mediaControlsOnClickPlayPause;
import static com.nixholas.lynx.ui.MediaControlUpdater.mediaControlsOnClickPrevious;
import static com.nixholas.lynx.ui.activities.MainActivity.getInstance;
import static com.nixholas.lynx.ui.activities.MainActivity.mediaManager;
import static com.nixholas.lynx.utils.AlbumService.getAlbumArt;
import static com.nixholas.lynx.utils.color.TextColorHelper.isColorDark;

/**
 * The Generic Notification Object for Lynx.
 *
 * Created by nixho on 14-Nov-16.
 *
 * http://www.laurivan.com/android-display-a-notification/
 */

public class LynxNotification extends BroadcastReceiver implements Runnable {
    // Notification Runnable
    AsyncTask<Void, Void, Void> LynxNotificationRunner;

    // Notification Tags
    private static final String NOTIF_PREVIOUS = "NOTI_PREVIOUS";
    private static final String NOTIF_PLAYPAUSE = "NOTI_PLAYPAUSE";
    private static final String NOTIF_NEXT = "NOTI_NEXT";
    private static final String NOTIF_DISMISS = "NOTI_DISMISS";

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
    private final Context mContext = getInstance();
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private NotificationCompat.Builder mBuilder;
    //private SwatchEnum backgroundSwatchEnum = SwatchEnum.NULL; // Initialize with null first

    // NormalView Widgets

    public LynxNotification() {
    }

    public LynxNotification(Context mContext) {
        //this.mContext = mContext;

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.notify(NOTIFICATION_ID, notification); // Just for Debugging

        prevIntent = new Intent(mContext, LynxNotification.class);
        pauseIntent = new Intent(mContext, LynxNotification.class);
        nextIntent = new Intent(mContext, LynxNotification.class);
        dismissIntent = new Intent(mContext, LynxNotification.class);
        //launchIntent = new Intent(mContext, MainActivity.class);

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
                , dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        normalView = new RemoteViews(getInstance().getPackageName(), R.layout.notification_normal);
        bigView = new RemoteViews(getInstance().getPackageName(), R.layout.notification_big);

        // Setup the normalView Static Contents
        normalView.setImageViewResource(R.id.noti_previous, R.drawable.ic_skip_previous_black_24dp);
        normalView.setImageViewResource(R.id.noti_next, R.drawable.ic_skip_next_black_24dp);

        // Setup the BigView Static Contents
        bigView.setImageViewResource(R.id.notibig_previous, R.drawable.ic_skip_previous_black_24dp);
        bigView.setImageViewResource(R.id.notibig_next, R.drawable.ic_skip_next_black_24dp);

        // http://stackoverflow.com/questions/13472990/implementing-onclick-listener-for-app-widget
        normalView.setOnClickPendingIntent(R.id.noti_playpause,
                getPendingSelfIntent(mContext, NOTIF_PLAYPAUSE));
        normalView.setOnClickPendingIntent(R.id.noti_previous,
                getPendingSelfIntent(mContext, NOTIF_PREVIOUS));
        normalView.setOnClickPendingIntent(R.id.noti_next,
                getPendingSelfIntent(mContext, NOTIF_NEXT));

        bigView.setOnClickPendingIntent(R.id.notibig_playpause,
                getPendingSelfIntent(mContext, NOTIF_PLAYPAUSE));
        bigView.setOnClickPendingIntent(R.id.notibig_previous,
                getPendingSelfIntent(mContext, NOTIF_PREVIOUS));
        bigView.setOnClickPendingIntent(R.id.notibig_next,
                getPendingSelfIntent(mContext, NOTIF_NEXT));
        bigView.setOnClickPendingIntent(R.id.notibig_dismiss,
                getPendingSelfIntent(mContext, NOTIF_DISMISS));

        // http://stackoverflow.com/questions/9214715/notifcation-launches-multiple-instances-of-activities
        normalView.setOnClickPendingIntent(R.id.noti_layout,
                PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        PendingIntent.FLAG_UPDATE_CURRENT));

        bigView.setOnClickPendingIntent(R.id.notibig_layout,
                PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    @Override
    public void run() {
        makeNotification(mContext);
    }

    private void makeNotification(Context context) {
        Log.d("LynxNotification", "makeNotification()");

        mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(mContext)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.untitled_album)
                .setOngoing(true)
                // Add media control buttons that invoke intents in your media service
                .addAction(R.drawable.ic_skip_previous_black_36dp, "Previous", prevPendingIntent) // #0
                .addAction(R.drawable.ic_pause_black_36dp, "Pause", pausePendingIntent)  // #1
                .addAction(R.drawable.ic_skip_next_black_36dp, "Next", nextPendingIntent)     // #2
                .addAction(R.drawable.ic_close_black_24dp, "Close", dismissPendingIntent) // #3
                // Apply the media style template
                // .setStyle(new NotificationCompat.MediaStyle()
                // .setShowCancelButton(true)
                // .setCancelButtonIntent(dismissPendingIntent)
                // .setShowActionsInCompactView(1 /* #1: pause button */, 2, 3)
                // .setMediaSession(mediaManager.getMediaSessionToken()))
                .setCustomContentView(normalView)
                .setCustomBigContentView(bigView)
                .setShowWhen(false); // Removes the timestamp for the notification
    }

    /**
     * http://stackoverflow.com/questions/22789588/how-to-update-notification-with-remoteviews
     *
     * It is not proper to reuse the remoteview objects.
     * http://stackoverflow.com/questions/7988018/custom-notification-java-lang-runtimeexception-bad-array-lengths
     */
    public void updateNotification() {
        LynxNotificationRunner = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        // Debugging Works
                        //Log.d("FilePath", filePathIsValid("content://media/external/audio/albumart/" + currentSong.getAlbumId()) + "");
                        //Log.d("Current Context", MainActivity.getInstance().getPackageName());
                        Log.d("PersistentNotifiation", "updateNotification()");

                        normalView = new RemoteViews(getInstance().getPackageName(), R.layout.notification_normal);
                        bigView = new RemoteViews(getInstance().getPackageName(), R.layout.notification_big);

                        final Song currentSong = mediaManager.getCurrent();

                        // Setup the normalView items
                        normalView.setTextViewText(R.id.noti_title, currentSong.getTitle());
                        normalView.setTextViewText(R.id.noti_artist, currentSong.getArtistName());

                        // Setup the bigView items
                        bigView.setTextViewText(R.id.notibig_title, currentSong.getTitle());
                        bigView.setTextViewText(R.id.notibig_artist, currentSong.getArtistName());
                        bigView.setTextViewText(R.id.notibig_album, currentSong.getAlbumName());

                        // Setup the bigView items
                        bigView.setTextViewText(R.id.notibig_title, currentSong.getTitle());
                        bigView.setTextViewText(R.id.notibig_artist, currentSong.getArtistName());
                        bigView.setTextViewText(R.id.notibig_album, currentSong.getAlbumName());

                        // Setup the onClicks
                        normalView.setOnClickPendingIntent(R.id.noti_playpause,
                                getPendingSelfIntent(mContext, NOTIF_PLAYPAUSE));
                        normalView.setOnClickPendingIntent(R.id.noti_previous,
                                getPendingSelfIntent(mContext, NOTIF_PREVIOUS));
                        normalView.setOnClickPendingIntent(R.id.noti_next,
                                getPendingSelfIntent(mContext, NOTIF_NEXT));

                        bigView.setOnClickPendingIntent(R.id.notibig_playpause,
                                getPendingSelfIntent(mContext, NOTIF_PLAYPAUSE));
                        bigView.setOnClickPendingIntent(R.id.notibig_previous,
                                getPendingSelfIntent(mContext, NOTIF_PREVIOUS));
                        bigView.setOnClickPendingIntent(R.id.notibig_next,
                                getPendingSelfIntent(mContext, NOTIF_NEXT));
                        bigView.setOnClickPendingIntent(R.id.notibig_dismiss,
                                getPendingSelfIntent(mContext, NOTIF_DISMISS));

                        // http://stackoverflow.com/questions/9214715/notifcation-launches-multiple-instances-of-activities
                        normalView.setOnClickPendingIntent(R.id.noti_layout,
                                PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class)
                                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                                        PendingIntent.FLAG_UPDATE_CURRENT));
                        bigView.setOnClickPendingIntent(R.id.notibig_layout,
                                PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class)
                                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                                        PendingIntent.FLAG_UPDATE_CURRENT));

                        // Debugging Album Art
                        //Log.d("FilePathIsValid", filePathIsValid("content://media/e0ternal/audio/albumart/" + currentSong.getAlbumId()) + "");

                        // Album Art
                        // http://stackoverflow.com/questions/7817551/how-to-check-file-exist-or-not-and-if-not-create-a-new-file-in-sdcard-in-async-t
                        //if (filePathIsValid("content://media/external/audio/albumart/" + currentSong.getAlbumId())) {

                        // Setup the albumArt first
                        //Uri sArtworkUri = Uri
                        //        .parse("content://media/external/audio/albumart");
                        //final Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, currentSong.getAlbumId());

                        Bitmap albumBitmap = getAlbumArt(getInstance(),
                                currentSong.getAlbumId());

                        final int albColor = Palette.from(albumBitmap)
                                .generate()
                                .getVibrantColor(Color.parseColor("#403f4d"));

                        // http://stackoverflow.com/questions/27394016/how-does-one-use-glide-to-download-an-image-into-a-bitmap
                        normalView.setImageViewBitmap(R.id.noti_albumart, albumBitmap);

                        bigView.setImageViewBitmap(R.id.notibig_albumart, albumBitmap);

                        //Log.d("Color[0]", color[0] + "");

                        // http://stackoverflow.com/questions/27209596/media-style-notification-not-working-after-update-to-android-5-0
                        if (mediaManager.mLynxMediaPlayer.isPlaying()) {
                            // Creating a Notifcation
                            // https://developer.android.com/guide/topics/ui/notifiers/notifications.html
                            mBuilder
                                    .setSmallIcon(R.drawable.ic_app_icon)
                                    .setCustomContentView(normalView)
                                    .setCustomBigContentView(bigView)
                                    // Converting albumArtUri to a Bitmap directly
                                    // http://stackoverflow.com/questions/3879992/how-to-get-bitmap-from-an-uri
                                    //.setLargeIcon(MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), albumArtUri))
                                    .setLargeIcon(albumBitmap)
                                    // Set the color for the notification
                                    // http://stackoverflow.com/questions/1299837/cannot-refer-to-a-non-final-variable-inside-an-inner-class-defined-in-a-differen
                                    .setColor(albColor)
                                    // http://stackoverflow.com/questions/5757997/hide-time-in-android-notification-without-using-custom-layout
                                    .setContentTitle(currentSong.getTitle())
                                    .setContentText(currentSong.getArtistName())
                                    .build();
                        } else {
                            // Creating a Notifcation
                            // https://developer.android.com/guide/topics/ui/notifiers/notifications.html
                            mBuilder
                                    .setSmallIcon(R.drawable.ic_app_icon)
                                    .setCustomContentView(normalView)
                                    .setCustomBigContentView(bigView)
                                    // Converting albumArtUri to a Bitmap directly
                                    // http://stackoverflow.com/questions/3879992/how-to-get-bitmap-from-an-uri
                                    //.setLargeIcon(MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), albumArtUri))
                                    .setLargeIcon(albumBitmap)
                                    // Set the color for the notification
                                    // http://stackoverflow.com/questions/1299837/cannot-refer-to-a-non-final-variable-inside-an-inner-class-defined-in-a-differen
                                    .setColor(albColor)
                                    // http://stackoverflow.com/questions/5757997/hide-time-in-android-notification-without-using-custom-layout
                                    .setContentTitle(currentSong.getTitle())
                                    .setContentText(currentSong.getArtistName())
                                    .build();
                        }

                        /**
                         * Now we'll need to set the colors appropriately
                         */
                        // Set the layout backgrounds first
                        normalView.setInt(R.id.noti_layout, "setBackgroundColor",
                                albColor);
                        bigView.setInt(R.id.notibig_layout, "setBackgroundColor",
                                albColor);

                        if (isColorDark(albColor)) {
                            //Setup the BigView Static Contents
                            normalView.setImageViewResource(R.id.noti_previous, R.drawable.ic_skip_previous_white_24dp);
                            normalView.setImageViewResource(R.id.noti_next, R.drawable.ic_skip_next_white_24dp);
                            bigView.setImageViewResource(R.id.notibig_previous, R.drawable.ic_skip_previous_white_24dp);
                            bigView.setImageViewResource(R.id.notibig_next, R.drawable.ic_skip_next_white_24dp);
                            bigView.setImageViewResource(R.id.notibig_dismiss, R.drawable.ic_close_white_24dp);

                            if (mediaManager.mLynxMediaPlayer.isPlaying()) {
                                normalView.setImageViewResource(R.id.noti_playpause, R.drawable.ic_pause_white_24dp);
                                bigView.setImageViewResource(R.id.notibig_playpause, R.drawable.ic_pause_white_24dp);
                            } else {
                                normalView.setImageViewResource(R.id.noti_playpause, R.drawable.ic_play_arrow_white_24dp);
                                bigView.setImageViewResource(R.id.notibig_playpause, R.drawable.ic_play_arrow_white_24dp);
                            }

                            normalView.setInt(R.id.noti_title, "setTextColor",
                                    Color.WHITE);
                            normalView.setInt(R.id.noti_artist, "setTextColor",
                                    Color.WHITE);
                            bigView.setInt(R.id.notibig_title, "setTextColor",
                                    Color.WHITE);
                            bigView.setInt(R.id.notibig_artist, "setTextColor",
                                    Color.WHITE);
                            bigView.setInt(R.id.notibig_album, "setTextColor",
                                    Color.WHITE);
                        } else {
                            //Setup the BigView Static Contents
                            normalView.setImageViewResource(R.id.noti_previous, R.drawable.ic_skip_previous_black_24dp);
                            normalView.setImageViewResource(R.id.noti_next, R.drawable.ic_skip_next_black_24dp);
                            bigView.setImageViewResource(R.id.notibig_previous, R.drawable.ic_skip_previous_black_24dp);
                            bigView.setImageViewResource(R.id.notibig_next, R.drawable.ic_skip_next_black_24dp);
                            bigView.setImageViewResource(R.id.notibig_dismiss, R.drawable.ic_close_black_24dp);

                            if (mediaManager.mLynxMediaPlayer.isPlaying()) {
                                normalView.setImageViewResource(R.id.noti_playpause, R.drawable.ic_pause_black_24dp);
                                bigView.setImageViewResource(R.id.notibig_playpause, R.drawable.ic_pause_black_24dp);
                            } else {
                                normalView.setImageViewResource(R.id.noti_playpause, R.drawable.ic_play_arrow_black_24dp);
                                bigView.setImageViewResource(R.id.notibig_playpause, R.drawable.ic_play_arrow_black_24dp);
                            }

                            normalView.setInt(R.id.noti_title, "setTextColor",
                                    Color.BLACK);
                            normalView.setInt(R.id.noti_artist, "setTextColor",
                                    Color.BLACK);
                            bigView.setInt(R.id.notibig_title, "setTextColor",
                                    Color.BLACK);
                            bigView.setInt(R.id.notibig_artist, "setTextColor",
                                    Color.BLACK);
                            bigView.setInt(R.id.notibig_album, "setTextColor",
                                    Color.BLACK);
                        }

                        mNotification = mBuilder.build();

                        mNotification.flags |= Notification.FLAG_AUTO_CANCEL;

                        mNotificationManager.notify(NOTIFICATION_ID, mNotification); // Notify the app to notify the system
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            }.execute();
    }

    private boolean filePathIsValid(String path) {
        return new File(path).exists();
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, LynxNotification.class);
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
        //Log.d("onReceive:", "Works" + intent.getAction());

        switch(intent.getAction()) {
            case NOTIF_PLAYPAUSE:
                // Debugging Purposes
                Log.d("onReceive:", NOTIF_PLAYPAUSE + " Running");
                mediaControlsOnClickPlayPause();
                break;

            case NOTIF_NEXT:
                // Debugging Purposes
                Log.d("onReceive:", NOTIF_NEXT + " Running");
                mediaControlsOnClickNext(getInstance().getCurrentFocus());
                break;

            case NOTIF_PREVIOUS:
                // Debugging Purposes
                Log.d("onReceive:", NOTIF_PREVIOUS + " Running");
                mediaControlsOnClickPrevious(getInstance().getCurrentFocus());
                break;

            case NOTIF_DISMISS:
                // Clear all notification
                // http://stackoverflow.com/questions/4141555/how-to-use-getsystemservice-in-a-non-activity-class
                mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(255);
                //mediaManager.mMediaPlayer.release();
                mediaManager.purgeMediaplayer();
                break;
            default:
                break;
        }
    }
}
