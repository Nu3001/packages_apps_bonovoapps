package com.bonovo.mcuupdate_and_setting;

import android.app.Activity;
import android.content.ServiceConnection;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.app.Fragment;
import android.content.ComponentName;
import android.os.IBinder;

public class MainActivity extends Activity implements ServiceConnection, RightFragmentSetting.CallbackInterface, RigthFragmentVersion.CallbackFragment1, McuFragment.CallbackMcu, RightFragmentOTGModel.CallBackOTG, BTUpdateFragment.CallbackMcuOP, RightFragmentKeysBackLight.CallBackColor {
    private final String TAG = "com.example.fragment.mainactivity";
    private FragmentTransaction fragmentTransaction;
    private FragmentService mService;
    private FragmentManager manager;
    PowerManager powerManager = null;
    PowerManager.WakeLock wakeLock = null;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent("com.bonovo.mcuupdate_and_setting.FragmentService");
        bindService(intent, this, BIND_AUTO_CREATE);
        Log.v("com.example.fragment.mainactivity", "-->Activity-->onCreate()");
        setContentView(0x7f030000);
        manager = getFragmentManager();
        fragmentTransaction = manager.beginTransaction();
        LeftFragment leftfragment = new LeftFragment();
        fragmentTransaction.add(0x7f080000, leftfragment, "leftfragment");
        fragmentTransaction.commit();
        powerManager = (PowerManager)getSystemService("power");
        wakeLock = powerManager.newWakeLock(0x1a, "My Lock");
    }
    
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.v("com.example.fragment.mainactivity", "-->Activity-->onServiceConnected()");
        mService = service.getServicer();
        manager = getFragmentManager();
        fragmentTransaction = manager.beginTransaction();
        RigthFragmentVersion rigthFragment = new RigthFragmentVersion();
        fragmentTransaction.add(0x7f080001, rigthFragment, "rigthFragment");
        fragmentTransaction.commit();
    }
    
    public void onServiceDisconnected(ComponentName name) {
    }
    
    protected void onStart() {
        super.onStart();
        Log.v("com.example.fragment.mainactivity", "-->Activity-->onStart()");
    }
    
    protected void onResume() {
        super.onResume();
        wakeLock.acquire();
        Log.v("com.example.fragment.mainactivity", "-->Activity-->onResume()");
    }
    
    protected void onPause() {
        super.onPause();
        wakeLock.release();
    }
    
    protected void onDestroy() {
        super.onDestroy();
        mService = null;
        unbindService(this);
    }
    
    public void setMute(boolean flag) {
        mService.setAsternMute(flag);
    }
    
    public void setCamera(boolean flag) {
        mService.setRearviewCamera(flag);
    }
    
    public void lowBrigthness(int progress) {
        mService.lowBrightness(progress);
    }
    
    public int getMcuVersion() {
        return mService.version();
    }
    
    public String getSystemVersion() {
        String str = SystemProperties.get("ro.rksdk.version", "unknown");
        return str;
    }
    
    public void autoVolume(int Progress) {
        mService.volumePercent(Progress);
    }
    
    public boolean checkSDCard() {
        return mService.checkSDCard();
    }
    
    public boolean checkFile() {
        return mService.checkFile();
    }
    
    public void cpyfile() {
        mService.cpyfile();
    }
    
    public boolean checkMcu() {
        return mService.checkMcu();
    }
    
    public int wipeMcuAPP() {
        return mService.wipeMcuAPP();
    }
    
    public boolean checkdBuffer() {
        return mService.checkdBuffer();
    }
    
    public void delMcuFile() {
        mService.delMcuFile();
    }
    
    public void rebootMcu() {
        mService.rebootMcu();
    }
    
    public int getLoopNum() {
        return FragmentService.loop;
    }
    
    public void switchOTG(int model) {
        mService.switchOTG(model);
    }
    
    public void setColor(int red, int green, int blue) {
        mService.setColor(red, green, blue);
    }
}
