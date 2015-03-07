package com.example.radio;

import com.example.radio.RadioInterface.RadioStatusChangeListener;
import com.example.radio.RadioService.ChannelItem;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class RadioActivity extends Activity implements 
		ServiceConnection, OnClickListener, OnSeekBarChangeListener ,OnLongClickListener{

	/** message id. */
	private static final int UPDATE_CHANNEL_LIST = 0;
	private static final int UPDATE_DETAIL_FREQ = 1;
	// private static final int AUTOSEARCH_COMPLETE = 2;
	// private static final int DISMISS_VOLUME_DIALOG = 3;
	private static final int DISMISS_INPUT_DIALOG = 4;
	private static final int VOLUME_DELAY_TIME = 3000;
	private static final int CHANNEL_SIZE = 48;						//Channel娑擃亝鏆�

	private static final int DIALOG_SCAN = 0;
	private static final int DIALOG_CONFIRM = 1;
	private static final int DIALOG_VOLUME = 2;
	private static final int DIALOG_EDIT = 3;
	private static final int DIALOG_INPUTFREQ_FM = 4;
	private static final int DIALOG_INPUTFREQ_AM = 5;

	private static boolean mDown = false;			//keyEvent flag

	private AlertDialog mVolCtrlDialog;
	private AlertDialog mInputDialog;
	private ProgressDialog mSearchDialog;
	private SeekBar mVolumeSeekBar;
	private AlertDialog warnDialog;

	private static final boolean DEBUG = true;
	private static int  flag = 0;						//閼奉亜濮╅幖婊冨酱閺嶅洩顔�
	private static int IS_AUTO_NOW = 1;
	private static int IS_STEP_NOW = 2;
	private final static String TAG = "RadioActivity";
	private RadioService radioService = null;

	private ImageButton mMiddleButtonForward;
	private ImageButton mMiddleButtonBackward;
	private ImageButton mTitleButtonClose;
	private ImageButton mTitleButtonHome;

	private Button mMiddleButtonFine;
	private Button mMiddleButtonStep;
	private Button mMiddleButtonAuto;
	private Button mBottomButtonImport;
	private Button mBottomButtonVolume;
	private Button mButtonButtonFm1;
	private Button mButtonButtonFm2;
	private Button mButtonButtonAm;
	private Button mButtonButtonSetting;
	//private Button mButtonButtoncollect;
	private Button[] channelBtn = new Button[CHANNEL_SIZE];
	Intent serviceIntent = new Intent("com.example.RadioService");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radio_player_layout);
		setupview();
		
		registerReceiver(myBroadcastReveiver, getIntentFilter());

		radioService.isLive = true;
		if(DEBUG) Log.d(TAG, "++++++onCreate()");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		radioService.isLive = false;
		super.onDestroy();
		if (mHandler != null) {
			mHandler.removeMessages(UPDATE_CHANNEL_LIST);
			mHandler.removeMessages(UPDATE_DETAIL_FREQ);
			mHandler = null;
		}
		if (mSearchDialog != null) {
			removeDialog(DIALOG_SCAN);
		}
		this.unregisterReceiver(myBroadcastReveiver);
		
		//获取音频服务
		AudioManager audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
		//注册接收的Receiver
		ComponentName mRemoteControlClientReceiverComponent;
		mRemoteControlClientReceiverComponent = new ComponentName(
		                getPackageName(), MediaButtonIntentReceiver.class.getName());
		audioManager.unregisterMediaButtonEventReceiver(mRemoteControlClientReceiverComponent);
		if(DEBUG) Log.d(TAG, "++++++onDestroy()");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unbindService(this);
				
		if(DEBUG) Log.d(TAG, "++++++onPause()");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		this.startService(serviceIntent);/* 閿熸枻鎷稲adioService閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹 */
		bindService(serviceIntent, this, BIND_AUTO_CREATE);
		
		//获取音频服务
		AudioManager audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
		//注册接收的Receiver
		ComponentName mRemoteControlClientReceiverComponent;
		mRemoteControlClientReceiverComponent = new ComponentName(
				getPackageName(), MediaButtonIntentReceiver.class.getName());
		//注册MediaButton
		audioManager.registerMediaButtonEventReceiver(mRemoteControlClientReceiverComponent);
				
		if(DEBUG) Log.d(TAG, "++++++onResume()");
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case R.id.btnhome:// 閿熸枻鎷烽敓鏂ゆ嫹 閿熸枻鎷烽敓鏂ゆ嫹閿熻緝鍖℃嫹Service
				if (DEBUG)
					Log.d(TAG, "btnhome has worked");
				finish();
				break;
			case R.id.btnclose:// 閿熸枻鎷烽敓鏂ゆ嫹 閿熻緝鍖℃嫹Service
				if (DEBUG)
					Log.d(TAG, "btnclose has worked");
				radioService.stopService(new Intent("com.example.RadioService"));
				finish();
				break;
			case R.id.btnimport:
				if (DEBUG)
					Log.d(TAG, "btnimport has worked");
				Intent it = new Intent(RadioActivity.this,
						RadioImportActivity.class);
				RadioActivity.this.startActivity(it);
				break;
			case R.id.btnfine:
				if (DEBUG)
					Log.d(TAG, "btnfine has worked");
				radioService.setFunctionId(0);
				mMiddleButtonBackward
						.setBackgroundResource(R.drawable.btnfinebackward);
				mMiddleButtonForward
						.setBackgroundResource(R.drawable.btnfineforward);
				break;
			case R.id.btnstep:
				if (DEBUG)
					Log.d(TAG, "btnstep has worked");
				radioService.setFunctionId(1);
				mMiddleButtonBackward
						.setBackgroundResource(R.drawable.btnstepbackward);
				mMiddleButtonForward
						.setBackgroundResource(R.drawable.btnstepforward);
				break;
			case R.id.btnauto:
				if (DEBUG)
					Log.d(TAG, "btnauto has worked");
				//showDialog(DIALOG_CONFIRM);
				radioService.STEP_OR_AUTO = IS_AUTO_NOW;
				new AlertDialog.Builder(RadioActivity.this)
						.setTitle(R.string.remind)
						.setCancelable(true)
						.setMessage(R.string.describe)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										for (int i = 0; i < CHANNEL_SIZE; i++) {
											channelBtn[i].setText("");
										}

										radioService.onAutoSearch();

									}
								})
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub

									}
								}).create().show();				
				break;
			case R.id.btnfm1:
				if (DEBUG)
					Log.d(TAG, "btnfm1 has worked");
				if(!radioService.mIsSearchThreadRunning){
					updateFreqView();
					radioSetSelect(RadioService.RADIO_FM1);
				}else {
					Toast.makeText(getApplicationContext(), R.string.searching, Toast.LENGTH_SHORT).show();
				}
//				updateFreqView();
//				radioSetSelect(RadioService.RADIO_FM1);
				break;
			case R.id.btnfm2:
				if (DEBUG)
					Log.d(TAG, "btnfm2 has worked");
				if(!radioService.mIsSearchThreadRunning){
					updateFreqView();
					radioSetSelect(RadioService.RADIO_FM2);
				}else {
					Toast.makeText(getApplicationContext(), R.string.searching, Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.btnam:
				if (DEBUG)
					Log.d(TAG, "btnam has worked");
				if(!radioService.mIsSearchThreadRunning){
					updateFreqView();
					radioSetSelect(RadioService.RADIO_AM);
				}else {
					Toast.makeText(getApplicationContext(), R.string.searching, Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.btnvolume:
				if (DEBUG)
					Log.d(TAG, "btlvoume has worked");
				showDialog(DIALOG_VOLUME);
				// radioService.popupVolumeWindows();
				break;
			case R.id.btnsetting:
				if (DEBUG)
					Log.d(TAG, "btnsrtting has worked");
				Intent setting = new Intent(RadioActivity.this, RadioSetting.class);
				//setting.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				RadioActivity.this.startActivity(setting);
				/*
				LayoutInflater setting =LayoutInflater.from(getApplicationContext());
				View myView = setting.inflate(R.layout.radio_setting_layout, null);
				Dialog dialog =new AlertDialog.Builder(RadioActivity.this)
						.setView(myView)
						.create();
				dialog.show();
				//dialog.getWindow().setLayout(729, 503);
				 */
				break;
//			case R.id.collect:
//				Log.d(TAG, "collect has worked");
//				break;
			case R.id.btnforward:
				if (radioService.getFunctionId() == 0) {
					radioService.fineRight(radioService.getCurrentFreq());
				} else {
					radioService.stepRight(radioService.getCurrentFreq());
					radioService.STEP_OR_AUTO = IS_STEP_NOW;
				}
				break;
			case R.id.btnbackward:
				if (radioService.getFunctionId() == 0) {
					radioService.fineLeft(radioService.getCurrentFreq());
				} else {
					radioService.stepLeft(radioService.getCurrentFreq());
					radioService.STEP_OR_AUTO = IS_STEP_NOW;
				}
				break;
			case R.id.channel0:
			case R.id.channel1:
			case R.id.channel2:
			case R.id.channel3:
			case R.id.channel4:
			case R.id.channel5:
			case R.id.channel6:
			case R.id.channel7:
			case R.id.channel8:
			case R.id.channel9:
			case R.id.channel10:
			case R.id.channel11:
			case R.id.channel12:
			case R.id.channel13:
			case R.id.channel14:
			case R.id.channel15:
			case R.id.channel16:
			case R.id.channel17:
			case R.id.channel18:
			case R.id.channel19:
			case R.id.channel20:
			case R.id.channel21:
			case R.id.channel22:
			case R.id.channel23:
			case R.id.channel24:
			case R.id.channel25:
			case R.id.channel26:
			case R.id.channel27:
			case R.id.channel28:
			case R.id.channel29:
			case R.id.channel30:
			case R.id.channel31:
			case R.id.channel32:
			case R.id.channel33:
			case R.id.channel34:
			case R.id.channel35:
			case R.id.channel36:
			case R.id.channel37:
			case R.id.channel38:
			case R.id.channel39:
			case R.id.channel40:
			case R.id.channel41:
			case R.id.channel42:
			case R.id.channel43:
			case R.id.channel44:
			case R.id.channel45:
			case R.id.channel46:
			case R.id.channel47:
						
				if (DEBUG)
					Log.v(TAG, "Click Button has worked");
				setChannelChecked(what - R.id.channel0);
				Log.v("ccc","setChannelChecked arg = " + (what - R.id.channel0));
				Log.v("ccc","CurChannel = " + radioService.getCurChannelId());
				break;
			case UPDATE_CHANNEL_LIST:
				updateChannelList();
				break;
			case DISMISS_INPUT_DIALOG:
				if (mInputDialog.isShowing()) {
					mInputDialog.dismiss();
				}
				break;
			case UPDATE_DETAIL_FREQ:
				updateFreqView();
				break;
			default:
				break;
			}
		}
	};

	private int editChannelId = -1;

	public boolean onLongClick(View v) {
		int viewId = v.getId();
		switch (viewId) {
		case R.id.channel0:
		case R.id.channel1:
		case R.id.channel2:
		case R.id.channel3:
		case R.id.channel4:
		case R.id.channel5:
		case R.id.channel6:
		case R.id.channel7:
		case R.id.channel8:
		case R.id.channel9:
		case R.id.channel10:
		case R.id.channel11:
		case R.id.channel12:
		case R.id.channel13:
		case R.id.channel14:
		case R.id.channel15:
		case R.id.channel16:
		case R.id.channel17:
		case R.id.channel18:
		case R.id.channel19:
		case R.id.channel20:
		case R.id.channel21:
		case R.id.channel22:
		case R.id.channel23:
		case R.id.channel24:
		case R.id.channel25:
		case R.id.channel26:
		case R.id.channel27:
		case R.id.channel28:
		case R.id.channel29:
		case R.id.channel30:
		case R.id.channel31:
		case R.id.channel32:
		case R.id.channel33:
		case R.id.channel34:
		case R.id.channel35:
		case R.id.channel36:
		case R.id.channel37:
		case R.id.channel38:
		case R.id.channel39:
		case R.id.channel40:
		case R.id.channel41:
		case R.id.channel42:
		case R.id.channel43:
		case R.id.channel44:
		case R.id.channel45:
		case R.id.channel46:
		case R.id.channel47:
			if (DEBUG)
				Log.d(TAG, "onLongClick channel has worked");
			if (radioService.getRadioType() == RadioService.RADIO_FM1) {
				editChannelId = viewId - R.id.channel0;
				if (DEBUG)
					Log.d(TAG, "onLongClick editChannelId = "+ editChannelId);
			} else if (radioService.getRadioType() == RadioService.RADIO_FM2) {
				editChannelId = viewId - R.id.channel0
						+ RadioService.RADIO_PAGE_COUNT;
				if (DEBUG)
					Log.d(TAG, "onLongClick editChannelId = "+ editChannelId);
			} else {
				editChannelId = viewId - R.id.channel0
						+ RadioService.RADIO_FM_COUNT;
				if (DEBUG)
					Log.d(TAG, "onLongClick editChannelId = "+ editChannelId);
			}
//			radioService.setChannelItem(editChannelId,
//					radioService.getChannelItem(editChannelId));
			showDialog(DIALOG_EDIT);
			break;
		default:
			break;
		}
		return true;
	}

	private void setupview() {
		mMiddleButtonBackward = (ImageButton) findViewById(R.id.btnbackward);
		mMiddleButtonBackward.setOnClickListener(this);
		mMiddleButtonForward = (ImageButton) findViewById(R.id.btnforward);
		mMiddleButtonForward.setOnClickListener(this);
		mTitleButtonClose = (ImageButton) findViewById(R.id.btnclose);
		mTitleButtonClose.setOnClickListener(this);
		//mTitleButtonHome = (ImageButton) findViewById(R.id.btnhome);
		//mTitleButtonHome.setOnClickListener(this);

		mButtonButtonFm1 = (Button) findViewById(R.id.btnfm1);
		mButtonButtonFm1.setOnClickListener(this);
		//mButtonButtonFm2 = (Button) findViewById(R.id.btnfm2);
		//mButtonButtonFm2.setOnClickListener(this);
		mButtonButtonAm = (Button) findViewById(R.id.btnam);
		mButtonButtonAm.setOnClickListener(this);
		//mBottomButtonVolume = (Button) findViewById(R.id.btnvolume);
		//mBottomButtonVolume.setOnClickListener(this);
		//mBottomButtonImport = (Button) findViewById(R.id.btnimport);
		//mBottomButtonImport.setOnClickListener(this);
		mButtonButtonSetting = (Button)findViewById(R.id.btnsetting);
		mButtonButtonSetting.setOnClickListener(this);
		mMiddleButtonFine = (Button) findViewById(R.id.btnfine);
		mMiddleButtonFine.setOnClickListener(this);
		mMiddleButtonStep = (Button) findViewById(R.id.btnstep);
		mMiddleButtonStep.setOnClickListener(this);
		mMiddleButtonAuto = (Button) findViewById(R.id.btnauto);
		mMiddleButtonAuto.setOnClickListener(this);		
//		mButtonButtoncollect = (Button)findViewById(R.id.collect);
//		mButtonButtoncollect.setOnClickListener(this);

		channelBtn[0] = (Button) findViewById(R.id.channel0);
		channelBtn[1] = (Button) findViewById(R.id.channel1);
		channelBtn[2] = (Button) findViewById(R.id.channel2);
		channelBtn[3] = (Button) findViewById(R.id.channel3);
		channelBtn[4] = (Button) findViewById(R.id.channel4);
		channelBtn[5] = (Button) findViewById(R.id.channel5);
		channelBtn[6] = (Button) findViewById(R.id.channel6);
		channelBtn[7] = (Button) findViewById(R.id.channel7);
		channelBtn[8] = (Button) findViewById(R.id.channel8);
		channelBtn[9] = (Button) findViewById(R.id.channel9);
		channelBtn[10] = (Button) findViewById(R.id.channel10);
		channelBtn[11] = (Button) findViewById(R.id.channel11);
		channelBtn[12] = (Button) findViewById(R.id.channel12);
		channelBtn[13] = (Button) findViewById(R.id.channel13);
		channelBtn[14] = (Button) findViewById(R.id.channel14);
		channelBtn[15] = (Button) findViewById(R.id.channel15);
		channelBtn[16] = (Button) findViewById(R.id.channel16);
		channelBtn[17] = (Button) findViewById(R.id.channel17);
		channelBtn[18] = (Button) findViewById(R.id.channel18);
		channelBtn[19] = (Button) findViewById(R.id.channel19);
		channelBtn[20] = (Button) findViewById(R.id.channel20);
		channelBtn[21] = (Button) findViewById(R.id.channel21);
		channelBtn[22] = (Button) findViewById(R.id.channel22);
		channelBtn[23] = (Button) findViewById(R.id.channel23);
		channelBtn[24] = (Button) findViewById(R.id.channel24);
		channelBtn[25] = (Button) findViewById(R.id.channel25);
		channelBtn[26] = (Button) findViewById(R.id.channel26);
		channelBtn[27] = (Button) findViewById(R.id.channel27);
		channelBtn[28] = (Button) findViewById(R.id.channel28);
		channelBtn[29] = (Button) findViewById(R.id.channel29);
		channelBtn[30] = (Button) findViewById(R.id.channel30);
		channelBtn[31] = (Button) findViewById(R.id.channel31);
		channelBtn[32] = (Button) findViewById(R.id.channel32);
		channelBtn[33] = (Button) findViewById(R.id.channel33);
		channelBtn[34] = (Button) findViewById(R.id.channel34);
		channelBtn[35] = (Button) findViewById(R.id.channel35);
		channelBtn[36] = (Button) findViewById(R.id.channel36);
		channelBtn[37] = (Button) findViewById(R.id.channel37);
		channelBtn[38] = (Button) findViewById(R.id.channel38);
		channelBtn[39] = (Button) findViewById(R.id.channel39);
		channelBtn[40] = (Button) findViewById(R.id.channel40);
		channelBtn[41] = (Button) findViewById(R.id.channel41);
		channelBtn[42] = (Button) findViewById(R.id.channel42);
		channelBtn[43] = (Button) findViewById(R.id.channel43);
		channelBtn[44] = (Button) findViewById(R.id.channel44);
		channelBtn[45] = (Button) findViewById(R.id.channel45);
		channelBtn[46] = (Button) findViewById(R.id.channel46);
		channelBtn[47] = (Button) findViewById(R.id.channel47);
		
		
		for (int i = 0; i < CHANNEL_SIZE; i++) {
			channelBtn[i].setOnClickListener(this);
			channelBtn[i].setOnLongClickListener(this);
		}
	}

	private RadioStatusChangeListener mRadioStatusListener = new RadioStatusChangeListener() {

		@Override
		public void onStatusChange(int type) {
			// TODO Auto-generated method stub
			Message msg = mHandler.obtainMessage(type);
			mHandler.sendMessage(msg);
		}
	};

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		// TODO Auto-generated method stub
//		 switch (keyCode) {
//         case KeyEvent.KEYCODE_MEDIA_NEXT:
//        	  if (radioService.getFunctionId() == 0) {
//					radioService.fineRight(radioService.getCurrentFreq());
//				} else {
//					radioService.stepRight(radioService.getCurrentFreq());
//				}
//             return true;
//         case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
//        	 if (radioService.getFunctionId() == 0) {
//					radioService.fineLeft(radioService.getCurrentFreq());
//				} else {
//					radioService.stepLeft(radioService.getCurrentFreq());
//				}
//        	 return true;
//		 }
//		return super.onKeyDown(keyCode, event);
//		
//	}

	public void setChannelChecked(int btnId) {
		int checkedId = 0;
		ChannelItem item;
		if (DEBUG)
			Log.v(TAG, "setChannelChecked is into here");
		if (radioService.getRadioType() == RadioService.RADIO_FM1) {
			checkedId = btnId;
		} else if (radioService.getRadioType() == RadioService.RADIO_FM2) {
			checkedId = btnId + RadioService.RADIO_PAGE_COUNT;
		} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
			checkedId = btnId + RadioService.RADIO_FM_COUNT;
		}
		item = radioService.getChannelItem(checkedId);
		if (item.freq.equals("")) {
			return;
		}
		// comtains閿熸枻鎷稟閿熸枻鎷烽敓鏂ゆ嫹A閿熻鍖℃嫹鏃堕敓鏂ゆ嫹閿熸枻鎷積quals閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鍙瘮杈冪鎷�
		if (item.freq.contains(".")) {
			radioService.setCurrentFreq(Integer.parseInt(item.freq.replaceAll(
					"\\.", "")) * 10);
		} else {
			radioService.setCurrentFreq(Integer.parseInt(item.freq.replaceAll(
					"\\.", "")));
		}

		for (int i = 0; i < RadioService.RADIO_PAGE_COUNT; i++) {
			if (channelBtn[i].isSelected()) {
				channelBtn[i].setSelected(false);
			}
		}
		channelBtn[btnId].setSelected(true);
		// check if playtype is the same as before,fm or am
		/*
		 * if (radioService.getCurChannelId() < RadioService.RADIO_FM_COUNT &&
		 * checkedId > RadioService.RADIO_FM_COUNT) { // pre // fm // now // am
		 * radioService.turnFmAm(1); } else if (radioService.getCurChannelId() >
		 * RadioService.RADIO_FM_COUNT && checkedId <
		 * RadioService.RADIO_FM_COUNT) { // pre am now fm
		 * radioService.turnFmAm(0); }
		 */
		radioService.setCurChannelId(checkedId);
		radioService.setFreq(radioService.getCurrentFreq());
		updateFreqView();
	}

	@Override
	// 閿熸枻鎷稲adioService閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		if (DEBUG)
			Log.v(TAG, "RadioService is connected");
		radioService = ((RadioService.ServiceBinder) service).getService();
		radioService.registStatusListener(mRadioStatusListener);
		if (radioService.getFunctionId() == 0) {
			mMiddleButtonBackward
					.setBackgroundResource(R.drawable.btnfinebackward);
			mMiddleButtonForward
					.setBackgroundResource(R.drawable.btnfineforward);
		} else if (radioService.getFunctionId() == 1) {
			mMiddleButtonBackward
					.setBackgroundResource(R.drawable.btnstepbackward);
			mMiddleButtonForward
					.setBackgroundResource(R.drawable.btnstepforward);
		}

//		radioService.setFreq(radioService.getCurrentFreq());
//		radioService.setVolume(radioService.getVolume());

		if (DEBUG)
			Log.v(TAG, "####setVolume#### " + radioService.getVolume());
//		radioSetSelect(radioService.getRadioType());
        updateFreqView();
		if (RadioService.RADIO_FM1 == radioService.getRadioType()) {
			((TextView) findViewById(R.id.radiotype)).setText("FM"); /* 閿熸枻鎷稦M1鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText("MHz");
			mButtonButtonFm1.setSelected(true);
			//mButtonButtonFm2.setSelected(false);
			mButtonButtonAm.setSelected(false);
		} else if (RadioService.RADIO_FM2 == radioService.getRadioType()) {
			((TextView) findViewById(R.id.radiotype)).setText("FM"); /* 閿熸枻鎷稦M2鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText("MHz");
			mButtonButtonFm1.setSelected(false);
			//mButtonButtonFm2.setSelected(true);
			mButtonButtonAm.setSelected(false);
		} else if (RadioService.RADIO_AM == radioService.getRadioType()) {
			((TextView) findViewById(R.id.radiotype)).setText("AM"); /* 閿熸枻鎷稟M鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText("KHz");
			mButtonButtonFm1.setSelected(false);
			//mButtonButtonFm2.setSelected(false);
			mButtonButtonAm.setSelected(true);
		}
		updateChannelList();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		if(DEBUG) Log.v(TAG, "RadioService is disconnected");
		radioService = null;
	}

	// 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷锋ā寮忛敓鏂ゆ嫹FM1 FM2 AM
	private void radioSetSelect(int type) {
		radioService.setRadioType(type);
		if (RadioService.RADIO_FM1 == type) {
			((TextView) findViewById(R.id.radiotype)).setText("FM"); /* 閿熸枻鎷稦M1鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText("MHz");
			mButtonButtonFm1.setSelected(true);
			//mButtonButtonFm2.setSelected(false);
			mButtonButtonAm.setSelected(false);
			radioService.turnFmAm(0);
			

		} else if (RadioService.RADIO_FM2 == type) {
			((TextView) findViewById(R.id.radiotype)).setText("FM"); /* 閿熸枻鎷稦M2鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText("MHz");
			mButtonButtonFm1.setSelected(false);
			//mButtonButtonFm2.setSelected(true);
			mButtonButtonAm.setSelected(false);
			radioService.turnFmAm(0);
		} else if (RadioService.RADIO_AM == type) {
			((TextView) findViewById(R.id.radiotype)).setText("AM"); /* 閿熸枻鎷稟M鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText("KHz");
			mButtonButtonFm1.setSelected(false);
			//mButtonButtonFm2.setSelected(false);
			mButtonButtonAm.setSelected(true);
			radioService.turnFmAm(1);
		}
		updateChannelList();

	}

	@Override
	// 閿熸枻鎷烽敓閰电鎷烽敓鏂ゆ嫹閿熻緝顤庢嫹閿熺丹Handler閿熸枻鎷烽敓鏂ゆ嫹
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int viewId = v.getId();
		Message msg = mHandler.obtainMessage(viewId);
		if (msg != null) {
			mHandler.sendMessage(msg);
		}
	}

	void updateFreqView() {
		int length, temp;
		ImageView channelView;
		temp = radioService.getCurrentFreq();
		if (DEBUG)
			Log.v(TAG, "<myu>curfreq = " + radioService.getCurrentFreq());
		// if (radioService.getCurChannelId() < RadioService.RADIO_PAGE_COUNT) {
		// /*閿熷壙璁规嫹娉ㄩ敓閰电尨鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹handleMessage閿熷彨璁规嫹搴旈敓鏂ゆ嫹閿熸枻鎷锋伅閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷峰疄閿熸枻鎷�/
		// Log.d(TAG,
		// "<myu>getCurChannelId is " + radioService.getCurChannelId()
		// + " RADIO_PAGE_COUNT is "
		// + RadioService.RADIO_PAGE_COUNT);
		// ((TextView) findViewById(R.id.radiotype)).setText("FM1");
		// ((TextView) findViewById(R.id.radiohz)).setText("MHz");
		// } else if (radioService.getCurChannelId() <
		// RadioService.RADIO_FM_COUNT) {
		// ((TextView) findViewById(R.id.radiotype)).setText("FM2");
		// ((TextView) findViewById(R.id.radiohz)).setText("MHz");
		// } else {
		// ((TextView) findViewById(R.id.radiotype)).setText("AM");
		// ((TextView) findViewById(R.id.radiohz)).setText("KHz");
		// }
		if (radioService.getCurChannelId() >= 0) {
			if (DEBUG)
				Log.v(TAG,
						"<myu>CurChannelId is "
								+ radioService.getCurChannelId());
			((TextView) findViewById(R.id.channeldetails))
					.setText((radioService.getChannelItem(radioService
							.getCurChannelId())).name);
		}
		if (temp >= RadioService.FM_LOW_FREQ && temp < 10000) {
			length = 2;
			((ImageView) findViewById(R.id.number1)).setVisibility(View.GONE);
			((ImageView) findViewById(R.id.number4))
					.setVisibility(View.VISIBLE);
		} else if (temp >= 10000 && temp <= RadioService.FM_HIGH_FREQ) {
			length = 3;
			((ImageView) findViewById(R.id.number1))
					.setVisibility(View.VISIBLE);
			((ImageView) findViewById(R.id.number4))
					.setVisibility(View.VISIBLE);
		} else if (temp >= RadioService.AM_LOW_FREQ && temp < 1000) {
			length = 2;
			temp *= 10;
			((ImageView) findViewById(R.id.number1)).setVisibility(View.GONE);
			((ImageView) findViewById(R.id.number4)).setVisibility(View.GONE);
		} else if (temp > 1000 && temp <= RadioService.AM_HIGH_FREQ) {
			length = 3;
			temp *= 10;
			((ImageView) findViewById(R.id.number1))
					.setVisibility(View.VISIBLE);
			((ImageView) findViewById(R.id.number4)).setVisibility(View.GONE);
		} else {
			return;
		}
		for (int i = length; i >= 0; i--) {
			if (i == length) {
				channelView = (ImageView) findViewById(R.id.number5);
			} else if (i == length - 1) {
				channelView = (ImageView) findViewById(R.id.number3);
			} else if (i == length - 2) {
				channelView = (ImageView) findViewById(R.id.number2);
			} else if (i == length - 3) {
				channelView = (ImageView) findViewById(R.id.number1);
			} else {
				channelView = (ImageView) findViewById(R.id.number5);
			}
			temp = temp / 10;
			switch (temp % 10) {
			case 0:
				channelView.setImageResource(R.drawable.number0);
				break;
			case 1:
				channelView.setImageResource(R.drawable.number1);
				break;
			case 2:
				channelView.setImageResource(R.drawable.number2);
				break;
			case 3:
				channelView.setImageResource(R.drawable.number3);
				break;
			case 4:
				channelView.setImageResource(R.drawable.number4);
				break;
			case 5:
				channelView.setImageResource(R.drawable.number5);
				break;
			case 6:
				channelView.setImageResource(R.drawable.number6);
				break;
			case 7:
				channelView.setImageResource(R.drawable.number7);
				break;
			case 8:
				channelView.setImageResource(R.drawable.number8);
				break;
			case 9:
				channelView.setImageResource(R.drawable.number9);
				break;
			}
		}

	}

	// 閿熸枻鎷烽敓鏂ゆ嫹Channel閿熷彨鎲嬫嫹
	void updateChannelList() { /* 閿熷壙鐚存嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹 閿熸枻鎷烽敓鎴尅鎷锋寚閿熸枻鎷�搴旈敓鏂ゆ嫹閿熺獤鐚存嫹 */
		int channelId, offset = 0;
		int pressedId = -1;
		ChannelItem item;

		channelId = radioService.getCurChannelId();
		if (radioService.getRadioType() == RadioService.RADIO_FM1) {
			offset = 0;
			if (DEBUG)
				Log.d(TAG, "<myu>FM1 Mode");
			if (channelId >= 0 && channelId < RadioService.RADIO_PAGE_COUNT) {
				pressedId = channelId;
			}
		} else if (radioService.getRadioType() == RadioService.RADIO_FM2) {
			if (DEBUG)
				Log.d(TAG, "<myu>FM2 Mode");
			offset = RadioService.RADIO_PAGE_COUNT;
			if (channelId >= RadioService.RADIO_PAGE_COUNT
					&& channelId < RadioService.RADIO_FM_COUNT) {
				pressedId = channelId - RadioService.RADIO_PAGE_COUNT;
			}
		} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
			if (DEBUG)
				Log.d(TAG, "<myu>AM Mode");
			offset = RadioService.RADIO_FM_COUNT;
			if (channelId >= RadioService.RADIO_FM_COUNT
					&& channelId < RadioService.RADIO_CHANNEL_COUNT) {
				pressedId = channelId - RadioService.RADIO_FM_COUNT;
			}
		}
		for (int i = 0; i < RadioService.RADIO_PAGE_COUNT; i++) { 
			item = radioService.getChannelItem(i + offset);
			if (item.abridge != null && !item.abridge.equals("")) {
				channelBtn[i].setText(item.abridge);
			} else if (item != null) {
				channelBtn[i].setText(item.freq);
			}
		}
		// view the select mode
		for (int i = 0; i < RadioService.RADIO_PAGE_COUNT; i++) {
			if (pressedId == i && pressedId < RadioService.RADIO_PAGE_COUNT) {
				// selected鐘舵�閿熸枻鎷烽敓鏂ゆ嫹閿熸彮浼欐嫹閰堕敓闃跺埡顒婃嫹閿熺祶iew閿熸枻鎷烽敓鑺傞潻鎷烽敓鏂ゆ嫹鐘舵�
				if(flag == 1){
					if(i>=0 && i < RadioService.RADIO_FM_COUNT){
						radioService.curChannelId = 0;
						channelBtn[i].setSelected(false);
					}else if (i >= RadioService.RADIO_FM_COUNT && i < RadioService.RADIO_PAGE_COUNT) {
						channelBtn[i].setSelected(false);
					}
					
				}else {
					channelBtn[pressedId].setSelected(true);
				}

				if (DEBUG)
					Log.d(TAG, "<myu>pressedId is " + pressedId + " int i = "
							+ i);
			} else if (channelBtn[i].isSelected()) {
				channelBtn[i].setSelected(false);
			}
		}
		
		flag = 0 ;
	}

	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		if (DEBUG)
			Log.d(TAG, "onCreateDialog is open");
		switch (id) {
		case DIALOG_SCAN: /* 閿熺殕璁规嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹鍊兼厱閿熸枻鎷烽敓锟�/
			mSearchDialog = new ProgressDialog(this);

			mSearchDialog.setTitle(getResources().getString(R.string.app_name));
			mSearchDialog.setMessage(getResources().getString(
					R.string.searchwait));
			mSearchDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mSearchDialog.setIndeterminate(true);
			mSearchDialog.setCancelable(true);

			mSearchDialog
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						public void onCancel(DialogInterface dialog) {
							if (DEBUG)
								Log.d(TAG, "Progress Dialog --- onCancel");
						}
					});
			return mSearchDialog;
		case DIALOG_VOLUME:
			LayoutInflater inflater = LayoutInflater.from(this);
			View settingView = inflater.inflate(R.layout.radio_setting_layout,
					null);
			return new AlertDialog.Builder(this).create();
			/*
			mVolumeSeekBar = new SeekBar(this);
			mVolumeSeekBar.setPadding(15, 5, 15, 10);
			mVolumeSeekBar.setMax(100);
			mVolumeSeekBar.setProgress(radioService.getVolume());
			mVolumeSeekBar.setOnSeekBarChangeListener(RadioActivity.this);
			mVolCtrlDialog = new AlertDialog.Builder(RadioActivity.this)
					.setTitle(getResources().getString(R.string.radio_volume))
					.setIcon(R.drawable.sound).setView(mVolumeSeekBar)
					.setCancelable(true).create();
			return mVolCtrlDialog;
			*/
		case DIALOG_EDIT: /* 閿熷彨杈炬嫹閿熸枻鎷�537 539 541 閿熸枻鎷烽敓鎴尅鎷锋寚閿熸枻鎷�閿熺獤鐚存嫹!! */

			if (DEBUG)
				Log.d(TAG, "onCreateDialog is in DIALOG_EDIT");
			if (editChannelId < 0
					|| editChannelId >= RadioService.RADIO_CHANNEL_COUNT) {
				if (DEBUG)
					Log.d(TAG, "<myu>DIALOG_EDIT editChannelId is "
							+ editChannelId + " RADIO_CHANNEL_COUNT is "
							+ " RadioService.RADIO_CHANNEL_COUNT");
				return null;
			}
			LayoutInflater factory = LayoutInflater.from(this);
			View channelEditView = factory.inflate(R.layout.radio_dialog_edit,
					null);

			final ChannelItem item;
			final EditText freqEdit,
			shortNameEdit,
			fullNameEdit;
			freqEdit = (EditText) channelEditView.findViewById(R.id.freq_edit);
			shortNameEdit = (EditText) channelEditView
					.findViewById(R.id.short_name_edit);
			fullNameEdit = (EditText) channelEditView
					.findViewById(R.id.full_name_edit);
			if (DEBUG)
				Log.d(TAG, "<myu>DIALOG_EDIT: editChannelId is "
						+ editChannelId);
			item = radioService.getChannelItem(editChannelId);
			if (DEBUG)
				Log.d(TAG, "********** getChannelItem "
						+ editChannelId);
			freqEdit.setText(item.freq);
			freqEdit.setSelection(freqEdit.length());
			shortNameEdit.setText(item.abridge);
			shortNameEdit.setSelection(shortNameEdit.length());
			fullNameEdit.setText(item.name);
			fullNameEdit.setSelection(fullNameEdit.length());
			if (DEBUG)
				Log.v(TAG, "freqEdit is " + item.freq + " shortNameEdit is "
						+ item.abridge + " fullNameEdit is " + item.name);

			new AlertDialog.Builder(this)
					.setTitle(R.string.edit_channel)
					.setView(channelEditView)
					.setCancelable(false)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									int freq;
									String freqStr, shortNameStr, fullNameStr;
									freqStr = freqEdit.getText().toString();
									shortNameStr = shortNameEdit.getText()
											.toString();
									fullNameStr = fullNameEdit.getText()
											.toString();
									if (radioService.getRadioType() != RadioService.RADIO_AM) {
										if (freqStr.equals("")
												|| freqStr.length() < 4
												|| freqStr.length() > 5) {
											if (DEBUG)
												Log.v(TAG,
														" <myu>freqStr.length is "
																+ freqStr
																		.length());
											showDialog(DIALOG_INPUTFREQ_FM);
											mHandler.sendEmptyMessageDelayed(
													DISMISS_INPUT_DIALOG,
													VOLUME_DELAY_TIME);
											removeDialog(DIALOG_EDIT);
											return;
										}
										if (freqStr.charAt(freqStr.length() - 2) != '.') {
											showDialog(DIALOG_INPUTFREQ_FM);
											mHandler.sendEmptyMessageDelayed(
													DISMISS_INPUT_DIALOG,
													VOLUME_DELAY_TIME);
											removeDialog(DIALOG_EDIT);
											return;
										}
										freq = Integer.parseInt(freqStr
												.replaceAll("\\.", "")) * 10;
										Log.d(TAG,
												"%%%%%%%%%%%%%%%%%%%%%%%%%freq =  "
														+ freq);
										if (freq < RadioService.FM_LOW_FREQ
												|| freq > RadioService.FM_HIGH_FREQ) {
											showDialog(DIALOG_INPUTFREQ_FM);
											mHandler.sendEmptyMessageDelayed(
													DISMISS_INPUT_DIALOG,
													VOLUME_DELAY_TIME);
											removeDialog(DIALOG_EDIT);
											return;
										}
									} else {
										if (freqStr.equals("")
												|| freqStr.contains(".")) {
											showDialog(DIALOG_INPUTFREQ_AM);
											mHandler.sendEmptyMessageDelayed(
													DISMISS_INPUT_DIALOG,
													VOLUME_DELAY_TIME);
											removeDialog(DIALOG_EDIT);
											return;
										}
										freq = Integer.parseInt(freqStr);
										if (freq < RadioService.AM_LOW_FREQ
												|| freq > RadioService.AM_HIGH_FREQ) {
											showDialog(DIALOG_INPUTFREQ_AM);
											mHandler.sendEmptyMessageDelayed(
													DISMISS_INPUT_DIALOG,
													VOLUME_DELAY_TIME);
											removeDialog(DIALOG_EDIT);
											return;
										}
									}
									item.freq = freqStr;
									item.abridge = shortNameStr;
									item.name = fullNameStr;
									radioService.setChannelItem(editChannelId,
											item);
									if (DEBUG)
										Log.v(TAG,
												"*****************setChannelItem = "
														+ editChannelId);
									mHandler.sendEmptyMessage(UPDATE_CHANNEL_LIST);
									if (editChannelId != -1
											&& editChannelId == radioService
													.getCurChannelId()) {
										radioService
												.setCurChannelId(editChannelId);
										radioService.setCurrentFreq(freq);
										radioService.setFreq(radioService
												.getCurrentFreq());
										updateFreqView();
									}
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									removeDialog(DIALOG_EDIT);
								}
							})
					.setNeutralButton(R.string.clear,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									freqEdit.setText("");
									freqEdit.setSelection(freqEdit.length());
									shortNameEdit.setText("");
									shortNameEdit.setSelection(shortNameEdit
											.length());
									fullNameEdit.setText("");
									fullNameEdit.setSelection(fullNameEdit
											.length());

									String freqStr, shortNameStr, fullNameStr;
									freqStr = freqEdit.getText().toString();
									shortNameStr = shortNameEdit.getText()
											.toString();
									fullNameStr = fullNameEdit.getText()
											.toString();

									item.freq = freqStr;
									item.abridge = shortNameStr;
									item.name = fullNameStr;
									if (DEBUG)
										Log.v(TAG, "freqEdit is " + item.freq
												+ " shortNameEdit is "
												+ item.abridge
												+ " fullNameEdit is "
												+ item.name);

									radioService.setChannelItem(editChannelId,
											item);
									if (DEBUG)
										Log.v(TAG,
												"*****************setChannelItem = "
														+ editChannelId);
									mHandler.sendEmptyMessage(UPDATE_CHANNEL_LIST);
									updateFreqView();
								}
							}).create().show();
			 break;
		case DIALOG_CONFIRM: /* 閿熺殕璁规嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹 */
			/*
			 * String viewStr; if (RadioService.RADIO_AM ==
			 * radioService.getRadioType()) { viewStr =
			 * getResources().getString(R.string.amsearch); } else { viewStr =
			 * getResources().getString(R.string.fmsearch); } return new
			 * AlertDialog
			 * .Builder(this).setMessage(viewStr).setCancelable(false)
			 * .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			 * public void onClick(DialogInterface dialog, int id) {
			 * showDialog(DIALOG_SCAN); radioService.onAutoSearch();
			 * removeDialog(DIALOG_CONFIRM); } }).setNegativeButton("No", new
			 * DialogInterface.OnClickListener() { public void
			 * onClick(DialogInterface dialog, int id) {
			 * removeDialog(DIALOG_CONFIRM); } }).create();
			 */
			radioService.onAutoSearch();
			break;
		case DIALOG_INPUTFREQ_FM: // 閿熸枻鎷烽敓鏂ゆ嫹閿熺殕浼欐嫹閿熸枻鎷�閿熸枻鎷烽敓鏂ゆ嫹涓篟.string.valid_freq_fm
			mInputDialog = new AlertDialog.Builder(this).setMessage(
					getResources().getString(R.string.valid_freq_fm)).create();
			return mInputDialog;
		case DIALOG_INPUTFREQ_AM:
			mInputDialog = new AlertDialog.Builder(this).setMessage(
					getResources().getString(R.string.valid_freq_am)).create();
			return mInputDialog;

		}

		return null;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		radioService.setVolume(progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {  
            moveTaskToBack(false);  
            return true;  
        }  
		return super.onKeyDown(keyCode, event);
	}
	
	public void nextSong() {
		int checkedId = 0;
		ChannelItem item;
		int curChannel = radioService.getCurChannelId();

		if (radioService.getRadioType() == RadioService.RADIO_FM1) {
			checkedId = curChannel;
			if (curChannel + 1 >= 48)
				curChannel = 46;
		} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
			checkedId = curChannel - RadioService.RADIO_FM_COUNT;
			if (checkedId < 0)
				checkedId = -1;

		}

		item = radioService.getChannelItem(curChannel + 1);
		if (item.freq.equals("")) {
			setChannelChecked(0);
		} else {
			setChannelChecked(checkedId + 1);
		}
	}
	
	public void lastSong() {
		int checkedId = 0;
		ChannelItem item;
		int finachannel = 0;
		int curChannel = radioService.getCurChannelId();

		if (radioService.getRadioType() == RadioService.RADIO_FM1) {
			checkedId = curChannel;
			if (checkedId - 1 < 0) {
				for (int i = 0; i < 48; i++) {
					if (radioService.getChannelItem(i).freq.equals("")) {
						finachannel = i;
						break;
					} else {
						continue;
					}
				}
				checkedId = finachannel;
				curChannel = finachannel;
			}
			if (curChannel + 1 >= 48)
				curChannel = 46;
		} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
			checkedId = curChannel - RadioService.RADIO_FM_COUNT;
			if (checkedId - 1 < 0) {
				for (int i = 48; i < 96; i++) {
					if (radioService.getChannelItem(i).freq.equals("")) {
						finachannel = i;
						break;
					} else {
						continue;
					}
				}
				checkedId = finachannel - RadioService.RADIO_FM_COUNT;
				curChannel = finachannel;
			}
			// if(checkedId < 0 )checkedId = -1;
		}

		item = radioService.getChannelItem(curChannel - 1);

		if (item.freq.equals("")) {
			setChannelChecked(0);
		} else {
			setChannelChecked(checkedId - 1);
		}
	}
	
	private IntentFilter getIntentFilter(){		
		IntentFilter myIntentFilter = new IntentFilter("Auto-Search");
		myIntentFilter.addAction("Radio.Media_Broadcast_Next");
		myIntentFilter.addAction("Radio.Media_Broadcast_Last");
		myIntentFilter.addAction("android.intent.action.BONOVO_SWITCH_FMAM");
		myIntentFilter.addAction("Radio_Auto_Complete");
		return myIntentFilter;
		
	};
	
	private BroadcastReceiver myBroadcastReveiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			/************ 娣囨繂鐡ㄩ懛顏勫З閹兼粎鍌ㄩ幖婊冨煂閻ㄥ嫬褰� *************/
			if (intent.getAction().equals("Auto-Search")) {
				int iCurfreq = intent.getIntExtra("auto-curfreq", 0);
				int mIdex = intent.getIntExtra("idex", 0);
				flag = intent.getIntExtra("flag", 0);
				ChannelItem item;
				Log.v(TAG, "**************iCurfreq = " + iCurfreq
						+ " *********mIdex = " + mIdex);
				if (radioService.getRadioType() == RadioService.RADIO_FM1
						|| radioService.getRadioType() == RadioService.RADIO_FM2) {
					float ccttyy = iCurfreq / 100f;
					if (mIdex >= CHANNEL_SIZE)
						mIdex = CHANNEL_SIZE - 1;
					item = radioService.getChannelItem(mIdex);
					item.freq = Float.toString(ccttyy);
					item.abridge = "";
					item.name = "";
					radioService.setChannelItem(mIdex, item);
					mHandler.sendEmptyMessage(UPDATE_CHANNEL_LIST);
					updateFreqView();
				} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
					int nIdex;
					nIdex = mIdex + CHANNEL_SIZE;
					if (nIdex >= CHANNEL_SIZE * 2)
						nIdex = CHANNEL_SIZE * 2 - 1;
					item = radioService.getChannelItem(nIdex);
					item.freq = Integer.toString(iCurfreq);
					item.abridge = "";
					item.name = "";
					radioService.setChannelItem(nIdex, item);
					mHandler.sendEmptyMessage(UPDATE_CHANNEL_LIST);
					updateFreqView();
				}
			}else if (intent.getAction().equals("Radio_Auto_Complete")) {
				//when auto search is complete,set Channal0 with the first freq
				setChannelChecked(0);
			} else if (intent.getAction().equals("Radio.Media_Broadcast_Next")) {
				nextSong();
			} else if (intent.getAction().equals("Radio.Media_Broadcast_Last")) {
				lastSong();
			} else if (intent.getAction().equals(
					"android.intent.action.BONOVO_SWITCH_FMAM")) {
				Log.v(TAG, "BONOVO_SWITCH_FMAM");
				if (radioService.getRadioType() == radioService.RADIO_FM1
						|| radioService.getRadioType() == radioService.RADIO_FM2) {
					if (!radioService.mIsSearchThreadRunning) {
						updateFreqView();
						radioSetSelect(RadioService.RADIO_AM);
					} else {
						Toast.makeText(getApplicationContext(),
								R.string.searching, Toast.LENGTH_SHORT).show();
					}
				} else {
					if (!radioService.mIsSearchThreadRunning) {
						updateFreqView();
						radioSetSelect(RadioService.RADIO_FM1);
					} else {
						Toast.makeText(getApplicationContext(),
								R.string.searching, Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	};
}
