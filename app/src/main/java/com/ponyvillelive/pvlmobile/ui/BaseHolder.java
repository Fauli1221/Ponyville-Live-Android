package com.ponyvillelive.pvlmobile.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ponyvillelive.pvlmobile.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by tinker on 29/02/16.
 */
public class BaseHolder extends RecyclerView.ViewHolder {

    public ImageView icon;
    public TextView title;
    public TextView subtitle;
    public Bitmap thumb;


    public Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            icon.setImageBitmap(bitmap);
            thumb = bitmap;
            colourise(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };


    public CardView baseView;
    public Palette colourPalette;

    public BaseHolder(View itemView) {
        super(itemView);
        baseView = (CardView) itemView;
        title = (TextView) itemView.findViewById(R.id.title);
        subtitle = (TextView) itemView.findViewById(R.id.subtitle);
        icon = (ImageView) itemView.findViewById(R.id.icon);
    }

    public void colourise(Bitmap bitmap) {
        colourPalette = Palette.from(bitmap).generate();
        title.setTextColor(colourPalette.getVibrantColor(Color.BLACK));
        subtitle.setTextColor(colourPalette.getMutedColor(Color.DKGRAY));
    }
}