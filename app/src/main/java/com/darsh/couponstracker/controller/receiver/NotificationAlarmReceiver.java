package com.darsh.couponstracker.controller.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.controller.notification.CouponsTrackerNotification;
import com.darsh.couponstracker.controller.util.Constants;

/**
 * <p>Created by darshan on 21/3/17.
 *
 * <p>On receiving an {@link Intent} with the action {@link Constants#ACTION_SHOW_NOTIFICATION},
 * calls {@link CouponsTrackerNotification#showDailyNotification()} to show the daily
 * notification indicating coupons expiring today.
 */

public class NotificationAlarmReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DebugLog.logMethod();
        DebugLog.logMessage("Action: " + intent.getAction());
        if (intent.getAction().equals(Constants.ACTION_SHOW_NOTIFICATION)) {
            new CouponsTrackerNotification(context).showDailyNotification();
        }
    }
}
