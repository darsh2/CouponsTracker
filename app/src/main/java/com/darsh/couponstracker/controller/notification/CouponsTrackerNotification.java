package com.darsh.couponstracker.controller.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import com.darsh.couponstracker.R;
import com.darsh.couponstracker.controller.sync.GoogleDriveService;
import com.darsh.couponstracker.controller.util.Constants;
import com.darsh.couponstracker.controller.util.Utilities;
import com.darsh.couponstracker.data.database.CouponContract;
import com.darsh.couponstracker.data.model.Coupon;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.ui.activity.ContainerActivity;
import com.darsh.couponstracker.ui.activity.CouponListActivity;
import com.darsh.couponstracker.ui.fragment.SettingsFragment;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.os.Build.VERSION_CODES.N;

/**
 * Created by darshan on 23/3/17.
 */

public class CouponsTrackerNotification {
    private static final String TAG = "com.darsh.couponstracker.NOTIFICATION";
    private static final int SYNC_NOTIFICATION_ID = 5001;
    private static final int DAILY_NOTIFICATION_ID = 5002;

    private Context context;

    private String contentTitle;
    private String contentText;

    private int largeIconId;
    private int smallIconId = R.mipmap.ic_launcher;

    public CouponsTrackerNotification(Context context) {
        this.context = context;
    }

    private void setContentTitle(int syncMode, boolean isSuccess) {
        DebugLog.logMethod();
        if (Build.VERSION.SDK_INT < N) {
            contentTitle = context.getString(R.string.app_name);
            return;
        }
        if (syncMode == GoogleDriveService.SyncMode.EXPORT) {
            contentTitle = isSuccess ? context.getString(R.string.sync_export_title)
                    : context.getString(R.string.error_sync_export_title);
        } else {
            contentTitle = isSuccess ? context.getString(R.string.sync_import_title)
                    : context.getString(R.string.error_sync_import_title);
        }
    }

    private void setContentText(int syncMode, boolean isSuccess) {
        DebugLog.logMethod();
        if (syncMode == GoogleDriveService.SyncMode.EXPORT) {
            contentText = isSuccess ? context.getString(R.string.sync_export_text)
                    : context.getString(R.string.error_sync_export_text);
        } else {
            contentText = isSuccess ? context.getString(R.string.sync_import_text)
                    : context.getString(R.string.error_sync_import_text);
        }
    }

    public void showSyncSuccessNotification(int syncMode) {
        DebugLog.logMethod();
        setContentTitle(syncMode, true);
        setContentText(syncMode, true);
        largeIconId = R.drawable.ic_check_circle_green_24dp;
        showSyncNotification();
    }

    public void showSyncErrorNotification(int syncMode) {
        DebugLog.logMethod();
        setContentTitle(syncMode, false);
        setContentText(syncMode, false);
        largeIconId = R.drawable.ic_error_red_24dp;
        showSyncNotification();
    }

    private void showSyncNotification() {
        DebugLog.logMethod();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(Utilities.getBitmap(context, largeIconId))
                .setSmallIcon(smallIconId)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(new Intent(context, CouponListActivity.class));
        PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(contentPendingIntent);

        // Cancel any previously shown notification
        NotificationManagerCompat.from(context).cancel(TAG, SYNC_NOTIFICATION_ID);
        // Show notification
        NotificationManagerCompat.from(context).notify(
                TAG,
                SYNC_NOTIFICATION_ID,
                builder.build()
        );
    }

    public void showDailyNotification() {
        new GetCouponsExpiringTodayTask().execute();
    }

    private class GetCouponsExpiringTodayTask extends AsyncTask<Void, Void, HashMap<String, Integer>> {
        @Override
        protected HashMap<String, Integer> doInBackground(Void... params) {
            Cursor cursor = context.getContentResolver().query(
                    CouponContract.CouponTable.URI,
                    CouponContract.CouponTable.PROJECTION,
                    CouponContract.CouponTable.COLUMN_VALID_UNTIL + " = ?",
                    new String[]{ String.valueOf(Utilities.getLongDateToday()) },
                    CouponContract.CouponTable.COLUMN_VALID_UNTIL
            );
            if (cursor == null) {
                return null;
            }

            HashMap<String, Integer> merchantCouponCountMap = new HashMap<>();
            while (cursor.moveToNext()) {
                Coupon coupon = Coupon.getCoupon(cursor);
                int count = 1;
                if (merchantCouponCountMap.containsKey(coupon.merchant)) {
                    count += merchantCouponCountMap.get(coupon.merchant);
                }
                merchantCouponCountMap.put(coupon.merchant, count);
            }
            return merchantCouponCountMap;
        }

        @Override
        protected void onPostExecute(HashMap<String, Integer> stringIntegerHashMap) {
            if (stringIntegerHashMap == null) {
                return;
            }
            buildAndShowDailyNotification(stringIntegerHashMap);
        }
    }

    private void buildAndShowDailyNotification(HashMap<String, Integer> merchantCouponCountMap) {
        String contentTitle = context.getString(R.string.coupons_expiring_today);
        String baseContextText = "%d coupons from %d merchants";
        String baseNotificationLine = "%d coupons from %s";
        if (Build.VERSION.SDK_INT < N) {
            contentTitle = context.getString(R.string.app_name);
            baseContextText = "%d coupons from %d merchants expires today";
            baseNotificationLine = "%d coupons from %s";
        }

        int numMerchants = merchantCouponCountMap.size();
        int numTotalCoupons = 0;
        NotificationCompat.InboxStyle notificationStyle = new NotificationCompat.InboxStyle();
        notificationStyle.setBigContentTitle(contentTitle);
        for (Map.Entry<String, Integer> entry : merchantCouponCountMap.entrySet()) {
            numTotalCoupons += entry.getValue();
            notificationStyle.addLine(String.format(baseNotificationLine, entry.getValue(), entry.getKey()));
        }
        String contentText = String.format(Locale.ENGLISH, baseContextText, numTotalCoupons, numMerchants);

        // Do not show notification if there are no coupons expiring today
        if (numTotalCoupons == 0) {
            notificationStyle = null;
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setStyle(notificationStyle)
                .setLargeIcon(Utilities.getBitmap(context, R.drawable.ic_notifications_24dp))
                .setSmallIcon(smallIconId)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);

        Intent contentIntent = new Intent(context, ContainerActivity.class);
        contentIntent.putExtra(Constants.BUNDLE_EXTRA_FRAGMENT_TYPE, Constants.FragmentType.NOTIFICATION_FRAGMENT);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(contentIntent);
        PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(contentPendingIntent);

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String ringtone = sharedPreferences.getString(SettingsFragment.KEY_NOTIFICATION_RINGTONE, "");
        boolean shouldVibrate = sharedPreferences.getBoolean(SettingsFragment.KEY_NOTIFICATION_VIBRATE, true);
        if (ringtone.length() == 0) {
            builder.setSound(null);
        } else {
            try {
                builder.setSound(Uri.parse(ringtone));
            } catch (NullPointerException e) {
                e.printStackTrace();
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            }
        }
        if (shouldVibrate) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        NotificationManagerCompat.from(context).cancel(TAG, DAILY_NOTIFICATION_ID);
        NotificationManagerCompat.from(context).notify(
                TAG,
                DAILY_NOTIFICATION_ID,
                builder.build()
        );
    }
}
