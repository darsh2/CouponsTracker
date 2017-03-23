package com.darsh.couponstracker.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.darsh.couponstracker.R;
import com.darsh.couponstracker.ui.fragment.CouponFragment;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.controller.util.Constants;
import com.darsh.couponstracker.controller.util.Utilities;

/**
 * Created by darshan on 11/3/17.
 */

public class CouponActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);
        DebugLog.logMethod();
        if (getIntent() == null
                || getIntent().getExtras() == null) {
            DebugLog.logMessage("Finishing CouponActivity");
            finish();
            return;
        }

        /*
        This is a hack to show CouponListActivity on clicking "View all footers"
        from widget list view. Since a PendingIntentTemplate has to be set for
        the list view as a whole with a destination specified, issue arises in
        phones because on clicking a coupon, the app should launch
         */
        if (getIntent().getBooleanExtra(Constants.BUNDLE_EXTRA_VIEW_ALL, false)) {
            finish();
            return;
        }

        Utilities.navigateIfSyncInProgress(getApplicationContext());

        if (getSupportFragmentManager() != null
                && getSupportFragmentManager().findFragmentByTag(CouponFragment.TAG) != null) {
            DebugLog.logMessage("Not null");
            return;
        }

        CouponFragment couponFragment = new CouponFragment();
        couponFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, couponFragment, CouponFragment.TAG)
                .commit();
    }
}
