package com.ponyvillelive.pvlmobile.media;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.mediarouter.media.MediaRouter;

import com.ponyvillelive.pvlmobile.R;
import com.ponyvillelive.pvlmobile.model.player.PlaylistItem;
import com.ponyvillelive.pvlmobile.util.Constants;

/**
 * Created by tinker on 29/01/16.
 */

public class MediaPlayerService extends Service {

    private static final String TAG  = "PVL MPService";
    public static final String ACTION_PLAY = TAG + "action_play";
    public static final String ACTION_PAUSE = TAG + "action_pause";
    public static final String ACTION_NEXT = TAG + "action_next";
    public static final String ACTION_PREVIOUS = TAG + "action_previous";
    public static final String ACTION_STOP = TAG + "action_stop";
    private static final int ONGOING_NOTIFICATION_ID = 58644;

    private final IBinder mBinder = new MediaBinder();
    public class MediaBinder extends Binder {
        public MediaPlayerService getService() {
            Log.d(TAG, "service returned");
            initMediaSession();
            return MediaPlayerService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public MediaController mController;
    private final IntentFilter noisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final NoisyAudioStreamReceiver mNoisyAudioStreamReceiver = new NoisyAudioStreamReceiver();


    private final int loadIconState = 0;
    private Handler loadHandler;

    private Bitmap artwork;
    public Bitmap defaultArtwork;

    private AudioManager audioManager;
    public MediaRouter mMediaRouter;
    private boolean mNeedResume;

    public final SessionManager mSessionManager = new SessionManager("pvl");
    public Player mPlayer;

    public MediaSession mMediaSession;

    public final MediaRouter.Callback mMediaRouterCB = new MediaRouter.Callback() {
        // Return a custom callback that will simply log all of the route events
        // for demonstration purposes.
        @Override
        public void onRouteAdded(@NonNull MediaRouter router, @NonNull MediaRouter.RouteInfo route) {
            Log.d(TAG, "onRouteAdded: route=" + route);
        }
        @Override
        public void onRouteChanged(@NonNull MediaRouter router, @NonNull MediaRouter.RouteInfo route) {
            Log.d(TAG, "onRouteChanged: route=" + route);
        }
        @Override
        public void onRouteRemoved(@NonNull MediaRouter router, @NonNull MediaRouter.RouteInfo route) {
            Log.d(TAG, "onRouteRemoved: route=" + route);
        }
        @Override
        public void onRouteSelected(@NonNull MediaRouter router, @NonNull MediaRouter.RouteInfo route) {
            Log.d(TAG, "onRouteSelected: route=" + route);
            mPlayer = Player.create(MediaPlayerService.this, route, mMediaSession);
            mPlayer.updatePresentation();
            mSessionManager.setPlayer(mPlayer);
            mSessionManager.unsuspend();
            //updateUi();
        }
        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
            Log.d(TAG, "onRouteUnselected: route=" + route);
            mMediaSession.setActive(false);
            //PlaylistItem item = getCheckedPlaylistItem();
            PlaylistItem item = null;
            if (item != null) {
                long pos = item.getPosition() + (mSessionManager.isPaused() ?
                        0 : (SystemClock.elapsedRealtime() - item.getTimestamp()));
                mSessionManager.suspend((int)pos);
            }
            mPlayer.updatePresentation();
            mPlayer.release();
        }
        @Override
        public void onRouteVolumeChanged(MediaRouter router, MediaRouter.RouteInfo route) {
            Log.d(TAG, "onRouteVolumeChanged: route=" + route);
        }
        @Override
        public void onRoutePresentationDisplayChanged(
                MediaRouter router, MediaRouter.RouteInfo route) {
            Log.d(TAG, "onRoutePresentationDisplayChanged: route=" + route);
            mPlayer.updatePresentation();
        }
        @Override
        public void onProviderAdded(MediaRouter router, MediaRouter.ProviderInfo provider) {
            Log.d(TAG, "onRouteProviderAdded: provider=" + provider);
        }
        @Override
        public void onProviderRemoved(MediaRouter router, MediaRouter.ProviderInfo provider) {
            Log.d(TAG, "onRouteProviderRemoved: provider=" + provider);
        }
        @Override
        public void onProviderChanged(MediaRouter router, MediaRouter.ProviderInfo provider) {
            Log.d(TAG, "onRouteProviderChanged: provider=" + provider);
        }
    };
    private PendingIntent mMediaPendingIntent;

    private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.d(TAG, "audio focus changed: " + focusChange);
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                mSessionManager.pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback
                // Raise volume back to normal
                mSessionManager.resume();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                audioManager.abandonAudioFocus(mAudioFocusListener);
                // Stop playback
                mSessionManager.pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // Lower the volume
            }

        }
    };



    private void handleIntent( Intent intent ) {
        if( intent == null || intent.getAction() == null )
            return;

        String action = intent.getAction();

        if( action.equalsIgnoreCase( ACTION_PLAY ) ) {
            mController.getTransportControls().play();
            mSessionManager.resume();
        } else if( action.equalsIgnoreCase( ACTION_PAUSE ) ) {
            mController.getTransportControls().pause();
            mSessionManager.pause();
        } else if( action.equalsIgnoreCase( ACTION_PREVIOUS ) ) {
            mController.getTransportControls().skipToPrevious();
            mController.getTransportControls().skipToPrevious();
        } else if( action.equalsIgnoreCase( ACTION_NEXT ) ) {
            mController.getTransportControls().skipToNext();
            mSessionManager.next();
        } else if( action.equalsIgnoreCase( ACTION_STOP ) ) {
            mController.getTransportControls().stop();
            mSessionManager.stop();
        } else initMediaSession();
    }



    private Notification.Action generateAction( int icon, String title, String intentAction ) {
        Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder( icon, title, pendingIntent ).build();
    }

    private void initNotification() {
        Log.d(TAG, "init notifcation");
        Notification.Action action = generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY);

        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction( ACTION_STOP );
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        Bitmap defaultArtwork = BitmapFactory.decodeResource(getResources(), R.drawable.pvl_logo);

        Notification.MediaStyle style = new Notification.MediaStyle().setMediaSession(mMediaSession.getSessionToken());
        style.setShowActionsInCompactView(0, 1, 2);
//        style.setShowCancelButton(true);
//        style.setCancelButtonIntent(pendingIntent);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setColor(Color.argb(255, 44, 127, 210)); // hard coded pvl blue... ?
        builder.setSmallIcon(R.drawable.pvl_logo);
        builder.setLargeIcon(defaultArtwork);
        builder.setContentTitle("Ponyville Live!");
        //builder.setContentText(station.subtitle);
        builder.setDeleteIntent(pendingIntent);
        builder.setShowWhen(false);
        builder.setStyle(style);
        //builder.setContentIntent() // fill this out to launch main app, loading to playing station direct

        builder.addAction( generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
        builder.addAction( action );
        builder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));
        startService(new Intent(this, MediaPlayerService.class)); // needed as app now binds directly to service
        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    public void updateViews(SurfaceView view, FrameLayout layout){
        mPlayer.setVideoLayout(layout, view);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "service on start");

        handleIntent(intent);
        return START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    public void initMediaSession() {
        Log.d(TAG, "init media session");

        if( mMediaSession != null) {
            //initNotification();
            sendBroadcast(new Intent(Constants.SERVICE_SETUP));
            return;
        }

        defaultArtwork = BitmapFactory.decodeResource(getResources(), R.drawable.pvl_logo);

        registerReceiver(mNoisyAudioStreamReceiver, noisyIntentFilter);

        // Build the PendingIntent for the remote control client
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mMediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);

        // Create the MediaSession
        mMediaSession = new MediaSession(this, TAG, null);
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);


        // Create and register the remote control client
        mMediaRouter = MediaRouter.getInstance(this);
        mMediaRouter.setMediaSession(mMediaSession);

        // Set up playback manager and player
        mPlayer = Player.create(MediaPlayerService.this,
                mMediaRouter.getSelectedRoute(), mMediaSession);
        mSessionManager.setPlayer(mPlayer);

        // Get the media router service.
        mMediaRouter = MediaRouter.getInstance(this);

        try{
            mController = new MediaController(this, mMediaSession.getSessionToken());
        }
        catch (Exception e){
            Log.e("PVL", "failed to attach to remote media session token: " + e);
            return;
        }

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMediaSession.setCallback(new MediaSession.Callback() {
             @Override
             public void onPlay() {
                 super.onPlay();
                 Log.e("MediaPlayerService", "onPlay");
                 int result = audioManager.requestAudioFocus(mAudioFocusListener,
                         AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                 if (result != AudioManager.AUDIOFOCUS_GAIN) {
                     return; //Failed to gain audio focus
                 }
                 if (!mPlayer.isRemotePlayback() && mNeedResume) {
                     mNeedResume = false;
                 }
                 mMediaSession.setActive(true);
                 mSessionManager.resume();
               //  buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
             }

             @Override
             public void onPause() {
                 super.onPause();
                 Log.e("MediaPlayerService", "onPause");
                 if (!mPlayer.isRemotePlayback() && !mSessionManager.isPaused()) {
                     mNeedResume = true;
                 }
                 mSessionManager.pause();
                // buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
             }

             @Override
             public void onSkipToNext() {
                 super.onSkipToNext();

                 Log.e("MediaPlayerService", "onSkipToNext");
                 mSessionManager.next();
               //  buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
             }

             @Override
             public void onSkipToPrevious() {
                 super.onSkipToPrevious();
                 Log.e("MediaPlayerService", "onSkipToPrevious NOT CURRENTLY SUPPORTED");

                 //Stop media player here
                 NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                 notificationManager.cancel(1);
                 Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                 stopService(intent);

                 //playStationPrev();
                 //buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
             }

             @Override
             public void onStop() {
                 super.onStop();
                 Log.e("MediaPlayerService", "onStop");
                 //Stop media player here
                 NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                 notificationManager.cancel(1);
                 Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                 stopService(intent);
             }

             @Override
             public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                 if (mediaButtonEvent != null) {
                     return handleMediaKey(
                             mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT));
                 }
                 return super.onMediaButtonEvent(mediaButtonEvent);
             }
         }
        );
       // initNotification();
        sendBroadcast(new Intent(Constants.SERVICE_SETUP));
    }

   /* private void buildNotification( NotificationCompat.Action action ) {
        PlaylistItem current = mSessionManager.getCurrentItem();
        if (null == current){
            // clear notification?
            Log.d(TAG, "NULL NOTIFICATION");
            return;
        }
        Log.d(TAG,"building notification..");
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle().setMediaSession(mMediaSession.getSessionToken());
        style.setShowActionsInCompactView(0, 1, 2);
        style.setShowCancelButton(true);
        style.setCancelButtonIntent(pendingIntent);

        androidx.appcompat.app.NotificationCompat.Builder builder = new androidx.appcompat.app.NotificationCompat.Builder(this);
        builder.setColor(Color.argb(255, 44, 127, 210)); // hard coded pvl blue... ?
        builder.setSmallIcon(R.drawable.pvl_logo);
        //builder.setLargeIcon(artwork.copy(artwork.getConfig(), true));
        builder.setLargeIcon(current.getThumb().copy(current.getThumb().getConfig(), true));
        builder.setContentTitle(current.getTitle());
        builder.setContentText(current.getSubtitle());
        builder.setDeleteIntent(pendingIntent);
        builder.setShowWhen(false);
        builder.setStyle(style);
        //builder.setContentIntent() // fill this out to launch main app, loading to playing station direct

        builder.addAction( generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
        builder.addAction( action );
        builder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));

        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }*/

    public boolean handleMediaKey(KeyEvent event) {
        if (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                case KeyEvent.KEYCODE_HEADSETHOOK:
                {
                    Log.d(TAG, "Received Play/Pause event from RemoteControlClient");
                    if (mSessionManager.isPaused()) {
                        mSessionManager.resume();
                    } else {
                        mSessionManager.pause();
                    }
                    return true;
                }
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                {
                    Log.d(TAG, "Received Play event from RemoteControlClient");
                    if (mSessionManager.isPaused()) {
                        mSessionManager.resume();
                    }
                    return true;
                }
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                {
                    Log.d(TAG, "Received Pause event from RemoteControlClient");
                    if (!mSessionManager.isPaused()) {
                        mSessionManager.pause();
                    }
                    return true;
                }
                case KeyEvent.KEYCODE_MEDIA_STOP:
                {
                    Log.d(TAG, "Received Stop event from RemoteControlClient");
                    mSessionManager.stop();
                    return true;
                }
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                {
                    Log.d(TAG, "Received Next event from RemoteControlClient");
                    mSessionManager.next();
                    return true;
                }
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                {
                    Log.d(TAG, "Received Previous event from RemoteControlClient");
                    //mSessionManager.pre;
                    return true;
                }
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "service unbound");
        return true;
    }

    @Override
    public void onRebind (Intent intent) {
        Log.d(TAG, "service rebinded");
        //super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "service destroyed");
        unregisterReceiver(mNoisyAudioStreamReceiver);
        mSessionManager.stop();
        if (mPlayer != null)
            mPlayer.release();
        mMediaSession.release();
        super.onDestroy();

    }

    private class NoisyAudioStreamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                // Pause the playback
                Log.d(TAG, "Became noisy (headphones out?). Pausing");
                mSessionManager.pause();
            }
        }
    }

}