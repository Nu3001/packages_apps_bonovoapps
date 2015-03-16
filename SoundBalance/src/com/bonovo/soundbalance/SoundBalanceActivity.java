package com.bonovo.soundbalance;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.content.IntentFilter;
import android.content.Intent;

public class SoundBalanceActivity extends Activity {
    private static final String TAG = "SoundBalanceActivity";
    private BroadcastReceiver mSoundBalanceReceiver;
    public int myCarType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_balance);
		
        Log.d("SoundBalanceActivity", "========onCreate========");
        IntentFilter intentFilter = new IntentFilter("com.android.internal.car.can.action.CAR_TYPE_RESPONSE");
        intentFilter.addCategory("com.android.internal.car.can.Car");
        registerReceiver(mSoundBalanceReceiver, intentFilter);
	}

	@Override  
    protected void onResume() {
        super.onResume();
        Intent it = new Intent("com.android.internal.car.can.action.CAR_TYPE_REQUEST");
        sendBroadcast(it);
    }


    public void swithchCarType() {
        switch(myCarType) {
            case 0:
            {
                Intent activityIntentGeneral = new Intent(this, GeneralActivity.class);
                startActivity(activityIntentGeneral);
                finish();
                break;
            }
            case 1:
            {
                Intent activityIntentSonata = new Intent(this, SonataActivity.class);
                startActivity(activityIntentSonata);
                finish();
                break;
            }
        }
    }
	

	@Override
	protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSoundBalanceReceiver);
    }
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals("android.intent.action.BONOVO_SOUND_BALANCE")){

                Log.d(TAG, "=====mSoundBalanceReceiver=====");
                if(intent.getAction().equals("com.android.internal.car.can.action.CAR_TYPE_RESPONSE")) {
                    myCarType = intent.getIntExtra("car_type", 0x0);
                    Log.d(TAG, "myCarType=====" + myCarType);
                    swithchCarType();
				}
			}
		}
	};
}
