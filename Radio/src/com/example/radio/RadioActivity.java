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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
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
	
	private static int FOCUS_BUTTON_ID = 0;
	private static int HEART_STATIC_FLAG = 0;

	private static boolean mDown = false;			//keyEvent flag

	private AlertDialog mVolCtrlDialog;
	private AlertDialog mInputDialog;
	private ProgressDialog mSearchDialog;
	private SeekBar mVolumeSeekBar;
	private AlertDialog warnDialog;
	private AlertDialog di;

	private static final boolean DEBUG = true;
	private static int  flag = 0;						//閼奉亜濮╅幖婊冨酱閺嶅洩顔�
	private static int IS_AUTO_NOW = 1;
	private static int IS_STEP_NOW = 2;
	private final static String TAG = "RadioActivity";
	private RadioService radioService = null;

	private ImageButton mButtonForward;
	private ImageButton mButtonBackward;
	private ImageButton mButtonClose;
	
	private CheckBox cBox;
	private View checkbox;

	private Button mButtonFine;
	private Button mButtonStep;
	private Button mButtonAuto;
	private Button mButtonImport;
	private Button mButtonVolume;
	private Button mButtonFm1;
	private Button mButtonAm;
	private Button mButtonSave;
	private Button mButtonHeart;
	private Button mButtonSetting;
	private Button mButtonCollect;
	private Button mButtonClear;
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
			case R.id.btnheart:
				add_or_clear_Collect();
				gone_Empty_ButtonView();
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
				mButtonBackward
						.setBackgroundResource(R.drawable.btnfinebackward);
				mButtonForward
						.setBackgroundResource(R.drawable.btnfineforward);
				break;
			case R.id.btnstep:
				if (DEBUG)
					Log.d(TAG, "btnstep has worked");
				radioService.setFunctionId(1);
				mButtonBackward
						.setBackgroundResource(R.drawable.btnstepbackward);
				mButtonForward
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
			case R.id.btnsave:
				String curfreq = null;
				int OUTSCOPE = 0, OUTSCOPE_FLAG = 1;
				int START = 0, END = 0, END_EMPTY = 0;
				if(radioService.getRadioType() == RadioService.RADIO_FM1) {
					curfreq = Double.toString(((double)radioService.getCurrentFreq() / 100.0));
					Log.v("RadioActivity", "heart curfreq = " + curfreq + " CurrentFreq = " + radioService.getCurrentFreq());
					if((radioService.getCurrentFreq() < RadioService.FM_LOW_FREQ) || (radioService.getCurrentFreq() > RadioService.FM_HIGH_FREQ)) {
						OUTSCOPE = OUTSCOPE_FLAG;
					}
					START = 0;
					END = 0x30;
					END_EMPTY = 0x2f;
				} else if(radioService.getRadioType() == RadioService.RADIO_AM) {
					curfreq = Integer.toString(radioService.getCurrentFreq());
					Log.v("RadioActivity", "heart curfreq = " + curfreq + " CurrentFreq = " + radioService.getCurrentFreq());
					if((radioService.getCurrentFreq() < RadioService.AM_LOW_FREQ) || (radioService.getCurrentFreq() > RadioService.AM_HIGH_FREQ)) {
						OUTSCOPE = OUTSCOPE_FLAG;
					}
					START = 0x30;
					END = 0x60;
					END_EMPTY = 0x5f;
				} else if(radioService.getRadioType() == RadioService.RADIO_COLLECT) {
					if((radioService.getCurrentFreq() < RadioService.FM_LOW_FREQ) || (radioService.getCurrentFreq() > RadioService.FM_HIGH_FREQ)) {
						curfreq = Integer.toString(radioService.getCurrentFreq());
					} else if((radioService.getCurrentFreq() < RadioService.AM_LOW_FREQ) || (radioService.getCurrentFreq() > RadioService.AM_HIGH_FREQ)) {
						curfreq = Double.toString(((double)radioService.getCurrentFreq() / 100.0));
					}
					START = 0x60;
					END = 0x90;
					END_EMPTY = 0x8f;
				}
				for(int i = START; i < END; i++) {
					Log.v("RadioActivity", "(" + i + ")= " + radioService.getChannelItem(i).freq);
					if(OUTSCOPE == OUTSCOPE_FLAG) {
						Toast.makeText(getApplicationContext(), R.string.invalid, 0).show();
						break;
					} else if(radioService.getChannelItem(i).freq.equals("")) {
						int empty = i;
						Log.v("RadioActivity", "empty = " + empty);
						if(empty == END_EMPTY) {
							Toast.makeText(getApplicationContext(), R.string.nowhere, 1).show();
						}
						RadioService.ChannelItem item = radioService.getChannelItem(empty);
						item.freq = curfreq;
						item.name = curfreq;
						radioService.setChannelItem(empty, item);
						updateChannelList();
						updateFreqView();
                        i = END;
					} else if(radioService.getChannelItem(i).freq.equals(curfreq)) {
						int sameFreq = i;
						Toast.makeText(getApplicationContext(), R.string.samefreq, 0).show();
						Log.v("RadioActivity", "sameFreq = " + sameFreq);
                        i=END;
					}
				}
				gone_Empty_ButtonView();
				break;
			case R.id.btnclear:
				new AlertDialog.Builder(RadioActivity.this)
                     .setTitle(R.string.remind)
			         .setCancelable(true)
					 .setMessage(R.string.clear_single_channel)
					 .setPositiveButton(R.string.ok,
					     new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								Log.v("RadioActivity", "1111111111");
								clear_Single_Content();
								Log.v("RadioActivity", "2222222222");
								gone_Empty_ButtonView();
								Log.v("RadioActivity", "3333333333");
								updateChannelList();
								updateFreqView();
							}
						})
					 .setNegativeButton(R.string.cancel,
						 new android.content.DialogInterface.OnClickListener() {
						
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
					.create().show();
				break;
			case R.id.btnfm1:
				if (DEBUG)
					Log.d(TAG, "btnfm1 has worked");
				if(!radioService.mIsSearchThreadRunning){
					updateFreqView();
					radioSetSelect(RadioService.RADIO_FM1);
					gone_Empty_ButtonView();
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
					gone_Empty_ButtonView();
				}else {
					Toast.makeText(getApplicationContext(), R.string.searching, Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.collect:
				if (DEBUG) Log.d(TAG, "btncollect has worked");
				if(!radioService.mIsSearchThreadRunning){
					updateFreqView();
					radioSetSelect(RadioService.RADIO_COLLECT);
					gone_Empty_ButtonView();
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
					Log.d(TAG, "btnsetting has worked");
				Intent setting = new Intent("com.example.radio.IntentActivity");
				RadioActivity.this.startActivity(setting);
				break;
			case R.id.btnforward:
				if (radioService.getFunctionId() == 0) {
					radioService.fineRight(radioService.getCurrentFreq());
					curFreq_Compare_To_Collect(radioService.getCurrentFreq());
				} else {
					radioService.stepRight(radioService.getCurrentFreq());
					radioService.STEP_OR_AUTO = IS_STEP_NOW;
				}
				break;
			case R.id.btnbackward:
				if (radioService.getFunctionId() == 0) {
					radioService.fineLeft(radioService.getCurrentFreq());
					curFreq_Compare_To_Collect(radioService.getCurrentFreq());
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
				setChannelChecked(what - R.id.channel0);
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
			} else if (radioService.getRadioType() == RadioService.RADIO_COLLECT) {
				editChannelId = viewId - R.id.channel0 + RadioService.RADIO_COLLECT_START;
				if (DEBUG)
					Log.d(TAG, "onLongClick editChannelId = "+ editChannelId);
			} else {
				editChannelId = viewId - R.id.channel0 + RadioService.RADIO_AM_START;
				if (DEBUG)
					Log.d(TAG, "onLongClick editChannelId = "+ editChannelId);
			}
			showDialog(DIALOG_EDIT);
			break;
		case R.id.btnclear:
			LayoutInflater layoutInflater = LayoutInflater.from(this);
			checkbox = layoutInflater.inflate(R.layout.checkbox, null);
			cBox = (CheckBox)checkbox.findViewById(R.id.check);
			SharedPreferences pre = getSharedPreferences("checkvalue", 0);
			String value = pre.getString("ischeck", "");
			if(value.endsWith("1")) {
                createDialog().dismiss();
				radioService.clearAllContent();
				gone_Empty_ButtonView();
				updateChannelList();
				updateFreqView();
			} else {
                createDialog().show();
			}
			break;
		case R.id.btnfm1:
		case R.id.btnam:
		case R.id.btnsetting:
			return true;
		default:
			break;
		}
		return true;
	}

	private void setupview() {
		mButtonBackward = (ImageButton) findViewById(R.id.btnbackward);
		mButtonBackward.setOnClickListener(this);
		mButtonForward = (ImageButton) findViewById(R.id.btnforward);
		mButtonForward.setOnClickListener(this);
		mButtonClose = (ImageButton) findViewById(R.id.btnclose);
		mButtonClose.setOnClickListener(this);

		mButtonFm1 = (Button) findViewById(R.id.btnfm1);
		mButtonFm1.setOnClickListener(this);
		mButtonAm = (Button) findViewById(R.id.btnam);
		mButtonAm.setOnClickListener(this);
		mButtonSetting = (Button)findViewById(R.id.btnsetting);
		mButtonSetting.setOnClickListener(this);
		mButtonFine = (Button) findViewById(R.id.btnfine);
		mButtonFine.setOnClickListener(this);
		mButtonStep = (Button) findViewById(R.id.btnstep);
		mButtonStep.setOnClickListener(this);
		mButtonAuto = (Button) findViewById(R.id.btnauto);
		mButtonAuto.setOnClickListener(this);
		mButtonCollect = (Button)findViewById(R.id.collect);
		mButtonCollect.setOnClickListener(this);
		mButtonHeart = (Button)findViewById(R.id.btnsave);
		mButtonHeart.setOnClickListener(this);
		mButtonClear = (Button)findViewById(R.id.btnclear);
		mButtonClear.setOnClickListener(this);
		mButtonClear.setOnLongClickListener(this);
		mButtonHeart = (Button)findViewById(R.id.btnheart);
		mButtonHeart.setOnClickListener(this);

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
	
	public void clear_Single_Content() {
		if((radioService.getRadioType() == RadioService.RADIO_FM1)) {
			RadioService.ChannelItem item = new RadioService.ChannelItem();
			item.freq = "";
			item.name = "";
			item.abridge = "";
			if(channelBtn[FOCUS_BUTTON_ID].isSelected()) {
				Log.v("RadioActivity", "4444444444");
				radioService.setChannelItem(FOCUS_BUTTON_ID, item);
			}
		} else if(radioService.getRadioType() == RadioService.RADIO_AM) {
			RadioService.ChannelItem item = new RadioService.ChannelItem();
			item.freq = "";
			item.name = "";
			item.abridge = "";
			if(channelBtn[FOCUS_BUTTON_ID].isSelected()) {
				radioService.setChannelItem((FOCUS_BUTTON_ID + RadioService.RADIO_AM_START), item);
			}
		} else if(radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			RadioService.ChannelItem item = new RadioService.ChannelItem();
			item.freq = "";
			item.name = "";
			item.abridge = "";
			if(channelBtn[FOCUS_BUTTON_ID].isSelected()) {
				radioService.setChannelItem((FOCUS_BUTTON_ID + RadioService.RADIO_COLLECT_START), item);
			}
		}
		HEART_STATIC_FLAG = 0;
		mButtonHeart.setBackground(getResources().getDrawable(R.drawable.btncollect_heart));
	}
	
	private void gone_Empty_ButtonView() {
		for(int j = 0; j < CHANNEL_SIZE; j++) {
			channelBtn[j].setVisibility(0);
		}
		
		updateChannelList();
		updateFreqView();
		
		if(radioService.getRadioType() == RadioService.RADIO_FM1) {
			int i = CHANNEL_SIZE;
			int gone_ID = 0;
			while(--i > 0) {
				RadioService.ChannelItem item = radioService.getChannelItem(i);
				if(!item.freq.equals("")) {
					break;
				}
			}
			if(++i >= 12) {
				gone_ID = i;
			} else {
				gone_ID = 12;
			}
			for(; gone_ID < CHANNEL_SIZE; gone_ID++) {
				channelBtn[gone_ID].setVisibility(0x8);
			}
		}
		else if(radioService.getRadioType() == RadioService.RADIO_AM) {
			int i = 0x5f;
			int gone_ID = 0x0;
			for(; i > 0x30; i = i - 0x1) {
                RadioService.ChannelItem item = radioService.getChannelItem(i);
				if(!item.freq.equals("")) {
					break;
				}
			}
			if((i - 0x2f) >= 0xc) {
				gone_ID = i - 0x2f;
			} else {
				gone_ID = 0xc;
			}
			for(; gone_ID < 0x30; gone_ID = gone_ID + 0x1) {
				channelBtn[gone_ID].setVisibility(0x8);
			}
		}
		else if(radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			int i = 0x8f;
			int gone_ID = 0x0;
			for(; i > 0x60; i = i - 0x1) {
                RadioService.ChannelItem item = radioService.getChannelItem(i);
				if(!item.freq.equals("")) {
					break;
				}
			}
			if((i - 0x60) >= 0xc) {
				gone_ID = i - 0x60;
			} else {
				gone_ID = 0xc;
			}
			for(; gone_ID < 0x30; gone_ID = gone_ID + 0x1) {
				channelBtn[gone_ID].setVisibility(0x8);
			}
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

	public void setChannelChecked(int btnId) {
		int checkedId = 0;
		ChannelItem item;
		if (DEBUG)
			Log.v(TAG, "setChannelChecked is into here,btnId = " + btnId);
		if (radioService.getRadioType() == RadioService.RADIO_FM1) {
			checkedId = btnId;
		} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
			checkedId = btnId + RadioService.RADIO_AM_START;
		} else if (radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			checkedId = btnId + RadioService.RADIO_COLLECT_START;
		}
		item = radioService.getChannelItem(checkedId);
		if (item.freq.equals("")) {
			if (DEBUG)
				Log.v("RadioActivity", "item.freq is empty!!!");
			return;
		}

		if(radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			if(item.freq.contains(".")) {
				radioService.turnFmAm(0);
				radioService.setCurrentFreq((Integer.parseInt(item.freq.replaceAll("\\.", "")) * 10));
			} else {
				radioService.turnFmAm(1);
				radioService.setCurrentFreq(Integer.parseInt(item.freq.replaceAll("\\.", "")));
			}
		} else if(radioService.getRadioType() == RadioService.RADIO_FM1) {
			if(item.freq.contains(".")) {
				radioService.turnFmAm(0);
				radioService.setCurrentFreq((Integer.parseInt(item.freq.replaceAll("\\.", "")) * 10));
			} else {
				radioService.turnFmAm(1);
				radioService.setCurrentFreq(Integer.parseInt(item.freq.replaceAll("\\.", "")));
			}
		} else if(radioService.getRadioType() == RadioService.RADIO_AM) {
			if(item.freq.contains(".")) {
				radioService.turnFmAm(0);
				radioService.setCurrentFreq((Integer.parseInt(item.freq.replaceAll("\\.", "")) * 10));
			} else {
				radioService.turnFmAm(1);
				radioService.setCurrentFreq(Integer.parseInt(item.freq.replaceAll("\\.", "")));
			}
		}

		for (int i = 0; i < RadioService.RADIO_PAGE_COUNT; i++) {
			if (channelBtn[i].isSelected()) {
				channelBtn[i].setSelected(false);
			}
			mButtonHeart.setBackground(getResources().getDrawable(R.drawable.btncollect_heart));
			HEART_STATIC_FLAG = 0;
			for(int start = 0x60; start < 0x90; start++) {
				if(radioService.getChannelItem(start).freq.equals(item.freq)) {
					mButtonHeart.setBackground(getResources().getDrawable(R.drawable.collect_d));
					HEART_STATIC_FLAG = 1;
				}
			}
		}
		channelBtn[btnId].setSelected(true);
		FOCUS_BUTTON_ID = btnId;
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
		curFreq_Compare_To_Collect(radioService.getCurrentFreq());
		gone_Empty_ButtonView();
		if (radioService.getFunctionId() == 0) {
			mButtonBackward
					.setBackgroundResource(R.drawable.btnfinebackward);
			mButtonForward
					.setBackgroundResource(R.drawable.btnfineforward);
		} else if (radioService.getFunctionId() == 1) {
			mButtonBackward
					.setBackgroundResource(R.drawable.btnstepbackward);
			mButtonForward
					.setBackgroundResource(R.drawable.btnstepforward);
		}

		if (DEBUG)
			Log.v(TAG, "####setVolume#### " + radioService.getVolume());
		updateFreqView();
		if (RadioService.RADIO_FM1 == radioService.getRadioType()) {
			((TextView) findViewById(R.id.radiotype)).setText("FM"); /* 閿熸枻鎷稦M1鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText("MHz");
			mButtonFm1.setSelected(true);
			mButtonAm.setSelected(false);
			mButtonCollect.setSelected(false);
		} else if (RadioService.RADIO_COLLECT == radioService.getRadioType()) {
			((TextView)findViewById(R.id.radiotype)).setText("FAV");
			((TextView)findViewById(R.id.radiohz)).setText(" ");
			mButtonFm1.setSelected(false);
			mButtonAm.setSelected(false);
			mButtonCollect.setSelected(true);
		} else if (RadioService.RADIO_AM == radioService.getRadioType()) {
			((TextView) findViewById(R.id.radiotype)).setText("AM"); /* 閿熸枻鎷稟M鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText("KHz");
			mButtonFm1.setSelected(false);
			mButtonAm.setSelected(true);
			mButtonCollect.setSelected(false);
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
			mButtonFm1.setSelected(true);
			mButtonAm.setSelected(false);
			mButtonCollect.setSelected(false);
			if((radioService.getCurChannelId() < 0x60) || (radioService.getCurChannelId() >= 0x90)) {
				radioService.turnFmAm(0);
			}
		} else if (RadioService.RADIO_COLLECT == type) {
			((TextView) findViewById(R.id.radiotype)).setText("FAV");
			((TextView) findViewById(R.id.radiohz)).setText(" ");
			mButtonFm1.setSelected(false);
			mButtonAm.setSelected(false);
			mButtonCollect.setSelected(true);
		} else if (RadioService.RADIO_AM == type) {
			((TextView) findViewById(R.id.radiotype)).setText("AM"); /* 閿熸枻鎷稟M鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText("KHz");
			mButtonFm1.setSelected(false);
			mButtonAm.setSelected(true);
			mButtonCollect.setSelected(false);
			if((radioService.getCurChannelId() < 0x60) || (radioService.getCurChannelId() >= 0x90)) {
				radioService.turnFmAm(1);
			}
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
		if (radioService.getCurChannelId() >= 0) {
			if (DEBUG)
				Log.v(TAG,"<myu>CurChannelId is " + radioService.getCurChannelId());
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
		if (DEBUG) Log.d("RadioActivity", "channelId = " + channelId);
		if (radioService.getRadioType() == RadioService.RADIO_FM1) {
			offset = 0;
			if (DEBUG)
				Log.d(TAG, "<myu>FM1 Mode");
			if (channelId >= 0 && channelId < RadioService.RADIO_PAGE_COUNT) {
				pressedId = channelId;
			}
		} else if (radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			if (DEBUG)
				Log.d(TAG, "<myu>COLLECT Mode");
			offset = RadioService.RADIO_COLLECT_START;
			if (channelId >= RadioService.RADIO_COLLECT_START
					&& channelId < RadioService.RADIO_CHANNEL_COUNT) {
				pressedId = channelId - RadioService.RADIO_COLLECT_START;
			}
		} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
			if (DEBUG)
				Log.d(TAG, "<myu>AM Mode");
			offset = RadioService.RADIO_AM_START;
			if (channelId >= RadioService.RADIO_AM_START && channelId < RadioService.RADIO_COLLECT_START) {
				pressedId = channelId - RadioService.RADIO_AM_START;
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
					if(i>=0 && i < RadioService.RADIO_PAGE_COUNT){
						radioService.curChannelId = 0;
						channelBtn[i].setSelected(false);
					}else if (i >= RadioService.RADIO_PAGE_COUNT && i < RadioService.RADIO_PAGE_COUNT) {
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
		case DIALOG_SCAN:
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
	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
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
		
	private AlertDialog createDialog() {
		di = new AlertDialog.Builder(this)
			.setTitle("\u6E29\u99A8\u63D0\u793A")
			.setMessage(R.string.clear_content)
			.setView(this.checkbox)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences.Editor localEditor = getSharedPreferences("checkvalue", 0).edit();
					if (cBox.isChecked()) {
						localEditor.putString("ischeck", "1");
					} else {
						localEditor.putString("ischeck", "0");
					}
					radioService.clearAllContent();
					gone_Empty_ButtonView();
					updateChannelList();
					updateFreqView();
					localEditor.commit();
					}
				})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}})
			.create();
		return di;
	}
	
	private int last_Effective_ChannelItem() {
		if(radioService.getRadioType() == RadioService.RADIO_FM1) {
			for(int id = 0x2f; id > 0; id--) {
				RadioService.ChannelItem item = radioService.getChannelItem(id);
				if(!item.freq.equals("")) {
					return id;
				}
			}
		} else if(radioService.getRadioType() == RadioService.RADIO_AM) {
			for(int id = 0x5f; id > 0x30; id--) {
				RadioService.ChannelItem item = radioService.getChannelItem(id);
				if(!item.freq.equals("")) {
					return id;
				}
			}
		} else if(radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			for(int id = 0x8f; id > 0x60; id--) {
				RadioService.ChannelItem item = radioService.getChannelItem(id);
				if(!item.freq.equals("")) {
					return id;
				}
			}
		}
		return 0;
	}
	
	private int first_Effective_ChannelItem(int id) {
		if(radioService.getRadioType() == RadioService.RADIO_FM1) {
			if(id < 0) {
				id = first_Effective_ChannelItem_For_Lastsong();
			}
			for(; id < 0x30; id++) {
				RadioService.ChannelItem item = radioService.getChannelItem(id);
				if(!item.freq.equals("")) {
					return id;
				}
			}
		}else if(radioService.getRadioType() == RadioService.RADIO_AM) {
			if(id < 0x30) {
				id = first_Effective_ChannelItem_For_Lastsong();
			}
			for(; id < 0x60; id++) {
                RadioService.ChannelItem item = radioService.getChannelItem(id);
				if(!item.freq.equals("")) {
					return (id - 0x30);
				}
			}
		}else if(radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			if(id < 0x60) {
				id = first_Effective_ChannelItem_For_Lastsong();
			}
			for(; id < 0x90; id++) {
                RadioService.ChannelItem item = radioService.getChannelItem(id);
				if(!item.freq.equals("")) {
					return (id - 0x60);
				}
			}
		}
		return 0;
	}
	
	public void nextSong() {
		int checkedId = 0;
		ChannelItem item;
		int curChannel = radioService.getCurChannelId();
		if(DEBUG) Log.v("RadioActivity", "curChannel =" + curChannel);

		if (radioService.getRadioType() == RadioService.RADIO_FM1) {
			checkedId = curChannel;
			if (curChannel + 1 >= 48)
				curChannel = 46;
		} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
			checkedId = curChannel - RadioService.RADIO_PAGE_COUNT;
			if ((checkedId < 0) || (checkedId >= 0x30))
				checkedId = -1;
		} else if(radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			checkedId = curChannel - 0x60;
			if(checkedId < 0) {
				checkedId = -1;
			}
		}

		item = radioService.getChannelItem(curChannel + 1);
		int valid_ID = last_Effective_ChannelItem();
		if(DEBUG) Log.v("RadioActivity", "valid_ID = " + valid_ID);
		
		if(curChannel == valid_ID) {
			if(DEBUG) Log.v("RadioActivity", "first_Effective_ChannelItem_For_Lastsong() --> =" + first_Effective_ChannelItem_For_Lastsong());
			setChannelChecked(first_Effective_ChannelItem_For_Lastsong());
		}else if (item.freq.equals("")) {
			if(DEBUG) Log.v("RadioActivity", "curChannel + 1 =" + (curChannel + 1) + " |first_Efclfective_ChannelItem(curChannel + 1) =" + first_Effective_ChannelItem(curChannel + 1));
			if(first_Effective_ChannelItem(curChannel + 1) < 0) {
				setChannelChecked(first_Effective_ChannelItem_For_Lastsong());
			} else {
				setChannelChecked(first_Effective_ChannelItem(curChannel + 1));
			}
		} else {
			setChannelChecked(checkedId + 1);
		}
	}
	
	private int last_Effective_ChannelItem_For_Lastsong(int id) {
		int startId = id;
		if(radioService.getRadioType() == RadioService.RADIO_FM1) {
			for(; startId > 0; startId--) {
				RadioService.ChannelItem item = radioService.getChannelItem(startId);
				if(!item.freq.equals("")) {
					return startId;
				}
			}
		}else if(radioService.getRadioType() == RadioService.RADIO_AM) {
			for(; startId > 0x30; startId--) {
                RadioService.ChannelItem item = radioService.getChannelItem(startId);
				if(!item.freq.equals("")) {
					return (startId - 0x30);
				}
			}
		}else if(radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			for(; startId > 0x60; startId--) {
                RadioService.ChannelItem item = radioService.getChannelItem(startId);
				if(!item.freq.equals("")) {
					return (startId - 0x60);
				}
			}
		}
		return 0;
	}
	
	private int first_Effective_ChannelItem_For_Lastsong() {
		if(radioService.getRadioType() == RadioService.RADIO_FM1) {
			int id = 0x0;
			for(; id < 0x30; id++) {
				RadioService.ChannelItem item = radioService.getChannelItem(id);
				if(!item.freq.equals("")) {
					return id;
				}
			}
		}else if(radioService.getRadioType() == RadioService.RADIO_AM) {
			int id = 0x30;
			for(; id < 0x60; id++) {
                RadioService.ChannelItem item = radioService.getChannelItem(id);
				if(!item.freq.equals("")) {
					return (id - 0x30);
				}
			}
		}else if(radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			int id = 0x60;
			for(; id < 0x90; id++) {
                RadioService.ChannelItem item = radioService.getChannelItem(id);
				if(!item.freq.equals("")) {
					return (id - 0x60);
				}
			}
		}
		return 0;
	}
	
	public void lastSong() {
		int checkedId = 0;
        RadioService.ChannelItem item;
		int curId = 0;
		int effective_id = 0;
		int curChannel = radioService.getCurChannelId();

		if (radioService.getRadioType() == RadioService.RADIO_FM1) {
			checkedId = curChannel;
			curId = curChannel;
			effective_id = last_Effective_ChannelItem();
			if (curChannel - 1 < 0) {
				checkedId = last_Effective_ChannelItem();
			}else if (curChannel + 1 >= 48) {
				checkedId = first_Effective_ChannelItem_For_Lastsong();
			}
			if(checkedId < 0)
				checkedId = 0;
		} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
			checkedId = curChannel - RadioService.RADIO_AM_START;
			curId = curChannel - RadioService.RADIO_AM_START;
			effective_id = last_Effective_ChannelItem() - RadioService.RADIO_AM_START;
			if((curChannel - 1 < RadioService.RADIO_AM_START) && (curChannel >= 0)) {
				checkedId = last_Effective_ChannelItem() - RadioService.RADIO_AM_START;
			} else if(curChannel + 1 > RadioService.RADIO_COLLECT_START) {
				checkedId = first_Effective_ChannelItem_For_Lastsong();
			} else if(checkedId < 0 ) {
				checkedId = 1;
			}
		} else if(radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			checkedId = curChannel - 0x60;
			curId = curChannel - 0x60;
			effective_id = last_Effective_ChannelItem() - 0x60;
			if(curChannel - 1 < 0x60) {
				checkedId = last_Effective_ChannelItem() - 0x60;
			} else if(curChannel + 1 > 0x90) {
				checkedId = first_Effective_ChannelItem_For_Lastsong();
			} else if(checkedId < 0) {
				checkedId = 0;
			}
		}

		if(curChannel - 1 < 0) {
			item = radioService.getChannelItem(curChannel);
		} else {
			item = radioService.getChannelItem(curChannel - 1);
		}
		
		
		if(DEBUG) Log.v("RadioActivity", "first_Effective_ChannelItem_For_Lastsong = --->" + first_Effective_ChannelItem_For_Lastsong() + " |curChannel = " + curChannel + " |checkedId = " + checkedId);
		if(curId == first_Effective_ChannelItem_For_Lastsong()) {
			if(DEBUG) Log.v("RadioActivity", "33333333333effective_id = " + effective_id);
			setChannelChecked(effective_id);
		}else if (item.freq.equals("")) {
			if(DEBUG) Log.v("RadioActivity", "22222222");
			if((last_Effective_ChannelItem_For_Lastsong(curChannel - 1) < 0) || (last_Effective_ChannelItem_For_Lastsong(curChannel - 1) > 0x30)) {
				if(DEBUG) Log.v("RadioActivity", "444444444");
				setChannelChecked(last_Effective_ChannelItem());
			} else {
				if(DEBUG) Log.v("RadioActivity", "55555555-->last_Effective_ChannelItem_For_Lastsong(curChannel - 1)=" + last_Effective_ChannelItem_For_Lastsong(curChannel - 1));
				setChannelChecked(last_Effective_ChannelItem_For_Lastsong(curChannel - 1));
			}
		} else {
			if(DEBUG) Log.v("RadioActivity", "66666--->checkedId - 1 = " + (checkedId - 1));
			setChannelChecked(checkedId - 1);
		}
	}

    public void add_or_clear_Collect() {
        String curfreq = "0";
        int OUTSCOPE = 0, OUTSCOPE_FLAG = 1;
        int START = 0x60, END = 0x90, END_EMPTY = 0x8f;
        int focus_btn = radioService.getCurChannelId();
        if (DEBUG)
            Log.v("RadioActivity", "&&&&&&radioService.getChannelItem(focus_btn).freq = " + radioService.getChannelItem(focus_btn).freq + " focus_btn=" + focus_btn);
        if (!radioService.getChannelItem(focus_btn).freq.equals("")) {
            if (HEART_STATIC_FLAG == 0) {
                if (radioService.getRadioType() == RadioService.RADIO_FM1) {
                    curfreq = Double.toString(((double) radioService.getCurrentFreq() / 100.0));
                    if (DEBUG)
                        Log.v("RadioActivity", "heart curfreq = " + curfreq + " CurrentFreq = " + radioService.getCurrentFreq());
                    if ((radioService.getCurrentFreq() < RadioService.FM_LOW_FREQ) || (radioService.getCurrentFreq() > RadioService.FM_HIGH_FREQ)) {
                        OUTSCOPE = OUTSCOPE_FLAG;
                    }
                    END_EMPTY = 0x8f;
                } else if (radioService.getRadioType() == RadioService.RADIO_AM) {
                    curfreq = Integer.toString(radioService.getCurrentFreq());
                    focus_btn = FOCUS_BUTTON_ID + 0x30;
                    if (DEBUG)
                        Log.v("RadioActivity", "heart curfreq = " + curfreq + " CurrentFreq = " + radioService.getCurrentFreq());
                    if ((radioService.getCurrentFreq() < RadioService.AM_LOW_FREQ) || (radioService.getCurrentFreq() > RadioService.AM_HIGH_FREQ)) {
                        OUTSCOPE = OUTSCOPE_FLAG;
                    } else {
                        END_EMPTY = 0x8f;
                    }
                } else if (radioService.getRadioType() == RadioService.RADIO_COLLECT) {
                    if ((radioService.getCurrentFreq() < RadioService.FM_LOW_FREQ) || (radioService.getCurrentFreq() > RadioService.FM_HIGH_FREQ)) {
                        curfreq = Integer.toString(radioService.getCurrentFreq());
                        focus_btn = FOCUS_BUTTON_ID + 0x60;
                    } else if ((radioService.getCurrentFreq() < RadioService.AM_LOW_FREQ) || (radioService.getCurrentFreq() > RadioService.AM_HIGH_FREQ)) {
                        curfreq = Double.toString(((double) radioService.getCurrentFreq() / 100.0));
                    } else {
                        END_EMPTY = 0x8f;
                    }
                }
                if (OUTSCOPE == OUTSCOPE_FLAG) {
                    Toast.makeText(getApplicationContext(), R.string.invalid, 0).show();
                    return;
                }
                int empty = 0;
                for (int i = START; i < END; i++) {
                    if (DEBUG)
                        Log.v("RadioActivity", "(" + i + ")= " + radioService.getChannelItem(i).freq);

                    if (radioService.getChannelItem(i).freq.equals(curfreq)) {
                        int sameFreq = i;
                        Toast.makeText(getApplicationContext(), R.string.same_addHeart_toast, 0).show();
                        if (DEBUG) Log.v("RadioActivity", "sameFreq = " + sameFreq);
                        return;
                    }

                    if (radioService.getChannelItem(i).freq.equals("")) {
                        empty = i;
                        if (DEBUG) Log.v("RadioActivity", "empty = " + empty);
                        i = END;
                    }
                }
                if (empty == END_EMPTY) {
                    Toast.makeText(getApplicationContext(), R.string.nowhere, 1).show();
                    return;
                }
                RadioService.ChannelItem item = radioService.getChannelItem(empty);
                if (DEBUG)
                    Log.v("RadioActivity", "*******************CurrentFreq =" + radioService.getCurrentFreq() + " |item.freq=" + radioService.getChannelItem(focus_btn).freq);
                if (!radioService.getChannelItem(focus_btn).freq.equals("")) {
                    if ((radioService.getCurrentFreq() == (Integer.parseInt(radioService.getChannelItem(focus_btn).freq.replaceAll("\\.", "")) * 10)) || (radioService.getCurrentFreq() == (Integer.parseInt(radioService.getChannelItem(focus_btn).freq.replaceAll("\\.", "")) * 1))) {
                        item.freq = curfreq;
                        item.name = radioService.getChannelItem(focus_btn).name;
                        item.abridge = radioService.getChannelItem(focus_btn).abridge;
                        radioService.setChannelItem(empty, item);
                    } else {
                        item.freq = curfreq;
                        item.name = "";
                        item.abridge = "";
                        radioService.setChannelItem(empty, item);
                    }
                    updateChannelList();
                    updateFreqView();
                    mButtonHeart.setBackground(getResources().getDrawable(R.drawable.collect_d));
                    HEART_STATIC_FLAG = 1;
                    Toast.makeText(getApplicationContext(), R.string.is_addHeart_toast, 0).show();
                    return;
                }
            } else {
                int checkedId = 0;
                mButtonHeart.setBackground(getResources().getDrawable(R.drawable.collect_u));
                Toast.makeText(getApplicationContext(), R.string.clear_addHeart_toast, 0).show();
                HEART_STATIC_FLAG = 0;
                if (radioService.getRadioType() == RadioService.RADIO_FM1) {
                } else if (radioService.getRadioType() == RadioService.RADIO_COLLECT) {
                    checkedId = FOCUS_BUTTON_ID + 0x60;
                } else if (radioService.getRadioType() == RadioService.RADIO_AM) {
                    checkedId = FOCUS_BUTTON_ID + 0x30;
                }
                RadioService.ChannelItem item = radioService.getChannelItem(checkedId);
                for (int j = 0x60; j < 0x90; j++) {
                    if (radioService.getChannelItem(j).freq.equals(item.freq)) {
                        item = radioService.getChannelItem(j);
                        item.freq = "";
                        item.name = "";
                        item.abridge = "";
                        radioService.setChannelItem(j, item);
                        updateChannelList();
                        updateFreqView();
                        j = 0x90;
                    }
                }
            }
        }
    }

    private void curFreq_Compare_To_Collect(int curFreq) {
		if(DEBUG) Log.v("RadioActivity", "curFreq_Compare_To_Collect-----curFreq=" + curFreq);
		int collect_Freq = 0;
		mButtonHeart.setBackground(getResources().getDrawable(R.drawable.collect_u));
		HEART_STATIC_FLAG = 0;
		for(int i = 0x60; i < 0x90; i++) {
			RadioService.ChannelItem item = radioService.getChannelItem(i);
			if(item.freq.contains(".")) {
				collect_Freq = Integer.parseInt(item.freq.replaceAll("\\.", "")) * 10;
			} else if(!item.freq.equals("")) {
				collect_Freq = Integer.parseInt(item.freq);
			}
			if(collect_Freq == curFreq) {
				HEART_STATIC_FLAG = 1;
				mButtonHeart.setBackground(getResources().getDrawable(R.drawable.collect_d));
				break;
			}
		}
	}
	
	private IntentFilter getIntentFilter(){		
		IntentFilter myIntentFilter = new IntentFilter("Auto-Search");
		myIntentFilter.addAction("Radio.Media_Broadcast_Next");
		myIntentFilter.addAction("Radio.Media_Broadcast_Last");
		myIntentFilter.addAction("android.intent.action.BONOVO_SWITCH_FMAM");
		myIntentFilter.addAction("Radio_Auto_Complete");		
		myIntentFilter.addAction("Step-Search");
		myIntentFilter.addAction("updateFreqView");
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
					gone_Empty_ButtonView();
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
			} else if (intent.getAction().equals("Step-Search")) {
			  int i = intent.getIntExtra("step-curfreq", 0);
			  curFreq_Compare_To_Collect(i);
			} else if (intent.getAction().equals("updateFreqView")) {
				updateFreqView();
			}
		}
	};
}
