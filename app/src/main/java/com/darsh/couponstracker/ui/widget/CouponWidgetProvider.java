package com.darsh.couponstracker.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.darsh.couponstracker.R;
import com.darsh.couponstracker.ui.activity.CouponActivity;
import com.darsh.couponstracker.ui.activity.CouponListActivity;
import com.darsh.couponstracker.logger.DebugLog;

/**
 * Created by darshan on 21/3/17.
 */

public class CouponWidgetProvider extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        DebugLog.logMethod();
        DebugLog.logMessage("Action: " + intent.getAction());
        super.onReceive(context, intent);
        if (!intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            return;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, context.getPackageName()));
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        DebugLog.logMethod();
        boolean isTablet = context.getResources().getBoolean(R.bool.is_tablet);
        for (int i = 0, numWidgets = appWidgetIds.length; i < numWidgets; i++) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteViews.setEmptyView(R.id.list_view_coupons, R.id.text_view_empty_widget);

            Intent adapterIntent = new Intent(context, CouponWidgetService.class);
            adapterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            remoteViews.setRemoteAdapter(R.id.list_view_coupons, adapterIntent);

            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
            if (!isTablet) {
                Intent onClickIntent = new Intent(context, CouponActivity.class);
                taskStackBuilder.addNextIntentWithParentStack(onClickIntent);
            } else {
                Intent onClickIntent = new Intent(context, CouponListActivity.class);
                taskStackBuilder.addNextIntent(onClickIntent);
            }
            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.list_view_coupons, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i], R.id.list_view_coupons);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


}
