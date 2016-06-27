package com.bonovo.bonovohandle;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.ComponentName;

import android.app.AlertDialog;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.app.Service;
import android.provider.Settings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.media.AudioManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.UserHandle;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

public class HandleService extends Service{

	private static final String TAG = "HandleService";
	private final int REMOVE_DIALOG = 0;
	private final int UPDATE_TEXT = 1;
	private final int VOLUME_ADD  = 2;
	private final int VOLUME_SUB  = 3;
	private final int VOLUME_MUTE = 4;
	private final int OPEN_LAST_APP = 5;
	private final int ACTIVATE_AIRPLANE_MODE = 6;
	private final int ACTIVATE_SHUTDOWN_WATCHDOG = 7;
	private final int DEF_VOLUME  = 10;
	private final int VOLUME_DIALOGE_TIMEOUT = 2000;
    private final int CHANNEL_LOUT1_VOL = 0;
    private final int CHANNEL_ROUT1_VOL = 1;
    private final int CHANNEL_LOUT2_VOL = 2;
    private final int CHANNEL_ROUT2_VOL = 3;
    private final String SOUND_BALANCE_CHANNEL = "channel";
    private final String SOUND_BALANCE_VOLUME = "volume";
    private final String SYSTEM_VOLUME = "VOLUME";
    private final String LOUT1_VOLUME = "LOUT1_VOLUME";
    private final String ROUT1_VOLUME = "ROUT1_VOLUME";
    private final String LOUT2_VOLUME = "LOUT2_VOLUME";
    private final String ROUT2_VOLUME = "ROUT2_VOLUME";
	private final String STORAGE = "storage";
	private final String STORAGE_S8 = "storage_s8";
	private final String PROCESS = "process";
	private final String APPLICATIONS = "applications";
	private final String APPS = "apps";
	private final String BONOVO_RADIO = "com.example.radio";
	private Context mContext = null;
	private TextView mTextView = null;
	private ImageView mImageView = null;
	private SeekBar mSeekBar = null;
	private boolean mMute = false;
    private boolean mIsAirplaneOn = false;
	private int carType = 0; //carType defult is Volkswagen;
	private int serialType = 0; //serialType defult is NON;
	private static int s8Volume = 0;//sonata8 volume 0;
	private SharedPreferences preferences;
	private SharedPreferences preferences2;
	private static boolean AppSwitchVisible = false;

    private static final boolean mIsKillProcessWhenScreenOff = false;

	private ArrayList<String> mInitProcessList = null;
    private ArrayList<String> appSwitchPackages = new ArrayList<String>();
	
	private ServiceBinder  serviceBinder = new ServiceBinder();

	static {
        System.loadLibrary("bonovohandle");
  	}
	private native final int jnigetbrightness() throws IllegalStateException;
	private native final int jniSystemInit() throws IllegalStateException;
	private native final int jniSetVolume(int volume) throws IllegalStateException;
    private native final int jniSetSoundBalance(int channel, int volume) throws IllegalStateException;
	private native final int jniSetMute(boolean mute) throws IllegalStateException;
	private native final int jniSetVideoChannel(int channel) throws IllegalStateException;
	private native final int jniSetAudioChannel(int channel) throws IllegalStateException;
	private native final boolean jniSendPowerKey() throws IllegalStateException;
	private native final boolean jniOnGoToSleep() throws IllegalStateException;
	private native final boolean jniOnWakeUp() throws IllegalStateException;
	
	public class ServiceBinder extends Binder{
		
		public HandleService getService(){
			return HandleService.this;
		}
	}

	private AudioManager amAudioManager;
	private int currentVol, maxVol;
	private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
 		 
			switch (focusChange) {
				case AudioManager.AUDIOFOCUS_GAIN:
					break;

				case AudioManager.AUDIOFOCUS_LOSS:
					break;

				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					break;

				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
					break;
				default:break;
			}
		}
	};
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if(intent.getAction().equals("android.intent.action.BONOVO_UPDATEBRIGHTNESS_KEY")){
				Log.v(TAG, "report");
//				Settings.System.putInt(mContext.getContentResolver(),
//				Settings.System.SCREEN_BRIGHTNESS, getBrightness());
			}else if(intent.getAction().equals("android.intent.action.BONOVO_VOLUMEADD_KEY")){
				if(dial == null){
//					Log.v(TAG, "++++++ VOLUME ADD+ dial is null");
					dial = createVolumeDialog(getVolume());
				}else{
					if(!dial.isShowing()){
						dial.show();
					}
					Message msg = mHandler.obtainMessage(REMOVE_DIALOG);
					mHandler.removeMessages(REMOVE_DIALOG);
					mHandler.sendMessageDelayed(msg, VOLUME_DIALOGE_TIMEOUT);
				}
				mHandler.sendMessage(mHandler.obtainMessage(VOLUME_ADD));
			}else if(intent.getAction().equals("android.intent.action.BONOVO_VOLUMESUB_KEY")){
//				Log.v(TAG, "++++++android.intent.action.BONOVO_VOLUMESUB_KEY");
				if(dial == null){
//					Log.v(TAG, "++++++ VOLUME SUB- dial is null");
					dial = createVolumeDialog(getVolume());
				}else{
					if(!dial.isShowing()){
						dial.show();
					}
					Message msg = mHandler.obtainMessage(REMOVE_DIALOG);
					mHandler.removeMessages(REMOVE_DIALOG);
					mHandler.sendMessageDelayed(msg, VOLUME_DIALOGE_TIMEOUT);
				}
				mHandler.sendMessage(mHandler.obtainMessage(VOLUME_SUB));
			}else if(intent.getAction().equals("android.intent.action.KEYCODE_BONOVO_SYSTEMMUTE_KEY")){
				//Log.v(TAG, "++++++ android.intent.action.KEYCODE_BONOVO_SYSTEMMUTE_KEY");
				if(dial == null){
					dial = createVolumeDialog(getVolume());
				}else{
					if(!dial.isShowing()){
						dial.show();
					}
					Message msg = mHandler.obtainMessage(REMOVE_DIALOG);
					mHandler.removeMessages(REMOVE_DIALOG);
					mHandler.sendMessageDelayed(msg, VOLUME_DIALOGE_TIMEOUT);
				}
				mHandler.sendMessage(mHandler.obtainMessage(VOLUME_MUTE));
			}else if(intent.getAction().equals("android.intent.action.BONOVO_UPDATEVOLUME_KEY")){
				// This will show the Volume Dialog without adjusting sound, allowing user to use
				// seekbar to adjust.
				if(dial == null){
					dial = createVolumeDialog(getVolume());
				}else{
					if(!dial.isShowing()){
						dial.show();
					}
					Message msg = mHandler.obtainMessage(REMOVE_DIALOG);
					mHandler.removeMessages(REMOVE_DIALOG);
					mHandler.sendMessageDelayed(msg, VOLUME_DIALOGE_TIMEOUT);
				}
			}else if(intent.getAction().equals("android.intent.action.BONOVO_SLEEP_KEY")){
				killAppsAndGoHome();
			}else if(intent.getAction().equals("android.intent.action.BONOVO_WAKEUP_KEY")){
//				Intent main_intent = new Intent(Intent.ACTION_MAIN);
//				main_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				main_intent.addCategory(Intent.CATEGORY_HOME);
//				startActivity(main_intent);
				//Intent startSeviceIntent = new Intent(Intent.ACTION_BOOT_COMPLETED);
				//mContext.sendBroadcast(startSeviceIntent);
			}else if(intent.getAction().equals("android.intent.action.BONOVO_RADIO_POWER_ON")){
				SharedPreferences sp = mContext.getSharedPreferences(APPLICATIONS, MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.putBoolean(BONOVO_RADIO, true);
				editor.commit();
			}else if(intent.getAction().equals("android.intent.action.BONOVO_RADIO_POWER_OFF")){
				SharedPreferences sp = mContext.getSharedPreferences(APPLICATIONS, MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.putBoolean(BONOVO_RADIO, false);
				editor.commit();
			//}else if(intent.getAction().equals("android.intent.action.BONOVO_RADIO_KEY")){
			//	Log.d(TAG, "-------- KEYCODE_BONOVO_RADIO ----------");
			//	openPackage(BONOVO_RADIO);
			}else if(intent.getAction().equals("android.intent.action.BONOVO_SEND_POWER_KEY")){
			    sendPowerKey();
			}else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                notifyMcuWakeUp();
                setWakeupStatus(true);
                mHandler.removeMessages(ACTIVATE_AIRPLANE_MODE);	// If we woke before the timeout, remove the leftover airplane mode message
                mHandler.removeMessages(ACTIVATE_SHUTDOWN_WATCHDOG);	// Remove the watchdog since we have deliberately woken up
                if(!mIsAirplaneOn){
                    setAirplaneModeOn(false);
                }
                AudioManager amAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                amAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
                Intent wakeup_intent = new Intent("android.intent.action.BONOVO_WAKEUP_KEY");
                mContext.sendBroadcast(wakeup_intent);
			}else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
				AudioManager amAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				int result = amAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
				setWakeupStatus(false);

				Message msg = mHandler.obtainMessage(ACTIVATE_AIRPLANE_MODE);
				mHandler.sendMessageDelayed(msg, getConnectedSleepTime());

				// If we are still running one minute after the MCU is supposed to have
				//  shut us down, we have to do it ourselves.  That's what this is for.
				if (getShutdownTime() < 65535) {
					Message msgwd = mHandler.obtainMessage(ACTIVATE_SHUTDOWN_WATCHDOG);
					mHandler.sendMessageDelayed(msgwd, (getShutdownTime() + 1) * 60000);	// Need to convert minutes to milliseconds
					Log.d(TAG, "Setting watchdog timer for " + (getShutdownTime() + 1) + " minutes.");
				}
				
                notifyMcuSleep();
                Intent sleep_intent = new Intent("android.intent.action.BONOVO_SLEEP_KEY");
                mContext.sendBroadcast(sleep_intent);
            }else if(intent.getAction().equals("android.intent.action.BONOVO_SET_SOUND_BALANCE")){
                int channel = intent.getIntExtra(SOUND_BALANCE_CHANNEL, -1);
                int volume = intent.getIntExtra(SOUND_BALANCE_VOLUME, -1);
                if(channel < 0 || volume < 0){
                    Log.e(TAG, "The data of channel volume is invalid!");
                }else{
                    if(!setChannelVolumeAndSave(channel, volume))
                        Log.e(TAG, "Set " + channel + " channel's volume failed.");
                }
            }else if(intent.getAction().equals("android.intent.action.BONOVO_GET_SOUND_BALANCE")){
                Intent sb_intent = new Intent("android.intent.action.BONOVO_SOUND_BALANCE");
                int volume = getChannelVolume(CHANNEL_LOUT1_VOL);
                sb_intent.putExtra(LOUT1_VOLUME, volume);
                volume = getChannelVolume(CHANNEL_ROUT1_VOL);
                sb_intent.putExtra(ROUT1_VOLUME, volume);
                volume = getChannelVolume(CHANNEL_LOUT2_VOL);
                sb_intent.putExtra(LOUT2_VOLUME, volume);
                volume = getChannelVolume(CHANNEL_ROUT2_VOL);
                sb_intent.putExtra(ROUT2_VOLUME, volume);
                mContext.sendBroadcast(sb_intent);
            }else if(intent.getAction().equals("com.android.internal.car.can.action.CAR_TYPE_RESPONSE")){
				carType = intent.getIntExtra("car_type", 0);
//				if(carType == 1){
//					Intent intentReadInfo = new Intent("com.android.internal.car.can.action.ACTION_S8_READINFO");
//					sendBroadcast(intentReadInfo);
//				}
				Log.d(TAG, "car_type = "+carType);
			}else if (intent.getAction().equals("com.android.internal.car.can.action.RECEIVED")) {
				Bundle bundle = intent.getBundleExtra("sonata8_bundle");
				if (bundle == null) {
					Log.v(TAG, "bundle is null");
				} else {
					s8Volume = bundle.getInt("s8_volume", 15);
					Log.v(TAG, "S8volume= "+s8Volume);
				}
			}else if (intent.getAction().equals("android.intent.action.SEND_FOR_HANDLER_CAR_TYPE")) {
				carType = intent.getIntExtra("handler_car_type", 0);
				Log.v(TAG, "Can Receiver Car_Type Changed carType="+carType);
			}else if(intent.getAction().equals("com.android.internal.car.can.action.SERIAL_TYPE_CHANGED")){
				serialType = intent.getIntExtra("serial_type", 0);
				preferences = context.getApplicationContext().getSharedPreferences("serial_checked_result_handlerService", Context.MODE_WORLD_READABLE);	//����ȫ�ֱ�������Ϣ
				preferences.edit()
								  .putInt("radioButton_Checked_Flag", serialType)
								  .commit();
				Log.v(TAG, "MCU to HandlerService--> serialType="+serialType);
			}else if(intent.getAction().equals("com.android.internal.car.can.action.CAR_TYPE_CHANGED")){
				carType = intent.getIntExtra("car_type", 0);
				preferences2 = context.getApplicationContext().getSharedPreferences("car_checked_result_handlerService", Context.MODE_WORLD_READABLE);	//����ȫ�ֱ�������Ϣ
				preferences2.edit()
								  .putInt("radioButton_Checked_Flag", carType)
								  .commit();
				Log.v(TAG, "MCU to HandlerService--> carType="+carType);
			}else if(intent.getAction().equals("android.intent.action.BONOVO_SET_CONNECTED_SLEEP_TIME")){
				long timeout = new Long(intent.getIntExtra("minutes", 1));
				Log.v(TAG, "Setting connected sleep time to "+ timeout);
				setConnectedSleepTime(timeout * 60000);	// intent values is in minutes, handler uses milliseconds. 60000 milliseconds in one minute.
			} else if (intent.getAction().equals("android.intent.action.BONOVO_SET_VIDEO_CHANNEL")) {
				int channel = intent.getIntExtra("channel", 0);
				jniSetVideoChannel(channel);

				Boolean VideoSwitcher = intent.getExtras().getBoolean("videoswitcher", true);

				if (VideoSwitcher == true) {
					Intent switcherintent = new Intent(context, VideoSwitcherActivity.class);
					if (channel == 0) {
						switcherintent.putExtra("stop", true);
					}
					switcherintent.putExtra("channel", channel);
					switcherintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(switcherintent);
				}
			} else if (intent.getAction().equals("android.intent.action.BONOVO_SET_AUDIO_CHANNEL")) {
				int channel = intent.getIntExtra("channel", 0);
				jniSetAudioChannel(channel);
            } else if (intent.getAction().equals("android.intent.action.XDAUTO_APP_SWITCH")) {
                Log.d(TAG, "============== HandleService:XDAUTO_APP_SWITCH intent Triggered");
                if (!AppSwitchVisible) {
                    getAppSwitch();
                    Intent switcherintent = new Intent(context, AppSwitchActivity.class);
                    switcherintent.putExtra("apps", appSwitchPackages);
                    switcherintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(switcherintent);
                }
                else {
                    Intent appSwitch = new Intent(AppSwitchActivity.SWITCH_APP);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(appSwitch);
                }
            }
		}
	};

    private void setWakeupStatus(boolean isWake){
        SharedPreferences sp = mContext.getSharedPreferences(STORAGE, MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putBoolean("WAKE_MODE", isWake);
		editor.commit();
    }

    private boolean getWakeupStatus(){
        SharedPreferences sp = mContext.getSharedPreferences(STORAGE, MODE_PRIVATE);
		return sp.getBoolean("WAKE_MODE", false);
    }

	private void setConnectedSleepTime(long milliSecondsToAirplaneMode){
		SharedPreferences sp = mContext.getSharedPreferences(STORAGE, MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putLong("CONNECTED_SLEEP_TIME", milliSecondsToAirplaneMode);
		editor.commit();
	}

	private long getConnectedSleepTime(){
		SharedPreferences sp = mContext.getSharedPreferences(STORAGE, MODE_PRIVATE);
		return sp.getLong("CONNECTED_SLEEP_TIME", 1000);		// default to 1 second (1000 milliseconds)
	}

	private long getShutdownTime(){
		SharedPreferences sp = mContext.getSharedPreferences("standby model", Context.MODE_WORLD_READABLE);
		return sp.getLong("standby checked", 1);		// default to 1 minute if we could not get it from the mcu preference
	}
    
    private void setAirplaneFlag(boolean isAirplane){
        SharedPreferences sp = mContext.getSharedPreferences(STORAGE, MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putBoolean("AIRPLANE_MODE", isAirplane);
		editor.commit();
    }

    private boolean getAirplaneFlag(){
        SharedPreferences sp = mContext.getSharedPreferences(STORAGE, MODE_PRIVATE);
		return sp.getBoolean("AIRPLANE_MODE", false);
    }

    private boolean isAirplaneOn(){
        int mode = Settings.Global.getInt(mContext.getContentResolver(),
                        Settings.Global.AIRPLANE_MODE_ON, 0);
        return (mode != 0);
    }

    private void setAirplaneModeOn(boolean enabling){
        Settings.Global.putInt(mContext.getContentResolver(),
             Settings.Global.AIRPLANE_MODE_ON, enabling ? 1:0);

        int mode = Settings.Global.getInt(mContext.getContentResolver(),
                        Settings.Global.AIRPLANE_MODE_ON, 0);

        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enabling);
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void killAppsAndGoHome(){
        if(!mIsKillProcessWhenScreenOff)
            return;

        Intent main_intent = new Intent(Intent.ACTION_MAIN);
		main_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		main_intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(main_intent);

		ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
	    List<RunningAppProcessInfo> procList = activityManager.getRunningAppProcesses();

        // get input method packages
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> imi = imm.getInputMethodList();
        int imCnt = (imi == null)?0:imi.size();
        
		for(Iterator<RunningAppProcessInfo> iterator = procList.iterator(); iterator.hasNext();){
			RunningAppProcessInfo procInfo = iterator.next();
			if(isSystemApp(procInfo.processName) ||
				"com.bonovo.bluetooth".equals(procInfo.processName)){
				continue;
			}

            boolean isInitPackage = false;
			for(Iterator<String> procInitName = mInitProcessList.iterator(); procInitName.hasNext();){
				String initName = procInitName.next();
				if(initName.equals(procInfo.processName)){
                    isInitPackage = true;
					continue;
				}
			}
            if(isInitPackage){
                continue;
            }

            boolean isInputMethodPackage = false;
            for(int i=0; i<imCnt; i++){
                String imPackageName = imi.get(i).getPackageName();
                if(imPackageName.equals(procInfo.processName)){
                    isInputMethodPackage = true;
                    break;
                }
            }
            if(isInputMethodPackage){
                continue;
            }

			activityManager.killBackgroundProcesses(procInfo.processName);
			activityManager.forceStopPackage(procInfo.processName);
		}
    }

	private boolean isSystemApp(String processName){
		/*if(mInitProcessList == null){
			return false;
		}
		for(Iterator<String> it = mInitProcessList.iterator(); it.hasNext();){
			String temp = it.next();
			Log.d(TAG, "isSystemApp processName:" + temp);
			if(processName.equals(temp)){
				return true;
			}
		}
		return false;
		*/
		return ("system".equals(processName) || ("com.android.launcher".equals(processName)) 
				|| "com.android.systemui".equals(processName) || ("com.example.radio").equals(processName));
	}

/*	private void saveProcessList() {
		ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procList = activityManager.getRunningAppProcesses();

		SharedPreferences sp = mContext.getSharedPreferences(PROCESS, MODE_WORLD_WRITEABLE);
		Editor editor = sp.edit();
		editor.putInt("count", procList.size());

		int i=0;
		for(Iterator<RunningAppProcessInfo> iterator = procList.iterator(); iterator.hasNext();){
			RunningAppProcessInfo procInfo = iterator.next();
			Log.d(TAG, "-------- saveProcessList processName:" + procInfo.processName);
			String proc = "proc" + i;
			editor.putString(proc, procInfo.processName);
			i++;
		}
		editor.commit();
	}*/

	private void readAndOpenProcess() {
		ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procList = activityManager.getRunningAppProcesses();
		ArrayList<String> list = new ArrayList<String>();
		for(Iterator<RunningAppProcessInfo> iterator = procList.iterator(); iterator.hasNext();){
			RunningAppProcessInfo procInfo = iterator.next();
			list.add(procInfo.processName);
		}

		SharedPreferences sp = mContext.getSharedPreferences(PROCESS, MODE_WORLD_WRITEABLE);
		int count = sp.getInt("count", 0);
		for(int i=0; i<count; i++) {
			String proc = "proc" + i;
			String procName = sp.getString(proc, "");
			i++;
			if(!list.contains(procName)){
				//openPackage(procName);
			}
		}
	}

	private void openPackage(String packageName){
		try {
			PackageInfo pkInfo = getPackageManager().getPackageInfo(packageName, 0);
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN);
			resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(pkInfo.packageName);

			List<ResolveInfo> apps = getPackageManager().queryIntentActivities(resolveIntent, 0);
			ResolveInfo rInfo = apps.iterator().next();
			if(rInfo != null){
				String pkName = rInfo.activityInfo.packageName;
				String clName = rInfo.activityInfo.name;

				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				ComponentName cn = new ComponentName(pkName, clName);
				intent.setComponent(cn);
				startActivity(intent);
				//startActivityAsUser(intent, UserHandle.USER_CURRENT);
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void openBonovoRadio() {
		SharedPreferences sp = mContext.getSharedPreferences(APPLICATIONS, MODE_PRIVATE);
		boolean isNeedRunRadio = sp.getBoolean(BONOVO_RADIO, false);
		if(isNeedRunRadio){
			openPackage(BONOVO_RADIO);
		}
		return;
	}
	
	private IntentFilter getIntentFilter() {
		IntentFilter myIntentFilter = new IntentFilter(
				"android.intent.action.BONOVO_UPDATEBRIGHTNESS_KEY");
		String updateVolume = "android.intent.action.BONOVO_UPDATEVOLUME_KEY";
		myIntentFilter.addAction(updateVolume);
		myIntentFilter.addAction("android.intent.action.BONOVO_VOLUMEADD_KEY");
		myIntentFilter.addAction("android.intent.action.BONOVO_VOLUMESUB_KEY");
		myIntentFilter.addAction("android.intent.action.KEYCODE_BONOVO_SYSTEMMUTE_KEY");
		myIntentFilter.addAction("android.intent.action.BONOVO_SLEEP_KEY");
		myIntentFilter.addAction("android.intent.action.BONOVO_WAKEUP_KEY");
		myIntentFilter.addAction("android.intent.action.BONOVO_RADIO_POWER_ON");
		myIntentFilter.addAction("android.intent.action.BONOVO_RADIO_POWER_OFF");
		myIntentFilter.addAction("android.intent.action.KEYCODE_BONOVO_RADIO");
		myIntentFilter.addAction("android.intent.action.BONOVO_SEND_POWER_KEY");
		myIntentFilter.addAction("android.intent.action.BONOVO_SET_SOUND_BALANCE");
		myIntentFilter.addAction("android.intent.action.BONOVO_GET_SOUND_BALANCE");
		myIntentFilter.addAction("android.intent.action.BONOVO_SET_CONNECTED_SLEEP_TIME");
		myIntentFilter.addAction("android.intent.action.BONOVO_SET_VIDEO_CHANNEL");
		myIntentFilter.addAction("android.intent.action.BONOVO_SET_AUDIO_CHANNEL");
		myIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		myIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
		myIntentFilter.addAction("com.android.internal.car.can.action.CAR_TYPE_RESPONSE");
		myIntentFilter.addCategory("com.android.internal.car.can.Car");
		myIntentFilter.addAction("com.android.internal.car.can.action.RECEIVED");
		myIntentFilter.addCategory("com.android.internal.car.can.Sonata8");
		myIntentFilter.addAction("android.intent.action.SEND_FOR_HANDLER_CAR_TYPE");
		myIntentFilter.addAction("com.android.internal.car.can.action.SERIAL_TYPE_CHANGED");
		myIntentFilter.addAction("com.android.internal.car.can.action.CAR_TYPE_CHANGED");
        myIntentFilter.addAction("android.intent.action.XDAUTO_APP_SWITCH");
		return myIntentFilter;
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");

		jniSystemInit();
		mContext = getApplicationContext();
//		Settings.System.putInt(mContext.getContentResolver(),
//	                    Settings.System.SCREEN_BRIGHTNESS, getBrightness());
//		try {
//            IPowerManager power = IPowerManager.Stub.asInterface(
//                    ServiceManager.getService("power"));
//            if (power != null) {
//                //power.setBacklightBrightness(getBrightness());
//                power.setTemporaryScreenBrightnessSettingOverride(getBrightness());
//            }
//        } catch (RemoteException doe) {
//            
//        }
        // set the airplane flag if shut down system in sleep last time
        if(!getWakeupStatus()){
            mIsAirplaneOn = getAirplaneFlag();
            setAirplaneModeOn(mIsAirplaneOn);
            setWakeupStatus(true);
        }
        
        readSharePreForS8();
        
        int volume = 100;
        volume = getChannelVolume(CHANNEL_LOUT1_VOL);
        if(volume >= 0){
            setChannelVolumeNoSave(CHANNEL_LOUT1_VOL, volume);
        }
        volume = getChannelVolume(CHANNEL_ROUT1_VOL);
        if(volume >= 0){
            setChannelVolumeNoSave(CHANNEL_ROUT1_VOL, volume);
        }
        volume = getChannelVolume(CHANNEL_LOUT2_VOL);
        if(volume >= 0){
            setChannelVolumeNoSave(CHANNEL_LOUT2_VOL, volume);
        }
        volume = getChannelVolume(CHANNEL_ROUT2_VOL);
        if(volume >= 0){
            setChannelVolumeNoSave(CHANNEL_ROUT2_VOL, volume);
        }
        
		this.registerReceiver(myReceiver, getIntentFilter());

//		//request car type from CanBusService.java
//		Intent sendIntent = new Intent("com.android.internal.car.can.action.CAR_TYPE_REQUEST");
//		sendBroadcast(sendIntent);
		
		volume = getVolume();
		if(volume == -1){
			setVolume(DEF_VOLUME);
			//setMuteStatus(mMute);
		}else{
			setVolume(volume);
		}
		mMute = getMuteStatus();
		setMuteStatus(mMute);
//		Log.d(TAG, "+++++++++ volume:" + volume + " mMute:" + mMute);

		if(mInitProcessList == null){
			mInitProcessList = new ArrayList<String>();
		}else{
			mInitProcessList.clear();
		}
		ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procList = activityManager.getRunningAppProcesses();
		for(Iterator<RunningAppProcessInfo> iterator = procList.iterator(); iterator.hasNext();){
			RunningAppProcessInfo procInfo = iterator.next();
			if(isSystemApp(procInfo.processName)){
				continue;
			}
			mInitProcessList.add(procInfo.processName);
		}
		mHandler.sendMessageDelayed(mHandler.obtainMessage(OPEN_LAST_APP), 10);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "onBind");
		return serviceBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");

		this.unregisterReceiver(myReceiver);
	}
	
	public int getBrightness(){
		Log.d(TAG, "getBrightness");
		return (jnigetbrightness());
	}

	private final int MAX_VOLUME = 32;
	private AlertDialog dial = null;
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//super.handleMessage(msg);
			int progress = 0;

			switch(msg.what){
			case REMOVE_DIALOG:
				if(dial != null){
					dial.cancel();
				}
				dial = null;
				mTextView = null;
				mImageView = null;
				mSeekBar = null;
				break;
			case VOLUME_ADD:
				if((mSeekBar == null)||(mTextView == null)||(mImageView == null)){
					break;
				}
				if(mMute){
					Log.d(TAG, "VOLUME_ADD--->mMute!!!");
					mMute = false;
					setMuteStatus(mMute);
					mImageView.setImageResource(R.drawable.ic_sysbar_volume);
					break;
				}
				progress = mSeekBar.getProgress();
				if(progress < MAX_VOLUME){
					progress = progress + 1;
					mSeekBar.setProgress(progress);
				}
				break;
			case VOLUME_SUB:
				if((mSeekBar == null)||(mTextView == null)||(mImageView == null)){
					break;
				}
				if(mMute) {
					Log.d(TAG, "VOLUME_SUB--->mMute!!!");
					mMute = false;
					setMuteStatus(mMute);
					mImageView.setImageResource(R.drawable.ic_sysbar_volume);
					break;
				}
				progress = mSeekBar.getProgress();
				if(progress > 0){
					progress = progress - 1;
					mSeekBar.setProgress(progress);
				}
				break;
			case VOLUME_MUTE:
				mMute = !mMute;
				Log.d(TAG, "VOLUME_MUTE--->mMute!!!");
				if(mImageView != null){
					if(mMute){
						mImageView.setImageResource(R.drawable.ic_sysbar_volume_mute);
					}else{
						mImageView.setImageResource(R.drawable.ic_sysbar_volume);
					}
				}
				setMuteStatus(mMute);
				break;
			case OPEN_LAST_APP:
				//readAndOpenProcess();
				openBonovoRadio();
				break;
			case ACTIVATE_AIRPLANE_MODE:
				mIsAirplaneOn = isAirplaneOn();              
				setAirplaneFlag(mIsAirplaneOn);
				if(!mIsAirplaneOn){
					setAirplaneModeOn(true);
				}
				break;
			case ACTIVATE_SHUTDOWN_WATCHDOG:
				// Shutdown the unit
				Log.d(TAG, "Watchdog shutdown timer activated. Attempting to shutdown unit.");
				
				Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
				intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				
				break;
			default:
				break;
			}
		}
		
	};

	private AlertDialog createVolumeDialog(int volume){
		View v = View.inflate(mContext, R.layout.volume_toast, null);
		AlertDialog.Builder b = new AlertDialog.Builder(mContext);
		b.setView(v);
		AlertDialog d = b.create();
		d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		// Make dialog non-focusable before showing, stops it breaking immersive mode apps like automate.
		d.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		d.show();


		d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		d.getWindow().setGravity(Gravity.BOTTOM);
		WindowManager.LayoutParams lp = d.getWindow().getAttributes();
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		Display dp = wm.getDefaultDisplay();

		lp.flags = lp.flags & (~lp.FLAG_DIM_BEHIND);
		d.getWindow().setAttributes(lp);
		d.getWindow().addFlags(lp.FLAG_NOT_FOCUSABLE | lp.FLAG_NOT_TOUCH_MODAL | lp.FLAG_WATCH_OUTSIDE_TOUCH);
		
		mTextView = (TextView)d.getWindow().findViewById(R.id.textView);
		mImageView = (ImageView)d.getWindow().findViewById(R.id.imageView);
		mSeekBar = (SeekBar)d.getWindow().findViewById(R.id.seekBar);
		
		mSeekBar.setMax(MAX_VOLUME);
		Log.d(TAG, "setProgress-->arg= "+volume);
		mSeekBar.setProgress(volume);
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				Message msg = mHandler.obtainMessage(REMOVE_DIALOG);
				mHandler.sendMessageDelayed(msg, VOLUME_DIALOGE_TIMEOUT);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				mHandler.removeMessages(REMOVE_DIALOG);
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if(mMute){
					Log.d(TAG, "onProgressChanged-->mMute!!!!");
					mMute = !mMute;
					mImageView.setImageResource(R.drawable.ic_sysbar_volume);
					setMuteStatus(mMute);
				}
				mTextView.setText(String.valueOf(progress));
				setVolume(progress);
			}
		});
		
		if(mMute){
			mImageView.setImageResource(R.drawable.ic_sysbar_volume_mute);
		}else{
			mImageView.setImageResource(R.drawable.ic_sysbar_volume);
		}
		mImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Message msg = mHandler.obtainMessage(REMOVE_DIALOG);
				mHandler.removeMessages(REMOVE_DIALOG);
				mHandler.sendMessageDelayed(msg, VOLUME_DIALOGE_TIMEOUT);
				mMute = !mMute;
				if(mMute){
					mImageView.setImageResource(R.drawable.ic_sysbar_volume_mute);
				}else{
					mImageView.setImageResource(R.drawable.ic_sysbar_volume);
				}
				setMuteStatus(mMute);
			}
		});
		
		mTextView.setText(String.valueOf(mSeekBar.getProgress()));
		mTextView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Message msg = mHandler.obtainMessage(REMOVE_DIALOG);
				mHandler.removeMessages(REMOVE_DIALOG);
				mHandler.sendMessageDelayed(msg, VOLUME_DIALOGE_TIMEOUT);
			}
		});

		Message msg = mHandler.obtainMessage(REMOVE_DIALOG);
		if(msg != null){
			mHandler.sendMessageDelayed(msg, VOLUME_DIALOGE_TIMEOUT);
		}
		return d;
	}
	
	private boolean setVolume(int volume) {
		Log.d(TAG, "setVolume!!!");
		if (carType != 1) {
			if (jniSetVolume(volume) != 0) {
				Log.e(TAG, "setVolume(" + volume + ") failed.");
				return false;
			}
			SharedPreferences sp = mContext.getSharedPreferences(STORAGE,
					MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putInt(SYSTEM_VOLUME, volume);
			editor.commit();
			return true;
		} else if (carType == 1) {
			Log.d(TAG, "setVolume!!!--->sendBroadcst with volume ="+volume);
			SharedPreferences sp = mContext.getSharedPreferences(STORAGE_S8,
					MODE_WORLD_READABLE);
			Editor editor = sp.edit();
			editor.putInt(SYSTEM_VOLUME, volume);
			editor.commit();
			Intent s8VolumeIntent = new Intent(
					"com.android.internal.car.can.action.ACTION_S8_VOLUME_CHANGED");
			s8VolumeIntent.putExtra("s8_volume", volume);
			sendBroadcast(s8VolumeIntent);
		}
		return true;
	}
	
	private int getVolume(){
		Log.d(TAG, "getVolume!!!");
//		Intent sendIntent = new Intent("com.android.internal.car.can.action.CAR_TYPE_REQUEST");
//		sendBroadcast(sendIntent);
//		try {
//			Thread.sleep(200);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
		if(carType != 1){
			Log.e(TAG, "getVolume!!!--->is not Sonata8");
			int volume;
			SharedPreferences sp = mContext.getSharedPreferences(STORAGE, MODE_PRIVATE);
			volume = sp.getInt(SYSTEM_VOLUME, -1);
			
			Log.e(TAG, "getVolume!!!return--->SharedPreferences volume=" + volume);
			return volume;
		}else if (carType == 1) {
			SharedPreferences sp = mContext.getSharedPreferences(STORAGE_S8, MODE_WORLD_READABLE);
			s8Volume = sp.getInt(SYSTEM_VOLUME, 15);
//			Intent intent = new Intent("com.android.internal.car.can.action.ACTION_S8_READINFO");
//			sendBroadcast(intent);
			Log.d(TAG, "getVolume!!!return--->s8Volume=" + s8Volume);
			return s8Volume;
		}
		return 0;
	}

    /*@
     *     Set the volume of one channel according the parameter(which channle),
     * then save the volume in SharedPreferences.
     *
     * @function: setChannelVolume
     * @param[0]: channel    The channel of volume system
     * @param[1]: volume     The volume of channel. it's should between 0 and 100. 
     * @return: true         Set volume successfully.
     *          false        Set volume failed.
     */
    private boolean setChannelVolumeAndSave(int channel, int volume){
        if(jniSetSoundBalance(channel, volume) < 0)
            return false;
        
        SharedPreferences sp = mContext.getSharedPreferences(STORAGE, MODE_PRIVATE);
		Editor editor = sp.edit();
        switch(channel){
        case CHANNEL_LOUT1_VOL:
            editor.putInt(LOUT1_VOLUME, volume);
            break;
        case CHANNEL_ROUT1_VOL:
            editor.putInt(ROUT1_VOLUME, volume);
            break;
        case CHANNEL_LOUT2_VOL:
            editor.putInt(LOUT2_VOLUME, volume);
            break;
        case CHANNEL_ROUT2_VOL:
            editor.putInt(ROUT2_VOLUME, volume);
            break;
        default:
            return false;
        }
		editor.commit();
        return true;
    }

    /*@
     *     Set the volume of one channel according the parameter(which channle),
     * but don't save the volume in SharedPreferences.
     *
     * @function: setChannelVolume
     * @param[0]: channel    The channel of volume system
     * @param[1]: volume     The volume of channel. it's should between 0 and 100. 
     * @return: true         Set volume successfully.
     *          false        Set volume failed.
     */
    private boolean setChannelVolumeNoSave(int channel, int volume){
        if(jniSetSoundBalance(channel, volume) < 0)
            return false;
        return true;
    }

    /*@
     * get the volume of one channel according the parameter(which channle)
     *
     * @function: getChannelVolume
     * @param: channel    the channel of volume system
     * @return: 0~100     the percent of volume
     *          -1        cann't get the channel volume throung SharedPreferences.
     *          -2        the parameter is invalid.
     */
    private int getChannelVolume(int channel){
		int volume;
		SharedPreferences sp = mContext.getSharedPreferences(STORAGE, MODE_PRIVATE);
		volume = sp.getInt(SYSTEM_VOLUME, -1);
		switch(channel){
        case CHANNEL_LOUT1_VOL:
            volume = sp.getInt(LOUT1_VOLUME, -1);
            break;
        case CHANNEL_ROUT1_VOL:
            volume = sp.getInt(ROUT1_VOLUME, -1);
            break;
        case CHANNEL_LOUT2_VOL:
            volume = sp.getInt(LOUT2_VOLUME, -1);
            break;
        case CHANNEL_ROUT2_VOL:
            volume = sp.getInt(ROUT2_VOLUME, -1);
            break;
        default:
            volume = -2;
            break;
        }
		return volume;
	}
	
	private void setMuteStatus(boolean mute){
		if(jniSetMute(mute) != 0){
			Log.e(TAG, "setMuteStatus(" + mute + ") failed.");
			return;
		}
		SharedPreferences sp = mContext.getSharedPreferences(STORAGE, MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putBoolean("MUTE", mute);
		editor.commit();
		return;
	}
	
	private boolean getMuteStatus(){
		SharedPreferences sp = mContext.getSharedPreferences(STORAGE, MODE_PRIVATE);
		return sp.getBoolean("MUTE", false);
	}

    public void getAppSwitch() {
        appSwitchPackages.clear();
        SharedPreferences sp = mContext.getSharedPreferences(APPS, MODE_PRIVATE);
        int size = sp.getInt("apparraysize", 0);

        for(int i=0; i<size; i++)
            appSwitchPackages.add(sp.getString("APP_" + i, ""));
    }

	public boolean sendPowerKey(){
		return jniSendPowerKey();
	}

    public boolean notifyMcuSleep(){
        return jniOnGoToSleep();
    }

    public boolean notifyMcuWakeUp(){
        return jniOnWakeUp();
    }
    
    private void readSharePreForS8(){
		preferences = this.getSharedPreferences("serial_checked_result_handlerService", Context.MODE_WORLD_READABLE);
		serialType = preferences.getInt("radioButton_Checked_Flag", 0);	//Ĭ��ȫ�ֱ���ΪNON
		preferences2 = this.getSharedPreferences("car_checked_result_handlerService", Context.MODE_WORLD_READABLE);
		carType = preferences2.getInt("radioButton_Checked_Flag", 0);	//Ĭ��ȫ�ֱ���ΪVolkswagen
    }
    
    public static class S8VolumeRecevier extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent arg1) {
			// TODO Auto-generated method stub
//			Log.v(TAG, "S8VolumeRecevier can be into!!!!");
			SharedPreferences sp = context.getSharedPreferences("storage_s8", MODE_WORLD_READABLE);
			int s8_Vol = sp.getInt("VOLUME", 15);
			
			Intent s8VolumeIntent = new Intent(
					"com.android.internal.car.can.action.ACTION_S8_VOLUME_CHANGED");
			s8VolumeIntent.putExtra("s8_volume", s8_Vol);
			context.sendBroadcast(s8VolumeIntent);
		}
    	
    }

    public static boolean isAppSwitchVisible() {
        return AppSwitchVisible;
    }

    public static void AppSwitchResumed() {
        AppSwitchVisible = true;
    }

    public static void AppSwitchPaused() {
        AppSwitchVisible = false;
    }

}
