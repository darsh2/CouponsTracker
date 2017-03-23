package com.darsh.couponstracker.data.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.darsh.couponstracker.data.database.CouponContract;

/**
 * Created by darshan on 13/3/17.
 */

public class Coupon implements Parcelable {
    public long id;
    public String merchant;
    public String category;
    public long validUntil;
    public String couponCode;
    public String description;
    public int state;

    public static final long DUMMY_LONG = Long.MIN_VALUE;
    public static final String DUMMY_STRING = String.valueOf(DUMMY_LONG);

    public static Coupon getDummy() {
        return new Coupon(
                DUMMY_LONG, DUMMY_STRING, DUMMY_STRING,
                DUMMY_LONG, DUMMY_STRING, DUMMY_STRING,
                0
        );
    }

    public static Coupon getCoupon(Cursor cursor) {
        Coupon coupon = new Coupon();
        coupon.id = cursor.getLong(CouponContract.CouponTable.POSITION_ID);
        coupon.merchant = cursor.getString(CouponContract.CouponTable.POSITION_MERCHANT);
        coupon.category = cursor.getString(CouponContract.CouponTable.POSITION_CATEGORY);
        coupon.validUntil = cursor.getLong(CouponContract.CouponTable.POSITION_VALID_UNTIL);
        coupon.couponCode = cursor.getString(CouponContract.CouponTable.POSITION_COUPON_CODE);
        coupon.description = cursor.getString(CouponContract.CouponTable.POSITION_DESCRIPTION);
        coupon.state = cursor.getInt(CouponContract.CouponTable.POSITION_COUPON_STATE);
        return coupon;
    }

    public static ContentValues getContentValues(Coupon coupon) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CouponContract.CouponTable.COLUMN_MERCHANT, coupon.merchant);
        contentValues.put(CouponContract.CouponTable.COLUMN_CATEGORY, coupon.category);
        contentValues.put(CouponContract.CouponTable.COLUMN_VALID_UNTIL, coupon.validUntil);
        contentValues.put(CouponContract.CouponTable.COLUMN_COUPON_CODE, coupon.couponCode);
        contentValues.put(CouponContract.CouponTable.COLUMN_DESCRIPTION, coupon.description);
        contentValues.put(CouponContract.CouponTable.COLUMN_COUPON_STATE, coupon.state);
        return contentValues;
    }

    public Coupon() {}

    public Coupon(long id, String merchant, String category,
                  long validUntil, String couponCode, String description,
                  int state) {
        this.id = id;
        this.merchant = merchant;
        this.category = category;
        this.validUntil = validUntil;
        this.couponCode = couponCode;
        this.description = description;
        this.state = state;
    }

    private Coupon(Parcel parcel) {
        this.id = parcel.readLong();
        this.merchant = parcel.readString();
        this.category = parcel.readString();
        this.validUntil = parcel.readLong();
        this.couponCode = parcel.readString();
        this.description = parcel.readString();
        this.state = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(merchant);
        dest.writeString(category);
        dest.writeLong(validUntil);
        dest.writeString(couponCode);
        dest.writeString(description);
        dest.writeInt(state);
    }

    public static final Parcelable.Creator<Coupon> CREATOR = new Creator<Coupon>() {
        @Override
        public Coupon createFromParcel(Parcel source) {
            return new Coupon(source);
        }

        @Override
        public Coupon[] newArray(int size) {
            return new Coupon[size];
        }
    };

    @Override
    public String toString() {
        return "Coupon { " +
                "id = " + id +
                ", merchant = " + merchant +
                ", category = " + category +
                ", validUntil = " + validUntil +
                ", couponCode = " + couponCode +
                ", description = " + description +
                ", state = " + state +
                " }";
    }
}
