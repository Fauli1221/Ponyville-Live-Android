package com.ponyvillelive.pvlmobile.fragments;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ponyvillelive.pvlmobile.R;
import com.ponyvillelive.pvlmobile.model.Episode;
import com.ponyvillelive.pvlmobile.model.Show;
import com.ponyvillelive.pvlmobile.model.net.ObjectResponse;
import com.ponyvillelive.pvlmobile.net.PonyvilleAPI;
import com.ponyvillelive.pvlmobile.ui.EpisodeAdapter;
import com.ponyvillelive.pvlmobile.ui.PreCachingLayoutManager;
import com.ponyvillelive.pvlmobile.ui.RecyclerItemClickListener;
import com.ponyvillelive.pvlmobile.util.Constants;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tinker on 29/02/16.
 */
public class ShowDetailFragment extends Fragment {

    private static final String DETAIL_KEY = "Princess Luna is best pony"; // silly tinker, just move this to Constants already!

    RecyclerView listView;
    private Show mShow;
    private PVLFragmentListener listener;
    private EpisodeAdapter adapter;
    private PreCachingLayoutManager layoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout errorLayout;
    private ImageView errorImage;

    public ShowDetailFragment() {
    }

    public static ShowDetailFragment newInstance(Show show) {
        ShowDetailFragment fragment = new ShowDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_KEY, show);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.mShow = getArguments().getParcelable(DETAIL_KEY);
        }

        if (savedInstanceState != null) {
            mShow = savedInstanceState.getParcelable(Constants.BUNDLE_RECYCLER_DETAIL);
            Episode[] episodes = mShow.episodes.toArray(new Episode[mShow.episodes.size()]);
            adapter = new EpisodeAdapter(getContext());
            adapter.setItems(episodes);
            adapter.notifyDataSetChanged();
        } else if (null == mShow) {
            // show error
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_detail, container, false);

        errorLayout = (LinearLayout) view.findViewById(R.id.error_layout);
        errorImage = (ImageView) view.findViewById(R.id.error_image);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_list);
        //mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getShowById(mShow.id);
            }
        });

        layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(layoutManager.getScreenHeight());

        listView = (RecyclerView) view.findViewById(android.R.id.list);

        if (null == adapter)
            adapter = new EpisodeAdapter(getContext());
        listView.setAdapter(adapter);

        if (null != mShow) {
            adapter.setItems(mShow.episodes.toArray(new Episode[mShow.episodes.size()]));
            adapter.notifyDataSetChanged();
        }

        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        listener.handleEpisodeSelected(mShow, adapter.getItem(position), adapter.getHolder(position).thumb);
                    }
                })
        );

        return view;
    }

    private void getShowById(String id) {
        Log.d("PVL", "loading show detail from ponyville Api: " + id);

        PonyvilleAPI ponyvilleApi = PonyvilleAPI.Builder.build(getActivity().getApplication());
        ponyvilleApi
                .getShow(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ObjectResponse<Show>>() {
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
                    public void onNext(ObjectResponse<Show> showResponse) {
                        Log.d("PVL", "apiSub onNext");
                        mShow = showResponse.result;
                        errorImage.setImageBitmap(null);
                        errorLayout.setVisibility(View.GONE);
                        Episode[] episodes = mShow.episodes.toArray(new Episode[mShow.episodes.size()]);
                        adapter.setItems(episodes);
                        adapter.notifyDataSetChanged();
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

        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(Constants.BUNDLE_RECYCLER_LAYOUT);
            listView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.BUNDLE_RECYCLER_LAYOUT, listView.getLayoutManager().onSaveInstanceState());
        outState.putParcelable(Constants.BUNDLE_RECYCLER_DETAIL, mShow);
    }
}
