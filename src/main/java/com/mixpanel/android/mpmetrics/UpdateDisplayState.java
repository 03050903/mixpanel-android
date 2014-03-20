package com.mixpanel.android.mpmetrics;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.mixpanel.android.surveys.SurveyActivity;

/**
 * This is a class intended for internal use by the library.
 * Users of the library should not interact with it directly.
 *
 * The interface to this class may change or disappear from release to release.
 */
@TargetApi(11)
public class UpdateDisplayState implements Parcelable {

    public static abstract class DisplayState implements Parcelable {

        private DisplayState() {}

        public abstract String getType();

        public static final class InAppNotificationState extends DisplayState {
            public static final String TYPE = "InAppNotificationState";

            public static final Creator<InAppNotificationState> CREATOR =
                    new Creator<InAppNotificationState>() {

                        @Override
                        public InAppNotificationState createFromParcel(final Parcel source) {
                            final Bundle read = new Bundle(InAppNotificationState.class.getClassLoader());
                            read.readFromParcel(source);
                            return new InAppNotificationState(read);
                        }

                        @Override
                        public InAppNotificationState[] newArray(final int size) {
                            return new InAppNotificationState[size];
                        }
                    };

            public InAppNotificationState(InAppNotification notification, int highlightColor) {
                mInAppNotification = notification;
                mHighlightColor = highlightColor;
            }

            public int getHighlightColor() {
                return mHighlightColor;
            }

            public InAppNotification getInAppNotification() {
                return mInAppNotification;
            }

            @Override
            public String getType() {
                return TYPE;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(final Parcel dest, final int flags) {
                final Bundle write = new Bundle();
                write.putParcelable(INAPP_KEY, mInAppNotification);
                write.putInt(HIGHLIGHT_KEY, mHighlightColor);
                dest.writeBundle(write);
            }

            private InAppNotificationState(Bundle in) {
                mInAppNotification = in.getParcelable(INAPP_KEY);
                mHighlightColor = in.getInt(HIGHLIGHT_KEY);
            }

            private final InAppNotification mInAppNotification;
            private final int mHighlightColor;

            private static String INAPP_KEY = "com.com.mixpanel.android.mpmetrics.UpdateDisplayState.InAppNotificationState.INAPP_KEY";
            private static String HIGHLIGHT_KEY = "com.com.mixpanel.android.mpmetrics.UpdateDisplayState.InAppNotificationState.HIGHLIGHT_KEY";
        }

        public static final class SurveyState extends DisplayState {
            public static final String TYPE = "SurveyState";

            public static final Creator<SurveyState> CREATOR =
                    new Creator<SurveyState>() {
                        @Override
                        public SurveyState createFromParcel(final Parcel source) {
                            final Bundle read = new Bundle(SurveyState.class.getClassLoader());
                            read.readFromParcel(source);
                            return new SurveyState(read);
                        }

                        @Override
                        public SurveyState[] newArray(final int size) {
                            return new SurveyState[size];
                        }
                    };

            public SurveyState(final Survey survey, final int highlightColor, final Bitmap background, final boolean showAskDialog) {
                mShowAskDialog = showAskDialog;
                mSurvey = survey;
                mHighlightColor = highlightColor;
                mBackground = background;
                mAnswers = new AnswerMap();
            }

            public Bitmap getBackground() {
                return mBackground;
            }

            public AnswerMap getAnswers() {
                return mAnswers;
            }

            public int getHighlightColor() {
                return mHighlightColor;
            }

            public Survey getSurvey() {
                return mSurvey;
            }

            @Override
            public String getType() {
                return TYPE;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(final Parcel dest, final int flags) {
                final Bundle out = new Bundle();
                out.putBoolean(SHOW_ASK_BUNDLE_KEY, mShowAskDialog);
                out.putInt(HIGHLIGHT_COLOR_BUNDLE_KEY, mHighlightColor);
                out.putParcelable(ANSWERS_BUNDLE_KEY, mAnswers);

                byte[] backgroundCompressed = null;
                if (mBackground != null) {
                    final ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    mBackground.compress(Bitmap.CompressFormat.PNG, 20, bs);
                    backgroundCompressed = bs.toByteArray();
                }
                out.putByteArray(BACKGROUND_COMPRESSED_BUNDLE_KEY, backgroundCompressed);
                out.putParcelable(SURVEY_BUNDLE_KEY, mSurvey);
                dest.writeBundle(out);
            }

            private SurveyState(Bundle in) {
                mShowAskDialog = in.getBoolean(SHOW_ASK_BUNDLE_KEY);
                mHighlightColor = in.getInt(HIGHLIGHT_COLOR_BUNDLE_KEY);
                mAnswers = in.getParcelable(ANSWERS_BUNDLE_KEY);

                final byte[] backgroundCompressed = in.getByteArray(BACKGROUND_COMPRESSED_BUNDLE_KEY);
                if (null != backgroundCompressed) {
                    mBackground = BitmapFactory.decodeByteArray(backgroundCompressed, 0, backgroundCompressed.length);
                } else {
                    mBackground = null;
                }

                mSurvey = in.getParcelable(SURVEY_BUNDLE_KEY);
            }

            private final boolean mShowAskDialog;
            private final Survey mSurvey;
            private final int mHighlightColor;
            private final AnswerMap mAnswers;
            private final Bitmap mBackground;

            private static final String SHOW_ASK_BUNDLE_KEY = "com.mixpanel.android.mpmetrics.UpdateDisplayState.SHOW_ASK_BUNDLE_KEY";
            private static final String SURVEY_BUNDLE_KEY = "com.mixpanel.android.mpmetrics.UpdateDisplayState.SURVEY_BUNDLE_KEY";
            private static final String HIGHLIGHT_COLOR_BUNDLE_KEY = "com.mixpanel.android.mpmetrics.UpdateDisplayState.HIGHLIGHT_COLOR_BUNDLE_KEY";
            private static final String ANSWERS_BUNDLE_KEY = "com.mixpanel.android.mpmetrics.UpdateDisplayState.ANSWERS_BUNDLE_KEY";
            private static final String BACKGROUND_COMPRESSED_BUNDLE_KEY = "com.mixpanel.android.mpmetrics.UpdateDisplayState.BACKGROUND_COMPRESSED_BUNDLE_KEY";
        }

        public static final Creator<DisplayState> CREATOR =
                new Creator<DisplayState>() {
                    @Override
                    public DisplayState createFromParcel(final Parcel source) {
                        final Bundle read = new Bundle(DisplayState.class.getClassLoader());
                        read.readFromParcel(source);
                        final String type = read.getString(STATE_TYPE_KEY);
                        final Bundle implementation = read.getBundle(STATE_IMPL_KEY);
                        if (InAppNotificationState.TYPE.equals(type)) {
                            return new InAppNotificationState(implementation);
                        } else if (SurveyState.TYPE.equals(type)) {
                            return new SurveyState(implementation);
                        } else {
                            throw new RuntimeException("Unrecognized display state type " + type);
                        }
                    }

                    @Override
                    public DisplayState[] newArray(final int size) {
                        return new DisplayState[size];
                    }
                };

        public static final String STATE_TYPE_KEY = "com.mixpanel.android.mpmetrics.UpdateDisplayState.DisplayState.STATE_TYPE_KEY";
        public static final String STATE_IMPL_KEY = "com.mixpanel.android.mpmetrics.UpdateDisplayState.DisplayState.STATE_IMPL_KEY";
    }

    public static final Parcelable.Creator<UpdateDisplayState> CREATOR = new Parcelable.Creator<UpdateDisplayState>() {
        @Override
        public UpdateDisplayState createFromParcel(Parcel in) {
            final Bundle read = new Bundle(UpdateDisplayState.class.getClassLoader());
            read.readFromParcel(in);
            return new UpdateDisplayState(read);
        }

        @Override
        public UpdateDisplayState[] newArray(int size) {
            return new UpdateDisplayState[size];
        }
    };

    /**
     * Client code should not call this method.
     */
    public static void proposeDisplay(final DisplayState state,
            final Activity parentActivity,
            final String distinctId,
            final String token) {
        if (! ConfigurationChecker.checkSurveyActivityAvailable(parentActivity.getApplicationContext())) {
            return;
        }

        synchronized(sUpdateDisplayLock) {
            final long currentTime = System.currentTimeMillis();
            final long deltaTime = currentTime - sUpdateDisplayLockMillis;

            if (sShowingIntentId > 0 && deltaTime > MAX_LOCK_TIME_MILLIS) {
                Log.i(LOGTAG, "UpdateDisplayState set long, long ago, without showing.");
                sUpdateDisplayState = null;
            }
            if (null == sUpdateDisplayState) {
                sUpdateDisplayState = new UpdateDisplayState(state, distinctId, token);
                final Intent surveyIntent = new Intent(parentActivity.getApplicationContext(), SurveyActivity.class);
                surveyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                surveyIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                UpdateDisplayState.sNextIntentId++;
                surveyIntent.putExtra(SurveyActivity.INTENT_ID_KEY, UpdateDisplayState.sNextIntentId);
                parentActivity.startActivity(surveyIntent);
            } else {
                if (MPConfig.DEBUG) Log.d(LOGTAG, "Already showing (or cooking) a survey, declining to show another.");
            }
        } // synchronized
    }

    /**
     * Client code should not call this method.
     */
    public static void releaseDisplayState(int intentId) {
        synchronized(sUpdateDisplayLock) {
            if (intentId == sShowingIntentId) {
                sShowingIntentId = -1;
                sUpdateDisplayState = null;
            }
        }
    }

    /**
     * Client code should not call this method.
     */
    public static UpdateDisplayState claimDisplayState(final int intentId) {
        synchronized(sUpdateDisplayLock) {
            final long currentTime = System.currentTimeMillis();
            final long deltaTime = currentTime - sIntentIdLockMillis;

            if (sShowingIntentId > 0 && deltaTime > MAX_LOCK_TIME_MILLIS) {
                Log.i(LOGTAG, "Survey activity claimed but never released lock, possible force quit.");
                sShowingIntentId = -1;
            }

            if (sShowingIntentId > 0 && sShowingIntentId != intentId) {
                // Someone else has claimed another intent already
                return null;
            } else if (sUpdateDisplayState == null) {
                // Nothing to claim, caller is too late (or crazy)
                return null;
            } else {
                // Claim is successful
                sShowingIntentId = intentId;
                return sUpdateDisplayState;
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final Bundle bundle = new Bundle();
        bundle.putString(DISTINCT_ID_BUNDLE_KEY, mDistinctId);
        bundle.putString(TOKEN_BUNDLE_KEY, mToken);
        bundle.putParcelable(DISPLAYSTATE_BUNDLE_KEY, mDisplayState);
        dest.writeBundle(bundle);
    }

    public DisplayState getDisplayState() {
        return mDisplayState;
    }

    public String getDistinctId() {
       return mDistinctId;
    }

    public String getToken() {
        return mToken;
    }

    // Package access for testing only- DO NOT CALL in library code
    /* package */ UpdateDisplayState(final DisplayState displayState, final String distinctId, final String token) {
        mDistinctId = distinctId;
        mToken = token;
        mDisplayState = displayState;
    }

    // Bundle must have the same ClassLoader that loaded this constructor.
    private UpdateDisplayState(Bundle read) {
        mDistinctId = read.getString(DISTINCT_ID_BUNDLE_KEY);
        mToken = read.getString(TOKEN_BUNDLE_KEY);
        mDisplayState = read.getParcelable(DISPLAYSTATE_BUNDLE_KEY);
    }

    /**
     * This class is intended for internal use by the Mixpanel library.
     * Users of the library should not interact directly with this class.
     */
    public static class AnswerMap implements Parcelable {

        @SuppressLint("UseSparseArrays")
        public AnswerMap() {
            mMap = new HashMap<Integer, String>();
        }

        public void put(Integer i, String s) {
            mMap.put(i, s);
        }

        public String get(Integer i) {
            return mMap.get(i);
        }

        public boolean contentEquals(AnswerMap other) {
            return mMap.equals(other.mMap);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            final Bundle out = new Bundle();
            for (final Map.Entry<Integer, String> entry:mMap.entrySet()) {
                final String keyString = Integer.toString(entry.getKey());
                out.putString(keyString, entry.getValue());
            }
            dest.writeBundle(out);
        }

        public static final Parcelable.Creator<AnswerMap> CREATOR =
            new Parcelable.Creator<AnswerMap>() {
            @Override
            public AnswerMap createFromParcel(Parcel in) {
                final Bundle read = new Bundle(AnswerMap.class.getClassLoader());
                final AnswerMap ret = new AnswerMap();
                read.readFromParcel(in);
                for (final String kString:read.keySet()) {
                    final Integer kInt = Integer.valueOf(kString);
                    ret.put(kInt, read.getString(kString));
                }
                return ret;
            }

            @Override
            public AnswerMap[] newArray(int size) {
                return new AnswerMap[size];
            }
        };

        private final HashMap<Integer, String> mMap;
    }

    private final String mDistinctId;
    private final String mToken;
    private final DisplayState mDisplayState;

    private static final Object sUpdateDisplayLock = new Object();
    private static long sUpdateDisplayLockMillis = -1;
    private static UpdateDisplayState sUpdateDisplayState = null;
    private static int sNextIntentId = 0;
    private static long sIntentIdLockMillis = -1;
    private static int sShowingIntentId = -1;

    private static final String LOGTAG = "MixpanelAPI UpdateDisplayState";
    private static final long MAX_LOCK_TIME_MILLIS = 12 * 60 * 60 * 1000; // Twelve hour timeout on survey activities

    private static final String DISTINCT_ID_BUNDLE_KEY = "com.mixpanel.android.mpmetrics.UpdateDisplayState.DISTINCT_ID_BUNDLE_KEY";
    private static final String TOKEN_BUNDLE_KEY = "com.mixpanel.android.mpmetrics.UpdateDisplayState.TOKEN_BUNDLE_KEY";
    private static final String DISPLAYSTATE_BUNDLE_KEY = "com.mixpanel.android.mpmetrics.UpdateDisplayState.DISPLAYSTATE_BUNDLE_KEY";

}
