package com.darsh.couponstracker.controller.sync;

import android.database.Cursor;

import com.darsh.couponstracker.data.database.CouponContract;
import com.darsh.couponstracker.data.model.Coupon;
import com.darsh.couponstracker.logger.DebugLog;
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
    protected boolean handleIntent() {
        DebugLog.logMethod();
        try {
            /*
            First retrieve the json representation of all coupons in
            the app. If this data is null, return false.
             */
            String couponsJson = getCouponsJson();
            if (couponsJson == null) {
                showError("No coupon data");
                return false;
            }

            // Get the drive app specific drive file to write data into.
            DriveFile driveFile = getDriveFile();
            boolean isNewFile = driveFile == null;
            // Get the driveContents to write data to.
            DriveContents driveContents = driveFile == null
                    ? createDriveFileContents()
                    : openDriveContentsFileInEditMode(driveFile);
            if (driveContents == null) {
                showError("Failed to create drive file");
                return false;
            }

            // Write the couponsJson to the app specific file in google drive
            if (!writeToDriveFile(driveContents, couponsJson, isNewFile)) {
                driveContents.discard(getGoogleApiClient());
                showError("Error occurred while exporting to Google drive");
                return false;
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            DebugLog.logMessage(e.getMessage());
            showError("Error: " + e.getMessage());
            return false;
        }
    }

    private DriveContents createDriveFileContents() {
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

    private DriveContents openDriveContentsFileInEditMode(DriveFile driveFile) {
        DebugLog.logMethod();
        // Get drive contents in write mode
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

    /**
     * Write the couponsJson to the driveContents, creating or updating the file as required.
     * Returns a boolean indicating success of this task.
     */
    private boolean writeToDriveFile(DriveContents driveContents, String couponsJson, boolean isNewFile) {
        DebugLog.logMethod();
        OutputStream outputStream = driveContents.getOutputStream();
        /*
        Ref: http://stackoverflow.com/a/4069104/3946664
        
        Avoiding usage of outputStream.write() since it requires a byte array to be
        passed. In case of String, the getBytes() should be avoided since it uses
        default encoding of the JVM which cannot be reliably predicted in a portable
        manner. Hence using Writer's write method.
         */
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

    /**
     * Creates a new file and adds the {@link DriveContents} passed to this method.
     * @return Returns a boolean stating the success of this operation
     */
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

    /**
     * Updates the existing file with the driveContents passed.
     * @return Returns a boolean indicating the success of this operation
     */
    private boolean commitToExistingFile(DriveContents driveContents) {
        DebugLog.logMethod();
        com.google.android.gms.common.api.Status status =
                driveContents.commit(getGoogleApiClient(), null).await();
        DebugLog.logMessage("Status code: " + status.getStatus().getStatusCode()
                + ", Status message: " + status.getStatus().getStatusMessage());
        return status.getStatus().isSuccess();
    }

    /**
     * Returns the json string of all the coupons present in the app.
     */
    private String getCouponsJson() {
        DebugLog.logMethod();
        ArrayList<Coupon> coupons = getCoupons();
        if (coupons == null || coupons.size() == 0) {
            showError("Error fetching coupons from app. Please try again");
            return null;
        }

        Gson gson = new Gson();
        return gson.toJson(coupons);
    }

    /**
     * Returns an {@link ArrayList} of {@link Coupon Coupons} that are saved
     * in the app. Returns null if any error while performing the query.
     */
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
