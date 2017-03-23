package com.darsh.couponstracker.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.darsh.couponstracker.R;
import com.darsh.couponstracker.ui.adapter.CouponListAdapter;
import com.darsh.couponstracker.controller.event.DataUpdateEvent;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.data.model.Coupon;
import com.darsh.couponstracker.controller.util.Constants;
import com.darsh.couponstracker.controller.util.Utilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by darshan on 13/3/17.
 */

public abstract class CouponListFragment extends Fragment {
    Unbinder unbinder;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.text_view_info)
    TextView textView;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private CouponListAdapter couponListAdapter;
    private int listPosition;
    private ArrayList<Coupon> coupons;

    private CompositeDisposable compositeDisposable;

    private ArrayList<String> merchants;
    private ArrayList<String> categories;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coupon_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        compositeDisposable = new CompositeDisposable();

        EventBus.getDefault().register(this);

        DebugLog.logMethod();

        restoreSavedInstanceState(savedInstanceState);
        initViews();

        return view;
    }

    private void restoreSavedInstanceState(Bundle savedInstanceState) {
        DebugLog.logMethod();
        if (savedInstanceState == null) {
            DebugLog.logMessage("savedInstanceState null");
            merchants = new ArrayList<>();
            categories = new ArrayList<>();
            return;
        }

        DebugLog.logMessage("restoring savedInstanceState");
        coupons = savedInstanceState.getParcelableArrayList(Constants.BUNDLE_EXTRA_COUPONS);
        listPosition = Math.max(0, savedInstanceState.getInt(Constants.BUNDLE_EXTRA_LIST_POSITION));
        merchants = savedInstanceState.getStringArrayList(Constants.BUNDLE_EXTRA_MERCHANT_SUGGESTIONS);
        categories = savedInstanceState.getStringArrayList(Constants.BUNDLE_EXTRA_CATEGORY_SUGGESTIONS);
    }

    private void initViews() {
        DebugLog.logMethod();
        couponListAdapter = new CouponListAdapter(getActivity());
        recyclerView.setAdapter(couponListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (coupons == null) {
            // Loading coupons from db for the first time
            loadCoupons();
        } else {
            // Coupons have already been loaded, just update views
            couponListAdapter.updateCoupons(coupons);
            if (coupons.size() > listPosition) {
                recyclerView.scrollToPosition(listPosition);
            } else {
                recyclerView.scrollToPosition(0);
            }
            updateView();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataUpdateEvent(DataUpdateEvent event) {
        loadCoupons();
    }

    private void loadCoupons() {
        DebugLog.logMethod();
        showLoadingView();
        Single<Boolean> loadCouponsSingle = Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                DebugLog.logMessage("loadCouponsSingle - call");
                return loadCouponsFromDb();
            }
        });
        loadCouponsSingle.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
        DisposableSingleObserver<Boolean> disposableSingleObserver = loadCouponsSingle
                .subscribeWith(new DisposableSingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean value) {
                        DebugLog.logMethod();
                        DebugLog.logMessage("Success: " + value);
                        if (!value) {
                            updateErrorView();
                            return;
                        }
                        couponListAdapter.updateCoupons(coupons);
                        updateView();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        DebugLog.logMethod();
                        DebugLog.logMessage(e.getMessage());
                        updateErrorView();
                    }
                });
        compositeDisposable.add(disposableSingleObserver);
    }

    protected abstract boolean loadCouponsFromDb();

    protected void updateCoupons(ArrayList<Coupon> coupons) {
        DebugLog.logMethod();
        DebugLog.logMessage("Coupons: " + coupons.toString());
        if (this.coupons == null) {
            this.coupons = new ArrayList<>(coupons.size());
        } else {
            this.coupons.clear();
        }
        this.coupons.addAll(coupons);
    }

    private void showLoadingView() {
        DebugLog.logMethod();
        progressBar.setVisibility(View.VISIBLE);
        textView.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void updateView() {
        DebugLog.logMethod();
        progressBar.setVisibility(View.INVISIBLE);
        if (coupons == null || coupons.size() == 0) {
            textView.setText(getString(R.string.error_no_coupons));
            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);

        } else {
            textView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateErrorView() {
        DebugLog.logMethod();
        progressBar.setVisibility(View.INVISIBLE);
        if (coupons.size() == 0) {
            textView.setText(getString(R.string.error_loading_coupons));
            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            Utilities.showToast(getContext(), getString(R.string.error_loading_coupons));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        DebugLog.logMethod();
        if (coupons != null) {
            DebugLog.logMessage("Coupons: " + coupons.size());
            outState.putParcelableArrayList(Constants.BUNDLE_EXTRA_COUPONS, coupons);
        }
        outState.putInt(
                Constants.BUNDLE_EXTRA_LIST_POSITION,
                ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition()
        );
        outState.putStringArrayList(Constants.BUNDLE_EXTRA_MERCHANT_SUGGESTIONS, merchants);
        outState.putStringArrayList(Constants.BUNDLE_EXTRA_CATEGORY_SUGGESTIONS, categories);
    }

    @Override
    public void onDestroyView() {
        DebugLog.logMethod();
        EventBus.getDefault().unregister(this);
        compositeDisposable.dispose();
        if (couponListAdapter != null) {
            couponListAdapter.releaseResources();
        }
        recyclerView.setLayoutManager(null);
        coupons = null;
        unbinder.unbind();
        super.onDestroyView();
    }

    protected synchronized void addMerchants(HashSet<String> tempMerchants) {
        DebugLog.logMethod();
        for (int i = 0, l = merchants.size(); i < l; i++) {
            tempMerchants.add(merchants.get(i));
        }
        merchants.clear();
        merchants.addAll(tempMerchants);
    }

    public ArrayList<String> getMerchants() {
        DebugLog.logMethod();
        return merchants;
    }

    protected synchronized void addCategories(HashSet<String> tempCategories) {
        DebugLog.logMethod();
        for (int i = 0, l = categories.size(); i < l; i++) {
            tempCategories.add(categories.get(i));
        }
        categories.clear();
        categories.addAll(tempCategories);
    }

    public ArrayList<String> getCategories() {
        DebugLog.logMethod();
        return categories;
    }
}
