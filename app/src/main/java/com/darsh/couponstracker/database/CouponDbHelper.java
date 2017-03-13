package com.darsh.couponstracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by darshan on 13/3/17.
 */

public class CouponDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "CouponsTracker.db";
    private static final int DB_VERSION = 1;

    CouponDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_COUPON_TABLE = "CREATE TABLE " + CouponContract.Coupon.TABLE_NAME
                + " ("
                + CouponContract.Coupon.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CouponContract.Coupon.COLUMN_MERCHANT + " TEXT NOT NULL, "
                + CouponContract.Coupon.COLUMN_CATEGORY + " TEXT NOT NULL DEFAULT 'general', "
                + CouponContract.Coupon.COLUMN_VALID_UNTIL + " INTEGER NOT NULL, "
                + CouponContract.Coupon.COLUMN_COUPON_CODE + " TEXT NOT NULL DEFAULT 'No code required', "
                + CouponContract.Coupon.COLUMN_DESCRIPTION + " TEXT NOT NULL DEFAULT 'Coupon description not set.', "
                + CouponContract.Coupon.COLUMN_COUPON_STATE + " INTEGER NOT NULL DEFAULT 0"
                + ");";
        db.execSQL(CREATE_COUPON_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
