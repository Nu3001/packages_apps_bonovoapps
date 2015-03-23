package com.bonovo.mcuupdate_and_setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ToolReceiver extends BroadcastReceiver {
    private static final String TAG = "com.example.android_fragment_demo1.ToolReceiver";
    
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive()");
        Intent myintent = new Intent(context, FragmentService.class);
        myintent.setAction("com.bonovo.mcuupdate_and_setting.FragmentService");
        context.startService(myintent);
    }
}
