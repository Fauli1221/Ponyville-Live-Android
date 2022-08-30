package com.ponyvillelive.pvlmobile.fragments;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ponyvillelive.pvlmobile.R;
import com.ponyvillelive.pvlmobile.model.NowPlayingMeta;
import com.ponyvillelive.pvlmobile.model.net.MapResponse;
import com.ponyvillelive.pvlmobile.net.PonyvilleAPI;
import com.ponyvillelive.pvlmobile.ui.NowPlayingAdapter;
import com.ponyvillelive.pvlmobile.ui.PreCachingLayoutManager;
import com.ponyvillelive.pvlmobile.ui.RecyclerItemClickListener;
import com.ponyvillelive.pvlmobile.util.Constants;

import java.util.Collection;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class StationListFragment extends Fragment {

    private static final String DETAIL_KEY = "Listening to PrinceWhateverer - Frailty";

    RecyclerView listView;

    private String                     stationType;
    private PVLFragmentListener listener;
    //private StationAdapter adapter;
    private NowPlayingAdapter adapter;
    private PreCachingLayoutManager layoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout errorLayout;
    private ImageView errorImage;

    public StationListFragment() {
    }

    public static StationListFragment newInstance(String stationType) {
        StationListFragment fragment = new StationListFragment();
        Bundle args = new Bundle();
        args.putString(DETAIL_KEY, stationType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.stationType = getArguments().getString(DETAIL_KEY, "audio");
        }

        if(savedInstanceState != null)
        {
            //Station[] stations = (Station[])savedInstanceState.getParcelableArray(Constants.BUNDLE_RECYCLER_LIST);
            NowPlayingMeta[] stations = (NowPlayingMeta[])savedInstanceState.getParcelableArray(Constants.BUNDLE_RECYCLER_LIST);
            //adapter = new StationAdapter(getContext());
            adapter = new NowPlayingAdapter(getContext());
            adapter.setItems(stations);
            adapter.notifyDataSetChanged();
        }
        else {
            getStationByCategory(stationType);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_station, container, false);

        errorLayout = (LinearLayout) view.findViewById(R.id.error_layout);
        errorImage = (ImageView) view.findViewById(R.id.error_image);


        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.station_list_layout);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getStationByCategory(stationType);
            }
        });

        layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(layoutManager.getScreenHeight());

        listView = (RecyclerView) view.findViewById(android.R.id.list);

        if (null == adapter)
            adapter = new NowPlayingAdapter(getContext());
        listView.setAdapter(adapter);

        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        listener.handleStationSelected(adapter.getItem(position).station, adapter.getHolder(position).thumb);
                    }
                })
        );

        return view;
    }

    private void getStationByCategory(String type){

        if (type == null){
            type = "audio";
        }

        Log.d("PVL", "loading stations from ponyvilleApi");

        PonyvilleAPI ponyvilleApi = PonyvilleAPI.Builder.build(getActivity().getApplication());
        ponyvilleApi
                //.getStationList(type)
                .getNowPlaying()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MapResponse<String, NowPlayingMeta>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("PVL", "apiSub error " + e);
                        errorImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.azura_error));
                        errorLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNext(MapResponse<String, NowPlayingMeta> stationArrayResponse) {
                        Log.d("PVL", "apiSub onNext");

                        Collection<NowPlayingMeta> result = stationArrayResponse.result.values();
                        NowPlayingMeta[] stations = result.toArray(new NowPlayingMeta[result.size()]);
                        errorImage.setImageBitmap(null);
                        errorLayout.setVisibility(View.GONE);
                        adapter.setItems(stations);
                        adapter.notifyDataSetChanged();
                        //listener.handleStationsLoaded(stations, false);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof PVLFragmentListener)) {
            throw new RuntimeException("Activities must implement StationFragmentListener");
        } else {
            listener = (PVLFragmentListener) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }



    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if(savedInstanceState != null)
        {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(Constants.BUNDLE_RECYCLER_LAYOUT);
            listView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.BUNDLE_RECYCLER_LAYOUT, listView.getLayoutManager().onSaveInstanceState());
        //outState.putParcelableArray(Constants.BUNDLE_RECYCLER_LIST, adapter.getItems() );
    }
}
