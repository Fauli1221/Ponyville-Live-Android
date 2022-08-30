package com.ponyvillelive.pvlmobile.media.players;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.mediarouter.media.MediaRouter;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.ponyvillelive.pvlmobile.R;
import com.ponyvillelive.pvlmobile.ui.OverlayDisplayWindow;

/**
 * Handles playback of a single media item using MediaPlayer in
 * OverlayDisplayWindow.
 */
public class OverlayPlayer extends LocalPlayer implements
        OverlayDisplayWindow.OverlayWindowListener {
    private static final String TAG = "OverlayPlayer";
    private final OverlayDisplayWindow mOverlay;
    public OverlayPlayer(Context context) {
        super(context);
        mOverlay = OverlayDisplayWindow.create(getContext(),
                getContext().getResources().getString(
                        R.string.media_route_overlay_text),
                1024, 768, Gravity.CENTER);
        mOverlay.setOverlayWindowListener(this);
    }
    @Override
    public void connect(MediaRouter.RouteInfo route) {
        super.connect(route);
        mOverlay.show();
    }
    @Override
    public void release() {
        super.release();
        mOverlay.dismiss();
    }
    @Override
    protected void updateSize() {
        int width = getVideoWidth();
        int height = getVideoHeight();
        if (width > 0 && height > 0) {
            mOverlay.updateAspectRatio(width, height);
        }
    }
    // OverlayDisplayWindow.OverlayWindowListener
    @Override
    public void onWindowCreated(Surface surface) {
        setSurface(surface);
    }
    @Override
    public void onWindowCreated(SurfaceHolder surfaceHolder) {
        setSurface(surfaceHolder);
    }
    @Override
    public void onWindowDestroyed() {
        setSurface((SurfaceHolder)null);
    }
    @Override
    public Bitmap getSnapshot() {
        if (getState() == STATE_PLAYING || getState() == STATE_PAUSED) {
            return mOverlay.getSnapshot();
        }
        return null;
    }
}