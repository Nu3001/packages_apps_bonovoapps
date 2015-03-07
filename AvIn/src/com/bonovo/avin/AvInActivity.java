package com.bonovo.avin;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class AvInActivity extends Activity {

	private static final String TAG = "AvInActivity";
	private static final boolean DEBUG = false;
	//private TextView mTextView;
	private boolean mIsAvInOn = false;
	private boolean mIsOpenSerial = false;
	
	private native final boolean jniopenserial() throws IllegalStateException;
	private native final boolean jniAvInSwitch(boolean offOn) throws IllegalStateException;
	private native final void jnicloseserial() throws IllegalStateException;
	
	public enum PhoneState{
		IDLE, RINGING, DIALING, ACTIVE, OFFHOOK;
	}
	
	static {
		System.loadLibrary("bonovoavin");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_av_in);
		if(DEBUG) Log.d(TAG, "onCreate()");
		//mTextView = (TextView)findViewById(R.id.tvShowInfo);
		registerReceiver(mBroadcastReceiver, getIntentFilter());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.av_in, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(DEBUG) Log.d(TAG, "onDestroy()");
		unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(DEBUG) Log.d(TAG, "onPause()");
		
		boolean ret = false;
		if(mIsOpenSerial){
			if(mIsAvInOn){
				if(jniAvInSwitch(false)){
					mIsAvInOn = false;
				}
			}
			jnicloseserial();
			mIsOpenSerial = false;
		}else{
			if(DEBUG) Log.d(TAG, "Serial open failed.");
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(DEBUG) Log.d(TAG, "onResume()");
		
		mIsAvInOn = false;
		mIsOpenSerial = jniopenserial();
		if(mIsOpenSerial){
			boolean ret = jniAvInSwitch(true);
			if(ret){
				mIsAvInOn = true;
			}
			if(DEBUG) Log.d(TAG, "jniAvInSwitch(true) : " + ret);
		}else{
			if(DEBUG) Log.d(TAG, "Serial open failed.");
			Toast.makeText(getApplicationContext(), "Open serial failed!", Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	private IntentFilter getIntentFilter() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("android.intent.action.PHONE_STATE_CHANGED");
		return myIntentFilter;
	};
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			String phoneState = PhoneState.OFFHOOK.toString();
			if("android.intent.action.PHONE_STATE_CHANGED".equals(action)){
				phoneState = intent.getStringExtra("phone_status");
				if(phoneState.equals(PhoneState.OFFHOOK.toString()) || 
						(phoneState.equals(PhoneState.IDLE.toString()))){
					if(!mIsAvInOn && jniAvInSwitch(true)){
						mIsAvInOn = true;
						Intent intent1 = new Intent(AvInActivity.this, AvInActivity.class);
						intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent1);
					}
				}else if(phoneState.equals(PhoneState.ACTIVE.toString()) || 
						phoneState.equals(PhoneState.RINGING.toString()) || 
						phoneState.equals(PhoneState.DIALING.toString())){
					
					boolean ret = jniAvInSwitch(false);
					if(mIsAvInOn && ret){
						mIsAvInOn = false;
					}
				}
			}
		}
		
	};

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		// TODO Auto-generated method stub
//		if(DEBUG) Log.d(TAG, "keyCode:" + keyCode);
//		if(KeyEvent.KEYCODE_BACK == keyCode){
//			if(mTextView != null){
//				mTextView.setText("Capture KEYCODE_BACK");
//			}
//		}
//		return super.onKeyDown(keyCode, event);
//	}

}
