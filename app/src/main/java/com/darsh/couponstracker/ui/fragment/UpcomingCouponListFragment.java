package com.darsh.couponstracker.ui.fragment;

import android.database.Cursor;

import com.darsh.couponstracker.controller.util.Utilities;
import com.darsh.couponstracker.data.database.CouponContract;
import com.darsh.couponstracker.data.model.Coupon;
import com.darsh.couponstracker.logger.DebugLog;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by darshan on 15/3/17.
 */

public class UpcomingCouponListFragment extends CouponListFragment {
    public static final String TAG = "UpcomingCouponListFragment";

    @Override
    protected boolean loadCouponsFromDb() {
        DebugLog.logMethod();
        if (getContext() == null) {
            return false;
        }

        DebugLog.logMessage("Today: " + String.valueOf(Utilities.getLongDateToday()));

        Cursor cursor = getContext().getContentResolver().query(
                CouponContract.CouponTable.URI,
                CouponContract.CouponTable.PROJECTION,
                CouponContract.CouponTable.COLUMN_VALID_UNTIL + " >= ?",
                new String[]{ String.valueOf(Utilities.getLongDateToday()) },
                CouponContract.CouponTable.COLUMN_VALID_UNTIL
        );
        if (cursor == null) {
            return false;
        }

        HashSet<String> merchants = new HashSet<>();
        HashSet<String> categories = new HashSet<>();
        ArrayList<Coupon> coupons = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            Coupon coupon = new Coupon();
            coupon.id = cursor.getLong(CouponContract.CouponTable.POSITION_ID);
            coupon.merchant = cursor.getString(CouponContract.CouponTable.POSITION_MERCHANT);
            coupon.category = cursor.getString(CouponContract.CouponTable.POSITION_CATEGORY);
            coupon.validUntil = cursor.getLong(CouponContract.CouponTable.POSITION_VALID_UNTIL);
            coupon.couponCode = cursor.getString(CouponContract.CouponTable.POSITION_COUPON_CODE);
            coupon.description = cursor.getString(CouponContract.CouponTable.POSITION_DESCRIPTION);
            coupon.state = cursor.getInt(CouponContract.CouponTable.POSITION_COUPON_STATE);
            coupons.add(coupon);

            merchants.add(coupon.merchant);
            categories.add(coupon.category);
        }
        cursor.close();

        addMerchants(merchants);
        addCategories(categories);
        updateCoupons(coupons);
        return true;
    }
}
