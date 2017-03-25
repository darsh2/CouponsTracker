package com.darsh.couponstracker.controller.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.controller.util.Constants;
import com.darsh.couponstracker.controller.util.Utilities;

/**
 * <p>Created by darshan on 21/3/17.
 *
 * <p>A {@link WakefulBroadcastReceiver} that listens for an {@link Intent} with
 * action as {@link Constants#ACTION_WIDGET_UPDATE}. On receiving such an intent,
 * informs app to update it's widget.
 */

public class AppWidgetAlarmReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DebugLog.logMethod();
        DebugLog.logMessage("Action: " + intent.getAction());
        if (intent.getAction().equals(Constants.ACTION_WIDGET_UPDATE)) {
            Utilities.updateAppWidget(context);
        }
    }
}
