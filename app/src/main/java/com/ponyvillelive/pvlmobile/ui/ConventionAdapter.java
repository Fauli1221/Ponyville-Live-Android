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
public class ConventionAdapter extends RecyclerView.Adapter<BaseHolder> {


    private final Context mContext;
    private List<Convention> conList;

    public ConventionAdapter(Context context) {
        this.mContext = context;
        this.conList = new ArrayList<>();
    }

    public void setItems(Convention[] cons){
        this.conList = Arrays.asList(cons);
    }

    public Convention[] getItems(){
        return conList.toArray(new Convention[conList.size()]);
    }

    public Convention getItem(int position){
        return conList.get(position);
    }

    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        Convention con = conList.get(position);

        holder.title.setText(con.name);
        holder.subtitle.setText(con.startDate + " to " + con.endDate);

        Picasso.with(mContext).load(con.imageUrl)
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
        return conList.size();
    }

}
