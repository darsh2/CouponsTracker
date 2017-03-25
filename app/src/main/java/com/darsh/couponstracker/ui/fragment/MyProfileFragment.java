package com.darsh.couponstracker.ui.fragment;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.darsh.couponstracker.R;
import com.darsh.couponstracker.controller.event.DataUpdateEvent;
import com.darsh.couponstracker.controller.util.Constants;
import com.darsh.couponstracker.controller.util.Utilities;
import com.darsh.couponstracker.data.database.CouponContract;
import com.darsh.couponstracker.data.model.Coupon;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.ui.adapter.CouponListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
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
 * Created by darshan on 19/3/17.
 */

public class MyProfileFragment extends Fragment {
    public static final String TAG = "MyProfileFragment";

    private Unbinder unbinder;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.text_view_coupons_used)
    TextView textViewCouponsUsed;

    @BindView(R.id.text_view_coupons_currently_available)
    TextView textViewCouponsCurrentlyAvailable;
    private int numCouponsCurrentlyAvailable;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.text_view_error)
    TextView errorTextView;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private CouponListAdapter couponListAdapter;
    private ArrayList<Coupon> usedCoupons;

    private int listPosition;

    private CompositeDisposable compositeDisposable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        unbinder = ButterKnife.bind(this, view);
        compositeDisposable = new CompositeDisposable();
        EventBus.getDefault().register(this);

        restoreSavedInstanceState(savedInstanceState);
        initViews();

        return view;
    }

    private void restoreSavedInstanceState(Bundle savedInstanceState) {
        DebugLog.logMethod();
        if (savedInstanceState == null) {
            DebugLog.logMessage("savedInstanceState is null");
            return;
        }
        numCouponsCurrentlyAvailable = savedInstanceState.getInt(Constants.BUNDLE_EXTRA_NUM_COUPONS_AVAILABLE);
        usedCoupons = savedInstanceState.getParcelableArrayList(Constants.BUNDLE_EXTRA_COUPONS);
        listPosition = Math.max(0, savedInstanceState.getInt(Constants.BUNDLE_EXTRA_LIST_POSITION));
    }

    private void initViews() {
        DebugLog.logMethod();
        initToolbar();
        initRecyclerView();
    }

    private void initToolbar() {
        DebugLog.logMethod();
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.menu_item_my_profile));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initRecyclerView() {
        DebugLog.logMethod();
        couponListAdapter = new CouponListAdapter(getActivity());
        recyclerView.setAdapter(couponListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (usedCoupons == null) {
            getUsedCoupons();
        } else {
            updateView();
            if (usedCoupons.size() > listPosition) {
                recyclerView.scrollToPosition(listPosition);
            } else {
                recyclerView.scrollToPosition(0);
            }
        }
    }

    @Subscribe
    public void onDataUpdateEvent(DataUpdateEvent event) {
        DebugLog.logMethod();
        getUsedCoupons();
    }

    private void getUsedCoupons() {
        DebugLog.logMethod();
        showLoadingView();

        Single<Boolean> usedCouponsSingle = Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                DebugLog.logMessage("usedCouponsSingle - call");
                getUsedCouponsFromDb();
                getNumCouponsCurrentlyAvailable();
                return true;
            }
        });
        usedCouponsSingle.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
        DisposableSingleObserver<Boolean> disposableSingleObserver = usedCouponsSingle.subscribeWith(new DisposableSingleObserver<Boolean>() {
            @Override
            public void onSuccess(Boolean value) {
                DebugLog.logMessage("usedCouponsSingle - onSuccess");
                updateView();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                DebugLog.logMessage("usedCouponsSingle - o");
                DebugLog.logMessage(e.getMessage());
                showErrorView();
            }
        });
        compositeDisposable.add(disposableSingleObserver);
    }

    private void getUsedCouponsFromDb() {
        DebugLog.logMethod();
        if (usedCoupons == null) {
            usedCoupons = new ArrayList<>();
        } else {
            usedCoupons.clear();
        }

        Cursor cursor = getContext().getContentResolver().query(
                CouponContract.CouponTable.URI,
                CouponContract.CouponTable.PROJECTION,
                CouponContract.CouponTable.COLUMN_COUPON_STATE + " = ?",
                new String[]{ "1" },
                CouponContract.CouponTable.COLUMN_VALID_UNTIL + " DESC"
        );
        if (cursor == null) {
            throw new SQLException("Null cursor");
        }

        while (cursor.moveToNext()) {
            Coupon coupon = new Coupon();
            coupon.id = cursor.getLong(CouponContract.CouponTable.POSITION_ID);
            coupon.merchant = cursor.getString(CouponContract.CouponTable.POSITION_MERCHANT);
            coupon.category = cursor.getString(CouponContract.CouponTable.POSITION_CATEGORY);
            coupon.validUntil = cursor.getLong(CouponContract.CouponTable.POSITION_VALID_UNTIL);
            coupon.couponCode = cursor.getString(CouponContract.CouponTable.POSITION_COUPON_CODE);
            coupon.description = cursor.getString(CouponContract.CouponTable.POSITION_DESCRIPTION);
            coupon.state = cursor.getInt(CouponContract.CouponTable.POSITION_COUPON_STATE);
            usedCoupons.add(coupon);
        }
        cursor.close();
    }

    private void getNumCouponsCurrentlyAvailable() {
        DebugLog.logMethod();
        Cursor cursor = getContext().getContentResolver().query(
                CouponContract.CouponTable.URI,
                CouponContract.CouponTable.PROJECTION,
                CouponContract.CouponTable.COLUMN_VALID_UNTIL + " >= ?"
                + " AND " + CouponContract.CouponTable.COLUMN_COUPON_STATE + " = ?",
                new String[]{
                        String.valueOf(Utilities.getLongDateToday()),
                        "0"
                },
                null
        );
        if (cursor == null) {
            numCouponsCurrentlyAvailable = -1;
            return;
        }
        numCouponsCurrentlyAvailable = cursor.getCount();
        cursor.close();
    }

    private void showLoadingView() {
        DebugLog.logMethod();
        progressBar.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void updateView() {
        DebugLog.logMethod();
        textViewCouponsUsed.setText(getString(R.string.my_profile_coupons_used) + " " + usedCoupons.size());
        textViewCouponsCurrentlyAvailable.setText(
                getString(R.string.my_profile_coupons_currently_available)
                        + " "
                        + (numCouponsCurrentlyAvailable == -1 ? "Error" : numCouponsCurrentlyAvailable)
        );

        progressBar.setVisibility(View.INVISIBLE);
        if (usedCoupons.size() == 0) {
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setText(getString(R.string.my_profile_no_coupons_used));
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            errorTextView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            couponListAdapter.updateCoupons(usedCoupons);
        }
    }

    private void showErrorView() {
        DebugLog.logMethod();
        textViewCouponsUsed.setVisibility(View.INVISIBLE);
        textViewCouponsCurrentlyAvailable.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        errorTextView.setText(getString(R.string.error_loading_coupons));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        DebugLog.logMethod();
        outState.putInt(Constants.BUNDLE_EXTRA_NUM_COUPONS_AVAILABLE, numCouponsCurrentlyAvailable);
        if (usedCoupons != null) {
            outState.putParcelableArrayList(Constants.BUNDLE_EXTRA_COUPONS, usedCoupons);
        }
        outState.putInt(
                Constants.BUNDLE_EXTRA_LIST_POSITION,
                ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition()
        );
    }

    @Override
    public void onDestroyView() {
        DebugLog.logMethod();
        EventBus.getDefault().unregister(this);
        couponListAdapter.releaseResources();
        compositeDisposable.clear();
        unbinder.unbind();
        super.onDestroyView();
    }
}
