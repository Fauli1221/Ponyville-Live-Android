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
import com.ponyvillelive.pvlmobile.model.net.ArrayResponse;
import com.ponyvillelive.pvlmobile.net.PonyvilleAPI;
import com.ponyvillelive.pvlmobile.ui.ConventionAdapter;
import com.ponyvillelive.pvlmobile.ui.PreCachingLayoutManager;
import com.ponyvillelive.pvlmobile.ui.RecyclerItemClickListener;
import com.ponyvillelive.pvlmobile.util.Constants;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tinker on 2/03/16.
 */
public class ConventionListFragment extends Fragment {

    RecyclerView listView;

    private PVLFragmentListener listener;
    private ConventionAdapter adapter;
    private PreCachingLayoutManager layoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout errorLayout;
    private ImageView errorImage;

    public ConventionListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SHOWS", "onCreate");

        if (savedInstanceState != null) {
            Convention[] shows = (Convention[]) savedInstanceState.getParcelableArray(Constants.BUNDLE_RECYCLER_LIST);
            adapter = new ConventionAdapter(getContext());
            adapter.setItems(shows);
            adapter.notifyDataSetChanged();
        } else {
            getCons();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_convention_list, container, false);
        Log.d("CONS", "onCreateView");

        errorLayout = (LinearLayout) view.findViewById(R.id.error_layout);
        errorImage = (ImageView) view.findViewById(R.id.error_image);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_list);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCons();
            }
        });

        layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(layoutManager.getScreenHeight());

        listView = (RecyclerView) view.findViewById(android.R.id.list);

        if (null == adapter)
            adapter = new ConventionAdapter(getContext());
        listView.setAdapter(adapter);

        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        listView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        listener.handleConventionSelected(adapter.getItem(position));
                    }
                })
        );

        return view;
    }

    private void getCons() {

        Log.d("PVL", "loading shows from ponyvilleApi");

        PonyvilleAPI ponyvilleApi = PonyvilleAPI.Builder.build(getActivity().getApplication());
        ponyvilleApi
                .getConventionList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayResponse<Convention>>() {
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
                    public void onNext(ArrayResponse<Convention> arrayResponse) {
                        Convention[] cons = arrayResponse.result;
                        Log.d("PVL", "cons: " + cons.length);
                        errorImage.setImageBitmap(null);
                        errorLayout.setVisibility(View.GONE);
                        adapter.setItems(cons);
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