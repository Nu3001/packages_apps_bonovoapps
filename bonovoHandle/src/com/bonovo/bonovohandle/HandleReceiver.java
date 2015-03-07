package com.bonovo.bonovohandle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HandleReceiver extends BroadcastReceiver {
	
	public static final String TAG = "HandleReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
//		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
//		    || intent.getAction().equals("android.intent.action.PRE_LAUNCHER"))
		{
			Log.d(TAG, "intent:" + intent.getAction());
			Intent intent_service = new Intent(context, HandleService.class);
			intent_service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(intent_service);
		}
	}

}
