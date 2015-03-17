package com.example.radio;

import android.app.Activity;
import android.content.ServiceConnection;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.content.Intent;
import android.content.ComponentName;
import android.os.IBinder;
import android.app.Fragment;

public class IntentActivity extends Activity implements ServiceConnection, MyPreferenceFragment.CallbackSetting {
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	private RadioService radioService;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty_layout);
		Intent settingIntent = new Intent("com.example.RadioService");
		bindService(settingIntent, this, 1);
	}
	
	protected void onResume() {
		super.onResume();
	}
	
	public void onServiceConnected(ComponentName name, IBinder service) {
		radioService = ((RadioService.ServiceBinder)ibinder)service.getService();
		fragmentManager = getFragmentManager();
		fragmentTransaction = fragmentManager.beginTransaction();
		MyPreferenceFragment fragment = new MyPreferenceFragment();
		fragmentTransaction.replace(R.id.empty, fragment).commit();
	}
	
	public void onServiceDisconnected(ComponentName name) {
		radioService = null;
	}
	
	protected void onDestroy() {
		super.onDestroy();
		radioService = null;
		unbindService(this);
	}
	
	public int getVolume() {
		return radioService.getVolume();
	}
	
	public void setVolume(int volume) {
		radioService.setVolume(volume);
	}
	
	public void setRemoteModel(boolean flag) {
		if(flag) {
			radioService.setRemote(1);
		} else {
			radioService.setRemote(0);
		}
	}
	
	public void readModelInfo() {
		radioService.readAndSetModelInfo();
	}
}
