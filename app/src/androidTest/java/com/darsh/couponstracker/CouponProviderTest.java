package com.darsh.couponstracker;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import com.darsh.couponstracker.database.CouponContract;
import com.darsh.couponstracker.database.CouponProvider;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.model.Coupon;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by darshan on 13/3/17.
 */

@RunWith(AndroidJUnit4.class)
public class CouponProviderTest extends ProviderTestCase2<CouponProvider> {
    public CouponProviderTest() {
        super(CouponProvider.class, CouponContract.AUTHORITY);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
    }

    @Test
    public void minimalCouponInsertTest() {

    }

    @Test
    public void invalidCouponInsertTest() {

    }

    @Test
    public void couponBulkInsertTest() {
        int numCouponsToInsert = 6;
        ContentValues[] contentValuesArray = new ContentValues[numCouponsToInsert];
        for (int i = 0; i < numCouponsToInsert; i++) {
            contentValuesArray[i] = TestUtilities.getCompleteCoupon();
        }
        int numCouponsInserted = getMockContentResolver().bulkInsert(CouponContract.Coupon.URI, contentValuesArray);
        assertEquals(numCouponsToInsert, numCouponsInserted);
    }

    @Test
    public void getCouponExpiringBeforeDateTest() {
        int numCouponsToInsert = TestUtilities.VALID_UNTIL.length;
        ContentValues[] contentValuesArray = new ContentValues[numCouponsToInsert];
        for (int i = 0; i < numCouponsToInsert; i++) {
            contentValuesArray[i] = TestUtilities.getCompleteCoupon();
            contentValuesArray[i].put(CouponContract.Coupon.COLUMN_VALID_UNTIL, TestUtilities.VALID_UNTIL[i]);
        }
        getMockContentResolver().bulkInsert(CouponContract.Coupon.URI, contentValuesArray);

        Cursor cursor = getMockContentResolver().query(
                CouponContract.Coupon.URI,
                CouponContract.Coupon.PROJECTION,
                CouponContract.Coupon.COLUMN_VALID_UNTIL + " < ?",
                new String[]{ String.valueOf(1490227199) },
                CouponContract.Coupon.COLUMN_VALID_UNTIL
        );
        if (cursor == null || cursor.getCount() == 0) {
            fail("Cursor was null or count is 0");
        }
        long prev = -1;
        while (cursor.moveToNext()) {
            Coupon coupon = new Coupon(
                    cursor.getLong(CouponContract.Coupon.POSITION_ID),
                    cursor.getString(CouponContract.Coupon.POSITION_MERCHANT),
                    cursor.getString(CouponContract.Coupon.POSITION_CATEGORY),
                    cursor.getLong(CouponContract.Coupon.POSITION_VALID_UNTIL),
                    cursor.getString(CouponContract.Coupon.POSITION_COUPON_CODE),
                    cursor.getString(CouponContract.Coupon.POSITION_DESCRIPTION),
                    cursor.getInt(CouponContract.Coupon.POSITION_COUPON_STATE)
            );
            DebugLog.logMessage(coupon.toString());
            if (coupon.validUntil < prev) {
                fail("Valid until exceeds limit");
            }
            prev = coupon.validUntil;
        }
        cursor.close();
    }
}
