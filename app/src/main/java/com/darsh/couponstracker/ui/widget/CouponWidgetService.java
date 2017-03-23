package com.darsh.couponstracker.ui.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.darsh.couponstracker.logger.DebugLog;

/**
 * Created by darshan on 21/3/17.
 */

public class CouponWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        DebugLog.logMethod();
        return new CouponRemoteViewsFactory(getApplicationContext());
    }
}
