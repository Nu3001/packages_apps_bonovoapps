package com.example.radio;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Address;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import com.android.internal.car.can.CanRadio;

public class RadioService extends Service implements RadioInterface,
        AudioManager.OnAudioFocusChangeListener {

	public static final String MSG_CLOSE = "com.example.radioplayer.close";
	private static final String TAG = "RadioService";
	private static final String APPLICATIONS = "applications";
	private static final String BONOVO_RADIO = "com.example.radio";

	private static final boolean DEBUG = false;

	private static final int RADIO_OPEN_7415 = 1;
	private static final int RADIO_CLOSE_7415 = 0;
	private static final int RADIO_POWER_ON = 1;
	private static final int RADIO_POWER_OFF = 0;
	private static final int RADIO_SET_MUTE = 1;
	private static final int RADIO_SET_NOTMUTE = 0;
	private static final int RADIO_TURN_CHANNEL = 1;
	private static final int RADIO_DATA_READ = 1;
	private static final int RADIO_DATA_SAVE = 0;
	private static final int RADIO_TYPE_FM = 0;

	public static final int RADIO_PAGE_COUNT = 48;
	public static final int RADIO_FM_COUNT = 48;
	public static final int RADIO_AM_COUNT = 48;
	public static final int RADIO_CHANNEL_COUNT = 144;
	public static int FLAG_AUTO = 0;		//自动搜台标记
	public static int STEP_OR_AUTO;			//step or auto flag
	public static int IS_AUTO_NOW = 1;
	public static int SEARCH_OVER = 0;
	
	public static final int CHINA_MODEL = 0;
	public static final int JAPAN_MODEL = 1;
	public static final int EUR_MODEL = 2;
	public static int RADIO_MODEL = 0;
	
	public static  int FM_LOW_FREQ = 8700;
	public static  int FM_HIGH_FREQ = 10800;
	public static  int AM_LOW_FREQ = 520;
	public static  int AM_HIGH_FREQ = 1710;

	private static final int UPDATE_CHANNEL_LIST = 0;
	private static final int UPDATE_DETAIL_FREQ = 1;
	private static final int AUTOSEARCH_COMPLETE = 2;
	private static final int START_SEARCH_THREAD = 3;

	private static final int RADIO_NO_ACTION = 0;
	private static final int RADIO_SEARCHING = 1;
	private static final int RADIO_START_SEARCH = 2;

	// show the FM back play notification on the title bar
	private static final int NOTIFICATION_ID = 1;
	PendingIntent contentIntent;
	Notification notification;

	// ������ SharedPreferences
	private int functionId = 0; // 0:΢�� 1������ 2���Զ�
	public int curChannelId = -1; // play id 0-47
	private int curFreq;
	private int radioType = RADIO_FM1;
	private int radioMute;
	private AudioManager mAudioManager;
	private static int mVolume = 100;
	private Context mContext;
	private static boolean mRemote;					//远程切换标记

    private int radioDuckVolume; // store radio volume for ducking purposes

	private List<ChannelItem> mChannelList ;
	private ArrayList<String> m_province_list = null;
	private ArrayList<List<String>> m_city_list = null;
	private ArrayList<List<List<ChannelItem>>> m_channel_list = null;
	private RadioStatusChangeListener mStatusListener;
	private SharedPreferences settings;
	public boolean mIsSearchThreadRunning = false; // һ��������̨���߳��������еı�
	
	private static boolean mDown = false;			//keyEvent flag
	public static boolean isLive = false;			//activity islive flag
	private CanRadio mCanRadio = null;
	
	static {
		System.loadLibrary("radio");
	}

	// jni
	private native final int jniPowerOnoff(int OnOff)
			throws IllegalStateException;

	private native final int jniSetVolume(int volume)
			throws IllegalStateException;

	private native final int jniSetMute(int muteState)
			throws IllegalStateException;

	private native final int jniGetMute() throws IllegalStateException;

	private native final int jniFineLeft(int freq) throws IllegalStateException;

	private native final int jniFineRight(int freq)
			throws IllegalStateException;

	private native final int jniStepLeft(int freq) throws IllegalStateException;

	private native final int jniStepRight(int freq)
			throws IllegalStateException;

	private native final int jniAutoSeek(int freq) throws IllegalStateException;

	private native final int jniReadSeek(int[] freqs, int count)
			throws IllegalStateException;

	private native final int jniTurnFmAm(int type) throws IllegalStateException;

	private native final int jniSetModel(int type) throws IllegalStateException;

	private native final int jniSetFreq(int freq) throws IllegalStateException;
	
	private native final int jniSetRemote(int remote) throws IllegalStateException;

	// Channel����
	static class ChannelItem {
		String freq;
		String name;
		String abridge;
	}

	private ServiceBinder serviceBinder = new ServiceBinder();

	public class ServiceBinder extends Binder {
		public RadioService getService() {

			return RadioService.this;
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		if(DEBUG) Log.v(TAG, "------onBind()");
		
		return serviceBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		if (DEBUG)
			Log.d(TAG, "------onUnbind()");
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mContext = this;
        mCanRadio = new CanRadio(this);
		synchronized (this) {
			settings = getSharedPreferences("RadioPreferences", MODE_PRIVATE);
//			SharedPreferences preferences = getSharedPreferences("CHECKED", 0);
//			mRemote = preferences.getBoolean("onoff", true);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
			mRemote = prefs.getBoolean("checkbox_remote_preference", true);
			readAndSetModelInfo();
			updatePreferences(RADIO_DATA_READ);
		}
		
		if(DEBUG) Log.d(TAG, "------onCreate()");
		PowerOnOff(true); // open radio
//		new Thread(new Runnable() {
//			public void run() {
				if (DEBUG)
					Log.v(TAG, "new Thread is up");
				// jniSetVolume(mVolume);
				// radioMute = jniGetMute(); //check if mute,avoid bring noisy
				// // when open power
				// if(radioMute == RADIO_SET_NOTMUTE){
				// jniSetMute(RADIO_SET_MUTE);
				// }
				// // jniPowerOnoff(RADIO_POWER_ON,RADIO_TURN_CHANNEL);
				if (RADIO_FM1 == radioType || RADIO_FM2 == radioType) {
					jniTurnFmAm(0); // open fm
				} else if (RADIO_AM == radioType) {
					jniTurnFmAm(1); // open am
				}
				if (DEBUG)
					Log.v(TAG, "getCurrentFreq()===curfreq ==="
							+ getCurrentFreq());
				// if(radioMute == RADIO_SET_NOTMUTE){
				// jniSetMute(RADIO_SET_NOTMUTE);
				// }
				updatePlaybackTitle();
				/*try{
					Thread.sleep(20);
				} catch (Exception e){
					e.printStackTrace();
				}*/
				setFreq(curFreq);
				setVolume(mVolume);
				if(mRemote){
					setRemote(1);
				}else {
					setRemote(0);
				}
				
//				String language = settings.getString("local_language", "zh");
//				if(!Locale.getDefault().getLanguage().equals(language)){
//					radioReadXML();
//				}
//			}
			
//		}).start();

		// handle event with audio
		// remove by bonovo zbiao
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);
		this.registerReceiver(myReceiver, getIntentFilter());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mAudioManager.abandonAudioFocus(this);
		stopForeground(false);
		if(DEBUG) Log.d(TAG, "------onDestroy()");
		updatePreferences(RADIO_DATA_SAVE);
		if (DEBUG)
			Log.d(TAG, "onDestroy()  end curfreq is " + curFreq);
		// this.unregisterReceiver(myReceiver);
		// �����JNI���ֻ�δ���
		PowerOnOff(false);
		this.unregisterReceiver(myReceiver);
	}

	private Handler mRadioplayerHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_CHANNEL_LIST:
				if (mStatusListener != null) {
					mStatusListener.onStatusChange(UPDATE_CHANNEL_LIST);
				}
				break;
			case UPDATE_DETAIL_FREQ:
				if (mStatusListener != null) {
					mStatusListener.onStatusChange(UPDATE_DETAIL_FREQ);
				}
				break;
			case AUTOSEARCH_COMPLETE:
				if (mStatusListener != null) {
					mStatusListener.onStatusChange(AUTOSEARCH_COMPLETE);
				}
				break;
			case START_SEARCH_THREAD:
				break;
			}
		}

	};

	@SuppressLint("HandlerLeak")
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int res, status = RADIO_START_SEARCH;
			int idex = 0;
			if (DEBUG)
				Log.d(TAG, "START SEARCH +++++++++++++!!!!");
			mIsSearchThreadRunning = true;
			int[] freq = new int[3];

			do {
				res = jniReadSeek(freq, 3);
				if (res < 0) {
					if (DEBUG)
						Log.e(TAG, "jniReadSeek error!");
					break;
				}
				status = freq[0];
				if (res == 0) {
//					 Log.d(TAG, "++++++++++++++++ res:" + res +
//					 " freq[0]:"+freq[0] + " freq[1]:" + freq[1] + " freq[2]:"
//					 + freq[2]);
					if (FLAG_AUTO == 1) {
						/***************auto model************/
						curFreq = freq[2];
						if(getRadioType() == RADIO_FM1){
							mCanRadio.sendRadioInfo(CanRadio.BAND_FM, curFreq);
						}else if (getRadioType() == RADIO_AM) {
							mCanRadio.sendRadioInfo(CanRadio.BAND_AM, curFreq);
						}
						if(isLive){
							mRadioplayerHandler.sendEmptyMessage(UPDATE_DETAIL_FREQ);
						}
						if (freq[1] == 1) {
							curFreq = freq[2]; // 自动搜到的台的频率
							Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@curFreq(FLAG Auto) ="+ curFreq);
							int autoFreq;
							
							autoFreq = curFreq;
							if(getRadioType() == RADIO_FM1){
								mCanRadio.sendRadioInfo(CanRadio.BAND_FM, curFreq);
							}else if (getRadioType() == RADIO_AM) {
								mCanRadio.sendRadioInfo(CanRadio.BAND_AM, curFreq);
							}
							
							Intent it = new Intent();
							Bundle mbundle = new Bundle();
							mbundle.putInt("auto-curfreq", autoFreq);
							mbundle.putInt("idex", idex);
							mbundle.putInt("flag", FLAG_AUTO);
							it.setAction("Auto-Search");
							it.putExtras(mbundle);
							sendBroadcast(it);
							
							idex++;
						}
					} else {
						/***************step model************/
						curFreq = freq[2];
						if(getRadioType() == RADIO_FM1){
							mCanRadio.sendRadioInfo(CanRadio.BAND_FM, curFreq);
						}else if (getRadioType() == RADIO_AM) {
							mCanRadio.sendRadioInfo(CanRadio.BAND_AM, curFreq);
						}
						
						Log.d(TAG, "curFreq(No-Auto) =" + curFreq);
						if(isLive){
							mRadioplayerHandler.sendEmptyMessage(UPDATE_DETAIL_FREQ);
						}
						
						/****单步时候发送curFreq给Activity广播,调用curFreq_Compare_To_Collect(int curFreq)
						 ****函数与收藏栏列表的频率作比较,变化爱心图标*************************************/
						Intent it = new Intent();
						Bundle mbundle = new Bundle();
						mbundle.putInt("step-curfreq", curFreq);
						it.setAction("Step-Search");
						it.putExtras(mbundle);
						sendBroadcast(it);
						/*******************************************************************************/
					}
//					curFreq = freq[2];
////					Log.d(TAG, "curFreq(Auto) ="+curFreq);
//					mRadioplayerHandler.sendEmptyMessage(UPDATE_DETAIL_FREQ);
				}
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} while (status != RADIO_NO_ACTION);
			if(freq[0] == SEARCH_OVER && STEP_OR_AUTO == IS_AUTO_NOW){
				//when auto search is complete,set Channal0 with the first freq 
				Intent intent = new Intent("Radio_Auto_Complete");
				sendBroadcast(intent);
			}
			
			FLAG_AUTO = 0;
			mIsSearchThreadRunning = false;
			updatePreferences(RADIO_DATA_SAVE);
			updatePlaybackTitle();
			Log.d(TAG, "+++++++++++++ STOP SEARCH!!!!");
		}
	};

	public void updatePlaybackTitle() {
		if (DEBUG)
			Log.v(TAG, "---Notification curFreq = " + curFreq);
		CharSequence contentTitle = getResources().getString(R.string.app_name);
		CharSequence contentText = getResources().getString(R.string.playing)
				+ ": " + changeCurFreqToCS(curFreq);
		contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
		new Intent(getApplicationContext(),
		RadioActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		notification = new Notification(R.drawable.import_channel,
				contentTitle, 0);
		notification.flags |= Notification.FLAG_ONGOING_EVENT; // ��flag����Ϊ���������ô֪ͨ�ͻ���QQһ��һֱ��״̬����ʾ
		// ָ��״̬��Ҫ��ʾ����Ϣ���������
		notification.setLatestEventInfo(getApplicationContext(), contentTitle,
				contentText, contentIntent);
		startForeground(NOTIFICATION_ID, notification); // ����״̬����Ϣ

	}

    @Override
    public void onAudioFocusChange(int focusChange) {
        // TODO Auto-generated method stub
        if (DEBUG)
            Log.v(TAG, "----onAudioFocusChange----focusChange:" + focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // Restore volume
                setVolume(radioDuckVolume);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                stopService(new Intent("com.example.RadioService"));
                this.stopSelf();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Reduce volume to Zero - save current volume;
                radioDuckVolume = getVolume();
                setVolume(0);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // reduce volume by 50%
                radioDuckVolume = getVolume();
                setVolume(getVolume() / 2);
                break;
        }

    }

	public CharSequence changeCurFreqToCS(int freq) { /* �˺���δʹ�� */
		StringBuffer sb = new StringBuffer();
		if (freq >= 8700 && freq <= 10800) {
			sb.append(freq / 100).append('.').append(freq / 10 % 10)
					.append("MHz");
		} else {
			sb.append(freq).append("KHz");
		}
		return sb;
	}

    private void PowerOnOff(boolean onOff) {

		if(onOff) {
			jniPowerOnoff(RADIO_POWER_ON);
			Intent intent = new Intent("android.intent.action.BONOVO_RADIO_POWER_ON");
        	sendBroadcast(intent);
		}else{
			jniPowerOnoff(RADIO_POWER_OFF);
			Intent intent = new Intent("android.intent.action.BONOVO_RADIO_POWER_OFF");
        	sendBroadcast(intent);
		}
        mCanRadio.controlRadioDisplay(onOff);
    }

	public void turnFmAm(int type) {
		if (DEBUG)
			Log.v(TAG, "<myu>turnFmAm");
		jniTurnFmAm(type);
	}

	public int getRadioType() {
		return radioType;
	}

	public void setRadioType(int type) {
		radioType = type;
	}

	public int setVolume(int volume) {
		mVolume = volume;
		if (DEBUG) Log.v(TAG, "(Service)mVolume = "+mVolume);
		jniSetVolume(mVolume);
		SharedPreferences.Editor editor = settings.edit(); /* ��editor���ڱ���״̬ */
		editor.putInt("mvolume", mVolume);
		editor.commit();
		return mVolume;
	}

	public int getVolume() {
		return mVolume;
	}

	public ChannelItem getChannelItem(int id) {
//		if (DEBUG)
//			Log.e(TAG, "id = " + id);
		if (mChannelList == null) {
			if (DEBUG)
				Log.e(TAG, "============ mChannelList is NULL!");
			return null;
		}
		if (id < 0 || id >= mChannelList.size()) {
			if (DEBUG)
				Log.v(TAG, "<myu>id is" + id + " mChannelList.size= "+mChannelList.size());
			return null;
		}
		return mChannelList.get(id);
	}

	public boolean setChannelItem(int id, ChannelItem item) {
		if (DEBUG)
			Log.e(TAG, "============ setChannelItem!");
		if (id < 0 || id > 144 || item == null) {
			return false;
		}
		mChannelList.set(id, item);
		return true;
	}

	@Override
	public void setFunctionId(int id) {
		// TODO Auto-generated method stub
		functionId = id;
	}

	@Override
	public int getFunctionId() {
		// TODO Auto-generated method stub
		return functionId;
	}

	@Override
	public void setCurrentFreq(int freq) {
		// TODO Auto-generated method stub
		curFreq = freq;
	}

	@Override
	public int getCurrentFreq() {
		// TODO Auto-generated method stub
		return curFreq;
	}

	@Override
	public int fineLeft(int freq) {
		// TODO Auto-generated method stub
		if (DEBUG)
			Log.v(TAG, " ++++++++JNI fineLeft worked. Input freq:" + freq);
		curFreq = jniFineLeft(freq);
		if(getRadioType() == RADIO_FM1){
			mCanRadio.sendRadioInfo(CanRadio.BAND_FM, curFreq);
		}else if (getRadioType() == RADIO_AM) {
			mCanRadio.sendRadioInfo(CanRadio.BAND_AM, curFreq);
		}
		if (DEBUG)
			Log.d(TAG, "------- curFreq:" + curFreq);
		if(isLive){
			mRadioplayerHandler.sendEmptyMessage(UPDATE_DETAIL_FREQ);
		}
		updatePreferences(RADIO_DATA_SAVE);
		updatePlaybackTitle();
		
		return curFreq;
	}

	@Override
	public int fineRight(int freq) {
		// TODO Auto-generated method stub
		if (DEBUG)
			Log.v(TAG, " JNI fineRight worked ");
		curFreq = jniFineRight(freq);
		if(getRadioType() == RADIO_FM1){
			mCanRadio.sendRadioInfo(CanRadio.BAND_FM, curFreq);
		}else if (getRadioType() == RADIO_AM) {
			mCanRadio.sendRadioInfo(CanRadio.BAND_AM, curFreq);
		}
		if(isLive){
			mRadioplayerHandler.sendEmptyMessage(UPDATE_DETAIL_FREQ);
		}
		updatePreferences(RADIO_DATA_SAVE);
		updatePlaybackTitle();

		return curFreq;
	}

	@Override
	public int stepLeft(int freq) {
		// TODO Auto-generated method stub
		if (DEBUG)
			Log.v(TAG, " JNI stepLeft worked ");
		if (mIsSearchThreadRunning) {
			Toast toast = Toast.makeText(getApplicationContext(),
					R.string.searchwait, Toast.LENGTH_SHORT);
			return freq;
		}
		jniSetFreq(freq);
		jniStepLeft(freq);
		new Thread(runnable).start();
		// mRadioplayerHandler.sendEmptyMessage(UPDATE_DETAIL_FREQ);
		
		return curFreq;
	}

	@Override
	public int stepRight(int freq) {
		// TODO Auto-generated method stub
		if (DEBUG)
			Log.v(TAG, " JNI stepRight worked ");
		if (mIsSearchThreadRunning) {
			Toast toast = Toast.makeText(getApplicationContext(),
					R.string.searchwait, Toast.LENGTH_SHORT);
			return freq;
		}
		jniSetFreq(freq);
		jniStepRight(freq);
		// mRadioplayerHandler.sendEmptyMessage(UPDATE_DETAIL_FREQ);
		new Thread(runnable).start();
		
		return curFreq;
	}

	@Override
	public int getCurChannelId() {
		// TODO Auto-generated method stub
		return curChannelId;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setCurChannelId(int id) {
		// TODO Auto-generated method stub
		curChannelId = id;

		Context context = getApplicationContext();
		CharSequence contentTitle = getResources().getString(R.string.app_name);
		CharSequence contentText = getResources().getString(R.string.playing)
				+ ": " + changeCurFreqToCS(curFreq);
		notification.when = System.currentTimeMillis();
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		startForeground(NOTIFICATION_ID, notification);
	}

	@Override
	public void registStatusListener(RadioStatusChangeListener listener) {
		// TODO Auto-generated method stub
		mStatusListener = listener;
	}

	@Override
	public void unRegistStatusListener() {
		// TODO Auto-generated method stub
		mStatusListener = null;
	}
	
	public void setRemote(int remote) {
		// TODO Auto-generated method stub
		jniSetRemote(remote);
	}

	@Override
	public void onAutoSearch() {
		// TODO Auto-generated method stub
		if (DEBUG)
			Log.d(TAG, "onAutoSearch");
		if (mIsSearchThreadRunning) {
			Toast toast = Toast.makeText(getApplicationContext(),
					R.string.searchwait, Toast.LENGTH_SHORT);
			return;
		}
		if (getRadioType() == RADIO_FM1 || getRadioType() == RADIO_FM2) {
			turnFmAm(0);
			for (int i = 0; i < RADIO_FM_COUNT; i++) {
				ChannelItem item = new ChannelItem();
				item.freq = "";
				item.name = "";
				item.abridge = "";
				setChannelItem(i, item);
			}

		} else if (getRadioType() == RADIO_AM) {
			turnFmAm(1);
			for (int i = 48; i < 96; i++) {
				ChannelItem item = new ChannelItem();
				item.freq = "";
				item.name = "";
				item.abridge = "";
				setChannelItem(i, item);
			}
		} else if (getRadioType() == RADIO_COLLECT) {
			for (int i = 96; i < RADIO_CHANNEL_COUNT; i++) {
				ChannelItem item = new ChannelItem();
				item.freq = "";
				item.name = "";
				item.abridge = "";
				setChannelItem(i, item);
			}

		}
		FLAG_AUTO = 1;
		jniSetFreq(curFreq);
		jniAutoSeek(curFreq);
		new Thread(runnable).start();

	}

	@Override
	public void setFreq(int freq) {
		// TODO Auto-generated method stub
		if (DEBUG)
			Log.v(TAG, "JNI setfreq has worked ------freq is " + freq);
		jniSetFreq(freq);
		updatePreferences(RADIO_DATA_SAVE);
		if(getRadioType() == RADIO_FM1){
			mCanRadio.sendRadioInfo(CanRadio.BAND_FM, curFreq);
		}else if (getRadioType() == RADIO_AM) {
			mCanRadio.sendRadioInfo(CanRadio.BAND_AM, curFreq);
		}
	}

	public ArrayList<String> radioGetProvince() {
		return m_province_list;
	}

	public List<String> radioGetCity(int id) {
		return m_city_list.get(id);
	}

	public List<String> radioGetChannel(int provinceId, int cityId) {
		List<String> viewList = new ArrayList<String>();
		for (ChannelItem channel : m_channel_list.get(provinceId).get(cityId)) {
			String viewStr = new String(" " + channel.freq + "   "
					+ channel.name);
			viewList.add(viewStr);
		}
		return viewList;
	}

	/**
	 * 获取当前系统语言
	 */
	private String getLocalLanguage() {
		// TODO Auto-generated method stub
		String language = Locale.getDefault().getLanguage();
//		SharedPreferences.Editor editor = settings.edit(); /* ��editor���ڱ���״̬ */
//		editor.putString("local_language", language);
//		editor.commit();
//		Log.v(TAG, "-->" + language);
		return language;
	}
	
	/* 解析XML */
	public boolean radioReadXML() {
		InputStream inputStream;
		String language = settings.getString("local_language", "zh");
		if(DEBUG)Log.v(TAG, "-->" + language + "|getLocalLanguage()== "+getLocalLanguage().equals(language));
		if (m_province_list != null && m_city_list != null
				&& m_channel_list != null ) {
			return true;
		}
		if (DEBUG)
			Log.v(TAG, "radioReadXML is into");
		XmlPullParser parser = Xml.newPullParser();
		/*
		 * Parse XML
		 */
		try {
			if(getLocalLanguage().equals("zh")){
				inputStream = getAssets().open("RadioDefChannel.xml"); 
			}else {
				inputStream = getAssets().open("RadioDefChannel_otherLanguage.xml"); 
			}

			parser.setInput(inputStream, "UTF-8");
			int eventType = parser.getEventType();

			ArrayList<String> curCityList = null;
			ChannelItem curChannelItem = null;
			List<ChannelItem> curItemList = null;
			List<List<ChannelItem>> curCityItemList = null;

			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					m_province_list = new ArrayList<String>();
					m_city_list = new ArrayList<List<String>>();
					m_channel_list = new ArrayList<List<List<ChannelItem>>>();
					break;
				case XmlPullParser.START_TAG:
					String name = parser.getName();
					if (name.equals("Province")) {
						m_province_list.add(parser.getAttributeValue(0));
						curCityList = new ArrayList<String>();
						curCityItemList = new ArrayList<List<ChannelItem>>();
					} else if (name.equals("City")) {
						curCityList.add(parser.getAttributeValue(0));
						curItemList = new ArrayList<ChannelItem>();
					} else if (name.equals("Channel")) {
						curChannelItem = new ChannelItem();
					} else if (name.equals("Abbr")) {
						curChannelItem.abridge = parser.getAttributeValue(0);
					} else if (name.equals("Name")) {
						curChannelItem.name = parser.getAttributeValue(0);
					} else if (name.equals("Freq")) {
						// String StrNoPoint =
						// parser.getAttributeValue(0).replaceAll("\\.", "");
						// curChannelItem.freq = Integer.parseInt(StrNoPoint);
						curChannelItem.freq = parser.getAttributeValue(0);
					}
					break;
				case XmlPullParser.END_TAG:
					if (parser.getName().equals("Province")) {
						if (curCityList != null) {
							m_city_list.add(curCityList);
							curCityList = null;
						}
						m_channel_list.add(curCityItemList);
						curCityItemList = null;
					} else if (parser.getName().equals("City")) {
						curCityItemList.add(curItemList);
						curItemList = null;
					} else if (parser.getName().equals("Channel")) {
						curItemList.add(curChannelItem);
						curChannelItem = null;
					}
					break;
				}
				eventType = parser.next();
			}
			inputStream.close();
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

	void updatePreferences(int type) {
		// .StringBuilder�࣬ʹ������������Ķ���Ĭ�ϻ���16���ַ�ĳ��ȣ�
		// ��Ҳ��������ָ����ʼ���ȡ����ӵ��ַ�������ɵĳ��ȣ�
		// ��StringBuilder������Զ����ӳ��������ɱ����ӵ��ַ�
		// �����Ƶ�����ַ��ӵ�����ʹ��StringBuilder���ó����Ч�ʴ����ߡ�
		// ��StringЧ�ʸߣ�һ��String����ĳ����ǹ̶��ģ����ܸı��������
		StringBuilder sbabridge = new StringBuilder(RADIO_CHANNEL_COUNT);
		StringBuilder sbfreq = new StringBuilder(RADIO_CHANNEL_COUNT);
		StringBuilder sbname = new StringBuilder(RADIO_CHANNEL_COUNT);

		if (type == RADIO_DATA_SAVE) {
			// get activities preferences.get edit to add value
			// SharedPreferences��һ������������ݴ洢��ʽ.���Լ�ֵ�����洢Ӧ�ó����
			// ������Ϣ��һ�ַ�ʽ����ֻ�ܴ洢��������͡�
			SharedPreferences.Editor editor = settings.edit(); /* ��editor���ڱ���״̬ */
			editor.putInt("fmtype", radioType); /* ������ */
			editor.putInt("funId", functionId);
			editor.putInt("chanId", curChannelId);
			editor.putInt("mvolume", mVolume);
			editor.putInt("mfreq", curFreq);
			for (ChannelItem item : mChannelList) {
				// StringBuilder.Append()�÷��൱��String��ġ�+��
				sbfreq.append(item.freq);
				sbfreq.append(";");
				sbname.append(item.name);
				sbname.append(";");
				sbabridge.append(item.abridge);
				sbabridge.append(";");
			}
			editor.putString("freq", sbfreq.toString()); /* д����� */
			editor.putString("name", sbname.toString());
			editor.putString("abridge", sbabridge.toString());
			editor.commit(); // commit to save. /*���д�����*/
		} else if (type == RADIO_DATA_READ) {
			mChannelList = new ArrayList<ChannelItem>(RADIO_CHANNEL_COUNT);
			if (settings.contains("fmtype")) {
				radioType = settings.getInt("fmtype", 0);
				functionId = settings.getInt("funId", 0);
				curChannelId = settings.getInt("chanId", 0);
				mVolume = settings.getInt("mvolume", 0);
				curFreq = settings.getInt("mfreq", 0);

				String[] freqArray = new String[0];
				String[] nameArray = new String[0];
				String[] abridgeArray = new String[0];
				String freqStr = settings.getString("freq", null); /* ��ȡ��� */
				String nameStr = settings.getString("name", null);
				String abridgeStr = settings.getString("abridge", null);
				if (abridgeStr != null && !abridgeStr.equals("")
						&& freqStr != null && !freqStr.equals("")
						&& nameStr != null && !nameStr.equals("")) {
					// split���ڱ�ʶ���ַ�߽���ַ��ַ�
					freqArray = freqStr.split(";");
					nameArray = nameStr.split(";");
					abridgeArray = abridgeStr.split(";");
				}
				if (DEBUG)
					Log.d(TAG, "----freqArray-----" + freqArray.length
							+ "--nameArray--" + nameArray.length
							+ "--abridgeArray--" + abridgeArray.length);
				/*
				 * if(freqArray != null){ for(int i=0; i<freqArray.length; i++){
				 * ChannelItem item = new ChannelItem(); item.freq =
				 * freqArray[i]; item.name = nameArray[i]; item.abridge =
				 * abridgeArray[i]; mChannelList.add(item); if(D)
				 * Log.d(TAG,"---dmlwei item.freq------"+item.freq); } for(int
				 * i=freqArray.length; i<RADIO_CHANNEL_COUNT; i++){ ChannelItem
				 * item = new ChannelItem(); item.freq = ""; item.name = "";
				 * item.abridge = ""; mChannelList.add(item); } }
				 */
				for (int i = 0; i < RADIO_CHANNEL_COUNT; i++) {
					ChannelItem item = new ChannelItem();
					if (i < freqArray.length) {
						item.freq = freqArray[i];
					} else {
						item.freq = "";
					}
					if (i < nameArray.length) {
						item.name = nameArray[i];
					} else {
						item.name = "";
					}
					if (i < abridgeArray.length) {
						item.abridge = abridgeArray[i];
					} else {
						item.abridge = "";
					}
					mChannelList.add(item);
				}
				if (curChannelId < 0 || curChannelId >= RADIO_CHANNEL_COUNT
						|| mChannelList.get(curChannelId).freq.equals("")) {
					curChannelId = 0;
					radioType = RADIO_FM1;
					if (mChannelList.get(curChannelId).freq.equals("")) {
						curChannelId = -1;
						curFreq = FM_LOW_FREQ;
						return;
					}
				}
				if (RADIO_FM1 == radioType) {
					if (curChannelId < RADIO_PAGE_COUNT) {
						// Integer.parseInt()���ַ��͵�����ת��Ϊ���͵�����
						// replaceAll()Դ�ַ��е�ĳһ�ַ���ַ�ȫ������ָ�����ַ���ַ�
						curFreq = Integer.parseInt(mChannelList
								.get(curChannelId).freq.replaceAll("\\.", "")) * 10;
					} else if (curChannelId >= RADIO_PAGE_COUNT
							&& curChannelId < RADIO_FM_COUNT) {
						radioType = RADIO_FM2;
						curFreq = Integer.parseInt(mChannelList
								.get(curChannelId).freq.replaceAll("\\.", "")) * 10;
					} else if (curChannelId >= RADIO_FM_COUNT && curChannelId < 96) {
						radioType = RADIO_AM;
						curFreq = Integer.parseInt(mChannelList
								.get(curChannelId).freq);
					} else if(curChannelId >= 96 && curChannelId < 144){
						radioType = RADIO_COLLECT;
						if(mChannelList.get(curChannelId).freq.contains(".")){
							curFreq = Integer.parseInt(mChannelList
									.get(curChannelId).freq.replaceAll("\\.", "")) * 10;
						}else{
							curFreq = Integer.parseInt(mChannelList
									.get(curChannelId).freq);
						}
					}
				} else if (RADIO_COLLECT == radioType) {
					if (curChannelId < RADIO_PAGE_COUNT) {
						radioType = RADIO_FM1;
						curFreq = Integer.parseInt(mChannelList
								.get(curChannelId).freq.replaceAll("\\.", "")) * 10;
					} else if (curChannelId >= RADIO_PAGE_COUNT
							&& curChannelId < RADIO_FM_COUNT) {
						curFreq = Integer.parseInt(mChannelList
								.get(curChannelId).freq.replaceAll("\\.", "")) * 10;
					} else if (curChannelId >= RADIO_FM_COUNT && curChannelId < 96) {
						radioType = RADIO_AM;
						curFreq = Integer.parseInt(mChannelList
								.get(curChannelId).freq);
					} else if(curChannelId >= 96 && curChannelId < 144){
						if(mChannelList.get(curChannelId).freq.contains(".")){
							curFreq = Integer.parseInt(mChannelList
									.get(curChannelId).freq.replaceAll("\\.", "")) * 10;
						}else{
							curFreq = Integer.parseInt(mChannelList
									.get(curChannelId).freq);
						}
						
					}
				} else if (RADIO_AM == radioType) {
					if (curChannelId < RADIO_PAGE_COUNT) {
						radioType = RADIO_FM1;
						curFreq = Integer.parseInt(mChannelList
								.get(curChannelId).freq.replaceAll("\\.", "")) * 10;
					} else if (curChannelId >= RADIO_PAGE_COUNT
							&& curChannelId < RADIO_FM_COUNT) {
						radioType = RADIO_FM2;
						curFreq = Integer.parseInt(mChannelList
								.get(curChannelId).freq.replaceAll("\\.", "")) * 10;
					} else if (curChannelId >= RADIO_FM_COUNT && curChannelId < 96) {
						curFreq = Integer.parseInt(mChannelList
								.get(curChannelId).freq);
					} else if (curChannelId >= 96 && curChannelId < 144){
						radioType = RADIO_COLLECT;
						if(mChannelList.get(curChannelId).freq.contains(".")){
							curFreq = Integer.parseInt(mChannelList
									.get(curChannelId).freq.replaceAll("\\.", "")) * 10;
						}else{
							curFreq = Integer.parseInt(mChannelList
									.get(curChannelId).freq);
						}
					}
				}
			} else { // read default data
				radioReadXML();
				importChannelList(0, 0, false);
			}
		}

	}

	public boolean importChannelList(int provinceId, int cityId, boolean refresh) {
		int fmNum = 0;
		int amNum = 0;
		int collectNum = 96;
		radioType = 0;
		functionId = 0;
		curChannelId = 0;
		if (DEBUG)
			Log.v(TAG, "<myu>importChannelList has been into");
		if (m_channel_list == null || m_channel_list.size() <= provinceId
				|| m_channel_list.get(provinceId).size() <= cityId) {
			return false;
		}
		if (DEBUG)
			Log.v(TAG, "mChannelList is " + mChannelList);
		mChannelList.clear();
		for (ChannelItem item : m_channel_list.get(provinceId).get(cityId)) {
			if (item.freq.contains(".")) {
				if(getRadioType() == RADIO_FM1){
					mChannelList.add(fmNum++, item);
					if(DEBUG)Log.v(TAG,"111111mChannelList.size() = "+mChannelList.size());
				}else if(getRadioType() == RADIO_COLLECT){
					mChannelList.add(collectNum++, item);
					if(DEBUG)Log.v(TAG,"222222mChannelList.size() = "+mChannelList.size());
				}
			} else {
				amNum++;
				mChannelList.add(item);
				if(DEBUG)Log.v(TAG,"3333333mChannelList.size() = "+mChannelList.size());
			}
		}
		for (int i = fmNum; i < RadioService.RADIO_FM_COUNT; i++) {
			ChannelItem item = new ChannelItem();
			item.freq = "";
			item.name = "";
			item.abridge = "";
			mChannelList.add(fmNum++, item);
			if(DEBUG)Log.v(TAG,"4444444mChannelList.size() = "+mChannelList.size());
		}
		for (int i = amNum; i < RadioService.RADIO_AM_COUNT; i++) {
			ChannelItem item = new ChannelItem();
			item.freq = "";
			item.name = "";
			item.abridge = "";
			mChannelList.add(item);
			if(DEBUG)Log.v(TAG,"5555555mChannelList.size() = "+mChannelList.size());
		}
		//add mChannelList.size() -> RADIO_CHANNEL_COUNT(144)
		for (int i = collectNum; i < RadioService.RADIO_CHANNEL_COUNT; i++) {
			ChannelItem item = new ChannelItem();
			item.freq = "";
			item.name = "";
			item.abridge = "";
			mChannelList.add(item);
			if(DEBUG)Log.v(TAG,"66666666mChannelList.size() = "+mChannelList.size());
			mChannelList.size();
		}
		if (RADIO_FM1 == radioType || RADIO_FM2 == radioType) {
			turnFmAm(RADIO_TYPE_FM);
			curFreq = Integer.parseInt(mChannelList.get(curChannelId).freq
					.replaceAll("\\.", "")) * 10;
		} else if (RADIO_AM == radioType) {
			curFreq = Integer
					.parseInt(mChannelList.get(fmNum + curChannelId).freq);
		} else if(RADIO_COLLECT == radioType){
			if(mChannelList.get(curChannelId).freq.contains(".")){
				curFreq = Integer.parseInt(mChannelList.get(curChannelId).freq
						.replaceAll("\\.", "")) * 10;
			}else{
				curFreq = Integer
						.parseInt(mChannelList.get(fmNum + curChannelId).freq);
			}
			
		}
		updatePreferences(RADIO_DATA_SAVE);
		if (refresh) {
			mRadioplayerHandler.sendEmptyMessage(UPDATE_CHANNEL_LIST);
			mRadioplayerHandler.sendEmptyMessage(UPDATE_DETAIL_FREQ);
		}
		return true;
	}
	
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(
					"android.intent.action.BONOVO_SLEEP_KEY")) {
				PowerOnOff(false);
				Log.v("myu", "BonovoRadio is sleep PowerOnOff");
				//stopService(new Intent("com.example.RadioService")); 
            }else if (intent.getAction().equals("android.intent.action.BONOVO_WAKEUP_KEY")){
                PowerOnOff(true);
                Log.v("myu", "BonovoRadio is wakeup");
                setFreq(curFreq);
                Log.v("myu", "BonovoRadio is wakeup-->setFreq");
        		if(getRadioType() == RADIO_FM1){
        			mCanRadio.sendRadioInfo(CanRadio.BAND_FM, curFreq);
        		}else if (getRadioType() == RADIO_AM) {
        			mCanRadio.sendRadioInfo(CanRadio.BAND_AM, curFreq);
        		}
			}else if (intent.getAction().equals("android.intent.action.BONOVO_RADIO_TURNDOWN")) {
				fineLeft(getCurrentFreq());
			}else if (intent.getAction().equals("android.intent.action.BONOVO_RADIO_TURNUP")) {
				fineRight(getCurrentFreq());
			}
		}
    };

    private IntentFilter getIntentFilter(){
        IntentFilter myIntentFilter = new IntentFilter("android.intent.action.BONOVO_SLEEP_KEY");
        //myIntentFilter.setPriority(Integer.MAX_VALUE);
        myIntentFilter.addAction("android.intent.action.BONOVO_WAKEUP_KEY");
        myIntentFilter.addAction("android.intent.action.BONOVO_RADIO_TURNDOWN");
        myIntentFilter.addAction("android.intent.action.BONOVO_RADIO_TURNUP");
		return myIntentFilter;
	}

	@Override
	public void clearAllContent() {
		// TODO Auto-generated method stub
		if (getRadioType() == RADIO_FM1 || getRadioType() == RADIO_FM2) {

			for (int i = 0; i < RADIO_FM_COUNT; i++) {
				ChannelItem item = new ChannelItem();
				item.freq = "";
				item.name = "";
				item.abridge = "";
				setChannelItem(i, item);
			}

		} else if (getRadioType() == RADIO_AM) {
			for (int i = 48; i < 96; i++) {
				ChannelItem item = new ChannelItem();
				item.freq = "";
				item.name = "";
				item.abridge = "";
				setChannelItem(i, item);
			}
		} else if(getRadioType() == RADIO_COLLECT){
			for (int i = 96; i < RADIO_CHANNEL_COUNT; i++) {
				ChannelItem item = new ChannelItem();
				item.freq = "";
				item.name = "";
				item.abridge = "";
				setChannelItem(i, item);
			}
		}
	}
	
	/*
	 * Read And Set Radio Model -->china japan europe
	 */
	public void readAndSetModelInfo(){
		SharedPreferences modelpre = getSharedPreferences(
				"CHECKED", 0);
		RADIO_MODEL = modelpre.getInt("radioModel", 0);
		if(RADIO_MODEL == JAPAN_MODEL){
			jniSetModel(JAPAN_MODEL);
			FM_HIGH_FREQ = 9000;
			FM_LOW_FREQ = 7600;
			AM_HIGH_FREQ = 1620;
			AM_LOW_FREQ = 522;
		}else if (RADIO_MODEL == EUR_MODEL) {
			jniSetModel(EUR_MODEL);
			FM_HIGH_FREQ = 10800;
			FM_LOW_FREQ = 8700;
			AM_HIGH_FREQ = 1620;
			AM_LOW_FREQ = 522;
		}else {
			jniSetModel(CHINA_MODEL);
			FM_HIGH_FREQ = 10800;
			FM_LOW_FREQ = 8700;
			AM_HIGH_FREQ = 1602;
			AM_LOW_FREQ = 531;
		}
	}
}
