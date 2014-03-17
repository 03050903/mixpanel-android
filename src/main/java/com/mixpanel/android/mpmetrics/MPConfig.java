package com.mixpanel.android.mpmetrics;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

/**
 * Stores global configuration options for the Mixpanel library.
 * May be overridden to achieve custom behavior.
 */
/* package */ class MPConfig {
    public static final String VERSION = "4.0.1-RC1";

    // Set to true to see lots of internal debugging logcat output.
    // This should be set to false in production builds
    public static final boolean DEBUG = true;

    // Name for persistent storage of app referral SharedPreferences
    /* package */ static final String REFERRER_PREFS_NAME = "com.mixpanel.android.mpmetrics.ReferralInfo";

    // Max size of Notification and Survey caches. Since they may contain images, we don't want to
    // suck up all of the memory on the device, particularly since multiple notifications or surveys
    // in a single session isn't really an ideal UX
    /* package */ static final int MAX_UPDATE_CACHE_ELEMENT_COUNT = 4;

    public static MPConfig readConfig(Context context) {
        final String packageName = context.getPackageName();
        try {
            final ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            Bundle configBundle = appInfo.metaData;
            if (null == configBundle) {
                configBundle = new Bundle();
            }
            return new MPConfig(configBundle);
        } catch (final NameNotFoundException e) {
            throw new RuntimeException("Can't configure Mixpanel with package name " + packageName, e);
        }
    }

    public MPConfig(Bundle metaData) {
        if (metaData.containsKey("com.mixpanel.android.MPConfig.AutoCheckForSurveys")) {
            Log.w(LOGTAG, "com.mixpanel.android.MPConfig.AutoCheckForSurveys has been deprecated in favor of " +
                          "com.mixpanel.android.MPConfig.AutoCheckMixpanelData. Please update this key as soon as possible.");
        }

        mBulkUploadLimit = metaData.getInt("com.mixpanel.android.MPConfig.BulkUploadLimit", 40); // 40 records default
        mFlushInterval = metaData.getInt("com.mixpanel.android.MPConfig.FlushInterval", 60 * 1000); // one minute default
        mDataExpiration = metaData.getInt("com.mixpanel.android.MPConfig.DataExpiration",  1000 * 60 * 60 * 48); // 48 hours default
        mDisableFallback = metaData.getBoolean("com.mixpanel.android.MPConfig.DisableFallback", true);
        mAutoCheckMixpanelData = metaData.getBoolean("com.mixpanel.android.MPConfig.AutoCheckMixpanelData", true) ||
                                 metaData.getBoolean("com.mixpanel.android.MPConfig.AutoCheckForSurveys", true);

        String eventsEndpoint = metaData.getString("com.mixpanel.android.MPConfig.EventsEndpoint");
        if (null == eventsEndpoint) {
            eventsEndpoint = "https://api.mixpanel.com/track?ip=1";
        }
        mEventsEndpoint = eventsEndpoint;

        String eventsFallbackEndpoint = metaData.getString("com.mixpanel.android.MPConfig.EventsFallbackEndpoint");
        if (null == eventsFallbackEndpoint) {
            eventsFallbackEndpoint = "http://api.mixpanel.com/track?ip=1";
        }
        mEventsFallbackEndpoint = eventsFallbackEndpoint;

        String peopleEndpoint = metaData.getString("com.mixpanel.android.MPConfig.PeopleEndpoint");
        if (null == peopleEndpoint) {
            peopleEndpoint = "https://api.mixpanel.com/engage";
        }
        mPeopleEndpoint = peopleEndpoint;

        String peopleFallbackEndpoint = metaData.getString("com.mixpanel.android.MPConfig.PeopleFallbackEndpoint");
        if (null == peopleFallbackEndpoint) {
            peopleFallbackEndpoint = "http://api.mixpanel.com/engage";
        }
        mPeopleFallbackEndpoint = peopleFallbackEndpoint;

        String decideEndpoint = metaData.getString("com.mixpanel.android.MPConfig.DecideEndpoint");
        if (null == decideEndpoint) {
            decideEndpoint = "https://decide.mixpanel.com/decide";
        }
        mDecideEndpoint = decideEndpoint;

        String decideFallbackEndpoint = metaData.getString("com.mixpanel.android.MPConfig.DecideFallbackEndpoint");
        if (null == decideFallbackEndpoint) {
            decideFallbackEndpoint = "http://decide.mixpanel.com/decide";
        }
        mDecideFallbackEndpoint = decideFallbackEndpoint;

        if (DEBUG) {
            Log.d(LOGTAG,
                "Mixpanel configured with:\n" +
                "    AutoCheckMixpanelData " + getAutoCheckMixpanelData() + "\n" +
                "    BulkUploadLimit " + getBulkUploadLimit() + "\n" +
                "    FlushInterval " + getFlushInterval() + "\n" +
                "    DataExpiration " + getDataExpiration() + "\n" +
                "    DisableFallback " + getDisableFallback() + "\n" +
                "    EventsEndpoint " + getEventsEndpoint() + "\n" +
                "    PeopleEndpoint " + getPeopleEndpoint() + "\n" +
                "    DecideEndpoint " + getDecideEndpoint() + "\n" +
                "    EventsFallbackEndpoint " + getEventsFallbackEndpoint() + "\n" +
                "    PeopleFallbackEndpoint " + getPeopleFallbackEndpoint() + "\n" +
                "    DecideFallbackEndpoint " + getDecideFallbackEndpoint() + "\n"
            );
        }
    }

    // Max size of queue before we require a flush. Must be below the limit the service will accept.
    public int getBulkUploadLimit() {
        return mBulkUploadLimit;
    }

    // Target max milliseconds between flushes. This is advisory.
    public int getFlushInterval() {
        return mFlushInterval;
    }

    // Throw away records that are older than this in milliseconds. Should be below the server side age limit for events.
    public int getDataExpiration() {
        return mDataExpiration;
    }

    public boolean getDisableFallback() {
        return mDisableFallback;
    }

    // Preferred URL for tracking events
    public String getEventsEndpoint() {
        return mEventsEndpoint;
    }

    // Preferred URL for tracking people
    public String getPeopleEndpoint() {
        return mPeopleEndpoint;
    }

    // Preferred URL for pulling decide data
    public String getDecideEndpoint() {
        return mDecideEndpoint;
    }

    // Fallback URL for tracking events if post to preferred URL fails
    public String getEventsFallbackEndpoint() {
        return mEventsFallbackEndpoint;
    }

    // Fallback URL for tracking people if post to preferred URL fails
    public String getPeopleFallbackEndpoint() {
        return mPeopleFallbackEndpoint;
    }

    // Fallback URL for pulling decide data if preferred URL fails
    public String getDecideFallbackEndpoint() {
        return mDecideFallbackEndpoint;
    }

    // Check for and show eligible surveys and in app notifications on Activity changes
    public boolean getAutoCheckMixpanelData() {
        return mAutoCheckMixpanelData;
    }

    private final int mBulkUploadLimit;
    private final int mFlushInterval;
    private final int mDataExpiration;
    private final boolean mDisableFallback;
    private final String mEventsEndpoint;
    private final String mEventsFallbackEndpoint;
    private final String mPeopleEndpoint;
    private final String mPeopleFallbackEndpoint;
    private final String mDecideEndpoint;
    private final String mDecideFallbackEndpoint;
    private final boolean mAutoCheckMixpanelData;

    private static final String LOGTAG = "MixpanelAPI.MPConfig";
}
