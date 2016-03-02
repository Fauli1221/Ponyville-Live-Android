package com.ponyvillelive.pvlmobile.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * Created by tinker on 30/01/16.
 */
public class PreCachingLayoutManager extends LinearLayoutManager {
    private static final int DEFAULT_EXTRA_LAYOUT_SPACE = 600;
    private int extraLayoutSpace = -1;
    private Context context;

    public PreCachingLayoutManager(Context context) {
        super(context);
        this.context = context;
    }

    public PreCachingLayoutManager(Context context, int extraLayoutSpace) {
        super(context);
        this.context = context;
        this.extraLayoutSpace = extraLayoutSpace;
    }

    public PreCachingLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        this.context = context;
    }

    public void setExtraLayoutSpace(int extraLayoutSpace) {
        this.extraLayoutSpace = extraLayoutSpace;
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        if (extraLayoutSpace > 0) {
            return extraLayoutSpace;
        }
        return DEFAULT_EXTRA_LAYOUT_SPACE;
    }

    // lazy to put this somewhere appropriate... ~ tinker
    public int getScreenHeight(){
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        double screenWidthInPixels = (double)config.screenWidthDp * dm.density;
        double screenHeightInPixels = screenWidthInPixels * dm.heightPixels / dm.widthPixels;
        return (int)(screenHeightInPixels + .5);
    }
}