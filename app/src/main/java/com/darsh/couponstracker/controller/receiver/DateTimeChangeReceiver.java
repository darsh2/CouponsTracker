package com.darsh.couponstracker.controller.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.controller.util.AppWidgetAlarmManager;
import com.darsh.couponstracker.controller.util.NotificationAlarmManager;

/**
 * <p>Created by darshan on 21/3/17.
 *
 * <p>Listen for changes in {@link Intent#ACTION_TIME_CHANGED}, {@link Intent#ACTION_DATE_CHANGED}
 * and {@link Intent#ACTION_TIMEZONE_CHANGED}. This is to reset any previously set alarms for updating
 * app widget and displaying daily notification.
 */

public class DateTimeChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DebugLog.logMethod();
        DebugLog.logMessage("Action: " + intent.getAction());
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIME_CHANGED)
                || action.equals(Intent.ACTION_DATE_CHANGED)
                || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
            // Cancel previously set alarms and set a new alarm
            AppWidgetAlarmManager.cancelAlarm(context);
            AppWidgetAlarmManager.setAlarm(context);

            NotificationAlarmManager.cancelAlarm(context);
            NotificationAlarmManager.setAlarm(context);
        }
    }
}
