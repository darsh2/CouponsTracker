package com.darsh.couponstracker.controller.sync;

import android.database.Cursor;

import com.darsh.couponstracker.data.database.CouponContract;
import com.darsh.couponstracker.logger.DebugLog;
import com.darsh.couponstracker.data.model.Coupon;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by darshan on 22/3/17.
 */

public class ExportToDriveService extends GoogleDriveService {
    public ExportToDriveService() {
        super();
        setSyncMode(SyncMode.EXPORT);
    }

    @Override
    protected void handleIntent() {
        DebugLog.logMethod();
        DriveFile driveFile = getDriveFile();
        boolean isNewFile = driveFile == null;
        DriveContents driveContents = driveFile == null
                ? createDriveFile()
                : openDriveFileInEditMode(driveFile);
        if (driveContents == null) {
            showError("Failed to create drive file");
            return;
        }

        String couponsJson = getCouponsJson();
        if (couponsJson == null) {
            driveContents.discard(getGoogleApiClient());
            showError("Error while reading coupon data");
            return;
        }

        if (!writeToDriveFile(driveContents, couponsJson, isNewFile)) {
            driveContents.discard(getGoogleApiClient());
            showError("Error occurred while exporting to Google drive");
            return;
        }
        driveContents.discard(getGoogleApiClient());
    }

    private DriveContents createDriveFile() {
        DebugLog.logMethod();
        DriveApi.DriveContentsResult driveContentsResult = Drive.DriveApi
                .newDriveContents(getGoogleApiClient())
                .await();
        DebugLog.logMessage("DriveContentsResult: statusCode - " + driveContentsResult.getStatus().getStatusCode()
                + ", statusMessage: " + driveContentsResult.getStatus().getStatusMessage());
        if (!driveContentsResult.getStatus().isSuccess()) {
            return null;
        }
        return driveContentsResult.getDriveContents();
    }

    private DriveContents openDriveFileInEditMode(DriveFile driveFile) {
        DebugLog.logMethod();
        DriveApi.DriveContentsResult driveContentsResult = driveFile.open(
                getGoogleApiClient(),
                DriveFile.MODE_WRITE_ONLY,
                null
        ).await();
        DebugLog.logMessage("DriveContentsResult: statusCode - " + driveContentsResult.getStatus().getStatusCode()
                + ", statusMessage: " + driveContentsResult.getStatus().getStatusMessage());
        if (!driveContentsResult.getStatus().isSuccess()) {
            return null;
        }
        return driveContentsResult.getDriveContents();
    }

    private boolean writeToDriveFile(DriveContents driveContents, String couponsJson, boolean isNewFile) {
        DebugLog.logMethod();
        OutputStream outputStream = driveContents.getOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);
        try {
            writer.write(couponsJson);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            DebugLog.logMessage(e.getMessage());
            return false;
        }
        return isNewFile ? commitToNewFile(driveContents) : commitToExistingFile(driveContents);
    }

    private boolean commitToNewFile(DriveContents driveContents) {
        DebugLog.logMethod();
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("CouponsTrackerData.txt")
                .setMimeType("text/plain")
                .build();

        DriveFolder.DriveFileResult driveFileResult = Drive.DriveApi
                .getAppFolder(getGoogleApiClient())
                .createFile(getGoogleApiClient(), changeSet, driveContents)
                .await();
        DebugLog.logMessage("DriveFileResult: statusCode - " + driveFileResult.getStatus().getStatusCode()
                + ", statusMessage: " + driveFileResult.getStatus().getStatusMessage());
        return driveFileResult.getStatus().isSuccess();
    }

    private boolean commitToExistingFile(DriveContents driveContents) {
        DebugLog.logMethod();
        com.google.android.gms.common.api.Status status =
                driveContents.commit(getGoogleApiClient(), null).await();
        DebugLog.logMessage("Status code: " + status.getStatus().getStatusCode()
                + ", Status message: " + status.getStatus().getStatusMessage());
        return status.getStatus().isSuccess();
    }

    private String getCouponsJson() {
        DebugLog.logMethod();
        ArrayList<Coupon> coupons = getCoupons();
        if (coupons == null) {
            showError("Error fetching coupons from app. Please try again");
            return null;
        }

        Gson gson = new Gson();
        return gson.toJson(coupons);
    }

    private ArrayList<Coupon> getCoupons() {
        DebugLog.logMethod();
        Cursor cursor = getContentResolver().query(
                CouponContract.CouponTable.URI,
                CouponContract.CouponTable.PROJECTION,
                null,
                null,
                null
        );
        if (cursor == null) {
            return null;
        }

        ArrayList<Coupon> coupons = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            coupons.add(Coupon.getCoupon(cursor));
        }
        cursor.close();
        return coupons;
    }
}
