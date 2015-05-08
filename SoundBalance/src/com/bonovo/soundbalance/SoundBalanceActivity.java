package com.bonovo.soundbalance;

import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class SoundBalanceActivity extends Activity {

	private static final String TAG = "SoundBalanceActivity";
	public int myCarType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_layout);
		Log.d(TAG, "========onCreate========");
		
		IntentFilter intentFilter = new IntentFilter("com.android.internal.car.can.action.CAR_TYPE_RESPONSE");
		intentFilter.addCategory("com.android.internal.car.can.Car");
		this.registerReceiver(mSoundBalanceReceiver, intentFilter);
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Intent it = new Intent("com.android.internal.car.can.action.CAR_TYPE_REQUEST");
		this.sendBroadcast(it);
	}
	

	private BroadcastReceiver mSoundBalanceReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.d(TAG, "=====mSoundBalanceReceiver=====");
			if(intent.getAction().equals("com.android.internal.car.can.action.CAR_TYPE_RESPONSE")){
				myCarType = intent.getIntExtra("car_type", 0);
				Log.d(TAG, "myCarType====="+myCarType);
				swithchCarType();
			}
		}
	};
	
	public void swithchCarType() {
		switch (myCarType) {
		case 0:
			Intent activityIntentGeneral = new Intent(
					SoundBalanceActivity.this, GeneralActivity.class);
			startActivity(activityIntentGeneral);
			finish();
			break;
		case 1:
			Intent activityIntentSonata = new Intent(SoundBalanceActivity.this,
					SonataActivity.class);
			startActivity(activityIntentSonata);
			finish();
			break;
		default:
			Intent activityIntentGeneralTwo = new Intent(
					SoundBalanceActivity.this, GeneralActivity.class);
			startActivity(activityIntentGeneralTwo);
			finish();
			break;
		}

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mSoundBalanceReceiver);
	}

}
