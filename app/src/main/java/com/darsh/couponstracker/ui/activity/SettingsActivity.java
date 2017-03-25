package com.darsh.couponstracker.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.darsh.couponstracker.R;
import com.darsh.couponstracker.controller.util.Constants;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.ui.fragment.SettingsFragment;

/**
 * Created by darshan on 18/3/17.
 */

public class SettingsActivity extends AppCompatActivity {
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        DebugLog.logMethod();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.menu_item_settings));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getSupportFragmentManager().findFragmentByTag(SettingsFragment.TAG) == null) {
            DebugLog.logMessage("Null");
            settingsFragment = new SettingsFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, settingsFragment, SettingsFragment.TAG)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        DebugLog.logMethod();
        DebugLog.logMessage("requestCode: " + requestCode + ", successful: " + (resultCode == RESULT_OK));

        /*
        First time an account is added, initially SettingsFragment.onConnectionFailed is called.
        On calling connectionResult.startResolutionForResult, onActivityResult of SettingsFragment
        is not getting called. Hence override onActivityResult of SettingsActivity, find SettingsFragment
        and if it exists, call connectGoogleApiClient.
         */
        SettingsFragment settingsFragment = (SettingsFragment) getFragmentManager().findFragmentByTag(SettingsFragment.TAG);
        DebugLog.logMessage("Settings Fragment is null? " + (settingsFragment == null));
        if (settingsFragment != null
                && requestCode == Constants.CONNECTION_RESOLUTION_REQUEST_CODE
                && resultCode == RESULT_OK) {
            settingsFragment.connectGoogleApiClient();
        }
    }
}
