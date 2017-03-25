package com.darsh.couponstracker.ui.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import com.darsh.couponstracker.controller.util.Utilities;
import com.darsh.couponstracker.data.database.CouponContract;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.ui.adapter.CouponListCursorAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by darshan on 23/3/17.
 */

public class NotificationCouponListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = "NotificationCouponListFragment";

    private static final int CURSOR_LOADER = 201;

    Unbinder unbinder;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.text_view_info)
    TextView textView;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private CouponListCursorAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_coupon_list, container, false);
        unbinder = ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.coupons_expiring_today));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (getActivity() != null) {
            getActivity().getSupportLoaderManager().initLoader(CURSOR_LOADER, null, this);
        }

        adapter = new CouponListCursorAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onDestroyView() {
        if (getActivity() != null) {
            getActivity().getSupportLoaderManager().destroyLoader(CURSOR_LOADER);
        }
        adapter.releaseResources();
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        DebugLog.logMethod();
        progressBar.setVisibility(View.VISIBLE);
        return new CursorLoader(
                getContext(),
                CouponContract.CouponTable.URI,
                CouponContract.CouponTable.PROJECTION,
                CouponContract.CouponTable.COLUMN_VALID_UNTIL + " = ?",
                new String[]{ String.valueOf(Utilities.getLongDateToday()) },
                CouponContract.CouponTable.COLUMN_VALID_UNTIL
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        DebugLog.logMethod();
        progressBar.setVisibility(View.INVISIBLE);
        if (data.getCount() == 0) {
            recyclerView.setVisibility(View.INVISIBLE);
            textView.setText(getString(R.string.no_coupons_expire_today));
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        DebugLog.logMethod();
        adapter.setCursor(null);
    }
}
