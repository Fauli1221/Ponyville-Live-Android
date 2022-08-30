package com.ponyvillelive.pvlmobile.model.player;

/**
 * Created by tinker on 4/02/16.
 * Adapted from Google Support v7 code
 */
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.SystemClock;
import androidx.mediarouter.media.MediaItemStatus;
/**
 * PlaylistItem helps keep track of the current status of an media item.
 */
public final class PlaylistItem {
    // immutables
    private final String mSessionId;
    private final String mItemId;
    private final Uri mUri;
    private final Bitmap mThumb;
    private final String mMime;
    private final PendingIntent mUpdateReceiver;
    // changeable states
    private int mPlaybackState = MediaItemStatus.PLAYBACK_STATE_PENDING;
    private int mContentPosition;
    private int mContentDuration;
    private long mTimestamp;
    private String mRemoteItemId;
    private String mTitle;
    private String mSubTitle;
    public PlaylistItem(String qid, String iid, Uri uri, Bitmap pic, String mime, PendingIntent pi, String title, String subtitle) {
        mSessionId = qid;
        mItemId = iid;
        mUri = uri;
        mThumb = pic;
        mMime = mime;
        mUpdateReceiver = pi;
        mTitle = title;
        mSubTitle = subtitle;
        setTimestamp(SystemClock.elapsedRealtime());
    }
    public void setRemoteItemId(String riid) {
        mRemoteItemId = riid;
    }
    public void setState(int state) {
        mPlaybackState = state;
    }
    public void setPosition(int pos) {
        mContentPosition = pos;
    }
    public void setTimestamp(long ts) {
        mTimestamp = ts;
    }
    public void setDuration(int duration) {
        mContentDuration = duration;
    }
    public String getSessionId() {
        return mSessionId;
    }
    public String getItemId() {
        return mItemId;
    }
    public String getTitle() { return mTitle; }
    public String getSubtitle() { return mSubTitle; }
    public String getRemoteItemId() {
        return mRemoteItemId;
    }
    public Uri getUri() {
        return mUri;
    }
    public Bitmap getThumb() { return mThumb; }

    public PendingIntent getUpdateReceiver() {
        return mUpdateReceiver;
    }
    public int getState() {
        return mPlaybackState;
    }
    public int getPosition() {
        return mContentPosition;
    }
    public int getDuration() {
        return mContentDuration;
    }
    public long getTimestamp() {
        return mTimestamp;
    }
    public MediaItemStatus getStatus() {
        return new MediaItemStatus.Builder(mPlaybackState)
                .setContentPosition(mContentPosition)
                .setContentDuration(mContentDuration)
                .setTimestamp(mTimestamp)
                .build();
    }
    @Override
    public String toString() {
        String state[] = {
                "PENDING",
                "PLAYING",
                "PAUSED",
                "BUFFERING",
                "FINISHED",
                "CANCELED",
                "INVALIDATED",
                "ERROR"
        };
        return "[" + mSessionId + "|" + mItemId + "|"
                + (mRemoteItemId != null ? mRemoteItemId : "-") + "|"
                + state[mPlaybackState] + "] " + mUri.toString();
    }
}
