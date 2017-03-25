package com.darsh.couponstracker.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.darsh.couponstracker.R;
import com.darsh.couponstracker.controller.util.Constants;
import com.darsh.couponstracker.controller.util.Utilities;
import com.darsh.couponstracker.data.model.Coupon;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.ui.adapter.CouponListAdapter;
import com.darsh.couponstracker.ui.adapter.FragmentTabsAdapter;
import com.darsh.couponstracker.ui.fragment.CouponFragment;
import com.darsh.couponstracker.ui.fragment.PastCouponListFragment;
import com.darsh.couponstracker.ui.fragment.UpcomingCouponListFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by darshan on 13/3/17.
 */

public class CouponListActivity extends AppCompatActivity implements CouponListAdapter.OnCouponClickListener {
    /**
     * Reference to the Unbinder contract when the view is bound using ButterKnife.
     * This is required to unbind the views on destroying the activity.
     */
    private Unbinder unbinder;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @BindView(R.id.floating_action_button)
    FloatingActionButton fab;

    private UpcomingCouponListFragment upcomingCouponListFragment;
    private PastCouponListFragment pastCouponListFragment;

    private boolean isTablet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_list);
        unbinder = ButterKnife.bind(this);

        DebugLog.logMethod();

        /*
        By default the widget alarm to update the widget and notifications
        for coupons that expire each day are enabled. Hence, start the alarms
        if this is the first time the app is launched.
         */
        Utilities.setAlarmsOnFirstLaunch(getApplicationContext());

        /*
        Always navigate to SettingsActivity if an import or export coupons
        task is running.
         */
        Utilities.navigateIfSyncInProgress(getApplicationContext());

        isTablet = getResources().getBoolean(R.bool.is_tablet);

        toolbar.setTitle(getString(R.string.app_name));
        toolbar.inflateMenu(R.menu.home_screen);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                DebugLog.logMethod();
                switch (item.getItemId()) {
                    case R.id.menu_item_my_profile: {
                        DebugLog.logMessage("My profile");
                        Intent intent = new Intent(CouponListActivity.this, ContainerActivity.class);
                        intent.putExtra(Constants.BUNDLE_EXTRA_FRAGMENT_TYPE, Constants.FragmentType.MY_PROFILE_FRAGMENT);
                        startActivity(intent);
                        return true;
                    }

                    case R.id.menu_item_settings: {
                        DebugLog.logMessage("Settings");
                        Intent intent = new Intent(CouponListActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        return true;
                    }
                }
                return true;
            }
        });
        initFragments(savedInstanceState);
        setUpViewPager();

        tabLayout.setupWithViewPager(viewPager);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTablet) {
                    startCouponActivity(CouponFragment.Mode.CREATE, Coupon.getDummy());
                } else {
                    loadCouponFragment(CouponFragment.Mode.CREATE, Coupon.getDummy());
                }
            }
        });

        handleWidgetItemClick();
    }

    private void initFragments(Bundle savedInstanceState) {
        DebugLog.logMethod();
        if (savedInstanceState != null) {
            DebugLog.logMessage("Getting fragments");
            upcomingCouponListFragment = (UpcomingCouponListFragment) getSupportFragmentManager().getFragment(savedInstanceState, UpcomingCouponListFragment.TAG);
            pastCouponListFragment = (PastCouponListFragment) getSupportFragmentManager().getFragment(savedInstanceState, PastCouponListFragment.TAG);
        } else {
            DebugLog.logMessage("Creating fragments");
            upcomingCouponListFragment = new UpcomingCouponListFragment();
            pastCouponListFragment = new PastCouponListFragment();
        }
    }

    private void setUpViewPager() {
        DebugLog.logMethod();
        FragmentTabsAdapter adapter = new FragmentTabsAdapter(getSupportFragmentManager());
        adapter.addFragment(upcomingCouponListFragment, getString(R.string.title_upcoming_coupon_list_fragment));
        adapter.addFragment(pastCouponListFragment, getString(R.string.title_past_coupon_list_fragment));
        viewPager.setAdapter(adapter);
    }

    /**
     * Loads the appropriate coupon that was clicked in the widget.
     */
    private void handleWidgetItemClick() {
        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            return;
        }

        Bundle bundle = intent.getExtras();
        boolean loadCouponFragment = intent.getBooleanExtra(Constants.BUNDLE_EXTRA_LOAD_COUPON_FRAGMENT, false);
        if (loadCouponFragment) {
            onCouponClick((Coupon) bundle.getParcelable(Constants.COUPON_PARCELABLE));
        }
    }

    @Override
    public void onCouponClick(Coupon coupon) {
        DebugLog.logMethod();
        if (!isTablet) {
            startCouponActivity(CouponFragment.Mode.VIEW, coupon);
        } else {
            loadCouponFragment(CouponFragment.Mode.VIEW, coupon);
        }
    }

    /**
     * Launches the CouponActivity which then loads the specified coupon
     * in the CouponFragment. This method is only called on phones.
     * @param mode Mode can be one of CREATE, EDIT or VIEW
     * @param coupon Coupon to be shown
     */
    private void startCouponActivity(int mode, Coupon coupon) {
        DebugLog.logMethod();
        Intent intent = new Intent(this, CouponActivity.class);
        intent.putExtras(getExtras(mode, coupon));
        startActivity(intent);
    }

    /**
     * Loads the passed coupon in the right fragment container in this
     * activity's layout. This is called only on tablets as tablets screen
     * have a master-detail layout.
     * @param mode Mode can be one of CREATE, EDIT or VIEW
     * @param coupon Coupon to be shown
     */
    private void loadCouponFragment(int mode, Coupon coupon) {
        DebugLog.logMethod();
        CouponFragment couponFragment = new CouponFragment();
        couponFragment.setArguments(getExtras(mode, coupon));
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_right, couponFragment, CouponFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    private Bundle getExtras(int mode, Coupon coupon) {
        DebugLog.logMethod();
        Bundle extras = new Bundle();
        extras.putInt(Constants.COUPON_FRAGMENT_MODE, mode);
        extras.putParcelable(Constants.COUPON_PARCELABLE, coupon);
        extras.putStringArrayList(
                Constants.BUNDLE_EXTRA_MERCHANT_SUGGESTIONS,
                upcomingCouponListFragment.getMerchants() == null
                        ? new ArrayList<String>()
                        : upcomingCouponListFragment.getMerchants()
        );
        extras.putStringArrayList(
                Constants.BUNDLE_EXTRA_CATEGORY_SUGGESTIONS,
                upcomingCouponListFragment.getCategories() == null
                        ? new ArrayList<String>()
                        : upcomingCouponListFragment.getCategories()
        );
        return extras;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        DebugLog.logMethod();
        getSupportFragmentManager().putFragment(outState, UpcomingCouponListFragment.TAG, upcomingCouponListFragment);
        getSupportFragmentManager().putFragment(outState, PastCouponListFragment.TAG, pastCouponListFragment);
    }

    @Override
    protected void onDestroy() {
        DebugLog.logMethod();
        upcomingCouponListFragment = null;
        pastCouponListFragment = null;
        viewPager.setAdapter(null);
        unbinder.unbind();
        super.onDestroy();
    }
}
