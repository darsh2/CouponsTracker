package com.darsh.couponstracker.controller.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by darshan on 14/3/17.
 */

public class Constants {
    public static final String IS_FIRST_LAUNCH = "is_first_launch";

    public static final String BUNDLE_EXTRA_COUPONS = "bundle_extra_coupons";
    public static final String BUNDLE_EXTRA_LIST_POSITION = "bundle_extra_list_position";
    public static final String BUNDLE_EXTRA_MERCHANT_SUGGESTIONS = "bundle_extra_merchant_suggestions";
    public static final String BUNDLE_EXTRA_CATEGORY_SUGGESTIONS = "bundle_extra_category_suggestions";

    public static final String BUNDLE_EXTRA_NUM_COUPONS_AVAILABLE = "num_coupons_available";

    public static final String COUPON_FRAGMENT_MODE = "coupon_fragment_mode";
    public static final String COUPON_PARCELABLE = "coupon_parcelable";

    public static final String DATE_PICKER_SHOWING = "date_picker_showing";
    public static final String DATE_PICKER_YEAR = "date_picker_year";
    public static final String DATE_PICKER_MONTH = "date_picker_month";
    public static final String DATE_PICKER_DAY_OF_MONTH = "date_picker_day_of_month";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ FragmentType.MY_PROFILE_FRAGMENT, FragmentType.NOTIFICATION_FRAGMENT })
    public @interface FragmentType {
        /**
         * Integer constant that is used to denote
         * {@link com.darsh.couponstracker.ui.fragment.MyProfileFragment
         * MyProfileFragment}
         */
        int MY_PROFILE_FRAGMENT = 2001;

        /**
         * Integer constant that is used to denote
         * {@link com.darsh.couponstracker.ui.fragment.NotificationCouponListFragment
         * NotificationCouponListFragment}
         */
        int NOTIFICATION_FRAGMENT = 2002;
    }

    public static final String BUNDLE_EXTRA_FRAGMENT_TYPE = "bundle_extra_fragment_type";
    public static final String BUNDLE_EXTRA_LOAD_COUPON_FRAGMENT = "bundle_extra_load_coupon_fragment";
    public static final String BUNDLE_EXTRA_VIEW_ALL = "bundle_extra_view_all";

    /*
    Include package name for custom actions since they are broadcast system-wide and
    have to be unique to avoid collisions.
     */
    public static final String ACTION_WIDGET_UPDATE = "com.darsh.couponstracker.ACTION_WIDGET_UPDATE";
    public static final String ACTION_SHOW_NOTIFICATION = "com.darsh.couponstracker.ACTION_SHOW_NOTIFICATION";

    public static final String ACTION_GOOGLE_DRIVE_SYNC = "com.darsh.couponstracker.ACTION_GOOGLE_DRIVE_SYNC";
    public static final int CONNECTION_RESOLUTION_REQUEST_CODE = 3001;

    public static final String IS_SYNC_RUNNING = "preference_is_sync_running";
    public static final String SYNC_MODE = "preference_sync_mode";
}
