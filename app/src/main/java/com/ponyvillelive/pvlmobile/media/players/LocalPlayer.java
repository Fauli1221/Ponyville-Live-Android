package com.ponyvillelive.pvlmobile.media.players;

/**
 * Created by tinker on 4/02/16.
 */

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.media.MediaItemStatus;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.ponyvillelive.pvlmobile.media.Player;
import com.ponyvillelive.pvlmobile.model.player.PlaylistItem;

import java.io.IOException;
/**
 * Handles playback of a single media item using MediaPlayer.
 */
public abstract class LocalPlayer extends Player implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener {
    private static final String TAG = "LocalPlayer";
    protected static final boolean DEBUG = true;
    private final Context mContext;
    private final Handler mHandler = new Handler();
    private final Handler mUpdateSurfaceHandler = new Handler(mHandler.getLooper());
    private MediaPlayer mMediaPlayer;
    private int mState = STATE_IDLE;
    private int mSeekToPos;
    private int mVideoWidth;
    private int mVideoHeight;
    private boolean mHasVideo = false;
    private Surface mSurface;
    private SurfaceHolder mSurfaceHolder;
    public LocalPlayer(Context context) {
        mContext = context;
        // reset media player
        reset();
    }
    @Override
    public boolean isRemotePlayback() {
        return false;
    }
    @Override
    public boolean isQueuingSupported() {
        return false;
    }
    @Override
    public void connect(RouteInfo route) {
        if (DEBUG) {
            Log.d(TAG, "connecting to: " + route);
        }
    }
    @Override
    public void release() {
        if (DEBUG) {
            Log.d(TAG, "releasing");
        }
        // release media player
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    // Player
    @Override
    public void play(final PlaylistItem item) {
        if (DEBUG) {
            Log.d(TAG, "play: item=" + item);
        }
        reset();
        mSeekToPos = item.getPosition();
        try {
            mMediaPlayer.setDataSource(mContext, item.getUri());
            mMediaPlayer.prepareAsync();
            nowPlaying = item;
        } catch (IllegalStateException e) {
            Log.e(TAG, "MediaPlayer throws IllegalStateException, uri=" + item.getUri());
        } catch (IOException e) {
            Log.e(TAG, "MediaPlayer throws IOException, uri=" + item.getUri());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "MediaPlayer throws IllegalArgumentException, uri=" + item.getUri());
        } catch (SecurityException e) {
            Log.e(TAG, "MediaPlayer throws SecurityException, uri=" + item.getUri());
        }
        if (item.getState() == MediaItemStatus.PLAYBACK_STATE_PLAYING) {
            resume();
        } else {
            pause();
        }
    }
    @Override
    public void seek(final PlaylistItem item) {
        if (DEBUG) {
            Log.d(TAG, "seek: item=" + item);
        }
        int pos = item.getPosition();
        if (mState == STATE_PLAYING || mState == STATE_PAUSED) {
            mMediaPlayer.seekTo(pos);
            mSeekToPos = pos;
        } else if (mState == STATE_IDLE || mState == STATE_PREPARING_FOR_PLAY
                || mState == STATE_PREPARING_FOR_PAUSE) {
            // Seek before onPrepared() arrives,
            // need to performed delayed seek in onPrepared()
            mSeekToPos = pos;
        }
    }
    @Override
    public void getStatus(final PlaylistItem item, final boolean update) {
        if (mState == STATE_PLAYING || mState == STATE_PAUSED) {
            // use mSeekToPos if we're currently seeking (mSeekToPos is reset
            // when seeking is completed)
            if (item.getDuration() == 0){
                int duration = mMediaPlayer.getDuration();
                item.setDuration(duration);
            }
            item.setPosition(mSeekToPos > 0 ?
                    mSeekToPos : mMediaPlayer.getCurrentPosition());
            item.setTimestamp(SystemClock.elapsedRealtime());
        }
        if (update && mCallback != null) {
            mCallback.onPlaylistReady();
        }
    }
    @Override
    public void pause() {
        if (DEBUG) {
            Log.d(TAG, "pause");
        }
        if (mState == STATE_PLAYING) {
            mMediaPlayer.pause();
            mState = STATE_PAUSED;
        } else if (mState == STATE_PREPARING_FOR_PLAY) {
            mState = STATE_PREPARING_FOR_PAUSE;
        }
    }
    @Override
    public void resume() {
        if (DEBUG) {
            Log.d(TAG, "resume");
        }
        if (mState == STATE_READY || mState == STATE_PAUSED) {
            mMediaPlayer.start();
            mState = STATE_PLAYING;
        } else if (mState == STATE_IDLE || mState == STATE_PREPARING_FOR_PAUSE) {
            mState = STATE_PREPARING_FOR_PLAY;
        }
    }
    @Override
    public void stop() {
        if (DEBUG) {
            Log.d(TAG, "stop");
        }
        if (mState == STATE_PLAYING || mState == STATE_PAUSED) {
            mMediaPlayer.stop();
            mState = STATE_IDLE;
        }
    }
    @Override
    public void enqueue(final PlaylistItem item) {
        throw new UnsupportedOperationException("LocalPlayer doesn't support enqueue!");
    }
    @Override
    public PlaylistItem remove(String iid) {
        throw new UnsupportedOperationException("LocalPlayer doesn't support remove!");
    }
    //MediaPlayer Listeners
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (DEBUG) {
            Log.d(TAG, "onPrepared");
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mState == STATE_IDLE) {
                    mState = STATE_READY;
                    updateVideoRect();
                } else if (mState == STATE_PREPARING_FOR_PLAY
                        || mState == STATE_PREPARING_FOR_PAUSE) {
                    int prevState = mState;
                    mState = mState == STATE_PREPARING_FOR_PLAY ? STATE_PLAYING : STATE_PAUSED;
                    updateVideoRect();
                    if (mSeekToPos > 0) {
                        if (DEBUG) {
                            Log.d(TAG, "seek to initial pos: " + mSeekToPos);
                        }
                        mMediaPlayer.seekTo(mSeekToPos);
                    }
                    if (prevState == STATE_PREPARING_FOR_PLAY) {
                        mMediaPlayer.start();
                    }
                }
                if (mCallback != null) {
                    mCallback.onPlaylistChanged();
                }
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (DEBUG) {
            Log.d(TAG, "onCompletion");
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onCompletion();
                }
            }
        });
    }
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (DEBUG) {
            Log.d(TAG, "onError");
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onError();
                }
            }
        });
        // return true so that onCompletion is not called
        return true;
    }
    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (DEBUG) {
            Log.d(TAG, "onSeekComplete");
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mSeekToPos = 0;
                if (mCallback != null) {
                    mCallback.onPlaylistChanged();
                }
            }
        });
    }

    protected Context getContext() { return mContext; }
    protected MediaPlayer getMediaPlayer() { return mMediaPlayer; }
    protected int getVideoWidth() { return mVideoWidth; }
    protected int getVideoHeight() { return mVideoHeight; }
    protected int getState() { return mState; }

    @Override
    protected void setSurface(Surface surface) {
        mSurface = surface;
        mSurfaceHolder = null;
        updateSurface();
    }

    @Override
    protected void setSurface(SurfaceHolder surfaceHolder) {
        mSurface = null;
        mSurfaceHolder = surfaceHolder;
        updateSurface();
    }

    @Override
    protected void setVideoLayout(FrameLayout layout, SurfaceView surface) {
        mSurface = null;
        mSurfaceHolder = surface.getHolder();
        try{
            ((SurfaceViewPlayer)this).setupVid(layout, surface);
        }
        catch (Exception e){
            // local player not surfaceView (i.e. overlaying on top of UI) - do nothing
        }
        updateSurface();
        updateSize();
    }

    protected void removeSurface(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == mSurfaceHolder) {
            setSurface((SurfaceHolder)null);
        }
    }

    protected void updateSurface() {
        mUpdateSurfaceHandler.removeCallbacksAndMessages(null);
        mUpdateSurfaceHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer == null) {
                    // just return if media player is already gone
                    return;
                }
                if (mSurface != null) {
                    // The setSurface API does not exist until V14+.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        mMediaPlayer.setSurface(mSurface);
                    } else {
                        throw new UnsupportedOperationException("MediaPlayer does not support "
                                + "setSurface() on this version of the platform.");
                    }
                } else if (mSurfaceHolder != null) {
                    try{
                        mMediaPlayer.setDisplay(mSurfaceHolder);
                    }
                    catch (Exception e){
                        Log.d(TAG,"Failed to set display: " + e);
                        mMediaPlayer.setDisplay(null);
                    }

                } else {
                    mMediaPlayer.setDisplay(null);
                }
            }
        });
    }

    protected abstract void updateSize();
    private void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        updateSurface();
        mState = STATE_IDLE;
        mSeekToPos = 0;
    }

    private void updateVideoRect() {
        if (mState != STATE_IDLE && mState != STATE_PREPARING_FOR_PLAY
                && mState != STATE_PREPARING_FOR_PAUSE) {
            int width = mMediaPlayer.getVideoWidth();
            int height = mMediaPlayer.getVideoHeight();
            if (width > 0 && height > 0) {
                mHasVideo = true;
                mVideoWidth = width;
                mVideoHeight = height;
                updateSize();
            } else {
                Log.e(TAG, "video rect is 0x0!");
                mVideoWidth = mVideoHeight = 0;
                mHasVideo = false;
            }
            // callback that video status determined
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onItemReady(mHasVideo);
                    }
                }
            });
        }
    }

}