
package com.radio.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.radio.R;
import com.example.radio.RadioActivity;
import com.example.radio.RadioService;

public class RadioPlayerAppWidget extends AppWidgetProvider {
    private static final String TAG = "RadioPlayerAppWidget";
    public static final String WIDGET_ACTION = "com.bonovo.radio.widget.ACTION_PLAY_STOP";
    private static final boolean D = true;
    private RadioPlayerStatusStore mRadioPlayerStatusStore;
    // private Intent mRadioStartIntent;
    // private Intent mStopIntent;
    private ComponentName mServiceComponent;
    private Intent mPlayStopIntent;
    private Intent mNextIntent;

    public RadioPlayerAppWidget() {
        mServiceComponent = new ComponentName("com.example.radio",
                "com.example.radio.RadioService");
        mPlayStopIntent = new Intent(WIDGET_ACTION);
        mNextIntent = new Intent(RadioService.ACTION_NEXT);
        mNextIntent.setComponent(mServiceComponent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (D)
            Log.d(TAG, "app widget , action : " + intent.getAction());
        if (mRadioPlayerStatusStore == null) {
            mRadioPlayerStatusStore = RadioPlayerStatusStore.getInstance();
            if (mRadioPlayerStatusStore.getContext() == null)
                mRadioPlayerStatusStore.setContext(context);
        }

        final String action = intent.getAction();
        if (WIDGET_ACTION.equals(action)) {
            String playStop = mRadioPlayerStatusStore.get(RadioPlayerStatusStore.KEY_PLAY_STOP);
            if (D)
                Log.d(TAG, "play stop : " + playStop);
            if (RadioPlayerStatusStore.VALUE_PLAY.equals(playStop)) {
                final Intent serviceIntent = new Intent();
                serviceIntent.setComponent(mServiceComponent);
                context.stopService(serviceIntent);
            }
            if (RadioPlayerStatusStore.VALUE_STOP.equals(playStop) ||
                    playStop.equals("")) {
                final Intent serviceIntent = new Intent();
                serviceIntent.setComponent(mServiceComponent);
                context.startService(serviceIntent);
            }
        }
        if (RadioPlayerStatusStore.ACTION_STATUS.equals(action)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            final Bundle bundle = intent.getExtras();
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(
                    "com.example.radio", "com.radio.widget.RadioPlayerAppWidget"));
            String fmAm = bundle.getString(RadioPlayerStatusStore.KEY_FM_AM);
            String hz = bundle.getString(RadioPlayerStatusStore.KEY_HZ);
            String playStop = bundle.getString(RadioPlayerStatusStore.KEY_PLAY_STOP);

            if (D)
                Log.d(TAG, "fmam : " + fmAm + ", hz : " + hz + ", play stop : " + playStop);

            for (int appWidgetId : appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId, fmAm, hz, playStop);
            }
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        String fmAm = mRadioPlayerStatusStore.get(RadioPlayerStatusStore.KEY_FM_AM);
        String hz = mRadioPlayerStatusStore.get(RadioPlayerStatusStore.KEY_HZ);
        String playStop = mRadioPlayerStatusStore.get(RadioPlayerStatusStore.KEY_PLAY_STOP);
        if (D)
            Log.d(TAG, "fmam : " + fmAm + ", hz : " + hz + ", play stop : " + playStop);
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, fmAm, hz, playStop);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
            String fmam, String hz, String playStop) {
        RemoteViews remoteViews = new RemoteViews("com.example.radio",
                R.layout.radio_player_app_widget_layout);
        remoteViews.setTextViewText(R.id.widget_text_fm_am, fmam);
        if (!TextUtils.isEmpty(hz)) {
            float radioHz = Float.parseFloat(hz);
            if (D)
                Log.d(TAG, " float radioHz = "+radioHz);
            if (fmam.equals(RadioPlayerStatusStore.VALUE_FM)){
            	radioHz = radioHz / 100;
            	remoteViews.setTextViewText(R.id.widget_text_number, String.valueOf(radioHz));
            }else if (fmam.equals(RadioPlayerStatusStore.VALUE_AM)) {
            	remoteViews.setTextViewText(R.id.widget_text_number, String.valueOf(hz));
			}else if(fmam.equals(RadioPlayerStatusStore.VALUE_COLLECT)){
				if(Integer.parseInt(hz) > RadioService.FM_LOW_FREQ){
					radioHz = radioHz / 100;
					remoteViews.setTextViewText(R.id.widget_text_number, String.valueOf(radioHz));
				}else {
					remoteViews.setTextViewText(R.id.widget_text_number, String.valueOf(hz));
				}
				
			}
                
            
        } else {
            remoteViews.setTextViewText(R.id.widget_text_number, "");
        }
        if (D)
            Log.d(TAG, "play or stop : " + playStop);
        if (playStop.equals(RadioPlayerStatusStore.VALUE_PLAY)) {
            remoteViews.setImageViewResource(R.id.widget_image_play_stop,
                    R.drawable.radio_player_app_widget_stop_background);
            // remoteViews.setOnClickPendingIntent(R.id.widget_image_play_stop,
            // PendingIntent
            // .getService(context, 0, mRadioStartIntent,
            // PendingIntent.FLAG_UPDATE_CURRENT));
            remoteViews.setOnClickPendingIntent(R.id.widget_image_play_stop, PendingIntent
                    .getBroadcast(context, 0, mPlayStopIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        } else {
            remoteViews.setImageViewResource(R.id.widget_image_play_stop,
                    R.drawable.radio_player_app_widget_play_background);
            // remoteViews.setOnClickPendingIntent(R.id.widget_image_play_stop,
            // PendingIntent
            // .getService(context, 0, mStopIntent,
            // PendingIntent.FLAG_UPDATE_CURRENT));
            remoteViews.setOnClickPendingIntent(R.id.widget_image_play_stop, PendingIntent
                    .getBroadcast(context, 0, mPlayStopIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        }
        remoteViews.setOnClickPendingIntent(R.id.widget_image_next, PendingIntent.getService(
                context, 0, mNextIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context, RadioActivity.class));
        remoteViews.setOnClickPendingIntent(R.id.widget_view_container,
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

    }
}
