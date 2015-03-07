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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.media.AudioManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.UserHandle;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

public class HandleService extends Service implements AudioManager.OnAudioFocusChangeListener{

	private static final String TAG = "HandleService";
	private final int REMOVE_DIALOG = 0;
	private final int UPDATE_TEXT = 1;
	private final int VOLUME_ADD  = 2;
	private final int VOLUME_SUB  = 3;
	private final int VOLUME_MUTE = 4;
	private final int OPEN_LAST_APP = 5;
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
	private final String PROCESS = "process";
	private final String APPLICATIONS = "applications";
	private final String BONOVO_RADIO = "com.example.radio";
	private Context mContext = null;
	private TextView mTextView = null;
	private ImageView mImageView = null;
	private SeekBar mSeekBar = null;
	private boolean mMute = false;
    private boolean mIsAirplaneOn = false;

    private static final boolean mIsKillProcessWhenScreenOff = false;

	private ArrayList<String> mInitProcessList = null;
	
	private ServiceBinder  serviceBinder = new ServiceBinder();

	static {
        System.loadLibrary("bonovohandle");
  	}
	private native final int jnigetbrightness() throws IllegalStateException;
	private native final int jniSystemInit() throws IllegalStateException;
	private native final int jniSetVolume(int volume) throws IllegalStateException;
    private native final int jniSetSoundBalance(int channel, int volume) throws IllegalStateException;
	private native final int jniSetMute(boolean mute) throws IllegalStateException;
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
				//amAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				//amAudioManager.requestAudioFocus(HandleService.this, AudioManager.STREAM_SYSTEM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
				//amAudioManager.abandonAudioFocus(HandleService.this);
				//amAudioManager.requestAudioFocusForCall(AudioManager.STREAM_RING,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
				//amAudioManager.abandonAudioFocusForCall();
				//Log.v(TAG, "amAudioManager.requestAudioFocusForCall");
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
                if(!mIsAirplaneOn){
                    setAirplaneModeOn(false);
                }
                Intent wakeup_intent = new Intent("android.intent.action.BONOVO_WAKEUP_KEY");
                mContext.sendBroadcast(wakeup_intent);
            }else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                mIsAirplaneOn = isAirplaneOn();
                setWakeupStatus(false);
                setAirplaneFlag(mIsAirplaneOn);
                if(!mIsAirplaneOn){
                    setAirplaneModeOn(true);
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
	
	private IntentFilter getIntentFilter(){
		IntentFilter myIntentFilter = new IntentFilter("android.intent.action.BONOVO_UPDATEBRIGHTNESS_KEY");
		String updateVolume="android.intent.action.BONOVO_UPDATEVOLUME_KEY";
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
        myIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        myIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
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
		this.registerReceiver(myReceiver, getIntentFilter());

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

	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			break;

		case AudioManager.AUDIOFOCUS_LOSS:
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			// Lost focus for a short time, but we have to stop
			// playback. We don't release the media player because playback
			// is likely to resume
			//if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			// Lost focus for a short time, but it's ok to keep playing
			// at an attenuated level
			//if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
			break;
		}
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
					mMute = false;
					setMuteStatus(mMute);
					mImageView.setImageResource(R.drawable.ic_lock_ringer_on);
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
				if(mMute){
					mMute = false;
					setMuteStatus(mMute);
					mImageView.setImageResource(R.drawable.ic_lock_ringer_on);
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
				if(mImageView != null){
					if(mMute){
						mImageView.setImageResource(R.drawable.ic_lock_ringer_off);
					}else{
						mImageView.setImageResource(R.drawable.ic_lock_ringer_on);
					}
				}
				setMuteStatus(mMute);
				break;
			case OPEN_LAST_APP:
				//readAndOpenProcess();
				openBonovoRadio();
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
//		d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
		d.show();

		d.getWindow().setLayout(618, 120);
		d.getWindow().setGravity(Gravity.TOP);
		WindowManager.LayoutParams lp = d.getWindow().getAttributes();
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		Display dp = wm.getDefaultDisplay();
//		lp.height = (int)(dp.getHeight() * 0.75);
//		lp.alpha = 0.6f;
		lp.y = 80;
		lp.flags = lp.flags & (~lp.FLAG_DIM_BEHIND);
		d.getWindow().setAttributes(lp);
		d.getWindow().addFlags(lp.FLAG_NOT_FOCUSABLE | lp.FLAG_NOT_TOUCH_MODAL | lp.FLAG_WATCH_OUTSIDE_TOUCH);
		
		mTextView = (TextView)d.getWindow().findViewById(R.id.textView);
		mImageView = (ImageView)d.getWindow().findViewById(R.id.imageView);
		mSeekBar = (SeekBar)d.getWindow().findViewById(R.id.seekBar);
		
		mSeekBar.setMax(MAX_VOLUME);
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
					mMute = !mMute;
					mImageView.setImageResource(R.drawable.ic_lock_ringer_on);
					setMuteStatus(mMute);
				}
				mTextView.setText(String.valueOf(progress));
				setVolume(progress);
			}
		});
		
		if(mMute){
			mImageView.setImageResource(R.drawable.ic_lock_ringer_off);
		}else{
			mImageView.setImageResource(R.drawable.ic_lock_ringer_on);
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
					mImageView.setImageResource(R.drawable.ic_lock_ringer_off);
				}else{
					mImageView.setImageResource(R.drawable.ic_lock_ringer_on);
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
	
	private boolean setVolume(int volume){
		if(jniSetVolume(volume) != 0){
			Log.e(TAG, "setVolume(" + volume + ") failed.");
			return false;
		}
		SharedPreferences sp = mContext.getSharedPreferences(STORAGE, MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt(SYSTEM_VOLUME, volume);
		editor.commit();
		return true;
	}
	
	private int getVolume(){
		int volume;
		SharedPreferences sp = mContext.getSharedPreferences(STORAGE, MODE_PRIVATE);
		volume = sp.getInt(SYSTEM_VOLUME, -1);
		
		return volume;
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

	public boolean sendPowerKey(){
		return jniSendPowerKey();
	}

    public boolean notifyMcuSleep(){
        return jniOnGoToSleep();
    }

    public boolean notifyMcuWakeUp(){
        return jniOnWakeUp();
    }
}
