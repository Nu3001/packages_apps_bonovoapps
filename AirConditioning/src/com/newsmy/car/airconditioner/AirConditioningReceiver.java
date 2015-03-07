package com.newsmy.car.airconditioner;

import com.android.internal.car.can.AirConditioning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by zybo on 8/6/14.
 */
public class AirConditioningReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
//        if (action.equals(AirConditioning.ACTION_NAME)) {
            Intent activityIntent = new Intent(context, NewsmyAirConditionerActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.putExtra(AirConditioning.BUNDLE_NAME,
                    intent.getBundleExtra(AirConditioning.BUNDLE_NAME));
            context.startActivity(activityIntent);
 //       }
    }
}
