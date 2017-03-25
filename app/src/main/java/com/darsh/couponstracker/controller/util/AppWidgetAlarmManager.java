package com.darsh.couponstracker.controller.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.darsh.couponstracker.controller.receiver.AppWidgetAlarmReceiver;
import com.darsh.couponstracker.logger.DebugLog;

import java.util.Calendar;

/**
 * Created by darshan on 21/3/17.
 */

public class AppWidgetAlarmManager {
    /**
     * Sets a daily recurring alarm that is to trigger that
     * 2 am device time.
     */
    public static void setAlarm(Context context) {
        DebugLog.logMethod();
        context.getApplicationContext();

        PendingIntent pendingIntent = getPendingIntent(context);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    /**
     * Cancels any previously set alarms of similar type.
     */
    public static void cancelAlarm(Context context) {
        DebugLog.logMethod();
        context = context.getApplicationContext();
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                .cancel(getPendingIntent(context));
    }

    private static PendingIntent getPendingIntent(Context context) {
        DebugLog.logMethod();
        Intent intent = new Intent(context, AppWidgetAlarmReceiver.class);
        intent.setAction(Constants.ACTION_WIDGET_UPDATE);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}
