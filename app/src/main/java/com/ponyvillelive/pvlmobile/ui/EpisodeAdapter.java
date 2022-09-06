package com.ponyvillelive.pvlmobile.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ponyvillelive.pvlmobile.R;
import com.ponyvillelive.pvlmobile.model.Episode;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tinker on 29/02/16.
 */
public class EpisodeAdapter extends RecyclerView.Adapter<BaseHolder> {


    private final Context mContext;
    private List<Episode> mList;
    private BaseHolder[] mHolders;

    public EpisodeAdapter(Context context) {
        this.mContext = context;
        this.mList = new ArrayList<>();
    }

    public void setItems(Episode[] episodes){
        this.mList = Arrays.asList(episodes);
        this.mHolders = new BaseHolder[mList.size()];
    }

    public Episode[] getItems(){
        return mList.toArray(new Episode[mList.size()]);
    }
    public Episode getItem(int position){
        return mList.get(position);
    }
    public BaseHolder getHolder(int position) { return mHolders[position]; }

    @Override
    public void onBindViewHolder(final BaseHolder holder, final int position) {
        Episode episode = mList.get(position);

        holder.title.setText(episode.title);
        holder.subtitle.setText(episode.summary);

        if (null != episode.thumbnailUrl && (episode.thumbnailUrl).trim().length() > 1){
            Picasso.with(mContext).load(episode.thumbnailUrl)
                    .placeholder(R.drawable.pvl_logo)
                    .into(holder.target);
        }
        // add show image possibility here before defaulting to pvl logo
        else {
            holder.icon.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_logo));
        }
        mHolders[position] = holder;

    }

    @NonNull
    @Override
    public BaseHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.view_episode_list_item, viewGroup, false);

        return new BaseHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
