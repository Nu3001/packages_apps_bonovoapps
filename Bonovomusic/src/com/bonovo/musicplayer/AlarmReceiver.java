package com.bonovo.musicplayer;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by zybo on 11/6/14.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    private static final boolean D = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (D)
            Log.d(TAG, "watch dog , wang wang wang ...");
        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!BonovoAudioPlayerService.sRunning) {
            nm.cancel(BonovoAudioPlayerService.NOTIFICATION_ID);
            final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(BonovoAudioPlayerService.getAlarmSender(context));
        }

    }
}
