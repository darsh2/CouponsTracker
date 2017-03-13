package com.darsh.couponstracker.logger;

import android.util.Log;

import com.darsh.couponstracker.BuildConfig;

/**
 * Created by darshan on 13/3/17.
 */

public class DebugLog {
    private static final boolean isDebugBuild = BuildConfig.DEBUG;
    private static boolean loggingEnabled = true;

    private static final int requiredMethodIndex = 3;

    private static boolean isLoggingEnabled() {
        return isDebugBuild && loggingEnabled;
    }

    public static void setLoggingEnabled(boolean loggingEnabled) {
        DebugLog.loggingEnabled = loggingEnabled;
    }

    public static void logMethod() {
        if (!isLoggingEnabled()) {
            return;
        }
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String className = stackTraceElements[requiredMethodIndex].getClassName();
        String methodName = stackTraceElements[requiredMethodIndex].getMethodName();
        Log.i(getTag(className), methodName);
    }

    public static void logMessage(String message) {
        if (!isLoggingEnabled()) {
            return;
        }
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String className = stackTraceElements[requiredMethodIndex].getClassName();
        Log.i(getTag(className), message);
    }

    private static String getTag(String className) {
        StringBuilder tag = new StringBuilder("DL-");
        for (char e : className.toCharArray()) {
            if (Character.isUpperCase(e)) {
                tag.append(e);
            }
        }
        return tag.toString();
    }
}
