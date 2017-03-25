package com.darsh.couponstracker.controller.event;

/**
 * <p>Created by darshan on 25/3/17.
 *
 * <p>Event is posted when a CouponFragment in CREATE or EDIT mode is
 * either loaded or removed. When loaded, showFab is set to false, else
 * true. This is a hacky and overkill of a way to hide and show fab in
 * tablets when CouponFragments are loaded in CREATE or EDIT mode. Will
 * update this logic later.
 */

public class FabVisibilityChangeEvent {
    public boolean showFab;

    public FabVisibilityChangeEvent(boolean showFab) {
        this.showFab = showFab;
    }
}
