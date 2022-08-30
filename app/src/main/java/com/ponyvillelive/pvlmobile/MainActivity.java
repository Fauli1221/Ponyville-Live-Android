package com.ponyvillelive.pvlmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.mediarouter.app.MediaRouteActionProvider;
import androidx.mediarouter.app.MediaRouteDiscoveryFragment;
import androidx.mediarouter.media.MediaControlIntent;
import androidx.mediarouter.media.MediaItemStatus;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;
import com.ponyvillelive.pvlmobile.fragments.ConventionDetailFragment;
import com.ponyvillelive.pvlmobile.fragments.ConventionListFragment;
import com.ponyvillelive.pvlmobile.fragments.PVLFragmentListener;
import com.ponyvillelive.pvlmobile.fragments.ShowDetailFragment;
import com.ponyvillelive.pvlmobile.fragments.ShowListFragment;
import com.ponyvillelive.pvlmobile.fragments.StationListFragment;
import com.ponyvillelive.pvlmobile.media.MediaPlayerService;
import com.ponyvillelive.pvlmobile.media.OverlayRouteProvider;
import com.ponyvillelive.pvlmobile.media.SessionManager;
import com.ponyvillelive.pvlmobile.model.Convention;
import com.ponyvillelive.pvlmobile.model.Episode;
import com.ponyvillelive.pvlmobile.model.Show;
import com.ponyvillelive.pvlmobile.model.Station;
import com.ponyvillelive.pvlmobile.model.player.PlaylistItem;
import com.ponyvillelive.pvlmobile.net.SoundCloudAPI;
import com.ponyvillelive.pvlmobile.net.YoutubeExtractor;
import com.ponyvillelive.pvlmobile.util.Constants;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PVLFragmentListener {

    private static final String DISCOVERY_FRAGMENT_TAG = "DiscoveryFragment";
    private final Handler mHandler = new Handler();
    public Boolean splashDismissed = false;
    protected MediaPlayerService mService;
    ImageButton glanceSize;
    boolean glanceSizeUp = true;
    private String TAG = "PVL";
    private SlidingUpPanelLayout mLayout;
    private PlaylistAdapter mPlayListItems;
    private ImageButton mPauseResumeButton;
    private ImageButton mPrevButton;
    private ImageButton mNextButton;
    private ImageButton mStopButton;
    private ImageButton mExtraButton;
    private FrameLayout videoLayout;
    private SurfaceView videoView;
    private DynamicListView playListView;
    private SeekBar mSeekBar;
    private final Runnable mUpdateSeekRunnable = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            // update Ui every 1 second
            mHandler.postDelayed(this, 1000);
        }
    };
    private boolean mSeeking; // ignore IDE -> it's being used!
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mSelector;
    private ServiceReceiver mServiceReceiver;
    private boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.MediaBinder binder = (MediaPlayerService.MediaBinder) service;
            mService = binder.getService();
            mBound = true;

            if (mService.mPlayer != null) {
                Log.d(TAG, "setting video views...");
                // setup local video view for the service player
                videoLayout = (FrameLayout) findViewById(R.id.player);
                videoView = (SurfaceView) findViewById(R.id.surface_view);
                //mService.updateViews(videoView, videoLayout);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        List<MediaRouter.ProviderInfo> proInfo = mMediaRouter.getProviders();
        boolean installed = false;
        for (MediaRouter.ProviderInfo info : proInfo) {
            if (info.getPackageName().equals("com.ponyvillelive.pvlmobile"))
                installed = true;
        }
        if (!installed)
            mMediaRouter.addProvider(new OverlayRouteProvider(this));


        // Create a route selector for the type of routes that we care about.
        mSelector = new MediaRouteSelector.Builder()
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
                .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                .addControlCategory(OverlayRouteProvider.CATEGORY_OVERLAY_ROUTE)
                .build();

        if (savedInstanceState != null) {
            System.gc();
            hideSplash(false);
        } else {
            Fragment stationFragment = new StationListFragment();
            loadFragment(stationFragment);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideSplash(true);
                }
            }, 1000);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        setupSlidingLayout();
    }

    private void setupSlidingLayout() {
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.setPanelState(PanelState.HIDDEN);
        LinearLayout glanceView = (LinearLayout) findViewById(R.id.dragView);
        mLayout.setDragView(glanceView);
        mLayout.setAnchorPoint(0.6f);

        glanceSize = (ImageButton) findViewById(R.id.glance_size);
        glanceSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLayout.getPanelState() == PanelState.COLLAPSED) {
                    mLayout.setPanelState(PanelState.ANCHORED);
                } else if (mLayout.getPanelState() == PanelState.ANCHORED) {
                    mLayout.setPanelState(PanelState.EXPANDED);
                } else {
                    mLayout.setPanelState(PanelState.COLLAPSED);
                }
            }
        });

        mLayout.setPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelExpanded(View panel) {
                //Log.i(TAG, "onPanelExpanded");
                //glanceSize.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_down));
                if (glanceSizeUp) {
                    Animation rotateAnim = new RotateAnimation(0, 180, glanceSize.getWidth() / 2, glanceSize.getHeight() / 2);
                    rotateAnim.setDuration(Constants.SHORT_DELAY_MILLIS);
                    rotateAnim.setInterpolator(new BounceInterpolator());
                    rotateAnim.setFillAfter(true);
                    glanceSize.startAnimation(rotateAnim);
                }
                glanceSizeUp = false;
            }

            @Override
            public void onPanelCollapsed(View panel) {
                //Log.i(TAG, "onPanelCollapsed");
                //glanceSize.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_up));
                if (!glanceSizeUp) {
                    Animation rotateAnim = new RotateAnimation(180, 360, glanceSize.getWidth() / 2, glanceSize.getHeight() / 2);
                    rotateAnim.setDuration(Constants.SHORT_DELAY_MILLIS);
                    rotateAnim.setInterpolator(new BounceInterpolator());
                    rotateAnim.setFillAfter(true);
                    glanceSize.startAnimation(rotateAnim);
                }
                glanceSizeUp = true;
            }

            @Override
            public void onPanelAnchored(View panel) {
                //Log.i(TAG, "onPanelAnchored");
                //glanceSize.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_up));
                if (!glanceSizeUp) {
                    Animation rotateAnim = new RotateAnimation(180, 360, glanceSize.getWidth() / 2, glanceSize.getHeight() / 2);
                    rotateAnim.setDuration(Constants.SHORT_DELAY_MILLIS);
                    rotateAnim.setInterpolator(new BounceInterpolator());
                    rotateAnim.setFillAfter(true);
                    glanceSize.startAnimation(rotateAnim);
                }
                glanceSizeUp = true;
            }

            @Override
            public void onPanelHidden(View panel) {
                //Log.i(TAG, "onPanelHidden");
            }
        });
        TextView glanceText = (TextView) findViewById(R.id.glance_text);
        //glanceText.setSelected(true);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Be sure to call the super class.
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mSelector);

        // Setup sliding panel menu item
        MenuItem item = menu.findItem(R.id.action_toggle);
        if (mLayout != null) {
            if (mLayout.getPanelState() == PanelState.HIDDEN) {
                item.setTitle(R.string.action_show);
            } else {
                item.setTitle(R.string.action_hide);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_toggle: {
                if (mLayout != null) {
                    if (mLayout.getPanelState() != PanelState.HIDDEN) {
                        mLayout.setPanelState(PanelState.HIDDEN);
                        item.setTitle(R.string.action_show);
                    } else {
                        mLayout.setPanelState(PanelState.COLLAPSED);
                        item.setTitle(R.string.action_hide);
                    }
                }
                return true;
            }
            case R.id.action_anchor: {
                if (mLayout != null) {
                    if (mLayout.getAnchorPoint() == 1.0f) {
                        mLayout.setAnchorPoint(0.7f);
                        mLayout.setPanelState(PanelState.ANCHORED);
                        item.setTitle(R.string.action_anchor_disable);
                    } else {
                        mLayout.setAnchorPoint(1.0f);
                        mLayout.setPanelState(PanelState.COLLAPSED);
                        item.setTitle(R.string.action_anchor_enable);
                    }
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("restore!", true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "resuming main activity");
        if (mServiceReceiver == null) mServiceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.SERVICE_SETUP);
        registerReceiver(mServiceReceiver, intentFilter);

        if (!mBound) {
            Log.d(TAG, "binding service...");
            Intent intent = new Intent(this, MediaPlayerService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "pausing main activity");
        if (mServiceReceiver != null)
            unregisterReceiver(mServiceReceiver);
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        mService.mSessionManager.setCallback(null);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "starting main activity");
        // Bind to LocalService if not already

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "stopping main activity");
    }


    private void continueCreate() {

        mPlayListItems = new PlaylistAdapter();
        SwipeDismissAdapter swipeDismissAdapter = new SwipeDismissAdapter(mPlayListItems, new PlaylistOnDismissCallback(mPlayListItems));

        playListView = (DynamicListView) findViewById(R.id.playlist);
        playListView.setAdapter(swipeDismissAdapter);

        playListView.enableDragAndDrop();
        playListView.enableSwipeToDismiss(new PlaylistOnDismissCallback(mPlayListItems));
        playListView.setOnItemMovedListener(new PlaylistOnItemMovedListener(mPlayListItems));
        playListView.setOnItemLongClickListener(new PlaylistOnItemLongClickListener(playListView));
        playListView.setOnItemClickListener(new PlaylistOnItemClickListener(mPlayListItems));


        // setup local video view for the service player
        videoLayout = (FrameLayout) findViewById(R.id.player);
        videoView = (SurfaceView) findViewById(R.id.surface_view);

        mService.updateViews(videoView, videoLayout);

        // Add a fragment to take care of media route discovery.
        // This fragment automatically adds or removes a callback whenever the activity
        // is started or stopped.
        FragmentManager fm = getSupportFragmentManager();
        DiscoveryFragment fragment = (DiscoveryFragment) fm.findFragmentByTag(
                DISCOVERY_FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new DiscoveryFragment();
            fragment.setCallback(mService.mMediaRouterCB);
            fragment.setRouteSelector(mSelector);
            fm.beginTransaction()
                    .add(fragment, DISCOVERY_FRAGMENT_TAG)
                    .commit();
        } else {
            fragment.setCallback(mService.mMediaRouterCB);
            fragment.setRouteSelector(mSelector);
        }

        // Test setup variety of media
        //testSetupSongs();

        // Initialize the layout.
        mPauseResumeButton = (ImageButton) findViewById(R.id.pause_resume_button);
        mPauseResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService.mSessionManager.isPaused()) {
                    //mService.mSessionManager.resume();
                    mService.mController.getTransportControls().play();
                } else {
                    //mService.mSessionManager.pause();
                    mService.mController.getTransportControls().pause();
                }
            }
        });
        mPrevButton = (ImageButton) findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "prev button pressed");
                mService.mController.getTransportControls().skipToPrevious();
            }
        });
        mNextButton = (ImageButton) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.mController.getTransportControls().skipToNext();
            }
        });
        mStopButton = (ImageButton) findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mService.mSessionManager.stop();
                mService.mController.getTransportControls().stop();
            }
        });
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                PlaylistItem item = getCheckedPlaylistItem();
                if (fromUser && item != null && item.getDuration() > 0) {
                    int pos = progress * item.getDuration() / 100;
                    mService.mSessionManager.seek(item.getItemId(), pos);
                    item.setPosition(pos);
                    item.setTimestamp(SystemClock.elapsedRealtime());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeeking = false;
                updateUi();
            }
        });
        // Schedule Ui update
        mHandler.postDelayed(mUpdateSeekRunnable, 1000);

        mService.mSessionManager.setCallback(new SessionManager.Callback() {
            @Override
            public void onStatusChanged() {
                updateUi();
                //Log.d(TAG, "status changed");
            }

            @Override
            public void onItemChanged(PlaylistItem item) {
                Log.d(TAG, "item changed: " + item.toString());
                //updatePlaylist();
            }

            @Override
            public void onItemReady(boolean hasVideo) {
                Log.d(TAG, "Session manager, item ready. Has video: " + hasVideo);
            }
        });

        updateUi();
    }

    private void updateProgress() {
        // Estimate content position from last status time and elapsed time.
        // (Note this might be slightly out of sync with remote side, however
        // it avoids frequent polling the MRP.)
        int progress = 0;
        PlaylistItem item = getCheckedPlaylistItem();
        if (item != null) {
            int state = item.getState();
            long duration = item.getDuration();
            if (duration <= 0) {
                if (state == MediaItemStatus.PLAYBACK_STATE_PLAYING
                        || state == MediaItemStatus.PLAYBACK_STATE_PAUSED) {
                    mService.mSessionManager.updateStatus();
                }
            } else {
                long position = item.getPosition();
                long timeDelta = mService.mSessionManager.isPaused() ? 0 :
                        (SystemClock.elapsedRealtime() - item.getTimestamp());
                progress = (int) (100.0 * (position + timeDelta) / duration);
            }
        }
        mSeekBar.setProgress(progress);
    }

    private void updateUi() {
        updatePlaylist();
        updateButtons();
        if (mService.mPlayer != null) {
            mService.mPlayer.updateMetadata();
        }
    }

    private void updatePlaylist() {

        mPlayListItems.clear();
        for (PlaylistItem item : mService.mSessionManager.getPlaylist()) {
            mPlayListItems.add(item);
        }

        // hide/show player layout depending on activity
        if (mPlayListItems.getCount() == 0)
            mLayout.setPanelState(PanelState.HIDDEN);
        else if (mLayout.getPanelState() == PanelState.HIDDEN)
            mLayout.setPanelState(PanelState.COLLAPSED);

        playListView.invalidate();
    }

    private void updateButtons() {
        MediaRouter.RouteInfo route = mService.mMediaRouter.getSelectedRoute();
        // show pause or resume icon depending on current state
        mPauseResumeButton.setImageResource(mService.mSessionManager.isPaused() ?
                R.drawable.ic_media_play : R.drawable.ic_media_pause);
        // only enable seek bar when duration is known
        PlaylistItem item = getCheckedPlaylistItem();
        mSeekBar.setEnabled(item != null && item.getDuration() > 0);
    }

    private PlaylistItem getCheckedPlaylistItem() {
        int count = playListView.getCount();
        int index = playListView.getCheckedItemPosition();

        //int count = 0;
        //int index = 0;
        if (count > 0) {
            if (index < 0 || index >= count) {
                index = 0;
                playListView.setItemChecked(0, true);
            }
            return mPlayListItems.getItem(index);
        }
        return mService.mSessionManager.getCurrentItem();
    }

    private void loadStreams(Station[] stations, boolean explicitLoad) {
        if (null == mService) return;
        if (explicitLoad) {
            for (Station station : stations) {
                //mService.addToPlaylist(Uri.parse(station.streamUrl), station.imageUrl, "video/mp4", station.name, station.genre);
            }
        }

    }

    private void playVideo(final String title, final String subtitle, String url, final Bitmap thumb){
        if (null == mService) return;
        Log.d(TAG, "resolving stream: " + url);


        // SoundCloud
        if (url.startsWith("http://soundcloud.com/") || url.startsWith("https://soundcloud.com/")) {
            url = SoundCloudAPI.resolve(url) + "/stream?client_id=" + SoundCloudAPI.API_KEY;
            Log.d("PVL", "Soundcloud URL: " + url);
            mService.mSessionManager.add(Uri.parse(url), thumb.copy(thumb.getConfig(), true),
                    "video/mp4", null, title, subtitle);
        }

        // Youtube
        else if (url.startsWith("http://www.youtube.com/") || url.startsWith("https://ww.youtube.com/")) {
            String ytCode = url.replace("http://www.youtube.com/watch?v=", "");
            ytCode = ytCode.replace("https://www.youtube.com/watch?v=", "");
            YoutubeExtractor ytEx = new YoutubeExtractor(ytCode);
            List<Integer> ytQual; // quality selection -> high to low. Change this to user defined setting
            ytQual = Arrays.asList(YoutubeExtractor.YOUTUBE_VIDEO_QUALITY_HD_1080,
                    YoutubeExtractor.YOUTUBE_VIDEO_QUALITY_HD_720,
                    YoutubeExtractor.YOUTUBE_VIDEO_QUALITY_MEDIUM_360,
                    YoutubeExtractor.YOUTUBE_VIDEO_QUALITY_SMALL_240);
            // change quality order depending on user setting
            ytEx.setPreferredVideoQualities(ytQual);
            ytEx.startExtracting(new YoutubeExtractor.YoutubeExtractorListener() {
                @Override
                public void onSuccess(YoutubeExtractor.YoutubeExtractorResult result) {
                    Log.d("PVL YT EX", "yt thumb: " + result.getDefaultThumbUri());
                    Log.d("PVL YT EX", "yt url: " + result.getVideoUri());
                    mService.mSessionManager.add(result.getVideoUri(), thumb.copy(thumb.getConfig(), true),
                            "video/mp4", null, title, subtitle);
                }

                @Override
                public void onFailure(Error error) {
                    Log.d("PVL YT EX", "failure: " + error);
                    // add toast notification
                }
            });
        }

        else {
            // else pass unprocessed and pray to Luna it's a valid link
            mService.mSessionManager.add(Uri.parse(url), thumb.copy(thumb.getConfig(), true),
                    "video/mp4", null, title, subtitle);
        }
    }

    private void playCon(Convention con, Convention.Video video, Bitmap thumb){
        if (null == thumb){
            // thumb = show.thumb // no bitmap on Show, resolve with picasso target and continue on load?
        }
        if (null == thumb){ // still null? use pvl logo
            thumb = mService.defaultArtwork;
        }

        playVideo(video.name, con.name, video.webUrl, thumb);
    }

    private void playShow(final Show show, final Episode episode, Bitmap thumb) {

        if (null == thumb){
            // thumb = show.thumb // no bitmap on Show, resolve with picasso target and continue on load?
        }
        if (null == thumb){ // still null? use pvl logo
            thumb = mService.defaultArtwork;
        }

        playVideo(episode.title, show.name, episode.webUrl, thumb);

    }



    private void playStation(Station station, Bitmap thumb) {
        if (null == mService) return;
        Log.d(TAG, "playStation called: " + station.name);

        if (null == thumb){
            thumb = mService.defaultArtwork;
        }

        mService.mSessionManager.add(Uri.parse(station.streamUrl), thumb.copy(thumb.getConfig(), true),
                "video/mp4", null, station.name, station.genre);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mLayout != null &&
                (mLayout.getPanelState() == PanelState.EXPANDED || mLayout.getPanelState() == PanelState.ANCHORED)) {
            mLayout.setPanelState(PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    private void loadFragment(final Fragment fragment) {
        final FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        //transaction.replace(R.id.main_fragment_frame, fragment);
        transaction.add(R.id.main_fragment_frame, fragment);
        //transaction.addToBackStack(fragment.getClass().getName());
        transaction.addToBackStack(null).commit();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.d(TAG, "nav: " + id);

        if (id == R.id.nav_stations) {
            Log.d(TAG, "nav_stations");
            Fragment fragment = new StationListFragment();
            loadFragment(fragment);

        } else if (id == R.id.nav_schedule) {

        } else if (id == R.id.nav_shows) {
            Log.d(TAG, "nav_shows");
            Fragment fragment = new ShowListFragment();
            loadFragment(fragment);

        } else if (id == R.id.nav_conventions) {
            Log.d(TAG, "nav_conventions");
            Fragment fragment = new ConventionListFragment();
            loadFragment(fragment);

        } else if (id == R.id.nav_songs) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean handleEpisodeSelected(Show show, Episode episode, Bitmap thumb) {
        playShow(show, episode, thumb);
        return true;
    }

    public boolean handleStationSelected(Station station, Bitmap thumb) {
        // handling to add default stream right now
        // will implement detail view later
        // and change this function to 'handleStreamSelected'
        Log.d("PVL", "clicked: " + station.name);
        playStation(station, thumb);
        return true;
    }

    public boolean handleStationsLoaded(Station[] stations, boolean explicit) {
        loadStreams(stations, explicit);
        return true;
    }

    public boolean handleShowSelected(Show show) {
        ShowDetailFragment fragment = ShowDetailFragment.newInstance(show);
        loadFragment(fragment);
        return true;
    }

    public boolean handleConventionSelected(Convention con) {
        ConventionDetailFragment fragment = ConventionDetailFragment.newInstance(con);
        loadFragment(fragment);
        return true;
    }

    public boolean handleVideoSelected(Convention con, Convention.Video video, Bitmap thumb){
        playCon(con, video, thumb);
        return true;
    }

    public void hideSplash(boolean animate) {
        if (splashDismissed) return;
        int mShortAnimationDuration = 500;
        final ImageView splashView = (ImageView) this.findViewById(R.id.imgSplash);
        splashView.setClickable(false);
        if (animate) {
            AnimatorSet set = new AnimatorSet();
            set.play(ObjectAnimator.ofFloat(splashView, "alpha", 1f, 0f));
            set.setDuration(mShortAnimationDuration);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    splashView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    splashView.setVisibility(View.GONE);
                }
            });
            set.start();
        } else {
            splashView.setVisibility(View.GONE);
        }

        getSupportActionBar().show();
        splashDismissed = true;
    }

    public static final class DiscoveryFragment extends MediaRouteDiscoveryFragment {
        private static final String TAG = "DiscoveryFragment";
        private MediaRouter.Callback mCallback;

        public DiscoveryFragment() {
            mCallback = null;
        }

        public void setCallback(MediaRouter.Callback cb) {
            Log.d(TAG, "callback set");
            mCallback = cb;
        }

        @Override
        public MediaRouter.Callback onCreateCallback() {
            return mCallback;
        }

        @Override
        public int onPrepareCallbackFlags() {
            // Add the CALLBACK_FLAG_UNFILTERED_EVENTS flag to ensure that we will
            // observe and log all route events including those that are for routes
            // that do not match our selector.  This is only for demonstration purposes
            // and should not be needed by most applications.
            return super.onPrepareCallbackFlags()
                    | MediaRouter.CALLBACK_FLAG_UNFILTERED_EVENTS;
        }
    }

    private class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.SERVICE_SETUP)) {
                Log.d(TAG, "service setup broadcast received");
                continueCreate();
            }
        }
    }

    private final class PlaylistAdapter extends ArrayAdapter<PlaylistItem> {
        public PlaylistAdapter() {

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View v;
            if (convertView == null) {
                v = getLayoutInflater().inflate(R.layout.layout_playlist_item, parent, false);
            } else {
                v = convertView;
            }
            final PlaylistItem item = getItem(position);

            TextView textView = (TextView) v.findViewById(R.id.title);
            TextView subtextView = (TextView) v.findViewById(R.id.subtitle);
            textView.setText(item.getTitle());
            subtextView.setText(item.getSubtitle());

            ImageView iconView = (ImageView) v.findViewById(R.id.icon);
            iconView.setImageBitmap(item.getThumb());

            ImageButton remBtn = (ImageButton) v.findViewById(R.id.playlist_remove);
            remBtn.setTag(item);
            remBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item != null) {
                        mService.mSessionManager.remove(item.getItemId());
                    }
                }
            });
            return v;
        }

        @Override
        public long getItemId(final int position) {
            return getItem(position).hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    private class PlaylistOnItemLongClickListener implements AdapterView.OnItemLongClickListener {

        private final DynamicListView mListView;

        PlaylistOnItemLongClickListener(final DynamicListView listView) {
            mListView = listView;
        }

        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            Log.d(TAG, "on click: " + position);
            if (mListView != null) {
                mListView.startDragging(position - mListView.getHeaderViewsCount());
            }
            return true;
        }
    }

    private class PlaylistOnDismissCallback implements OnDismissCallback {

        private final ArrayAdapter<PlaylistItem> mAdapter;

        PlaylistOnDismissCallback(final ArrayAdapter<PlaylistItem> adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onDismiss(final ViewGroup listView, final int[] reverseSortedPositions) {
            for (int position : reverseSortedPositions) {
                PlaylistItem item = mAdapter.getItem(position);
                mService.mSessionManager.remove(item.getItemId());
                mAdapter.remove(position);
            }
        }
    }

    private class PlaylistOnItemMovedListener implements OnItemMovedListener {

        private final ArrayAdapter<PlaylistItem> mAdapter;

        PlaylistOnItemMovedListener(final ArrayAdapter<PlaylistItem> adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onItemMoved(final int originalPosition, final int newPosition) {
            // implement update to media session!

        }
    }

    private class PlaylistOnItemClickListener implements AdapterView.OnItemClickListener {

        private final ArrayAdapter<PlaylistItem> mAdapter;

        PlaylistOnItemClickListener(final ArrayAdapter<PlaylistItem> adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            Log.d(TAG, "on click: " + position);
        }
    }
}
