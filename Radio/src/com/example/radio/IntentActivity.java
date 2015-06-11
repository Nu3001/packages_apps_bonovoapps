package com.example.radio;

import com.example.radio.MyPreferenceFragment.CallbackSetting;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class IntentActivity extends Activity implements ServiceConnection, CallbackSetting{
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	private RadioService radioService;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.empty_layout);

		Intent settingIntent = new Intent("com.example.RadioService");
		this.bindService(settingIntent, this, BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// TODO Auto-generated method stub
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		radioService = ((RadioService.ServiceBinder) service).getService();
		fragmentManager = this.getFragmentManager();
		fragmentTransaction = fragmentManager.beginTransaction();

		MyPreferenceFragment fragment = new MyPreferenceFragment();

		fragmentTransaction.replace(R.id.empty, fragment).commit();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		radioService = null;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		radioService = null;
		this.unbindService(this);
	}

	@Override
	public int getVolume() {
		// TODO Auto-generated method stub
		return radioService.getVolume();
	}
	
	public void setVolume(int volume){
		radioService.setVolume(volume, true);
	}

	@Override
	public void setRemoteModel(boolean flag) {
		// TODO Auto-generated method stub
		if(flag){
			radioService.setRemote(1);
		}else {
			radioService.setRemote(0);
		}
	}

	@Override
	public void readModelInfo() {
		// TODO Auto-generated method stub
		radioService.readAndSetModelInfo();
	}

}
