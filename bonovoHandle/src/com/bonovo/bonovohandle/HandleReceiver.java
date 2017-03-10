package com.bonovo.bonovohandle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HandleReceiver extends BroadcastReceiver {
	
	public static final String TAG = "HandleReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "intent:" + intent.getAction());
		if (intent.getAction().equals("android.intent.action.PRE_LAUNCHER")) {
			Intent intent_service = new Intent(context, HandleService.class);
			intent_service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(intent_service);
		} else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Intent intent_media = new Intent(context, MediaListener.class);
			intent_media.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(intent_media);
		}
	}

}
