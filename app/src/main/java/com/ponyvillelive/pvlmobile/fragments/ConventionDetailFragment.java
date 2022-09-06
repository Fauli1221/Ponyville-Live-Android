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
import com.ponyvillelive.pvlmobile.model.Convention;
import com.ponyvillelive.pvlmobile.model.net.ObjectResponse;
import com.ponyvillelive.pvlmobile.net.PonyvilleAPI;
import com.ponyvillelive.pvlmobile.ui.PreCachingLayoutManager;
import com.ponyvillelive.pvlmobile.ui.RecyclerItemClickListener;
import com.ponyvillelive.pvlmobile.ui.VideoAdapter;
import com.ponyvillelive.pvlmobile.util.Constants;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tinker on 2/03/16.
 */
public class ConventionDetailFragment extends Fragment {

    private static final String DETAIL_KEY = "Princess Luna is best pony"; // silly tinker, just move this to Constants already!

    RecyclerView listView;
    private Convention mCon;
    private PVLFragmentListener listener;
    private VideoAdapter adapter;
    private PreCachingLayoutManager layoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout errorLayout;
    private ImageView errorImage;

    public ConventionDetailFragment() {
    }

    public static ConventionDetailFragment newInstance(Convention con) {
        ConventionDetailFragment fragment = new ConventionDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_KEY, con);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.mCon = getArguments().getParcelable(DETAIL_KEY);
        }

        if (savedInstanceState != null) {
            mCon = savedInstanceState.getParcelable(Constants.BUNDLE_RECYCLER_DETAIL);
            Convention.Video[] videos = mCon.archives.videos.toArray(new Convention.Video[mCon.archives.videos.size()]);
            adapter = new VideoAdapter(getContext());
            adapter.setItems(videos);
            adapter.notifyDataSetChanged();
        } else if (null == mCon) {
            // show error
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_convention_detail, container, false);

        errorLayout = view.findViewById(R.id.error_layout);
        errorImage = view.findViewById(R.id.error_image);

        mSwipeRefreshLayout = view.findViewById(R.id.refresh_list);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getConventionById(mCon.id);
            }
        });

        layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(layoutManager.getScreenHeight());

        listView = view.findViewById(android.R.id.list);

        if (null == adapter)
            adapter = new VideoAdapter(getContext());
        listView.setAdapter(adapter);

        if (null != mCon) {
            getConventionById(mCon.id);
        }

        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        listener.handleVideoSelected(mCon, adapter.getItem(position), adapter.getHolder(position).thumb);
                    }
                })
        );

        return view;
    }

    private void getConventionById(String id) {
        Log.d("PVL", "loading con detail from ponyville Api: " + id);

        PonyvilleAPI ponyvilleApi = PonyvilleAPI.Builder.build(getActivity().getApplication());
        ponyvilleApi
                .getConvention(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ObjectResponse<Convention>>() {
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
                    public void onNext(ObjectResponse<Convention> showResponse) {
                        Log.d("PVL", "apiSub onNext");

                        mCon = showResponse.result;
                        Log.d("PLV", "received: " + mCon.name);

                        if (null == mCon.archives || null == mCon.archives.videos) {
                            // no attached videos to con, show Azura image
                            errorImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.azura_error));
                            errorLayout.setVisibility(View.VISIBLE);
                            mSwipeRefreshLayout.setRefreshing(false);
                        } else {
                            errorImage.setImageBitmap(null);
                            errorLayout.setVisibility(View.GONE);
                            Convention.Video[] videos = mCon.archives.videos.toArray(new Convention.Video[mCon.archives.videos.size()]);
                            adapter.setItems(videos);
                            adapter.notifyDataSetChanged();
                            mSwipeRefreshLayout.setRefreshing(false);
                        }


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
        outState.putParcelable(Constants.BUNDLE_RECYCLER_DETAIL, mCon);
    }
}