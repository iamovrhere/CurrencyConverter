package com.ovrhere.android.currencyconverter.test;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

/** Simple text content observer to check when items change. */
public class UtilityTestContentObserver extends ContentObserver {
    final HandlerThread mHT;
    boolean mContentChanged;

    public static UtilityTestContentObserver newTestContentObserver() {
        HandlerThread ht = new HandlerThread("ContentObserverThread");
        ht.start();
        return new UtilityTestContentObserver(ht);
    }

    private UtilityTestContentObserver(HandlerThread ht) {
        super(new Handler(ht.getLooper()));
        mHT = ht;
    }

    // On earlier versions of Android, this onChange method is called
    @Override
    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        mContentChanged = true;
    }

    public void waitForNotificationOrFail() {
        // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
        new PollingCheck(5000) {
            @Override
            protected boolean check() {
                return mContentChanged;
            }
        }.run();
        mHT.quit();
    }
}