package com.darsh.couponstracker.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.darsh.couponstracker.event.DataUpdateEvent;
import com.darsh.couponstracker.logger.DebugLog;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by darshan on 13/3/17.
 */

public class CouponProvider extends ContentProvider {
    private CouponDbHelper couponDbHelper;

    private static final int COUPON = 100;
    private static final int COUPON_WITH_ID = 101;

    public static UriMatcher uriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(CouponContract.AUTHORITY, CouponContract.PATH_COUPON, COUPON);
        matcher.addURI(CouponContract.AUTHORITY, CouponContract.PATH_COUPON_WITH_ID, COUPON_WITH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        couponDbHelper = new CouponDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        DebugLog.logMethod();
        DebugLog.logMessage("Uri " + uri.toString());

        Cursor cursor;
        SQLiteDatabase readableDatabase = couponDbHelper.getReadableDatabase();
        switch (uriMatcher.match(uri)) {
            case COUPON: {
                DebugLog.logMessage("COUPON");
                cursor = readableDatabase.query(
                        CouponContract.CouponTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case COUPON_WITH_ID: {
                DebugLog.logMessage("COUPON_WITH_ID");
                cursor = readableDatabase.query(
                        CouponContract.CouponTable.TABLE_NAME,
                        projection,
                        CouponContract.CouponTable.COLUMN_ID + " = ?",
                        new String[]{ CouponContract.CouponTable.getCouponIdFromUri(uri) },
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default: {
                throw new UnsupportedOperationException("Unknown URI:" + uri);
            }
        }

        if (getContext() != null && getContext().getContentResolver() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        DebugLog.logMethod();
        DebugLog.logMessage("Uri " + uri.toString());

        Uri returnUri;
        SQLiteDatabase writableDatabase = couponDbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case COUPON: {
                DebugLog.logMessage("COUPON");
                long rowId = writableDatabase.insert(
                        CouponContract.CouponTable.TABLE_NAME,
                        null,
                        values
                );
                returnUri = CouponContract.CouponTable.makeUriForCoupon(rowId);
                break;
            }

            default: {
                throw new UnsupportedOperationException("Unknown URI:" + uri);
            }
        }

        EventBus.getDefault().post(new DataUpdateEvent());
        if (getContext() != null && getContext().getContentResolver() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        DebugLog.logMethod();
        DebugLog.logMessage("Uri " + uri.toString());
        final SQLiteDatabase writableDatabase = couponDbHelper.getWritableDatabase();
        int numRowsDeleted;

        // To delete all rows and get count of number of rows deleted
        if (null == selection) {
            selection = "1";
        }
        switch (uriMatcher.match(uri)) {
            case COUPON: {
                DebugLog.logMessage("COUPON");
                numRowsDeleted = writableDatabase.delete(
                        CouponContract.CouponTable.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }

            case COUPON_WITH_ID: {
                DebugLog.logMessage("COUPON_WITH_ID");
                String couponId = CouponContract.CouponTable.getCouponIdFromUri(uri);
                numRowsDeleted = writableDatabase.delete(
                        CouponContract.CouponTable.TABLE_NAME,
                        CouponContract.CouponTable.COLUMN_ID + " = ?",
                        new String[]{ couponId }
                );
                break;
            }

            default: {
                throw new UnsupportedOperationException("Unknown URI:" + uri);
            }
        }

        EventBus.getDefault().post(new DataUpdateEvent());
        if (getContext() != null && getContext().getContentResolver() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        DebugLog.logMethod();
        DebugLog.logMessage("Uri: " + uri);

        int numRowsAffected = 0;
        SQLiteDatabase writableDatabase = couponDbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case COUPON_WITH_ID: {
                DebugLog.logMessage("COUPON_WITH_ID");
                String couponId = CouponContract.CouponTable.getCouponIdFromUri(uri);
                numRowsAffected = writableDatabase.update(
                        CouponContract.CouponTable.TABLE_NAME,
                        values,
                        CouponContract.CouponTable.COLUMN_ID + " = ?",
                        new String[]{ couponId }
                );
                break;
            }

            default: {
            }
        }

        EventBus.getDefault().post(new DataUpdateEvent());
        return numRowsAffected;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        DebugLog.logMethod();
        DebugLog.logMessage("Uri " + uri.toString());

        final SQLiteDatabase writableDatabase = couponDbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case COUPON: {
                DebugLog.logMessage("COUPON");
                writableDatabase.beginTransaction();
                int numCouponsInserted = 0;
                try {
                    long rowId;
                    for (int i = 0, l = values.length; i < l; i++) {
                        rowId = writableDatabase.insert(
                                CouponContract.CouponTable.TABLE_NAME,
                                null,
                                values[i]
                        );
                        if (rowId != -1) {
                            numCouponsInserted++;
                        }
                    }
                    writableDatabase.setTransactionSuccessful();
                } finally {
                    writableDatabase.endTransaction();
                }
                EventBus.getDefault().post(new DataUpdateEvent());
                if (getContext() != null) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return numCouponsInserted;
            }

            default: {
                return super.bulkInsert(uri, values);
            }
        }
    }

    @Override
    public void shutdown() {
        DebugLog.logMethod();
        couponDbHelper.close();
        super.shutdown();
    }
}