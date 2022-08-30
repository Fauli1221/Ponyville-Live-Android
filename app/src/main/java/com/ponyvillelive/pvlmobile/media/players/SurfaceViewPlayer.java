package com.ponyvillelive.pvlmobile.media.players;

import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.mediarouter.media.MediaRouter;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ponyvillelive.pvlmobile.R;

/**
 * Handles playback of a single media item using MediaPlayer in SurfaceView
 */
public class SurfaceViewPlayer extends LocalPlayer implements
        SurfaceHolder.Callback {
    private static final String TAG = "SurfaceViewPlayer";
    private MediaRouter.RouteInfo mRoute;
    private SurfaceView mSurfaceView; // used to be final
    private FrameLayout mLayout; // used to be final
    private MediaPresentation mPresentation;
    public SurfaceViewPlayer(Context context) {
        super(context);
        //setupVid(((MediaPlayerService) context).videoLayout, ((MediaPlayerService) context).videoView);
    }

    protected void setupVid(FrameLayout layout, SurfaceView view){
        mLayout = layout;
        mSurfaceView = view;
        // add surface holder callback
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);
    }

    @Override
    public void connect(MediaRouter.RouteInfo route) {
        super.connect(route);
        mRoute = route;
    }
    @Override
    public void release() {
        super.release();
        // dismiss presentation display
        if (mPresentation != null) {
            Log.i(TAG, "Dismissing presentation because the activity is no longer visible.");
            mPresentation.dismiss();
            mPresentation = null;
        }
        // remove surface holder callback
        if (null == mSurfaceView || null == mLayout) return;
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.removeCallback(this);
        // hide the surface view when SurfaceViewPlayer is destroyed
        mSurfaceView.setVisibility(View.GONE);
        mLayout.setVisibility(View.GONE);
    }
    @Override
    public void updatePresentation() {
        // Get the current route and its presentation display.
        Display presentationDisplay = mRoute != null ? mRoute.getPresentationDisplay() : null;
        // Dismiss the current presentation if the display has changed.
        if (mPresentation != null && mPresentation.getDisplay() != presentationDisplay) {
            Log.i(TAG, "Dismissing presentation because the current route no longer "
                    + "has a presentation display.");
            mPresentation.dismiss();
            mPresentation = null;
        }
        // Show a new presentation if needed.
        if (mPresentation == null && presentationDisplay != null) {
            Log.i(TAG, "Showing presentation on display: " + presentationDisplay);
            mPresentation = new MediaPresentation(getContext(), presentationDisplay);
            mPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                Log.w(TAG, "Couldn't show presentation!  Display was removed in "
                        + "the meantime.", ex);
                mPresentation = null;
            }
        }
        updateContents();
    }
    // SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,
                               int width, int height) {
        if (DEBUG) {
            Log.d(TAG, "surfaceChanged: " + width + "x" + height);
        }
        setSurface(holder);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (DEBUG) {
            Log.d(TAG, "surfaceCreated");
        }
        setSurface(holder);
        updateSize();
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (DEBUG) {
            Log.d(TAG, "surfaceDestroyed");
        }
        removeSurface(holder);
    }
    @Override
    protected void updateSize() {
        try{
            int width = getVideoWidth();
            int height = getVideoHeight();
            Log.i(TAG, "int video rect is " + width + "x" + height);
            if (width > 0 && height > 0) {
                if (mPresentation == null) {
                    if (mLayout.getVisibility() != View.VISIBLE){
                        Log.d(TAG, "setting visible...");
                        mLayout.setVisibility(View.VISIBLE);
                        mSurfaceView.setVisibility(View.VISIBLE);
                        mLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    int surfaceWidth = mLayout.getWidth();
                    int surfaceHeight = mLayout.getHeight();

                    Log.i(TAG, "layout rect is " + surfaceWidth + "x" + surfaceHeight);
                    // Calculate the new size of mSurfaceView, so that video is centered
                    // inside the framelayout with proper letterboxing/pillarboxing
                    if (surfaceHeight == 0 && surfaceWidth == 0){
                        return;
                    }
                    ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
                    if (surfaceHeight == 0 || surfaceWidth * height < surfaceHeight * width) {
                        // Black bars on top&bottom, mSurfaceView has full layout width,
                        // while height is derived from video's aspect ratio
                        Log.d(TAG, "1");
                        lp.width = surfaceWidth;
                        lp.height = surfaceWidth * height / width;

                    } else {
                        // Black bars on left&right, mSurfaceView has full layout height,
                        // while width is derived from video's aspect ratio
                        Log.d(TAG, "2");
                        lp.width = surfaceHeight * width / height;
                        lp.height = surfaceHeight;
                    }
                    Log.i(TAG, "video rect is " + lp.width + "x" + lp.height);
                    mSurfaceView.setLayoutParams(lp);
                } else {
                    mPresentation.updateSize(width, height);
                }
            }
        }
        catch(Exception e){
            // lp is null, no video frame attached (running headless)
        }
    }


    private void updateContents() {
        // Show either the content in the main activity or the content in the presentation
        if (null == mLayout || null == mSurfaceView) return;
        if (mPresentation != null) {
            mLayout.setVisibility(View.GONE);
            mSurfaceView.setVisibility(View.GONE);
        } else {
            mLayout.setVisibility(View.VISIBLE);
            mSurfaceView.setVisibility(View.VISIBLE);
        }
    }
    // Listens for when presentations are dismissed.
    private final DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (dialog == mPresentation) {
                        Log.i(TAG, "Presentation dismissed.");
                        mPresentation = null;
                        updateContents();
                    }
                }
            };

    // Presentation
    private final class MediaPresentation extends Presentation {
        private SurfaceView mPresentationSurfaceView;
        public MediaPresentation(Context context, Display display) {
            super(context, display);
        }
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // Be sure to call the super class.
            super.onCreate(savedInstanceState);
            // Inflate the layout.
            setContentView(R.layout.media_router_presentation);
            // Set up the surface view.
            mPresentationSurfaceView = (SurfaceView)findViewById(R.id.surface_view);
            SurfaceHolder holder = mPresentationSurfaceView.getHolder();
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            holder.addCallback(SurfaceViewPlayer.this);
            Log.i(TAG, "Presentation created");
        }
        public void updateSize(int width, int height) {
            int surfaceHeight = getWindow().getDecorView().getHeight();
            int surfaceWidth = getWindow().getDecorView().getWidth();
            ViewGroup.LayoutParams lp = mPresentationSurfaceView.getLayoutParams();
            if (surfaceWidth * height < surfaceHeight * width) {
                lp.width = surfaceWidth;
                lp.height = surfaceWidth * height / width;
            } else {
                lp.width = surfaceHeight * width / height;
                lp.height = surfaceHeight;
            }
            Log.i(TAG, "Presentation video rect is " + lp.width + "x" + lp.height);
            mPresentationSurfaceView.setLayoutParams(lp);
        }
    }
}