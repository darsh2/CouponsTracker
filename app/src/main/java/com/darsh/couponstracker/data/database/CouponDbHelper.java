package com.darsh.couponstracker.data.database;

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
        String CREATE_COUPON_TABLE = "CREATE TABLE " + CouponContract.CouponTable.TABLE_NAME
                + " ("
                + CouponContract.CouponTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CouponContract.CouponTable.COLUMN_MERCHANT + " TEXT NOT NULL, "
                + CouponContract.CouponTable.COLUMN_CATEGORY + " TEXT NOT NULL, "
                + CouponContract.CouponTable.COLUMN_VALID_UNTIL + " INTEGER NOT NULL, "
                + CouponContract.CouponTable.COLUMN_COUPON_CODE + " TEXT NOT NULL, "
                + CouponContract.CouponTable.COLUMN_DESCRIPTION + " TEXT NOT NULL, "
                + CouponContract.CouponTable.COLUMN_COUPON_STATE + " INTEGER NOT NULL"
                + ");";
        db.execSQL(CREATE_COUPON_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
