
package com.radio.widget;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class RadioPlayerStatusStore {
    public static final String ACTION_STATUS = "com.bonovo.radio.RADIO_STATUS";
    private static final String SP_NAME = "radio_status";
    public static final String KEY_FM_AM = "fm_am";
    public static final String KEY_PLAY_STOP = "play_stop";
    public static final String KEY_HZ = "hz";
    public static final String VALUE_FM = "FM";
    public static final String VALUE_AM = "AM";
    public static final String VALUE_COLLECT = "收藏栏";
    public static final String VALUE_PLAY = "play";
    public static final String VALUE_STOP = "stop";

    private RadioPlayerStatusStore() {
    }

    private static RadioPlayerStatusStore sRadioPlayerStatusStore;
    private WeakReference<Context> mContextRef;

    public static RadioPlayerStatusStore getInstance() {
        if (sRadioPlayerStatusStore == null) {
            sRadioPlayerStatusStore = new RadioPlayerStatusStore();
        }
        return sRadioPlayerStatusStore;
    }

    public void setContext(final Context context) {
        mContextRef = new WeakReference<Context>(context);
        init();
    }

    public Context getContext() {
        if (mContextRef != null)
            return mContextRef.get();
        return null;
    }

    private SharedPreferences mSp;

    public void init() {
        final Context context = getContext();
        if (context == null)
            return;
        mSp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public void clear() {
        mSp.edit().clear();
    }

    public void put(String key, String value) {
        mSp.edit().putString(key, value).commit();
        broadcastStatus();
    }

    public String get(String key) {
        return mSp.getString(key, "");
    }

    private void broadcastStatus() {
        final Context context = getContext();
        if (context == null)
            return;
        Intent intent = new Intent(ACTION_STATUS);
        intent.putExtra(KEY_FM_AM, get(KEY_FM_AM));
        intent.putExtra(KEY_HZ, get(KEY_HZ));
        intent.putExtra(KEY_PLAY_STOP, get(KEY_PLAY_STOP));
        context.sendBroadcast(intent);
    }
}
