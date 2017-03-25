package com.darsh.couponstracker.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.darsh.couponstracker.R;
import com.darsh.couponstracker.controller.util.Constants;
import com.darsh.couponstracker.controller.util.Utilities;
import com.darsh.couponstracker.data.database.CouponContract;
import com.darsh.couponstracker.data.model.Coupon;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.ui.fragment.CouponFragment;

import java.util.ArrayList;

/**
 * Created by darshan on 20/3/17.
 */

class CouponRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final int MAX_WIDGET_ROWS_LIMIT = 5;
    private static final int NUM_VIEW_TYPES = 2;

    private Context context;
    private ArrayList<Coupon> coupons;

    private boolean isTablet = false;

    CouponRemoteViewsFactory(Context context) {
        DebugLog.logMessage("CouponRemoteViewsFactory");
        this.context = context;
        this.coupons = new ArrayList<>();
        this.isTablet = context.getResources().getBoolean(R.bool.is_tablet);
    }

    @Override
    public void onCreate() {
        DebugLog.logMethod();
    }

    @Override
    public void onDataSetChanged() {
        DebugLog.logMethod();
        Cursor cursor = context.getContentResolver().query(
                CouponContract.CouponTable.URI,
                CouponContract.CouponTable.PROJECTION,
                CouponContract.CouponTable.COLUMN_VALID_UNTIL + " >= ?",
                new String[]{ String.valueOf(Utilities.getLongDateToday()) },
                CouponContract.CouponTable.COLUMN_VALID_UNTIL
        );
        if (cursor == null) {
            return;
        }

        coupons.clear();
        int count = Math.min(MAX_WIDGET_ROWS_LIMIT + 1, cursor.getCount());
        int l = count == MAX_WIDGET_ROWS_LIMIT + 1 ? count - 1 : count;
        for (int i = 0; i < l; i++) {
            cursor.moveToNext();
            Coupon coupon = new Coupon();
            coupon.id = cursor.getLong(CouponContract.CouponTable.POSITION_ID);
            coupon.merchant = cursor.getString(CouponContract.CouponTable.POSITION_MERCHANT);
            coupon.category = cursor.getString(CouponContract.CouponTable.POSITION_CATEGORY);
            coupon.validUntil = cursor.getLong(CouponContract.CouponTable.POSITION_VALID_UNTIL);
            coupon.couponCode = cursor.getString(CouponContract.CouponTable.POSITION_COUPON_CODE);
            coupon.description = cursor.getString(CouponContract.CouponTable.POSITION_DESCRIPTION);
            coupon.state = cursor.getInt(CouponContract.CouponTable.POSITION_COUPON_STATE);
            coupons.add(coupon);
        }
        if (count == 6) {
            coupons.add(Coupon.getDummy());
        }
        cursor.close();
    }

    @Override
    public void onDestroy() {
        DebugLog.logMethod();
    }

    @Override
    public int getCount() {
        if (coupons == null) {
            return 0;
        }
        return coupons.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == MAX_WIDGET_ROWS_LIMIT) {
            return showWidgetItemViewAll();
        }

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_item_coupon);
        remoteViews.setTextViewText(R.id.text_view_merchant, coupons.get(position).merchant);
        remoteViews.setTextViewText(R.id.text_view_category, coupons.get(position).category);
        remoteViews.setTextViewText(R.id.text_view_valid_until, Utilities.getStringDate(coupons.get(position).validUntil));
        remoteViews.setTextViewText(R.id.text_view_coupon_code, coupons.get(position).couponCode);

        int colorId = R.color.material_orange_900;
        String couponStateText = context.getString(R.string.coupon_state_available);
        if (coupons.get(position).state == 1) {
            colorId = R.color.material_green_700;
            couponStateText = context.getString(R.string.coupon_state_used);
        }
        remoteViews.setTextColor(R.id.text_view_coupon_state, ContextCompat.getColor(context.getApplicationContext(), colorId));
        remoteViews.setTextViewText(R.id.text_view_coupon_state, couponStateText);

        Bundle extras = new Bundle();
        extras.putInt(Constants.COUPON_FRAGMENT_MODE, CouponFragment.Mode.VIEW);
        extras.putParcelable(Constants.COUPON_PARCELABLE, coupons.get(position));
        if (!isTablet) {
            extras.putStringArrayList(Constants.BUNDLE_EXTRA_MERCHANT_SUGGESTIONS, new ArrayList<String>());
            extras.putStringArrayList(Constants.BUNDLE_EXTRA_CATEGORY_SUGGESTIONS, new ArrayList<String>());
        } else {
            extras.putBoolean(Constants.BUNDLE_EXTRA_LOAD_COUPON_FRAGMENT, true);
        }

        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        remoteViews.setOnClickFillInIntent(R.id.widget_row, fillInIntent);

        return remoteViews;
    }

    private RemoteViews showWidgetItemViewAll() {
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(Constants.BUNDLE_EXTRA_VIEW_ALL, true);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_item_view_all);
        remoteViews.setOnClickFillInIntent(R.id.widget_row, fillInIntent);
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return NUM_VIEW_TYPES;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
