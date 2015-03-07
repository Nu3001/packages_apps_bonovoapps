package com.bonovo.keyeditor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KeyReceiver extends BroadcastReceiver {
	
	private static final String TAG = "KeyReceiver";
	private static final boolean DEBUG = false;
	
	public KeyReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO: This method is called when the BroadcastReceiver is receiving
		// an Intent broadcast.
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                 || intent.getAction().equals("android.intent.action.PRE_LAUNCHER"))
			{
			Log.d(TAG, "intent:" + intent.getAction());
			
			Intent intent_service = new Intent(context, KeyService.class);
			intent_service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(intent_service);
		}
	}
}
