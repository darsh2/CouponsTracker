package com.darsh.couponstracker;

import android.content.ContentValues;

import com.darsh.couponstracker.data.database.CouponContract;

import java.util.Random;

/**
 * Created by darshan on 13/3/17.
 */

public class TestUtilities {
    private static Random random;

    static {
        random = new Random(System.currentTimeMillis());
    }

    private static String[] MERCHANTS = { "Amazon", "Ola", "Uber", "Vodafone", "BigBasket" };

    private static String[] CATEGORIES = { "Utilities", "Transport", "Provision", "Medical", "Banking", "Recharge" };

    static long[] VALID_UNTIL = { 1490140799, 1514744999, 1490659199, 1490227199, 1489390353 };

    private static long[] VALID_UNTIL_RANDOM = new long[6];
    static {
        long time = System.currentTimeMillis();
        for (int i = 0; i < VALID_UNTIL_RANDOM.length; i++) {
            VALID_UNTIL_RANDOM[i] = time + random.nextLong();
        }
    }

    private static String[] COUPON_CODES = { "CC1", "CC2", "CC3", "CC4", "CC5" };

    private static String[] DESCRIPTION = { "Free rides", "Extra talktime", "50% cashbacks", "Buy 1 get 1 free", "Free premium trial" };

    static ContentValues getCompleteCoupon() {
        Random random = new Random(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(CouponContract.CouponTable.COLUMN_MERCHANT, MERCHANTS[random.nextInt(MERCHANTS.length)]);
        contentValues.put(CouponContract.CouponTable.COLUMN_CATEGORY, CATEGORIES[random.nextInt(CATEGORIES.length)]);
        contentValues.put(CouponContract.CouponTable.COLUMN_VALID_UNTIL, VALID_UNTIL_RANDOM[random.nextInt(VALID_UNTIL_RANDOM.length)]);
        contentValues.put(CouponContract.CouponTable.COLUMN_COUPON_CODE, COUPON_CODES[random.nextInt(COUPON_CODES.length)]);
        contentValues.put(CouponContract.CouponTable.COLUMN_DESCRIPTION, DESCRIPTION[random.nextInt(DESCRIPTION.length)]);
        return contentValues;
    }
}
