package com.darsh.couponstracker.controller.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.darsh.couponstracker.ui.activity.SettingsActivity;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.ui.widget.CouponWidgetProvider;

import java.util.Calendar;

/**
 * Created by darshan on 14/3/17.
 */

public class Utilities {
    private static final int YEAR_START_INDEX = 0;
    private static final int MONTH_START_INDEX = 4;
    private static final int DAY_OF_MONTH_START_INDEX = 6;

    private static final String[] MONTHS = new String[]{
            "",
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    public static long getLongDateToday() {
        Calendar calendar = Calendar.getInstance();
        return getLongDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    /**
     * <p>First converts the given date to the format YYYYMMdd, ie, if year = 2017,
     * month = 2, dayOfMonth = 3, generates a String as "20170203". This string
     * is then parsed as a long to get a long of value 20170203.
     *
     * <p>This method is chosen to represent dates after taking into the following
     * considerations:
     *
     * <ul>
     * <li>First considered of using date and time for coupon validity. However it
     * would become too tedious just to add a new coupon. Besides most offers last
     * till the end of the day.</li>
     *
     * <li>Generate UTC date from year, month and dayOfMonth and then save the date
     * as a long in database. This method provides for easy ordering of dates as well.
     * However the drawback of this method is that if current time of adding coupon
     * is 23 March, 2017 3:45pm and coupons are retrieved at 23 March, 2017 4:00pm.
     * In this scenario it would say that the coupon is no longer available to use.</li>
     *
     * <li>Generate UTC date from year, month and dayOfMonth as YYYY MM dd 23:59:59 so
     * the previous method drawback is overcome. Say at the time of adding coupon, time
     * zone is IST and {@link com.darsh.couponstracker.data.model.Coupon#validUntil Coupon.validUntil}
     * is set as 2017 03 23 23:59:59. Converting this to UTC results in 2017 03 23 18:29:59.
     * Assume device time zone is changed to 'America/Los Angeles'. This translates to
     * validUntil being set as 2017 03 23 11:29:59 which again leads to the drawback of
     * the previous method.</li>
     *
     * <li>Saving the date in the form YYYY-MM-dd as a text in database and then sorting
     * using sqlite's datetime(date) avoids all the problems discussed above but leads to
     * slower queries due to the call required to datetime(). Sorting the text as is without
     * using sqlite's inbuilt datetime() is another alternative but sorting text is again
     * a performance constraint.</li>
     * </ul>
     *
     * <p>Taking into consideration all the above factors, it is decided to represent date
     * as YYYYMMdd and then save the long value of it in database. Sorting by date is also
     * easy as it is ordering on an integer.
     *
     * @param year Year
     * @param month Month
     * @param dayOfMonth Day of month
     * @return Returns the long value of the String representation of the date
     */
    public static long getLongDate(int year, int month, int dayOfMonth) {
        DebugLog.logMethod();
        String date = String.valueOf(year)
                + (month < 10 ? '0' + String.valueOf(month) : String.valueOf(month))
                + (dayOfMonth < 10 ? '0' + String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth));
        return Long.parseLong(date);
    }

    /**
     * Returns date of form long value YYYYMMdd as a String dd MMM, YYYY.
     * Ex: 20170323 gets converted to 23 Mar, 2017
     * @param longDate long value representing date as YYYYMMdd
     */
    public static String getStringDate(long longDate) {
        DebugLog.logMethod();
        String date = String.valueOf(longDate);
        return date.substring(DAY_OF_MONTH_START_INDEX) + " "
                + MONTHS[Integer.parseInt(date.substring(MONTH_START_INDEX, DAY_OF_MONTH_START_INDEX))] + ", "
                + date.substring(YEAR_START_INDEX, MONTH_START_INDEX);
    }

    public static boolean isCouponExpired(long validUntil) {
        return validUntil < getLongDateToday();
    }

    public static Bitmap getBitmap(Context context, int resourceId) {
        return BitmapFactory.decodeResource(
                context.getResources(),
                resourceId
        );
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void updateAppWidget(Context context) {
        DebugLog.logMethod();
        context = context.getApplicationContext();
        Intent intent = new Intent(context, CouponWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(
                        context,
                        CouponWidgetProvider.class
                ));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        context.sendBroadcast(intent);
    }

    /**
     * Sets app widget and daily notification alarms if this is the first time
     * the app is being launched.
     */
    public static void setAlarmsOnFirstLaunch(Context context) {
        DebugLog.logMethod();
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (!sharedPreferences.getBoolean(Constants.IS_FIRST_LAUNCH, true)) {
            return;
        }
        AppWidgetAlarmManager.setAlarm(context);
        NotificationAlarmManager.setAlarm(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.IS_FIRST_LAUNCH, false);
        editor.apply();
    }

    /**
     * Updates the default {@link SharedPreferences} with information regarding state of sync
     * task and the sync mode. This is necessary because if user pressed home button and then
     * relaunches app by clicking app icon, app has to be aware if any sync task is still in
     * progress.
     * @param context Application context
     * @param isSyncRunning Boolean that states if sync task still running
     * @param syncMode Integer indicating
     */
    public static synchronized void updateSharedPreferences(Context context, boolean isSyncRunning, int syncMode) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.IS_SYNC_RUNNING, isSyncRunning);
        editor.putInt(Constants.SYNC_MODE, syncMode);
        editor.apply();
    }

    /**
     * If home button was pressed when Google Drive sync is in progress and
     * the app is relaunched by clicking it's icon, this method will open
     * {@link SettingsActivity} if Drive sync is still in progress.
     */
    public static void navigateIfSyncInProgress(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context.getApplicationContext());
        if (sharedPreferences.getBoolean(Constants.IS_SYNC_RUNNING, false)) {
            Intent intent = new Intent(context, SettingsActivity.class);
            context.startActivity(intent);
        }
    }
}
