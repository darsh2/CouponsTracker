package com.darsh.couponstracker.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by darshan on 12/3/17.
 */

public final class CouponContract {
    public static final String AUTHORITY = "com.darsh.couponstracker";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    static final String PATH_COUPON = "coupon";
    static final String PATH_COUPON_WITH_ID = "coupon/*";

    private CouponContract() {
    }

    public static final class Coupon implements BaseColumns {
        public static final Uri URI = BASE_URI.buildUpon().appendPath(PATH_COUPON).build();

        static final String TABLE_NAME = "coupon";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_MERCHANT = "merchant";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_VALID_UNTIL = "valid_until";
        public static final String COLUMN_COUPON_CODE = "coupon_code";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_COUPON_STATE = "coupon_state";

        public static final int POSITION_ID = 0;
        public static final int POSITION_MERCHANT = 1;
        public static final int POSITION_CATEGORY = 2;
        public static final int POSITION_VALID_UNTIL = 3;
        public static final int POSITION_COUPON_CODE = 4;
        public static final int POSITION_DESCRIPTION = 5;
        public static final int POSITION_COUPON_STATE = 6;


        public static final String[] PROJECTION = new String[]{
                COLUMN_ID,
                COLUMN_MERCHANT,
                COLUMN_CATEGORY,
                COLUMN_VALID_UNTIL,
                COLUMN_COUPON_CODE,
                COLUMN_DESCRIPTION,
                COLUMN_COUPON_STATE
        };

        public static Uri makeUriForCoupon(long id) {
            return URI.buildUpon().appendPath(String.valueOf(id)).build();
        }

        static String getCouponIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }
    }
}
