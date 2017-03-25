package com.darsh.couponstracker;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import com.darsh.couponstracker.data.database.CouponContract;
import com.darsh.couponstracker.data.database.CouponProvider;
import com.darsh.couponstracker.data.model.Coupon;
import com.darsh.couponstracker.logger.DebugLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by darshan on 13/3/17.
 */

@RunWith(AndroidJUnit4.class)
public class CouponTableProviderTest extends ProviderTestCase2<CouponProvider> {
    public CouponTableProviderTest() {
        super(CouponProvider.class, CouponContract.AUTHORITY);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
    }

    @Test
    public void couponBulkInsertTest() {
        int numCouponsToInsert = 6;
        ContentValues[] contentValuesArray = new ContentValues[numCouponsToInsert];
        for (int i = 0; i < numCouponsToInsert; i++) {
            contentValuesArray[i] = TestUtilities.getCompleteCoupon();
        }
        int numCouponsInserted = getMockContentResolver().bulkInsert(CouponContract.CouponTable.URI, contentValuesArray);
        assertEquals(numCouponsToInsert, numCouponsInserted);
    }

    @Test
    public void getCouponExpiringBeforeDateTest() {
        int numCouponsToInsert = TestUtilities.VALID_UNTIL.length;
        ContentValues[] contentValuesArray = new ContentValues[numCouponsToInsert];
        for (int i = 0; i < numCouponsToInsert; i++) {
            contentValuesArray[i] = TestUtilities.getCompleteCoupon();
            contentValuesArray[i].put(CouponContract.CouponTable.COLUMN_VALID_UNTIL, TestUtilities.VALID_UNTIL[i]);
        }
        getMockContentResolver().bulkInsert(CouponContract.CouponTable.URI, contentValuesArray);

        Cursor cursor = getMockContentResolver().query(
                CouponContract.CouponTable.URI,
                CouponContract.CouponTable.PROJECTION,
                CouponContract.CouponTable.COLUMN_VALID_UNTIL + " < ?",
                new String[]{ String.valueOf(1490227199) },
                CouponContract.CouponTable.COLUMN_VALID_UNTIL
        );
        if (cursor == null || cursor.getCount() == 0) {
            fail("Cursor was null or count is 0");
        }
        long prev = -1;
        while (cursor.moveToNext()) {
            Coupon coupon = new Coupon(
                    cursor.getLong(CouponContract.CouponTable.POSITION_ID),
                    cursor.getString(CouponContract.CouponTable.POSITION_MERCHANT),
                    cursor.getString(CouponContract.CouponTable.POSITION_CATEGORY),
                    cursor.getLong(CouponContract.CouponTable.POSITION_VALID_UNTIL),
                    cursor.getString(CouponContract.CouponTable.POSITION_COUPON_CODE),
                    cursor.getString(CouponContract.CouponTable.POSITION_DESCRIPTION),
                    cursor.getInt(CouponContract.CouponTable.POSITION_COUPON_STATE)
            );
            DebugLog.logMessage(coupon.toString());
            if (coupon.validUntil < prev) {
                fail("Valid until exceeds limit");
            }
            prev = coupon.validUntil;
        }
        cursor.close();
    }

    @Test
    public void testCouponsToJson() {
        ArrayList<Coupon> coupons = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            coupons.add(Coupon.getDummy());
        }
        Gson gson = new Gson();
        String json = gson.toJson(coupons);
        assertEquals(json != null && json.length() > 0, true);
    }

    @Test
    public void testCouponsFromJson() {
        String json = "[{\"category\":\"-9223372036854775808\",\"couponCode\":\"-9223372036854775808\",\"description\":\"-9223372036854775808\",\"id\":-9223372036854775808,\"merchant\":\"-9223372036854775808\",\"state\":0,\"validUntil\":-9223372036854775808},{\"category\":\"-9223372036854775808\",\"couponCode\":\"-9223372036854775808\",\"description\":\"-9223372036854775808\",\"id\":-9223372036854775808,\"merchant\":\"-9223372036854775808\",\"state\":0,\"validUntil\":-9223372036854775808},{\"category\":\"-9223372036854775808\",\"couponCode\":\"-9223372036854775808\",\"description\":\"-9223372036854775808\",\"id\":-9223372036854775808,\"merchant\":\"-9223372036854775808\",\"state\":0,\"validUntil\":-9223372036854775808},{\"category\":\"-9223372036854775808\",\"couponCode\":\"-9223372036854775808\",\"description\":\"-9223372036854775808\",\"id\":-9223372036854775808,\"merchant\":\"-9223372036854775808\",\"state\":0,\"validUntil\":-9223372036854775808}]";
        Gson gson = new Gson();
        Type collectionType = new TypeToken<ArrayList<Coupon>>(){}.getType();
        ArrayList<Coupon> coupons = gson.fromJson(json, collectionType);
        assertEquals(coupons != null && coupons.size() == 4, true);
    }
}
