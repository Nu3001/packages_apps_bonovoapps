package com.example.bonovomcu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class McuReceiver extends BroadcastReceiver{
	
	private static final String TAG = "com.example.bonovomcu.McuReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onReceive()");
		//start service with boot completed
		if(intent.getAction().equals(Intent.ACTION_PRE_LAUNCHER)){
			//Toast.makeText(context, "McuService has been worked!!!", Toast.LENGTH_SHORT).show();
			Intent myintent = new Intent(context, McuServicer.class);
			myintent.setAction("com.example.McuServicer");
			context.startService(myintent);
		}
		
	}
	
}
