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
 * <p>On shutting down or restarting device, all app created alarms are lost.
 * This class thus listens for {@link Intent#ACTION_BOOT_COMPLETED} to reset
 * app widget and daily notification alarms.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DebugLog.logMethod();
        DebugLog.logMessage("Action: " + intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            AppWidgetAlarmManager.setAlarm(context);
            NotificationAlarmManager.setAlarm(context);
        }
    }
}
