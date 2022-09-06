package com.ponyvillelive.pvlmobile.media;

/**
 * Created by tinker on 4/02/16.
 * Adapted from Google Support v7 code
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.ponyvillelive.pvlmobile.media.players.OverlayPlayer;
import com.ponyvillelive.pvlmobile.media.players.RemotePlayer;
import com.ponyvillelive.pvlmobile.media.players.SurfaceViewPlayer;
import com.ponyvillelive.pvlmobile.model.player.PlaylistItem;

/**
 * Abstraction of common playback operations of media items, such as play,
 * seek, etc. Used by PlaybackManager as a backend to handle actual playback
 * of media items.
 */

public abstract class Player {
    private static final String TAG = "SampleMediaRoutePlayer";
    protected static final int STATE_IDLE = 0;
    protected static final int STATE_PREPARING_FOR_PLAY = 1;
    protected static final int STATE_PREPARING_FOR_PAUSE = 2;
    protected static final int STATE_READY = 3;
    protected static final int STATE_PLAYING = 4;
    protected static final int STATE_PAUSED = 5;
    private static final long PLAYBACK_ACTIONS = PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY;
    private static final PlaybackStateCompat INIT_PLAYBACK_STATE = new PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, .0f).build();
    protected Callback mCallback;
    protected MediaSessionCompat mMediaSession;
    protected PlaylistItem nowPlaying;
    public abstract boolean isRemotePlayback();
    public abstract boolean isQueuingSupported();
    public abstract void connect(RouteInfo route);
    public abstract void release();
    // basic operations that are always supported
    public abstract void play(final PlaylistItem item);
    public abstract void seek(final PlaylistItem item);
    public abstract void getStatus(final PlaylistItem item, final boolean update);
    public abstract void pause();
    public abstract void resume();
    public abstract void stop();
    // advanced queuing (enqueue & remove) are only supported
    // if isQueuingSupported() returns true
    public abstract void enqueue(final PlaylistItem item);
    public abstract PlaylistItem remove(String iid);
    // track info for current media item
    public void updateTrackInfo() {}
    public String getDescription() { return ""; }
    public Bitmap getSnapshot() { return null; }
    // presentation display
    public void updatePresentation() {}
    protected void setSurface(Surface surface){}
    protected void setSurface(SurfaceHolder surfaceHolder) {}
    protected void setVideoLayout(FrameLayout layout, SurfaceView surface) {}
    public void setCallback(Callback callback) {
        mCallback = callback;
    }
    public static Player create(Context context, RouteInfo route, MediaSessionCompat session) {
        Player player;
        if (route != null && route.supportsControlCategory(
                MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)) {
            player = new RemotePlayer(context);
        } else if (route != null) {
            player = new SurfaceViewPlayer(context);
        } else {
            player = new OverlayPlayer(context);
        }
        player.setMediaSession(session);
        player.initMediaSession();
        player.connect(route);
        return player;
    }
    protected void initMediaSession() {
        if (mMediaSession == null) {
            return;
        }
        mMediaSession.setMetadata(null);
        mMediaSession.setPlaybackState(INIT_PLAYBACK_STATE);
    }
    public void updateMetadata() {
        if (mMediaSession == null) {
            return;
        }
        MediaMetadataCompat.Builder bob = new MediaMetadataCompat.Builder();
        if (nowPlaying != null){
            bob.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, nowPlaying.getTitle());
            bob.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, nowPlaying.getSubtitle());
            bob.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, nowPlaying.getUri().toString());
            bob.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, getSnapshot());
        }

        mMediaSession.setMetadata(bob.build());
    }
    protected void publishState(int state) {
        if (mMediaSession == null) {
            return;
        }
        PlaybackStateCompat.Builder bob = new PlaybackStateCompat.Builder();
        bob.setActions(PLAYBACK_ACTIONS);
        switch (state) {
            case STATE_PLAYING:
                bob.setState(PlaybackStateCompat.STATE_PLAYING, -1, 1);
                break;
            case STATE_READY:
            case STATE_PAUSED:
                bob.setState(PlaybackStateCompat.STATE_PAUSED, -1, 0);
                break;
            case STATE_IDLE:
                bob.setState(PlaybackStateCompat.STATE_STOPPED, -1, 0);
                break;
        }
        PlaybackStateCompat pbState = bob.build();
        Log.d(TAG, "Setting state to " + pbState);
        mMediaSession.setPlaybackState(pbState);
        mMediaSession.setActive(state != STATE_IDLE);
    }
    private void setMediaSession(MediaSessionCompat session) {
        mMediaSession = session;
    }
    public interface Callback {
        void onError();
        void onCompletion();
        void onPlaylistChanged();
        void onPlaylistReady();
        void onItemReady(boolean hasVideo);
    }
}