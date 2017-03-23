package com.darsh.couponstracker.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.darsh.couponstracker.R;
import com.darsh.couponstracker.controller.event.SyncCompleteEvent;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.controller.sync.ExportToDriveService;
import com.darsh.couponstracker.controller.sync.GoogleDriveService;
import com.darsh.couponstracker.controller.sync.ImportFromDriveService;
import com.darsh.couponstracker.controller.util.Constants;
import com.darsh.couponstracker.controller.util.NotificationAlarmManager;
import com.darsh.couponstracker.controller.util.Utilities;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by darshan on 18/3/17.
 */

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        TimePickerDialog.OnTimeSetListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = "SettingsFragment";

    public static final String KEY_EXPORT_TO_DRIVE = "export_to_drive";
    public static final String KEY_IMPORT_FROM_DRIVE = "import_from_drive";

    public static final String KEY_NOTIFICATION_STATE = "notification_state";
    public static final String KEY_NOTIFICATION_TIME = "notification_time";
    public static final String KEY_NOTIFICATION_RINGTONE = "notification_ringtone";
    public static final String KEY_NOTIFICATION_VIBRATE = "notification_vibrate";

    public static final int DEFAULT_NOTIFICATION_TIME = 220;

    private Preference timePreference;
    private RingtonePreference ringtonePreference;

    private GoogleApiClient googleApiClient;

    private int syncMode = -1;
    private boolean showDialog = false;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        DebugLog.logMethod();

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext());
        syncMode = sharedPreferences.getInt(Constants.SYNC_MODE, -1);
        showDialog = sharedPreferences.getBoolean(Constants.IS_SYNC_RUNNING, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DebugLog.logMethod();
        EventBus.getDefault().register(this);

        buildGoogleApiClient();
        findPreference(KEY_EXPORT_TO_DRIVE).setOnPreferenceClickListener(this);
        findPreference(KEY_IMPORT_FROM_DRIVE).setOnPreferenceClickListener(this);

        timePreference = findPreference(KEY_NOTIFICATION_TIME);
        timePreference.setSummary(getNotificationTimeSummary());
        timePreference.setOnPreferenceClickListener(this);

        ringtonePreference = (RingtonePreference) findPreference(KEY_NOTIFICATION_RINGTONE);
        ringtonePreference.setSummary(getRingtoneTitle());

        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        showProgressDialog();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        DebugLog.logMethod();
        EventBus.getDefault().unregister(this);

        googleApiClient.disconnect();
        googleApiClient = null;

        // Dismiss dialog to prevent WindowLeak
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;

        findPreference(KEY_EXPORT_TO_DRIVE).setOnPreferenceClickListener(null);
        findPreference(KEY_IMPORT_FROM_DRIVE).setOnPreferenceClickListener(null);

        timePreference.setOnPreferenceClickListener(null);
        timePreference = null;

        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        DebugLog.logMethod();
        switch (key) {
            case KEY_NOTIFICATION_STATE: {
                DebugLog.logMessage("Notification State");
                // Cancel any active alarms and reset alarm based on state.
                NotificationAlarmManager.cancelAlarm(getActivity().getApplicationContext());
                NotificationAlarmManager.setAlarm(getActivity().getApplicationContext());
                return;
            }

            case KEY_NOTIFICATION_TIME: {
                DebugLog.logMessage("Notification Time");
                timePreference.setSummary(getNotificationTimeSummary());
                /*
                Cancel any active alarms and reset alarm based on state. This is necessary
                because time at which the alarms have to be shown is now changed.
                 */
                NotificationAlarmManager.cancelAlarm(getActivity().getApplicationContext());
                NotificationAlarmManager.setAlarm(getActivity().getApplicationContext());
                return;
            }

            case KEY_NOTIFICATION_RINGTONE: {
                DebugLog.logMessage("Notification Ringtone");
                ringtonePreference.setSummary(getRingtoneTitle());
                return;
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        DebugLog.logMethod();
        DebugLog.logMessage("Preference: " + preference.getKey());

        switch (preference.getKey()) {
            case KEY_EXPORT_TO_DRIVE: {
                showDialog = true;
                syncMode = GoogleDriveService.SyncMode.EXPORT;
                connectGoogleApiClient();
                return true;
            }

            case KEY_IMPORT_FROM_DRIVE: {
                showDialog = true;
                syncMode = GoogleDriveService.SyncMode.IMPORT;
                connectGoogleApiClient();
                return true;
            }

            case KEY_NOTIFICATION_TIME: {
                int notificationTime = getNotificationTime();
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        getActivity(),
                        R.style.PickerTheme,
                        this,
                        notificationTime / 100,
                        notificationTime % 100,
                        true
                );
                timePickerDialog.show();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        DebugLog.logMethod();
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_NOTIFICATION_TIME, hourOfDay * 100 + minute);
        editor.apply();
    }

    /**
     * Returns notification time in HH:MM 24 hour format.
     */
    private String getNotificationTimeSummary() {
        DebugLog.logMethod();
        int notificationTime = getNotificationTime();
        String time;
        if ((notificationTime / 100) < 10) {
            time = "0" + (notificationTime / 100);
        } else {
            time = "" + (notificationTime / 100);
        }
        time += ":";
        if ((notificationTime % 100) < 10) {
            time += "0" + (notificationTime % 100);
        } else {
            time += "" + (notificationTime % 100);
        }
        return String.format(getString(R.string.settings_set_notification_time_summary), time);
    }

    private int getNotificationTime() {
        DebugLog.logMethod();
        return getPreferenceManager().getSharedPreferences()
                .getInt(KEY_NOTIFICATION_TIME, DEFAULT_NOTIFICATION_TIME);
    }

    private String getRingtoneTitle() {
        DebugLog.logMethod();
        String ringtoneUri = getPreferenceManager().getSharedPreferences().getString(
                KEY_NOTIFICATION_RINGTONE,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString()
        );
        Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(ringtoneUri));
        String ringtoneTitle = ringtone.getTitle(getActivity());
        // Prevent memory leaks
        ringtone.stop();

        return ringtoneTitle;
    }

    /**
     * Creates the {@link GoogleApiClient} object that adds {@link Drive#API}
     * with scopes - {@link Drive#SCOPE_FILE} and {@link Drive#SCOPE_APPFOLDER}
     */
    private void buildGoogleApiClient() {
        DebugLog.logMethod();
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * If {@link #googleApiClient} is already connected or connecting
     * in progress, returns without doing anything. Else makes an
     * asynchronous call via {@link GoogleApiClient#connect()} to
     * establish a connection.
     */
    public void connectGoogleApiClient() {
        DebugLog.logMethod();
        if (googleApiClient.isConnecting()
                || googleApiClient.isConnected()) {
            DebugLog.logMessage("GoogleApiClient is already connected or is currently connecting");
            return;
        }
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        DebugLog.logMethod();
        googleApiClient.disconnect();

        Utilities.updateSharedPreferences(getActivity().getApplicationContext(), true, syncMode);
        showProgressDialog();

        Intent googleDriveService;
        if (syncMode == GoogleDriveService.SyncMode.IMPORT) {
            googleDriveService = new Intent(getActivity(), ImportFromDriveService.class);
        } else {
            googleDriveService = new Intent(getActivity(), ExportToDriveService.class);
        }
        googleDriveService.setAction(Constants.ACTION_GOOGLE_DRIVE_SYNC);
        getActivity().startService(googleDriveService);

    }

    @Override
    public void onConnectionSuspended(int i) {
        DebugLog.logMethod();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        DebugLog.logMethod();
        DebugLog.logMessage("ConnectionResult: " + connectionResult.toString());
        DebugLog.logMessage("ConnectionResult error: " + connectionResult.getErrorCode() + "\n" + connectionResult.getErrorMessage());
        if (!connectionResult.hasResolution()) {
            GoogleApiAvailability.getInstance()
                    .getErrorDialog(getActivity(), connectionResult.getErrorCode(), 0)
                    .show();
            return;
        }

        try {
            connectionResult.startResolutionForResult(getActivity(), Constants.CONNECTION_RESOLUTION_REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            // Unable to resolve, message user appropriately
            e.printStackTrace();
            DebugLog.logMessage(e.getMessage());
            Utilities.showToast(getActivity(), getString(R.string.google_drive_no_resolution));
        }
    }

    /**
     * Creates and shows an {@link AlertDialog} indicating to the
     * user that Google Drive Sync is in progress. It is not cancellable
     * and hence the user will not be able to perform any other action
     * in the app until Google Drive Sync is complete.
     */
    private void showProgressDialog() {
        DebugLog.logMethod();
        // There is no sync process running
        if (!showDialog) {
            return;
        }

        // Do not recreate the dialog if already visible
        if (progressDialog != null && progressDialog.isShowing()) {
            return;
        }

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(getString(R.string.progress_dialog_title));
        progressDialog.setMessage(syncMode == GoogleDriveService.SyncMode.EXPORT
                ? getString(R.string.progress_dialog_message_export)
                : getString(R.string.progress_dialog_message_import)
        );
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncCompleteEvent(SyncCompleteEvent syncCompleteEvent) {
        syncMode = -1;
        showDialog = false;
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
