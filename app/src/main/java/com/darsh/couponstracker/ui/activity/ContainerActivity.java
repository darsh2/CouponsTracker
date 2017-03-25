package com.darsh.couponstracker.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.darsh.couponstracker.R;
import com.darsh.couponstracker.controller.util.Constants;
import com.darsh.couponstracker.controller.util.Utilities;
import com.darsh.couponstracker.data.model.Coupon;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.ui.adapter.CouponListAdapter;
import com.darsh.couponstracker.ui.adapter.CouponListCursorAdapter;
import com.darsh.couponstracker.ui.fragment.CouponFragment;
import com.darsh.couponstracker.ui.fragment.MyProfileFragment;
import com.darsh.couponstracker.ui.fragment.NotificationCouponListFragment;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;

/**
 * Created by darshan on 19/3/17.
 */

public class ContainerActivity extends AppCompatActivity implements CouponListAdapter.OnCouponClickListener,
        CouponListCursorAdapter.OnCursorCouponClickListener {
    private int fragmentType = -1;
    private boolean isTablet = false;

    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        DebugLog.logMethod();

        isTablet = getResources().getBoolean(R.bool.is_tablet);

        fragmentType = getIntent().getIntExtra(Constants.BUNDLE_EXTRA_FRAGMENT_TYPE, -1);
        if (fragmentType == -1) {
            finish();
            return;
        }

        /*
        While this is not a launcher activity, this activity can be called when a daily
        notification is clicked. Hence at this instant, if there is an ongoing Drive
        sync task in progress, always navigate to SettingsActivity.
         */
        Utilities.navigateIfSyncInProgress(getApplicationContext());

        if (fragmentType == Constants.FragmentType.MY_PROFILE_FRAGMENT) {
            // Load MyProfileFragment only if it is not already present
            if (getSupportFragmentManager().findFragmentByTag(MyProfileFragment.TAG) == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(getFragmentContainerId(false), new MyProfileFragment(), MyProfileFragment.TAG)
                        .commit();
            }
        } else if (fragmentType == Constants.FragmentType.NOTIFICATION_FRAGMENT) {
            // Load NotificationCouponListFragment only if it is not already present
            if (getSupportFragmentManager().findFragmentByTag(NotificationCouponListFragment.TAG) == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(getFragmentContainerId(false), new NotificationCouponListFragment(), NotificationCouponListFragment.TAG)
                        .commit();
            }
        }
    }

    @Override
    public void onCouponClick(Coupon coupon) {
        DebugLog.logMethod();
        DebugLog.logMessage("Coupon: " + coupon.toString());
        showCouponFromFragment(coupon);
    }

    @Override
    public void onCursorCouponClick(Coupon coupon) {
        DebugLog.logMethod();
        DebugLog.logMessage("Coupon: " + coupon.toString());
        showCouponFromFragment(coupon);
    }

    /**
     * Loads the passed coupon in a CouponFragment. If the device is a
     * phone, starts {@link CouponActivity} to handle loading of the coupon.
     * Else, loads coupon in {@link CouponFragment} found in the tablet
     * layout.
     * @param coupon Coupon to be shown
     */
    private void showCouponFromFragment(Coupon coupon) {
        DebugLog.logMethod();
        Bundle extras = getExtras(CouponFragment.Mode.VIEW, coupon);

        // Start CouponActivity if is not a tablet
        if (!isTablet) {
            Intent intent = new Intent(this, CouponActivity.class);
            intent.putExtras(extras);
            startActivity(intent);
            return;
        }

        CouponFragment couponFragment = new CouponFragment();
        couponFragment.setArguments(extras);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(getFragmentContainerId(true), couponFragment, CouponFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    private Bundle getExtras(int mode, Coupon coupon) {
        DebugLog.logMethod();
        return getExtras(mode, coupon, new ArrayList<String>(), new ArrayList<String>());
    }

    private Bundle getExtras(int mode, Coupon coupon, ArrayList<String> merchantSuggestions, ArrayList<String> categorySuggestions) {
        DebugLog.logMethod();
        Bundle extras = new Bundle();
        extras.putInt(Constants.COUPON_FRAGMENT_MODE, mode);
        extras.putParcelable(Constants.COUPON_PARCELABLE, coupon);
        extras.putStringArrayList(Constants.BUNDLE_EXTRA_MERCHANT_SUGGESTIONS, merchantSuggestions);
        extras.putStringArrayList(Constants.BUNDLE_EXTRA_CATEGORY_SUGGESTIONS, categorySuggestions);
        return extras;
    }

    /**
     * Returns the fragment container id to which a fragment is to be loaded in.
     * If device is a phone, there is only a single container. Else if it is to
     * show a coupon, return the right container, else the left.
     */
    private int getFragmentContainerId(boolean isCouponView) {
        DebugLog.logMethod();
        if (!isTablet) {
            return R.id.fragment_container;
        }
        int id = R.id.fragment_container_left;
        if (isCouponView) {
            id = R.id.fragment_container_right;
        }
        return id;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DebugLog.logMethod();
        if (item.getItemId() == android.R.id.home) {
            DebugLog.logMessage("Home button pressed");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DebugLog.logMethod();
        DebugLog.logMessage("BackStackEntryCount: " + getSupportFragmentManager().getBackStackEntryCount());
        /*
        If it is not MyProfileFragment or there still are coupons displayed by
        CouponFragment, do not load interstitial ad.
         */
        if (fragmentType != Constants.FragmentType.MY_PROFILE_FRAGMENT
                || getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
            return;
        }

        DebugLog.logMessage("Show Interstitial Ad");
        /*
        If the interstitial ad is loaded, then show. Else delegate call to the
        super method without waiting for the ad to load.
         */
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            super.onBackPressed();
        }
    }

    private void initInterstitialAd() {
        DebugLog.logMethod();
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                DebugLog.logMethod();
                ContainerActivity.this.finish();
            }
        });
        requestNewInterstitial();
    }

    /**
     * Loads a new interstitial ad.
     */
    private void requestNewInterstitial() {
        DebugLog.logMethod();
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(getString(R.string.test_device_id))
                .build();
        interstitialAd.loadAd(adRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DebugLog.logMethod();
        initInterstitialAd();
    }

    @Override
    protected void onStop() {
        super.onStop();
        DebugLog.logMethod();
        interstitialAd.setAdListener(null);
        interstitialAd = null;
    }
}
