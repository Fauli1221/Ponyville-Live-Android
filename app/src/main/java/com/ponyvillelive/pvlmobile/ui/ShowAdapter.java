package com.ponyvillelive.pvlmobile.ui;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ponyvillelive.pvlmobile.R;
import com.ponyvillelive.pvlmobile.model.Show;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tinker on 29/02/16.
 */
public class ShowAdapter extends RecyclerView.Adapter<BaseHolder> {


    private final Context mContext;
    private List<Show> showList;

    public ShowAdapter(Context context) {
        this.mContext = context;
        this.showList = new ArrayList<>();
    }

    public void setItems(Show[] shows){
        this.showList = Arrays.asList(shows);
    }

    public Show[] getItems(){
        return showList.toArray(new Show[showList.size()]);
    }

    public Show getItem(int position){
        return showList.get(position);
    }

    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        Show show = showList.get(position);

        holder.title.setText(show.name);
        holder.subtitle.setText(show.description);

        Picasso.with(mContext).load(show.imageUrl)
                .placeholder(R.drawable.pvl_logo)
                .into(holder.target);
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.view_show_list_item, viewGroup, false);

        return new BaseHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return showList.size();
    }

}

