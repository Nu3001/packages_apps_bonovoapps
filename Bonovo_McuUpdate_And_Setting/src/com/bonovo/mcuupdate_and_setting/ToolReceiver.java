package com.bonovo.mcuupdate_and_setting;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ToolReceiver extends BroadcastReceiver {
	private static final String TAG = "com.example.android_fragment_demo1.ToolReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.v(TAG, "onReceive()");
//		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			//Toast.makeText(context, "FragmentService has been worked!!!", Toast.LENGTH_SHORT).show();
			Intent myintent = new Intent(context, FragmentService.class);
			myintent.setAction("com.bonovo.mcuupdate_and_setting.FragmentService");
			context.startService(myintent);
//		}
	}
}
