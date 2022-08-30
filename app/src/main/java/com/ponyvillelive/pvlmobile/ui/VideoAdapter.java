package com.ponyvillelive.pvlmobile.ui;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ponyvillelive.pvlmobile.R;
import com.ponyvillelive.pvlmobile.model.Convention;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tinker on 2/03/16.
 */
public class VideoAdapter extends RecyclerView.Adapter<BaseHolder> {


    private final Context mContext;
    private List<Convention.Video> mList;
    private BaseHolder[] mHolders;

    public VideoAdapter(Context context) {
        this.mContext = context;
        this.mList = new ArrayList<>();
    }

    public void setItems(Convention.Video[] videos){
        this.mList = Arrays.asList(videos);
        this.mHolders = new BaseHolder[mList.size()];
    }

    public Convention.Video[] getItems(){
        return mList.toArray(new Convention.Video[mList.size()]);
    }
    public Convention.Video getItem(int position){
        return mList.get(position);
    }
    public BaseHolder getHolder(int position) { return mHolders[position]; }

    @Override
    public void onBindViewHolder(final BaseHolder holder, final int position) {
        Convention.Video video = mList.get(position);

        holder.title.setText(video.name);
        holder.subtitle.setText(video.description);

        if (null != video.thumbnailUrl && (video.thumbnailUrl.toString()).trim().length() > 1){
            Picasso.with(mContext).load(video.thumbnailUrl.toString())
                    .placeholder(R.drawable.pvl_logo)
                    .into(holder.target);
        }
        mHolders[position] = holder;

    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.view_video_list_item, viewGroup, false);

        return new BaseHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
