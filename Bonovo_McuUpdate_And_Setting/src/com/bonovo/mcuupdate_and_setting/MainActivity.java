package com.bonovo.mcuupdate_and_setting;

import com.bonovo.mcuupdate_and_setting.McuFragment.CallbackMcu;
import com.bonovo.mcuupdate_and_setting.RightFragmentKeysBackLight.CallBackColor;
import com.bonovo.mcuupdate_and_setting.RightFragmentOTGModel.CallBackOTG;
import com.bonovo.mcuupdate_and_setting.RightFragmentSetting.CallbackInterface;
import com.bonovo.mcuupdate_and_setting.RightFragmentStandby.CallBackStandby;
import com.bonovo.mcuupdate_and_setting.RigthFragmentVersion.CallbackFragment1;
import com.bonovo.mcuupdate_and_setting.BTUpdateFragment.CallbackMcuOP;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.SystemProperties;

public class MainActivity extends Activity implements ServiceConnection ,
		CallbackInterface ,CallbackFragment1 ,CallbackMcu ,CallBackOTG ,CallbackMcuOP,
		CallBackColor ,CallBackStandby{
	
	private final String TAG = "com.example.fragment.mainactivity";
	
	private FragmentManager manager;
	private FragmentTransaction fragmentTransaction;
	private FragmentService mService;
	PowerManager powerManager = null;  
    WakeLock wakeLock = null; 
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent("com.bonovo.mcuupdate_and_setting.FragmentService");
		this.bindService(intent, this, BIND_AUTO_CREATE);
		Log.v(TAG, "-->Activity-->onCreate()");
		setContentView(R.layout.activity_main);
		manager = getFragmentManager();
		fragmentTransaction = manager.beginTransaction();
		
		LeftFragment leftfragment = new LeftFragment();
		fragmentTransaction.add(R.id.left, leftfragment,"leftfragment");
		
		fragmentTransaction.commit();
		
		powerManager = (PowerManager)this.getSystemService(this.POWER_SERVICE);
		wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		Log.v(TAG, "-->Activity-->onServiceConnected()");
		mService = ((FragmentService.ServiceBinder)service).getServicer();
		
		manager = getFragmentManager();
		fragmentTransaction = manager.beginTransaction();
		
		RigthFragmentVersion rigthFragment = new RigthFragmentVersion();
		fragmentTransaction.add(R.id.rigth, rigthFragment,"rigthFragment");
		
		fragmentTransaction.commit();	
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.v(TAG, "-->Activity-->onStart()");
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		wakeLock.acquire(); 
		Log.v(TAG, "-->Activity-->onResume()");
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		wakeLock.release();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mService = null;
		this.unbindService(this);
	}
	
	//fu  xie  interface 
	@Override
	public void setMute(boolean flag) {
		// TODO Auto-generated method stub
		mService.setAsternMute(flag);
	}

	@Override
	public void setCamera(boolean flag) {
		// TODO Auto-generated method stub
		mService.setRearviewCamera(flag);
		
	}

	@Override
	public void lowBrigthness(int progress) {
		// TODO Auto-generated method stub
		mService.lowBrightness(progress);
	}

	@Override
	public int getMcuVersion() {
		// TODO Auto-generated method stub
		return mService.version();
	}

	@Override
	public String getSystemVersion() {
		// TODO Auto-generated method stub
		String str = SystemProperties.get("ro.rksdk.version",Build.UNKNOWN);
		return str;
	}

	@Override
	public void autoVolume(int Progress) {
		// TODO Auto-generated method stub
		mService.volumePercent(Progress);
	}

	@Override
	public boolean checkSDCard() {
		// TODO Auto-generated method stub
		
		return mService.checkSDCard();
	}

	@Override
	public boolean checkFile() {
		// TODO Auto-generated method stub
		return mService.checkFile();
	}

	@Override
	public void cpyfile() {
		// TODO Auto-generated method stub
		mService.cpyfile();
	}

	@Override
	public boolean checkMcu() {
		// TODO Auto-generated method stub
		return mService.checkMcu();
	}

	@Override
	public int wipeMcuAPP() {
		// TODO Auto-generated method stub
		return mService.wipeMcuAPP();
	}

	@Override
	public boolean checkdBuffer() {
		// TODO Auto-generated method stub
		return mService.checkdBuffer();
	}

	@Override
	public void delMcuFile() {
		// TODO Auto-generated method stub
		mService.delMcuFile();
	}

	@Override
	public void rebootMcu() {
		// TODO Auto-generated method stub
		mService.rebootMcu();
	}

	@Override
	public int getLoopNum() {
		// TODO Auto-generated method stub
		return mService.loop;
	}

	@Override
	public void switchOTG(int model) {
		// TODO Auto-generated method stub
		mService.switchOTG(model);
	}

	@Override
	public void setColor(int red, int green, int blue) {
		// TODO Auto-generated method stub
		mService.setColor(red, green, blue);
	}

	@Override
	public void setStandby(int time) {
		// TODO Auto-generated method stub
		mService.setStandy(time);
	}

	@Override
	public boolean checkInterSDCard() {
		// TODO Auto-generated method stub
		return mService.checkInterSDFile();
	}

}
