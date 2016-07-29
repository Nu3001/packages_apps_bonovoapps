package com.example.radio;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.android.internal.car.can.CanRadio;
import com.radio.widget.RadioPlayerStatusStore;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RadioService extends Service implements RadioInterface,
        AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_START = "com.example.radioplayer.start";
    public static final String EXTRA_KEY_STOP = "com.example.radioplayer.key_stop";
    public static final String ACTION_STOP = "com.example.radioplayer.stop";
    public static final String ACTION_NEXT = "com.example.radioplayer.next";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            final boolean stop = intent.getBooleanExtra(EXTRA_KEY_STOP, false);
            Log.d(TAG, "start radio service, stop :" + stop);
            if (stop)
                stopSelf();
        }
        if (ACTION_NEXT.equals(action)){
            if (getFunctionId() == FUNCTION_SCAN) {
                setFunctionId(FUNCTION_STEP_SEEK);
            } else if (getFunctionId() == FUNCTION_FINE_TUNE) {
                fineRight(getCurrentFreq());
            } else {
                stepRight(getCurrentFreq());
            }
        }
        return 0;
    }

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
	public static final int ITUREGION1_MODEL = 3;
	public static final int ITUREGION2_MODEL = 4;
	public static final int ITUREGION3_MODEL = 5;
	public static int RADIO_MODEL = 0;

    public static final int NEW_LAYOUT = 1;
    public static final int OLD_LAYOUT = 2;
	public static final int CUSTOM_LAYOUT = 3;
    public static int RADIO_LAYOUT = 1;

    public static int colors[] = new int [4];
    public static int colorFilters[] = new int [4];

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

	private RemoteControlClient mRemoteControlClient;

	// ������ SharedPreferences
	private int functionId = 0; // 0:΢�� 1������ 2���Զ�
	public int curChannelId = -1; // play id 0-47
	private int curFreq;
	private int radioType = RADIO_FM1;
	private int radioVolume;
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

	private boolean mTransientLossOfFocus = false;
	private boolean mRadioFocus = false;
	private CanRadio mCanRadio = null;

	private int mGVolume = mVolume;

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
		if(DEBUG) Log.v(TAG, "------onBind()");

		return serviceBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (DEBUG)
			Log.d(TAG, "------onUnbind()");
		return super.onUnbind(intent);
	}


    private RadioPlayerStatusStore mRadioPlayerStatusStore;
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
        mCanRadio = new CanRadio(this);
        mRadioPlayerStatusStore = RadioPlayerStatusStore.getInstance();
        mRadioPlayerStatusStore.setContext(this);
        mRadioPlayerStatusStore.put(RadioPlayerStatusStore.KEY_PLAY_STOP,
                RadioPlayerStatusStore.VALUE_PLAY);
		synchronized (this) {
			settings = getSharedPreferences("RadioPreferences", MODE_PRIVATE);
//			mRemote = preferences.getBoolean("onoff", true);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			mRemote = prefs.getBoolean("checkbox_remote_preference", true);
			readAndSetModelInfo();
			updatePreferences(RADIO_DATA_READ);
		}

		if(DEBUG) Log.d(TAG, "------onCreate()");
		PowerOnOff(true); // open radio
				if (DEBUG)
					Log.v(TAG, "new Thread is up");

				if (RADIO_FM1 == radioType || RADIO_FM2 == radioType) {
					jniTurnFmAm(0); // open fm
                    mRadioPlayerStatusStore.put(RadioPlayerStatusStore.KEY_FM_AM, RadioPlayerStatusStore.VALUE_FM);
				} else if (RADIO_AM == radioType) {
					jniTurnFmAm(1); // open am
                    mRadioPlayerStatusStore.put(RadioPlayerStatusStore.KEY_FM_AM, RadioPlayerStatusStore.VALUE_AM);
				}else if(RADIO_COLLECT == radioType){
                    mRadioPlayerStatusStore.put(RadioPlayerStatusStore.KEY_FM_AM, RadioPlayerStatusStore.VALUE_COLLECT);
				}
				if (DEBUG)
					Log.v(TAG, "getCurrentFreq()===curfreq ==="
							+ getCurrentFreq());

				setFreq(curFreq);
				setVolume(mVolume);
				if(mRemote){
					setRemote(1);
				}else {
					setRemote(0);
				}
		// handle event with audio
		// remove by bonovo zbiao
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); /* �������� */
//********* removed by bonovo zbiao
		mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);
		//radioVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		this.registerReceiver(myReceiver, getIntentFilter());
		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		ComponentName component =
				new ComponentName(this, MediaButtonIntentReceiver.class);
		mediaButtonIntent.setComponent(component);
		PendingIntent mediaPendingIntent =
				PendingIntent.getBroadcast(getApplicationContext(), 0,
						mediaButtonIntent, 0);
		mRemoteControlClient =
				new RemoteControlClient(mediaPendingIntent);
		mAudioManager.registerMediaButtonEventReceiver(component);
		mAudioManager.registerRemoteControlClient(mRemoteControlClient);

		mRemoteControlClient.setTransportControlFlags(
				RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
						RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(false);
		if(DEBUG) Log.d(TAG, "------onDestroy()");
		updatePreferences(RADIO_DATA_SAVE);
        mRadioPlayerStatusStore.put(RadioPlayerStatusStore.KEY_PLAY_STOP,
                RadioPlayerStatusStore.VALUE_STOP);
		if (DEBUG)
			Log.d(TAG, "onDestroy()  end curfreq is " + curFreq);
		PowerOnOff(false);
		this.unregisterReceiver(myReceiver);
		mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
		ComponentName component =
				new ComponentName(this, MediaButtonIntentReceiver.class);
		mAudioManager.unregisterMediaButtonEventReceiver(component);
		mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
		mAudioManager.abandonAudioFocus(this);
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
			Log.d(TAG, "+++++++++++++ STOP SEARCH!!!!");
		}
	};

	public void updatePlaybackTitle() {
		// we can eventually embed RDS data in here, but for now just what info we have available.
		String artist;
		String album;
		String song;
		if (getCurChannelId() == -1) {
			artist = album = song = getResources().getString(R.string.app_name) + " : " +
			formatFreqDisplay (getCurrentFreq());
		} else {
			song = getResources().getString(R.string.app_name) + " : " +
					formatFreqDisplay (getCurrentFreq());
			artist = getChannelItem(getCurChannelId()).name;
			album = getChannelItem(getCurChannelId()).abridge;
			if (artist.equals("")) {
				artist = song;
			}
			if (album.equals("")) {
				album = song;
			}
		}
		updatePlaybackTitle(album,artist,song);
	}
	private void updatePlaybackTitle(String album,String artist,String song){
		Bitmap artwork;

		MetadataEditor editor = mRemoteControlClient.editMetadata(false);

		artwork = BitmapFactory.decodeResource(getResources(),R.drawable.radio);
		editor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK,artwork);
		editor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, album);
		editor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, artist);
		editor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE,song);
		editor.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, Long.MAX_VALUE);

		editor.apply();
	}

	private String formatFreqDisplay(int freq) {
		return formatFreqDisplay(this, freq);
	}

	public static String formatFreqDisplay(Context context, int freq) {
		String display;

		if (freq < 1000) {
			display = Integer.toString(freq) + " " +
					context.getResources().getString(R.string.khz);
		} else {
			display = String.valueOf(freq/100.0) + " " +
					context.getResources().getString(R.string.mhz);
		}

		return display;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		//if (DEBUG)
		Log.v(TAG, "----onAudioFocusChange----focusChange:" + focusChange);
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			Log.v(TAG, "----onAudioFocusChange----AudioManager.AUDIOFOCUS_GAIN:" + focusChange+"getRadioStatus()="+getRadioStatus()+" mTransientLossOfFocus="+mTransientLossOfFocus);

			if (getRadioStatus()==false && mTransientLossOfFocus) {
                                mTransientLossOfFocus = false;
                                PowerOnOff(true);
				    setFreq(getCurrentFreq());
                    }
                // Restore volume
                duckVolume(radioDuckVolume);
			break;
		case AudioManager.AUDIOFOCUS_LOSS:
			Log.v(TAG, "----onAudioFocusChange----AudioManager.AUDIOFOCUS_LOSS:" + focusChange);
			//************ removed by bonovo zbiao
                stopService(new Intent("com.example.RadioService"));
                this.stopSelf();

                break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			Log.v(TAG, "----onAudioFocusChange----AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:" + focusChange);
            // Reduce volume to Zero - save current volume;
            radioDuckVolume = getVolume();
            duckVolume(0);
			 if (getRadioStatus() ==true) {

                                mTransientLossOfFocus = true;
				    PowerOnOff(false);
                      }


			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			Log.v(TAG, "----onAudioFocusChange----AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:" + focusChange);
			// reduce volume by 50%
            radioDuckVolume = getVolume();
            duckVolume(getVolume() / 2);
			break;

		}

    }

	boolean m_PowerOnOffStatus=false;
    private void PowerOnOff(boolean onOff) {
		m_PowerOnOffStatus=onOff;
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

	public boolean getRadioStatus() {
		return m_PowerOnOffStatus;
	}

	public void turnFmAm(int type) {
		if (DEBUG)
			Log.v(TAG, "<myu>turnFmAm type=" + type );
		jniTurnFmAm(type);
        String fmType = null;
        if (type == RADIO_FM1 || type == RADIO_FM2) {
            fmType = RadioPlayerStatusStore.VALUE_FM;
        }
        if (type == RADIO_AM) {
            fmType = RadioPlayerStatusStore.VALUE_AM;
        }
        if(type == RADIO_COLLECT){
        	fmType = RadioPlayerStatusStore.VALUE_COLLECT;
        }
        mRadioPlayerStatusStore.put(RadioPlayerStatusStore.KEY_FM_AM, fmType);
	}

	public int getRadioType() {
		return radioType;
	}

	public void setRadioType(int type) {
		radioType = type;
	}

	public int duckVolume(int volume) {
		jniSetVolume(volume);
		return volume;
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
		functionId = id;
	}

	@Override
	public int getFunctionId() {
		return functionId;
	}

	@Override
	public void setCurrentFreq(int freq) {
		curFreq = freq;
	}

	@Override
	public int getCurrentFreq() {
		return curFreq;
	}

	@Override
	public int fineLeft(int freq) {
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

		return curFreq;
	}

	@Override
	public int fineRight(int freq) {
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

		return curFreq;
	}

	@Override
	public int stepLeft(int freq) {
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
		return curChannelId;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setCurChannelId(int id) {
		curChannelId = id;
	}

	@Override
	public void registStatusListener(RadioStatusChangeListener listener) {
		mStatusListener = listener;
	}

	@Override
	public void unRegistStatusListener() {
		mStatusListener = null;
	}

	public void setRemote(int remote) {
		jniSetRemote(remote);
	}

	@Override
	public void onAutoSearch() {
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
		Log.v(TAG, "JNI setfreq has worked ------freq is " + freq);
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

        String fmType = null;
        if (radioType == RADIO_FM1 || radioType == RADIO_FM2) {
            fmType = RadioPlayerStatusStore.VALUE_FM;
        }
        if (radioType == RADIO_AM) {
            fmType = RadioPlayerStatusStore.VALUE_AM;
        }
        if (radioType == RADIO_COLLECT) {
            fmType = RadioPlayerStatusStore.VALUE_COLLECT;
        }
        mRadioPlayerStatusStore.put(RadioPlayerStatusStore.KEY_FM_AM, fmType);
        mRadioPlayerStatusStore.put(RadioPlayerStatusStore.KEY_HZ, String.valueOf(curFreq));
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
            editor.putInt("radioLayout", RADIO_LAYOUT);
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
                RADIO_LAYOUT = settings.getInt("radioLayout", 0);
                getCustomColors();

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
		//Log.v(TAG,".provinceId= "+provinceId+"cityId="+cityId);
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

				Log.v(TAG,".freq= "+item.freq +"name="+item.name);

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
			if(mChannelList.get(curChannelId).freq!="")
			{
				curFreq = Integer.parseInt(mChannelList.get(curChannelId).freq
						.replaceAll("\\.", "")) * 10;
			}

		} else if (RADIO_AM == radioType) {
			if(mChannelList.get(fmNum + curChannelId).freq!="")
			{
				curFreq = Integer
						.parseInt(mChannelList.get(fmNum + curChannelId).freq);
			}

		} else if(RADIO_COLLECT == radioType){

			if(mChannelList.get(curChannelId).freq!="")
			{
				if(mChannelList.get(curChannelId).freq.contains(".")){
					curFreq = Integer.parseInt(mChannelList.get(curChannelId).freq
							.replaceAll("\\.", "")) * 10;
				}else{
					if(mChannelList.get(fmNum + curChannelId).freq!="")
					{
						curFreq = Integer
								.parseInt(mChannelList.get(fmNum + curChannelId).freq);
					}

				}
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
			else if (intent.getAction().equals("android.intent.action.BONOVO_RADIO_SETFM")){

				int fm_freq = intent.getIntExtra("FM",0);

				if(fm_freq>0)
				{
					turnFmAm(0);
					setCurrentFreq(fm_freq/10);
					setRadioType(RadioService.RADIO_FM1);
				}

				int am_freq = intent.getIntExtra("AM",0);

				if(am_freq>0)
				{
					turnFmAm(1);
					setCurrentFreq(am_freq/1000);
					setRadioType(RadioService.RADIO_AM);
				}

				setFreq(getCurrentFreq());



				if (mStatusListener != null) {
					mStatusListener.onStatusChange(UPDATE_DETAIL_FREQ);
				}

				Log.v(TAG, "BONOVO_RADIO_SETFM    FM="+fm_freq+",AM="+am_freq);

			}else if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
				int nStreamType = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE,0);
				int nValue = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE,0);
				int nOldValue = intent.getIntExtra(AudioManager.EXTRA_PREV_VOLUME_STREAM_VALUE,0);

				Log.v("myu", "BroadcastReceiver is VOLUME_CHANGED_ACTION nStreamType"+nStreamType+",nValue="+nValue+",nOldValue="+nOldValue);

				if(nStreamType==3 && nOldValue>0 && nValue==0)
				{
					mGVolume=getVolume();
					//int ret= setVolume(nValue);
					//Log.v("myu", "BroadcastReceiver is VOLUME_CHANGED_ACTION ret="+ret);
				}
				else if(nStreamType==3)
				{
					//int ret=setVolume(mGVolume);

					//Log.v("myu", "BroadcastReceiver is VOLUME_CHANGED_ACTION ret="+ret);
				}

			}
		}
    };

    private IntentFilter getIntentFilter(){
        IntentFilter myIntentFilter = new IntentFilter("android.intent.action.BONOVO_SLEEP_KEY");
        //myIntentFilter.setPriority(Integer.MAX_VALUE);
        myIntentFilter.addAction("android.intent.action.BONOVO_WAKEUP_KEY");
        myIntentFilter.addAction("android.intent.action.BONOVO_RADIO_TURNDOWN");
        myIntentFilter.addAction("android.intent.action.BONOVO_RADIO_TURNUP");
	 	myIntentFilter.addAction("android.intent.action.BONOVO_RADIO_SETFM");
	 	myIntentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
		return myIntentFilter;
	}

	@Override
	public void clearAllContent() {
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

		RADIO_MODEL = settings.getInt("radioModel", 0);
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
		}else if (RADIO_MODEL == CHINA_MODEL) {
			jniSetModel(CHINA_MODEL);
			FM_HIGH_FREQ = 10800;
			FM_LOW_FREQ = 8700;
			AM_HIGH_FREQ = 1602;
			AM_LOW_FREQ = 531;
		} else if (RADIO_MODEL == ITUREGION1_MODEL) {
			jniSetModel(ITUREGION1_MODEL);
			FM_HIGH_FREQ = 10800;
			FM_LOW_FREQ = 8750;
			AM_HIGH_FREQ = 153;
			AM_LOW_FREQ = 279;
		}else if (RADIO_MODEL == ITUREGION2_MODEL) {
			jniSetModel(ITUREGION2_MODEL);
			FM_HIGH_FREQ = 10790;
			FM_LOW_FREQ = 8790;
			AM_HIGH_FREQ = 1710;
			AM_LOW_FREQ = 540;
		}else if (RADIO_MODEL == ITUREGION3_MODEL) {
			jniSetModel(ITUREGION3_MODEL);
			FM_HIGH_FREQ = 10800;
			FM_LOW_FREQ = 8750;
			AM_HIGH_FREQ = 1611;
			AM_LOW_FREQ = 531;
		}
	}

    public int getLayout() {
        RADIO_LAYOUT = settings.getInt("radioLayout", 0);
        return RADIO_LAYOUT;
    }

	public int getScanDelayMs() {
		// Default delay = 3 seconds
		return settings.getInt("scanDelayMsecs", 3000);
	}

    public void setCustomColors(int alpha, int red, int green, int blue, int brightness, int contrast, int saturation, int hue) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("alpha", alpha);
        editor.putInt("red", red);
        editor.putInt("green", green);
        editor.putInt("blue", blue);
        editor.putInt("brightness", brightness);
        editor.putInt("contrast", contrast);
        editor.putInt("saturation", saturation);
        editor.putInt("hue", hue);
        editor.commit();
        getCustomColors();

    }

    public void getCustomColors() {
        colors[0] = settings.getInt("alpha", 0);
        colors[1] = settings.getInt("red", 0);
        colors[2] = settings.getInt("green", 0);
        colors[3] = settings.getInt("blue", 0);
        colorFilters[0] = settings.getInt("brightness", 0);
        colorFilters[1] = settings.getInt("contrast", 0);
        colorFilters[2] = settings.getInt("saturation", 0);
        colorFilters[3] = settings.getInt("hue", 0);
    }

    public Drawable changeColor(Bitmap bmap) {

        int aRGB = Color.argb(colors[0], colors[1], colors[2], colors[3]);
        ColorFilter colorFilter = ColorFilterGenerator.adjustColor(colorFilters[0], colorFilters[1], colorFilters[2], colorFilters[3]);

        Canvas canvas = new Canvas();
        Bitmap result = Bitmap.createBitmap(bmap.getWidth(), bmap.getHeight(), Bitmap.Config.ARGB_8888);

        canvas.setBitmap(result);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);

        paint.setColorFilter(new PorterDuffColorFilter(aRGB, PorterDuff.Mode.MULTIPLY));
        canvas.drawBitmap(bmap, 0, 0, paint);
        paint.setColorFilter(null);

        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(result, 0, 0, paint);
        paint.setColorFilter(null);

        return new BitmapDrawable(getResources(), result);
    }

}
