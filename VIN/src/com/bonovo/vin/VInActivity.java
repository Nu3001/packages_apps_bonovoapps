package com.bonovo.vin;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class VInActivity extends Activity {

	private static final String TAG = "VIn";
	private static final boolean DEBUG = false;
	private boolean mIsVideoOn = false;
	private boolean mIsOpenSerial = false;
	
	public enum PhoneState{
		IDLE, RINGING, DIALING, ACTIVE, OFFHOOK;
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			String phoneState = PhoneState.OFFHOOK.toString();
			if("android.intent.action.PHONE_STATE_CHANGED".equals(action)){
				phoneState = intent.getStringExtra("phone_status");
				if(phoneState.equals(PhoneState.OFFHOOK.toString()) || 
						(phoneState.equals(PhoneState.IDLE.toString()))){
					if(!mIsVideoOn && jniVideoInSwitch(true)){
						mIsVideoOn = true;
						Intent intent1 = new Intent(VInActivity.this, VInActivity.class);
						intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent1);
					}
				}else if(phoneState.equals(PhoneState.ACTIVE.toString()) || 
						phoneState.equals(PhoneState.RINGING.toString()) || 
						phoneState.equals(PhoneState.DIALING.toString())){
					
					boolean ret = jniVideoInSwitch(false);
					if(mIsVideoOn && ret){
						mIsVideoOn = false;
					}
				}
			}
		}
		
	};
	
	private IntentFilter getIntentFilter() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("android.intent.action.PHONE_STATE_CHANGED");
		return myIntentFilter;
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vin);
		registerReceiver(mBroadcastReceiver, getIntentFilter());
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		boolean ret = false;
		if(mIsOpenSerial){
			ret = jniVideoInSwitch(false);
			if(ret){
				mIsVideoOn = false;
			}else{
				if(DEBUG) Log.d(TAG, "jniVideoInSwitch(false) : " + ret);
			}
			jniCloseSerial();
			mIsOpenSerial = false;
		}else{
			if(DEBUG) Log.d(TAG, "Serial donot had opened.");
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mIsVideoOn = false;
		mIsOpenSerial = jniOpenSerial();
		if(mIsOpenSerial){
			boolean ret = jniVideoInSwitch(true);
			if(ret){
				mIsVideoOn = true;
			}else{
				if(DEBUG) Log.d(TAG, "jniVideoInSwitch(true) : " + ret);
			}
		}else{
			if(DEBUG) Log.e(TAG, "Serial donot had opened.");
			Toast.makeText(getApplicationContext(), R.string.failed_open_serial, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.vin, menu);
		return true;
	}

	static {
		System.loadLibrary("bonovovin");
	}
	private native final boolean jniOpenSerial() throws IllegalStateException;
	private native final void jniCloseSerial() throws IllegalStateException;
	private native final boolean jniVideoInSwitch(boolean offOn) throws IllegalStateException;
}
