
package com.newsmy.car.radar;

import com.android.internal.car.can.Radar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by zybo on 8/13/14.
 */
public class RadarReceiver extends BroadcastReceiver {

    public static boolean mReverseStatus; // reverse status
    public static boolean mIsCamerCheck;  // camer status
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if(action.equals("com.android.internal.car.can.action.RECEIVED")){
            Intent activityIntent = new Intent(context, NewsmyCarRadarActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.putExtra(Radar.BUNDLE_NAME,
                    intent.getBundleExtra(Radar.BUNDLE_NAME));
            context.startActivity(activityIntent);
        }else if(action.equals("android.intent.action.KEYCODE_BONOVO_REVERSE_STATUS")){
            mReverseStatus = intent.getBooleanExtra("reverse_status", false);
            mIsCamerCheck  = intent.getBooleanExtra("camer_flag", false);
            Log.d("Radar", "==== mReverseStatus:" + mReverseStatus + "  mIsCamerCheck:" + mIsCamerCheck);
        }
    }
}
