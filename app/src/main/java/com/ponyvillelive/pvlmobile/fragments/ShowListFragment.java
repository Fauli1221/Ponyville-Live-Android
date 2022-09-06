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
import com.ponyvillelive.pvlmobile.model.Show;
import com.ponyvillelive.pvlmobile.model.net.ArrayResponse;
import com.ponyvillelive.pvlmobile.net.PonyvilleAPI;
import com.ponyvillelive.pvlmobile.ui.PreCachingLayoutManager;
import com.ponyvillelive.pvlmobile.ui.RecyclerItemClickListener;
import com.ponyvillelive.pvlmobile.ui.ShowAdapter;
import com.ponyvillelive.pvlmobile.util.Constants;

import java.util.Arrays;
import java.util.Comparator;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tinker on 29/02/16.
 */
public class ShowListFragment extends Fragment {

    RecyclerView listView;

    private PVLFragmentListener listener;
    private ShowAdapter adapter;
    private PreCachingLayoutManager layoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout errorLayout;
    private ImageView errorImage;

    public ShowListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SHOWS", "onCreate");

        if (savedInstanceState != null) {
            Show[] shows = (Show[]) savedInstanceState.getParcelableArray(Constants.BUNDLE_RECYCLER_LIST);
            adapter = new ShowAdapter(getContext());
            adapter.setItems(shows);
            adapter.notifyDataSetChanged();
        } else {
            getShows();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_list, container, false);
        Log.d("SHOWS", "onCreateView");

        errorLayout = view.findViewById(R.id.error_layout);
        errorImage = view.findViewById(R.id.error_image);

        mSwipeRefreshLayout = view.findViewById(R.id.refresh_list);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getShows();
            }
        });

        layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(layoutManager.getScreenHeight());

        listView = view.findViewById(android.R.id.list);

        if (null == adapter)
            adapter = new ShowAdapter(getContext());
        listView.setAdapter(adapter);

        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        listener.handleShowSelected(adapter.getItem(position));
                    }
                })
        );

        return view;
    }

    private void getShows() {

        Log.d("PVL", "loading shows from ponyvilleApi");

        PonyvilleAPI ponyvilleApi = PonyvilleAPI.Builder.build(getActivity().getApplication());
        ponyvilleApi
                .getAllShows()
                        //.getShows() // currently latest is returning junk -> look into this.
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayResponse<Show>>() {
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
                    public void onNext(ArrayResponse<Show> arrayResponse) {
                        Show[] shows = arrayResponse.result;
                        Log.d("PVL", "shows: " + shows.length);
                        // sort alphabetically (instead of latest)
                        // make this an UI option
                        Arrays.sort(shows, new Comparator<Show>() {
                            @Override
                            public int compare(Show lhs, Show rhs) {
                                if (null == lhs.name) return -1;
                                if (null == rhs.name) return 1;
                                return lhs.name.compareTo(rhs.name);
                            }
                        });
                        errorImage.setImageBitmap(null);
                        errorLayout.setVisibility(View.GONE);
                        adapter.setItems(shows);
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
        Log.d("SHOWS", "onViewStateRestored");
        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(Constants.BUNDLE_RECYCLER_LAYOUT);
            listView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("PVL", "saving show list");
        outState.putParcelable(Constants.BUNDLE_RECYCLER_LAYOUT, listView.getLayoutManager().onSaveInstanceState());
        outState.putParcelableArray(Constants.BUNDLE_RECYCLER_LIST, adapter.getItems());
    }
}