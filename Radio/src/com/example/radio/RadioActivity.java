package com.example.radio;

import com.example.radio.RadioInterface.RadioStatusChangeListener;
import com.example.radio.RadioService.ChannelItem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.content.SharedPreferences.Editor;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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

	private static int FOCUS_BUTTON_ID = 0;
	private static int HEART_STATIC_FLAG = 0;

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
	private CheckBox cBox;
	private AlertDialog di;
	private View checkbox;

	private static final boolean DEBUG = false;
	private static int  flag = 0;						//閼奉亜濮╅幖婊冨酱閺嶅洩顔�
	private static int IS_AUTO_NOW = 1;
	private static int IS_STEP_NOW = 2;
	private final static String TAG = "RadioActivity";
	private RadioService radioService = null;

    private int currentLayout = 1;
    private View topPanel, midPanel, botPanel;
    private Bitmap bmapTop, bmapMid, bmapBot;

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
	private Button mButtonSaveToFavs;
	private Button mButtonButtonHeart;
	private Button mButtonClear;
	private Button mButtonAddHeart;
	private Button[] channelBtn = new Button[CHANNEL_SIZE];
	Intent serviceIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radio_player_layout_1);

		serviceIntent = new Intent(getApplicationContext(), RadioService.class);
		setupview();

		registerReceiver(myBroadcastReveiver, getIntentFilter());

		RadioService.isLive = true;
		if(DEBUG) Log.d(TAG, "++++++onCreate()");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onDestroy() {
		RadioService.isLive = false;
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
		super.onPause();
		stopScan();
		unbindService(this);

		if(DEBUG) Log.d(TAG, "++++++onPause()");
	}

	@Override
	protected void onResume() {
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
			/*case R.id.btnhome:// 閿熸枻鎷烽敓鏂ゆ嫹 閿熸枻鎷烽敓鏂ゆ嫹閿熻緝鍖℃嫹Service
				if (DEBUG)
					Log.d(TAG, "btnhome has worked");
				finish();
				break; */
			case R.id.btnclose:// 閿熸枻鎷烽敓鏂ゆ嫹 閿熻緝鍖℃嫹Service
				if (DEBUG)
					Log.d(TAG, "btnclose has worked");
				radioService.stopService(new Intent("com.example.radio.RadioService"));
				finish();
				break;
			case R.id.btncollect:
				add_or_clear_Collect();
				gone_Empty_ButtonView();
				break;
//			case R.id.btnimport:
//				if (DEBUG)
//					Log.d(TAG, "btnimport has worked");
//				Intent it = new Intent(RadioActivity.this,
//						RadioImportActivity.class);
//				RadioActivity.this.startActivity(it);
//				break;
			case R.id.btnfine:
				if (DEBUG)
					Log.d(TAG, "btnfine has worked");
				stopScan();
				radioService.setFunctionId(RadioInterface.FUNCTION_FINE_TUNE);
				if (currentLayout == 1) {
					mMiddleButtonBackward
							.setBackgroundResource(R.drawable.btnfinebackward1);
					mMiddleButtonForward
							.setBackgroundResource(R.drawable.btnfineforward1);
				}
				else {
					mMiddleButtonBackward
							.setBackgroundResource(R.drawable.btnfinebackward2);
					mMiddleButtonForward
							.setBackgroundResource(R.drawable.btnfineforward2);
				}
				break;
			case R.id.btnstep:
				if (DEBUG)
					Log.d(TAG, "btnstep has worked");
				stopScan();
				radioService.setFunctionId(RadioInterface.FUNCTION_STEP_SEEK);
				if (currentLayout == 1) {
					mMiddleButtonBackward
							.setBackgroundResource(R.drawable.btnstepbackward1);
					mMiddleButtonForward
							.setBackgroundResource(R.drawable.btnstepforward1);
				}
				else {
					mMiddleButtonBackward
							.setBackgroundResource(R.drawable.btnstepbackward2);
					mMiddleButtonForward
							.setBackgroundResource(R.drawable.btnstepforward2);
				}
				break;
			case R.id.btnauto:
				if (DEBUG)
					Log.d(TAG, "btnauto has worked");
				//showDialog(DIALOG_CONFIRM);
				RadioService.STEP_OR_AUTO = IS_AUTO_NOW;
				new AlertDialog.Builder(RadioActivity.this)
						.setTitle(R.string.remind)
						.setCancelable(true)
						.setMessage(R.string.describe)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										for (int i = 0; i < CHANNEL_SIZE; i++) {
											channelBtn[i].setText("");
										}

										radioService.onAutoSearch();

									}
								})
						.setNegativeButton(R.string.cancel, null).create().show();
				break;
			case R.id.btnsave:
				int empty ,sameFreq ;
				String curfreq = null;
				int OUTSCOPE = 0 ,OUTSCOPE_FLAG = 1 ;
				int START = 0,END = 0,END_EMPTY = 0;
				if(radioService.getRadioType() == RadioService.RADIO_FM1){
					// make the current freq type int to String
					curfreq = Double.toString(radioService.getCurrentFreq()/100.0);
					Log.v(TAG,"heart curfreq = "+curfreq+" CurrentFreq = "+radioService.getCurrentFreq());

					//if the freq's value outof the cur_Type. example: curfreq is 10390 ,but cur_Type is AM, 520~1710
					if(radioService.getCurrentFreq() < RadioService.FM_LOW_FREQ || radioService.getCurrentFreq() > RadioService.FM_HIGH_FREQ){
						OUTSCOPE = OUTSCOPE_FLAG;
					}
					START = 0; END = RadioService.RADIO_PAGE_COUNT; END_EMPTY = RadioService.RADIO_PAGE_COUNT - 1;

				}else if(radioService.getRadioType() == RadioService.RADIO_AM){
					// make the current freq type int to String
					curfreq = Integer.toString(radioService.getCurrentFreq());
					Log.v(TAG,"heart curfreq = "+curfreq+" CurrentFreq = "+radioService.getCurrentFreq());
					//if the freq's value outof the cur_Type. example: curfreq is 10390 ,but cur_Type is AM, 520~1710
					if(radioService.getCurrentFreq() < RadioService.AM_LOW_FREQ || radioService.getCurrentFreq() > RadioService.AM_HIGH_FREQ){
						OUTSCOPE = OUTSCOPE_FLAG;
					}
					START = RadioService.RADIO_PAGE_COUNT; END = 96;END_EMPTY = 96 - 1;

				}else if(radioService.getRadioType() == RadioService.RADIO_COLLECT){
					//when the type is RADIO_COLLECT ,the curfreq can be FM also can be AM too
					if(radioService.getCurrentFreq() < RadioService.FM_LOW_FREQ  || radioService.getCurrentFreq() > RadioService.FM_HIGH_FREQ){
						// make the current freq type int to String
						curfreq = Integer.toString(radioService.getCurrentFreq());
					}else if(radioService.getCurrentFreq() < RadioService.AM_LOW_FREQ || radioService.getCurrentFreq() > RadioService.AM_HIGH_FREQ){
						curfreq = Double.toString(radioService.getCurrentFreq()/100.0);
					}

					START = 96; END = 144;END_EMPTY = 144 - 1;
				}
				for (int i = START; i < END; i++) {
					if(DEBUG)Log.v(TAG,"("+i+")= "+radioService.getChannelItem(i).freq);
					if(OUTSCOPE == OUTSCOPE_FLAG){
						//if the freq's value outof the cur_Type. example: curfreq is 10390 ,but cur_Type is AM, 520~1710
						Toast.makeText(getApplicationContext(), R.string.invalid, Toast.LENGTH_SHORT).show();
						break;
					}else{
						if (radioService.getChannelItem(i).freq.equals("")) {
							//if freq is empty,save the i and assigned to empty
							empty = i;
							if(DEBUG)Log.v(TAG,"empty = " + empty);

							if(empty == END_EMPTY){
								//if emty in the end ,show a toast to tell user
								Toast.makeText(getApplicationContext(), R.string.nowhere, Toast.LENGTH_LONG).show();
							}

							//add the curfreq into the empty channel and refresh view
							ChannelItem item  ;
							item = radioService.getChannelItem(empty);
							item.freq = curfreq;
							item.name = curfreq;
							radioService.setChannelItem(empty,item);
							updateChannelList();
							updateFreqView();

							break;
						} else if(radioService.getChannelItem(i).freq.equals(curfreq)){
							//if curfreq == channel.freq, make a Toast
							sameFreq = i;
							Toast.makeText(getApplicationContext(), R.string.samefreq, Toast.LENGTH_SHORT).show();
							if(DEBUG)Log.v(TAG,"sameFreq = " + sameFreq);
							break;
						}
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
							public void onClick(DialogInterface dialog,
									int which) {
								Log.v(TAG,"1111111111");
								clear_Single_Content();
								Log.v(TAG,"2222222222");
								gone_Empty_ButtonView();
								Log.v(TAG,"3333333333");
								updateChannelList();
								updateFreqView();
							}
						})
				.setNegativeButton(R.string.cancel, null).create().show();
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
			/*case R.id.btnfm2:
				if (DEBUG)
					Log.d(TAG, "btnfm2 has worked");
				if(!radioService.mIsSearchThreadRunning){
					updateFreqView();
					radioSetSelect(RadioService.RADIO_FM2);
					mMiddleButtonForward.setVisibility(View.VISIBLE);
					mMiddleButtonBackward.setVisibility(View.VISIBLE);
				}else {
					Toast.makeText(getApplicationContext(), R.string.searching, Toast.LENGTH_SHORT).show();
				}
				break; */
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
				if (DEBUG)
					Log.d(TAG, "btncollect has worked");
				if(!radioService.mIsSearchThreadRunning){
					updateFreqView();
					radioSetSelect(RadioService.RADIO_COLLECT);
					gone_Empty_ButtonView();
				}else {
					Toast.makeText(getApplicationContext(), R.string.searching, Toast.LENGTH_SHORT).show();
				}
				break;
			/*case R.id.btnvolume:
				if (DEBUG)
					Log.d(TAG, "btlvoume has worked");
				showDialog(DIALOG_VOLUME);
				// radioService.popupVolumeWindows();
				break; */
			case R.id.btnsetting:
				if (DEBUG)
					Log.d(TAG, "btnsrtting has worked");
//				Intent setting = new Intent(RadioActivity.this, RadioSetting.class);
//				//setting.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//				RadioActivity.this.startActivity(setting);
				Intent setting = new Intent("com.example.radio.IntentActivity");
				//setting.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				RadioActivity.this.startActivity(setting);
				break;
			case R.id.btnforward:
				if (radioService.getFunctionId() == RadioInterface.FUNCTION_SCAN) {
					stopScan();
				} else if (radioService.getFunctionId() == RadioInterface.FUNCTION_FINE_TUNE) {
					radioService.fineRight(radioService.getCurrentFreq());
					curFreq_Compare_To_Collect(radioService.getCurrentFreq());
				} else {
					radioService.stepRight(radioService.getCurrentFreq());
					RadioService.STEP_OR_AUTO = IS_STEP_NOW;
				}
				break;
			case R.id.btnbackward:
				if (radioService.getFunctionId() == RadioInterface.FUNCTION_SCAN) {
					stopScan();
				} else if (radioService.getFunctionId() == RadioInterface.FUNCTION_FINE_TUNE) {
					radioService.fineLeft(radioService.getCurrentFreq());
					curFreq_Compare_To_Collect(radioService.getCurrentFreq());
				} else {
					radioService.stepLeft(radioService.getCurrentFreq());
					RadioService.STEP_OR_AUTO = IS_STEP_NOW;
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
				radioSetSelect(radioService.getRadioType());
				updateFreqView();
				break;
			default:
				break;
			}
		}
	};

	private final Runnable mScanRunnable = new Runnable() {
		@Override
		public void run() {
			if (radioService.getFunctionId() == RadioInterface.FUNCTION_SCAN) {
				if (mScanStartFreq != -1 && radioService.getCurrentFreq() == mScanStartFreq) {
					// We've made it all the way around!
					stopScan();
				} else {
					radioService.stepRight(radioService.getCurrentFreq());
					mHandler.postDelayed(this, radioService.getScanDelayMs());
				}
			} else {
				Toast.makeText(RadioActivity.this, "Not scanning, current function=" + radioService.getFunctionId(), Toast.LENGTH_SHORT).show();
			}
		}
	};

	private int mSavedFunctionId = -1;
	private int mScanStartFreq = -1;

	public void startScan() {
		if (radioService.getFunctionId() != RadioInterface.FUNCTION_SCAN) {
			mScanStartFreq = radioService.getCurrentFreq();
			mSavedFunctionId = radioService.getFunctionId();

			String freq = RadioService.formatFreqDisplay(this, mScanStartFreq);
			Toast.makeText(this, getString(R.string.start_scan, freq), Toast.LENGTH_LONG).show();
			if (DEBUG) Log.v(TAG, "Scanning from " + freq);

			mMiddleButtonStep.setText(R.string.scanning);
			radioService.setFunctionId(RadioInterface.FUNCTION_SCAN);
			radioService.stepRight(radioService.getCurrentFreq());
			mHandler.postDelayed(mScanRunnable, radioService.getScanDelayMs());
		} else {
			Log.i(TAG, "NOT starting scan, because function=" + radioService.getFunctionId());
		}
	}

	public void stopScan() {
		if (radioService.getFunctionId() == RadioInterface.FUNCTION_SCAN) {
			mHandler.removeCallbacks(mScanRunnable);
			radioService.setFunctionId(mSavedFunctionId);
			mSavedFunctionId = -1;
			mScanStartFreq = -1;
			if (DEBUG) Log.v(TAG, "Stop Scan at " + radioService.getCurrentFreq());
		} else {
			if (DEBUG) Log.v(TAG, "stopScan: not currently scanning; func=" + radioService.getFunctionId());
		}

		mMiddleButtonStep.setText(R.string.step);
	}

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
				editChannelId = viewId - R.id.channel0
						+ 96;
				if (DEBUG)
					Log.d(TAG, "onLongClick editChannelId = "+ editChannelId);
				break;
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
		case R.id.btnclear:
			/*******init the checkbox layout******/
			LayoutInflater lauoutInflater = LayoutInflater.from(RadioActivity.this);
			checkbox = lauoutInflater.inflate(R.layout.checkbox, null);
			cBox = (CheckBox) checkbox.findViewById(R.id.check);

			SharedPreferences pre = getSharedPreferences("checkvalue", MODE_PRIVATE);
			String value = pre.getString("ischeck", "");
			/*****if check the NO_Remind,the Dialog no show next time******/
			if (value.endsWith("1")) {
				createDialog().dismiss();

				//clear all content and refresh view
				radioService.clearAllContent();
				gone_Empty_ButtonView();
				updateChannelList();
				updateFreqView();
			} else {
				createDialog().show();
			}
			break;
        case R.id.btnstep:
            // Long-press STEP button enables the SCAN feature
			startScan();
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
        mMiddleButtonStep.setOnLongClickListener(this);
		mMiddleButtonAuto = (Button) findViewById(R.id.btnauto);
		mMiddleButtonAuto.setOnClickListener(this);
		mButtonSaveToFavs = (Button)findViewById(R.id.collect);
		mButtonSaveToFavs.setOnClickListener(this);
		mButtonButtonHeart = (Button) findViewById(R.id.btnsave);
		mButtonButtonHeart.setOnClickListener(this);
		mButtonClear = (Button) findViewById(R.id.btnclear);
		mButtonClear.setOnClickListener(this);
		mButtonClear.setOnLongClickListener(this);
		mButtonAddHeart = (Button) findViewById(R.id.btncollect);
		mButtonAddHeart.setOnClickListener(this);

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
			Message msg = mHandler.obtainMessage(type);
			mHandler.sendMessage(msg);
		}
	};

	/*
	 * Removing a single Button
	 * */
	public void clear_Single_Content() {
		if (radioService.getRadioType() == RadioInterface.RADIO_FM1 || radioService.getRadioType() == RadioInterface.RADIO_FM2) {

				ChannelItem item = new ChannelItem();
				item.freq = "";
				item.name = "";
				item.abridge = "";
				if(channelBtn[FOCUS_BUTTON_ID].isSelected()){
					Log.v(TAG, "4444444444");
					radioService.setChannelItem(FOCUS_BUTTON_ID, item);
				}

		} else if (radioService.getRadioType() == RadioInterface.RADIO_AM) {
				ChannelItem item = new ChannelItem();
				item.freq = "";
				item.name = "";
				item.abridge = "";
				if(channelBtn[FOCUS_BUTTON_ID].isSelected()){
					radioService.setChannelItem(FOCUS_BUTTON_ID + 48, item);
				}
		} else if(radioService.getRadioType() == RadioInterface.RADIO_COLLECT){
				ChannelItem item = new ChannelItem();
				item.freq = "";
				item.name = "";
				item.abridge = "";
				if(channelBtn[FOCUS_BUTTON_ID].isSelected()){
					radioService.setChannelItem(FOCUS_BUTTON_ID + 96, item);
				}
		}
		//删除当前焦点频道后,把全局变量置为0，说明当前焦点频率(此时频率为空)与收藏栏不存在相同，并且把爱心变为空心
		HEART_STATIC_FLAG = 0;
		mButtonAddHeart.setBackground(getResources().getDrawable(R.drawable.btncollect_heart));
		/*******************************************************************************************/
	}

	/**
	 * 末尾Channel信息为空时 隐藏其界面
	 */
	private void gone_Empty_ButtonView() {
		ChannelItem item;
		for(int j=0; j<48; j++){
			channelBtn[j].setVisibility(View.VISIBLE);
		}
		updateChannelList();
		updateFreqView();
		if(radioService.getRadioType() == RadioInterface.RADIO_FM1){
			int i = 47;
			int gone_ID = 0;
			for(; i>0; i--){
				item = radioService.getChannelItem(i);
				if(!item.freq.equals("")){
					break;
				}
			}
			if((i+1)>=12){
				gone_ID = i+1;
			}else {
				gone_ID = 12;
			}

			for(; gone_ID<48; gone_ID++){
				channelBtn[gone_ID].setVisibility(View.GONE);
			}
		}else if (radioService.getRadioType() == RadioInterface.RADIO_AM) {
			int i = 95;
			int gone_ID = 0;
			for(; i>48; i--){
				item = radioService.getChannelItem(i);
				if(!item.freq.equals("")){
					break;
				}
			}

			if((i-47)>=12){
				gone_ID = i-47;
			}else {
				gone_ID = 12;
			}
			for(; gone_ID<48; gone_ID++){
				channelBtn[gone_ID].setVisibility(View.GONE);
			}
		}else if (radioService.getRadioType() == RadioInterface.RADIO_COLLECT) {
			int i = 143;
			int gone_ID = 0;
			for(; i>96; i--){
				item = radioService.getChannelItem(i);
				if(!item.freq.equals("")){
					break;
				}
			}
			if((i-96)>=12){
				gone_ID = i-96;
			}else {
				gone_ID = 12;
			}
			for(; gone_ID<48; gone_ID++){
				channelBtn[gone_ID].setVisibility(View.GONE);
			}
		}
	}

	public void setChannelChecked(int btnId) {
		int checkedId = 0;

		ChannelItem item;

		if (DEBUG)
			Log.v(TAG, "setChannelChecked is into here,btnId = "+btnId);
		//checkedID's scope 0 ~ RADIO_CHANNEL_COUNT
		if (radioService.getRadioType() == RadioService.RADIO_FM1) {
			checkedId = btnId;
		} else if (radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			checkedId = btnId + 96;
		} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
			checkedId = btnId + RadioService.RADIO_FM_COUNT;
		}
		item = radioService.getChannelItem(checkedId);
		if (item.freq.equals("")) {
			if (DEBUG)
				Log.v(TAG, "item.freq is empty!!!");
			return;
		}

		if (radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			//******** If Typ is RADIO_COLLECT,According to the frequency of judgment //
			//******** whether FM or AM												  //
			if (item.freq.contains(".")) {
				radioService.turnFmAm(0);
				radioService.setCurrentFreq(Integer.parseInt(item.freq
						.replaceAll("\\.", "")) * 10);
			} else {
				radioService.turnFmAm(1);
				radioService.setCurrentFreq(Integer.parseInt(item.freq
						.replaceAll("\\.", "")));
			}
			//************************************************************************//
			//************************************************************************//
		} else if (radioService.getRadioType() == RadioService.RADIO_FM1) {
			if (item.freq.contains(".")) {
				radioService.turnFmAm(0);
				radioService.setCurrentFreq(Integer.parseInt(item.freq
						.replaceAll("\\.", "")) * 10);
			} else {
				radioService.turnFmAm(1);
				radioService.setCurrentFreq(Integer.parseInt(item.freq
						.replaceAll("\\.", "")));
			}
		} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
			if (item.freq.contains(".")) {
				radioService.turnFmAm(0);
				radioService.setCurrentFreq(Integer.parseInt(item.freq
						.replaceAll("\\.", "")) * 10);
			} else {
				radioService.turnFmAm(1);
				radioService.setCurrentFreq(Integer.parseInt(item.freq
						.replaceAll("\\.", "")));
			}
		}


		for (int i = 0; i < RadioService.RADIO_PAGE_COUNT; i++) {
			if (channelBtn[i].isSelected()) {
				channelBtn[i].setSelected(false);
			}
			mButtonAddHeart.setBackground(getResources().getDrawable(R.drawable.btncollect_heart));
			HEART_STATIC_FLAG = 0;
			/*遍历收藏栏所有频率值 若存在与当前频率相同,则爱心变为红实心图标*/
			for(int start=96; start<144; start++){
				if(radioService.getChannelItem(start).freq.equals(item.freq)){
					mButtonAddHeart.setBackground(getResources().getDrawable(R.drawable.collect_d));
					HEART_STATIC_FLAG = 1;
				}
			}
			/***********************************************************/
		}
		channelBtn[btnId].setSelected(true);
		FOCUS_BUTTON_ID = btnId;
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
		if (DEBUG)
			Log.v(TAG, "RadioService is connected");
		radioService = ((RadioService.ServiceBinder) service).getService();
		radioService.registStatusListener(mRadioStatusListener);

        currentLayout = radioService.getLayout();
        updateLayout(currentLayout);

		int fm_freq = getIntent().getIntExtra("FM",0);

		if(fm_freq>0)
		{
			radioService.setCurrentFreq(fm_freq/10);
			radioService.turnFmAm(0);
			radioSetSelect(RadioService.RADIO_FM1);
		}

		int am_freq = getIntent().getIntExtra("AM",0);

		if(am_freq>0)
		{
			radioService.setCurrentFreq(am_freq/1000);
			radioService.turnFmAm(1);
			radioSetSelect(RadioService.RADIO_AM);
		}

		curFreq_Compare_To_Collect(radioService.getCurrentFreq());
		gone_Empty_ButtonView();
		if (radioService.getFunctionId() == RadioInterface.FUNCTION_FINE_TUNE) {
			if (currentLayout == 1) {
				mMiddleButtonBackward
						.setBackgroundResource(R.drawable.btnfinebackward1);
				mMiddleButtonForward
						.setBackgroundResource(R.drawable.btnfineforward1);
			}
			else {
				mMiddleButtonBackward
						.setBackgroundResource(R.drawable.btnfinebackward2);
				mMiddleButtonForward
						.setBackgroundResource(R.drawable.btnfineforward2);
			}

		} else if (radioService.getFunctionId() == RadioInterface.FUNCTION_STEP_SEEK) {
			if (currentLayout == 1) {
				mMiddleButtonBackward
						.setBackgroundResource(R.drawable.btnstepbackward1);
				mMiddleButtonForward
						.setBackgroundResource(R.drawable.btnstepforward1);
			}
			else{
				mMiddleButtonBackward
						.setBackgroundResource(R.drawable.btnstepbackward2);
				mMiddleButtonForward
						.setBackgroundResource(R.drawable.btnstepforward2);
			}
		}

		if (DEBUG)
			Log.v(TAG, "####setVolume#### " + radioService.getVolume());
        updateFreqView();
		if (RadioService.RADIO_FM1 == radioService.getRadioType()) {
			((TextView) findViewById(R.id.radiotype)).setText("FM"); /* 閿熸枻鎷稦M1鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText("MHz");
			mButtonButtonFm1.setSelected(true);
			//mButtonButtonFm2.setSelected(false);
			mButtonButtonAm.setSelected(false);
			mButtonSaveToFavs.setSelected(false);
		} else if (RadioService.RADIO_COLLECT == radioService.getRadioType()) {
			((TextView) findViewById(R.id.radiotype)).setText("FAV"); /* 閿熸枻鎷稦M2鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText(" ");
			mButtonButtonFm1.setSelected(false);
			//mButtonButtonFm2.setSelected(true);
			mButtonButtonAm.setSelected(false);
			mButtonSaveToFavs.setSelected(true);
		} else if (RadioService.RADIO_AM == radioService.getRadioType()) {
			((TextView) findViewById(R.id.radiotype)).setText("AM"); /* 閿熸枻鎷稟M鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText("KHz");
			mButtonButtonFm1.setSelected(false);
			//mButtonButtonFm2.setSelected(false);
			mButtonButtonAm.setSelected(true);
			mButtonSaveToFavs.setSelected(false);
		}
		updateChannelList();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
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
			mButtonSaveToFavs.setSelected(false);

			if(radioService.getCurChannelId() >= 96 && radioService.getCurChannelId() < 144){
				//If focus in the RADIO_COLLECT Type,No need turnFMAM
			}else {
				radioService.turnFmAm(0);
			}

		} else if (RadioService.RADIO_COLLECT == type) {
			((TextView) findViewById(R.id.radiotype)).setText("FAV"); /* 閿熸枻鎷稦M2鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText(" ");
			mButtonButtonFm1.setSelected(false);
			//mButtonButtonFm2.setSelected(false);
			mButtonButtonAm.setSelected(false);
			mButtonSaveToFavs.setSelected(true);

		} else if (RadioService.RADIO_AM == type) {
			((TextView) findViewById(R.id.radiotype)).setText("AM"); /* 閿熸枻鎷稟M鏃秚ext閿熺殕璁规嫹閿熷彨浼欐嫹閿熸枻鎷锋伅 */
			((TextView) findViewById(R.id.radiohz)).setText("kHz");
			mButtonButtonFm1.setSelected(false);
			//mButtonButtonFm2.setSelected(false);
			mButtonButtonAm.setSelected(true);
			mButtonSaveToFavs.setSelected(false);

			if(radioService.getCurChannelId() >= 96 && radioService.getCurChannelId() < 144){
				//If focus in the RADIO_COLLECT Type,No need turnFMAM
			}else {
				radioService.turnFmAm(1);
			}

		}
		updateChannelList();

	}

	@Override
	// 閿熸枻鎷烽敓閰电鎷烽敓鏂ゆ嫹閿熻緝顤庢嫹閿熺丹Handler閿熸枻鎷烽敓鏂ゆ嫹
	public void onClick(View v) {
		int viewId = v.getId();
		Message msg = mHandler.obtainMessage(viewId);
		if (msg != null) {
			mHandler.sendMessage(msg);
		}
	}

	public int findChannelID(int freq) {
		int counter;
		int tempFreq;
		int channelID = -1;
		for (counter = 0; counter < RadioService.RADIO_CHANNEL_COUNT; counter++) {
			ChannelItem item;
			tempFreq = 0;
			item = radioService.getChannelItem(counter);
			if(item.freq.contains(".")){
				tempFreq =Integer.parseInt(item.freq
						.replaceAll("\\.", "")) * 10;
			}else if(!item.freq.equals("")){
				tempFreq = Integer.parseInt(item.freq);
			}
			if (tempFreq == freq) {
				channelID = counter;
				break;
			}
		}
		return channelID;
	}

	void updateFreqView() {
		int length,tempID,temp;
		ImageView channelView;
		temp = radioService.getCurrentFreq();
		// if we've stepped into a new freq, the set ID might not be accurate
		// let's check & fix
		tempID = findChannelID(temp);
		if (tempID != radioService.getCurChannelId()) {
			radioService.setCurChannelId(tempID);
		}
		if (DEBUG)
			Log.v(TAG, "<myu>curfreq = " + radioService.getCurrentFreq());
		if (radioService.getCurChannelId() >= 0) {
			if (DEBUG)
				Log.v(TAG,
						"<myu>CurChannelId is "
								+ radioService.getCurChannelId());
			((TextView) findViewById(R.id.channeldetails))
					.setText((radioService.getChannelItem(radioService
							.getCurChannelId())).name);
		} else {
			// We aren't set to an active ID/Favorite, so we need to clear the textviews
			((TextView) findViewById(R.id.channeldetails))
					.setText("");
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
		// update metadata
		radioService.updatePlaybackTitle();

	}

	// 閿熸枻鎷烽敓鏂ゆ嫹Channel閿熷彨鎲嬫嫹
	void updateChannelList() { /* 閿熷壙鐚存嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹 閿熸枻鎷烽敓鎴尅鎷锋寚閿熸枻鎷�搴旈敓鏂ゆ嫹閿熺獤鐚存嫹 */
		int channelId, offset = 0;
		int pressedId = -1;
		ChannelItem item;

		channelId = radioService.getCurChannelId();
		if (DEBUG)
			Log.d(TAG, "channelId = "+channelId);
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
			/***If Type is RADIO_COLLECT ,presseId = channeID - 96(offset)****/
			/************channeID 0 ~ RADIO_CHANNEL_COUNT*********************/
			offset = 96;
			if (channelId >= 96
					&& channelId < 144) {
				pressedId = channelId - 96;
			}
			/*****************************************************************/
		} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
			if (DEBUG)
				Log.d(TAG, "<myu>AM Mode");
			offset = RadioService.RADIO_FM_COUNT;
			if (channelId >= RadioService.RADIO_FM_COUNT
					&& channelId < 96) {
				pressedId = channelId - RadioService.RADIO_FM_COUNT;
			}
		}
		/*****setChannelText ,If abridge is not NULL ,set abridge.Otherwise set freq****/
		for (int i = 0; i < RadioService.RADIO_PAGE_COUNT; i++) {
			item = radioService.getChannelItem(i + offset);
			if (item.abridge != null && !item.abridge.equals("")) {
				channelBtn[i].setText(item.abridge);
			} else if (item != null) {
				channelBtn[i].setText(item.freq);
			}
		}
		/*******************************************************************************/

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
		if (DEBUG)
			Log.d(TAG, "onCreateDialog is open");
		switch (id) {
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
		if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
		return super.onKeyDown(keyCode, event);
	}

	/*************The No_Remind Dialog*******************/
	private AlertDialog createDialog() {
		di = new AlertDialog.Builder(this).setTitle("温馨提示")
				.setMessage(R.string.clear_content).setView(checkbox)
				.setPositiveButton(R.string.ok ,new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences pre = getSharedPreferences(
								"checkvalue", MODE_PRIVATE);
						Editor editor = pre.edit();
						if (cBox.isChecked()) {
							editor.putString("ischeck", "1");
						} else {
							editor.putString("ischeck", "0");
						}
						//clear all content and refresh view
						radioService.clearAllContent();
						gone_Empty_ButtonView();
						updateChannelList();
						updateFreqView();

						editor.commit();

					}
				}).setNegativeButton(R.string.cancel, null)
				.create();
		return di;
	}

	/**
	 * @return 返回48~0个Button中第一个有效信息的Button ID号
	 */
	private int last_Effective_ChannelItem() {
		ChannelItem item;
		if(radioService.getRadioType() == RadioInterface.RADIO_FM1){
			int id = 47;
			for(; id>0; id--){
				item = radioService.getChannelItem(id);
				if(!item.freq.equals("")){
					return id;
				}
			}
		}else if (radioService.getRadioType() == RadioInterface.RADIO_AM) {
			int id = 95;
			for(; id>48; id--){
				item = radioService.getChannelItem(id);
				if(!item.freq.equals("")){
					return id;
				}
			}
		}else if (radioService.getRadioType() == RadioInterface.RADIO_COLLECT) {
			int id = 143;
			for(; id>96; id--){
				item = radioService.getChannelItem(id);
				if(!item.freq.equals("")){
					return id;
				}
			}
		}
		return 0;
	}

	/**
	 * @param id 遇见的item.freq为空时的id（可以大于48）
	 * @return 返回id~各自Type结尾channelID 第一个有效 id(可以大于48，所以注意返回值要小于48，需要做转换)
	 */
	private int first_Effective_ChannelItem(int id){
		ChannelItem item;
		if(radioService.getRadioType() == RadioInterface.RADIO_FM1){
			if(id < 0){
				id = first_Effective_ChannelItem_For_Lastsong();
			}
			for(; id<48; id++){
				item = radioService.getChannelItem(id);
				if(!item.freq.equals("")){
					return id;
				}
			}
		}else if (radioService.getRadioType() == RadioInterface.RADIO_AM) {
			if(id < 48){
				id = first_Effective_ChannelItem_For_Lastsong();
			}
			for(; id<96; id++){
				item = radioService.getChannelItem(id);
				if(!item.freq.equals("")){
					return id - 48;
				}
			}
		}else if (radioService.getRadioType() == RadioInterface.RADIO_COLLECT) {
			if(id < 96){
				id = first_Effective_ChannelItem_For_Lastsong();
			}
			for(; id<144; id++){
				item = radioService.getChannelItem(id);
				if(!item.freq.equals("")){
					return id - 96;
				}
			}
		}
		return 0;
	}

	public void nextSong() {
		int checkedId = 0;
		ChannelItem item = null;
		int curChannel = radioService.getCurChannelId();
		Log.v(TAG, "curChannel =" + curChannel);

		if (radioService.getRadioType() == RadioService.RADIO_FM1) {
			checkedId = curChannel;
			if (curChannel + 1 > 48) {
				curChannel = 46;
			}

		} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
			checkedId = curChannel - RadioService.RADIO_FM_COUNT;
			if (checkedId < 0 || checkedId >= 48) {
				checkedId = -1;
			}
		} else if (radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			checkedId = curChannel - 96;
			if (checkedId < 0) {
				checkedId = -1;
			}
		}
		item = radioService.getChannelItem(curChannel + 1);
		int valid_ID = last_Effective_ChannelItem();
		Log.v(TAG, "valid_ID = "+valid_ID);
		if (curChannel == valid_ID) {
			Log.v(TAG, "first_Effective_ChannelItem_For_Lastsong() --> ="+first_Effective_ChannelItem_For_Lastsong());
			setChannelChecked(first_Effective_ChannelItem_For_Lastsong());
		} else if (item.freq.equals("")) {
			Log.v(TAG, "curChannel + 1 ="+(curChannel + 1)+" |first_Efclfective_ChannelItem(curChannel + 1) ="+first_Effective_ChannelItem(curChannel + 1));
			if(first_Effective_ChannelItem(curChannel + 1) < 0){
				setChannelChecked(first_Effective_ChannelItem_For_Lastsong());
			}else {
				setChannelChecked(first_Effective_ChannelItem(curChannel + 1));
			}
		} else {
			Log.v(TAG, "()()()()checkedId + 1 = "+(checkedId + 1));
			setChannelChecked(checkedId + 1);
		}
	}

	/**
	 * @param id 遇见的item.freq为空时的id（可以大于48）
	 * @return 返回id~各自Type起始channelID内第一个有效 id(可以大于48，所以注意返回值要小于48，需要做转换)
	 */
	private int last_Effective_ChannelItem_For_Lastsong(int id){
		ChannelItem item;
		int startId = id;
		if(radioService.getRadioType() == RadioInterface.RADIO_FM1){
			for( ;startId>0; startId--){
				item = radioService.getChannelItem(startId);
				if(!item.freq.equals("")){
					return startId;
				}
			}
		}else if (radioService.getRadioType() == RadioInterface.RADIO_AM) {
			for(; startId>48; startId--){
				item = radioService.getChannelItem(startId);
				if(!item.freq.equals("")){
					return startId - 48;
				}
			}
		}else if (radioService.getRadioType() == RadioInterface.RADIO_COLLECT) {
			for(; startId>96; startId--){
				item = radioService.getChannelItem(startId);
				if(!item.freq.equals("")){
					return startId - 96;
				}
			}
		}
		return 0;
	}

	/**
	 * 各自Type：0~48 中第一个有效频道ID
	 */
	private int first_Effective_ChannelItem_For_Lastsong() {
		ChannelItem item;
		if(radioService.getRadioType() == RadioInterface.RADIO_FM1){
			int id = 0 ;
			for(; id<48; id++){
				item = radioService.getChannelItem(id);
				if(!item.freq.equals("")){
					return id;
				}
			}
		}else if (radioService.getRadioType() == RadioInterface.RADIO_AM) {
			int id = 48;
			for(; id<96; id++){
				item = radioService.getChannelItem(id);
				if(!item.freq.equals("")){
					return id - 48;
				}
			}
		}else if (radioService.getRadioType() == RadioInterface.RADIO_COLLECT) {
			int id = 96;
			for(; id<144; id++){
				item = radioService.getChannelItem(id);
				if(!item.freq.equals("")){
					return id - 96;
				}
			}
		}
		return 0;
	}

	public void lastSong() {
		int checkedId = 0;
		ChannelItem item;
		int curId = 0; //curId为根据当前Type:= curChannel或者curChannel-48或者curChannel-96
		int effective_id = 0;
		int curChannel = radioService.getCurChannelId();
		if (radioService.getRadioType() == RadioService.RADIO_FM1){
			checkedId = curChannel;
			curId = curChannel;
			effective_id = last_Effective_ChannelItem();
			if(curChannel - 1 <0){
				checkedId = last_Effective_ChannelItem();
			}else if(curChannel + 1 >48){
				checkedId = first_Effective_ChannelItem_For_Lastsong();
			}

			if (checkedId < 0) {
				checkedId = 0;
			}
		}else if (radioService.getRadioType() == RadioService.RADIO_AM) {
			checkedId = curChannel - 48;
			curId = curChannel - 48;
			effective_id = last_Effective_ChannelItem() - 48;
			if(curChannel - 1 <48 && curChannel  >=0){
				checkedId = last_Effective_ChannelItem() - 48;
			}else if(curChannel + 1 >96){
				checkedId = first_Effective_ChannelItem_For_Lastsong();
			}

			if (checkedId < 0) {
				checkedId = 1;
			}
		}else if (radioService.getRadioType() == RadioService.RADIO_COLLECT) {
			checkedId = curChannel - 96;
			curId = curChannel - 96;
			effective_id = last_Effective_ChannelItem() - 96;
			if(curChannel - 1 <96){
				checkedId = last_Effective_ChannelItem() - 96;
			}else if(curChannel + 1 >144){
				checkedId = first_Effective_ChannelItem_For_Lastsong();
			}

			if (checkedId < 0) {
				checkedId = 0;
			}
		}
		if(curChannel - 1 < 0){
			item = radioService.getChannelItem(curChannel);
		}else {
			item = radioService.getChannelItem(curChannel - 1);
		}

		Log.v(TAG, "first_Effective_ChannelItem_For_Lastsong = --->" + first_Effective_ChannelItem_For_Lastsong()+" |curChannel = "+curChannel+" |checkedId = "+checkedId);
		if(curId == first_Effective_ChannelItem_For_Lastsong()){
			Log.v(TAG, "33333333333effective_id = "+effective_id);
			setChannelChecked(effective_id);
		}else if (item.freq.equals("")) {
			Log.v(TAG, "22222222");
			if(last_Effective_ChannelItem_For_Lastsong(curChannel - 1) < 0 ||last_Effective_ChannelItem_For_Lastsong(curChannel - 1) > 48){
				Log.v(TAG, "444444444");
				setChannelChecked(last_Effective_ChannelItem());
			}else {
				Log.v(TAG, "55555555-->last_Effective_ChannelItem_For_Lastsong(curChannel - 1)="+last_Effective_ChannelItem_For_Lastsong(curChannel - 1));
				setChannelChecked(last_Effective_ChannelItem_For_Lastsong(curChannel - 1));
			}
		}else {
			if((checkedId - 1) < 0){
				Log.v(TAG, "111111111111 last_Effective_ChannelItem = "+last_Effective_ChannelItem());
				if(last_Effective_ChannelItem() > 48){
					setChannelChecked(last_Effective_ChannelItem() - 48);
				}else {
					setChannelChecked(last_Effective_ChannelItem());
				}
			}else {
				Log.v(TAG, "66666--->checkedId - 1 = "+(checkedId - 1));
				setChannelChecked(checkedId - 1);
			}
		}

	}

	/**
	 * 爱心Btn功能
	 */
	public void add_or_clear_Collect(){
		int empty ,sameFreq ;
		String curfreq = null;
		int OUTSCOPE = 0 ,OUTSCOPE_FLAG = 1 ;
		int START = 0,END = 0,END_EMPTY = 0;
		int focus_btn = radioService.getCurChannelId();//当前焦点Button对应的Type的Id：0~144;
		Log.v(TAG, "&&&&&&radioService.getChannelItem(focus_btn).freq = "+radioService.getChannelItem(focus_btn).freq + " focus_btn="+focus_btn);
		if(!radioService.getChannelItem(focus_btn).freq.equals("")){
			if(HEART_STATIC_FLAG == 0){
				//当全局变量为0时,说明当前焦点频道在收藏栏中不存在,点击爱心按钮为添加到收藏栏,且爱心变红
				if(radioService.getRadioType() == RadioService.RADIO_FM1){
					// make the current freq type int to String
					curfreq = Double.toString(radioService.getCurrentFreq()/100.0);
					focus_btn = FOCUS_BUTTON_ID;
					Log.v(TAG,"heart curfreq = "+curfreq+" CurrentFreq = "+radioService.getCurrentFreq());

					//if the freq's value outof the cur_Type. example: curfreq is 10390 ,but cur_Type is AM, 520~1710
					if(radioService.getCurrentFreq() < RadioService.FM_LOW_FREQ || radioService.getCurrentFreq() > RadioService.FM_HIGH_FREQ){
						OUTSCOPE = OUTSCOPE_FLAG;
					}
					START = 96; END = 144; END_EMPTY = 144 - 1;

				}else if(radioService.getRadioType() == RadioService.RADIO_AM){
					// make the current freq type int to String
					curfreq = Integer.toString(radioService.getCurrentFreq());
					focus_btn = FOCUS_BUTTON_ID + 48;
					Log.v(TAG,"heart curfreq = "+curfreq+" CurrentFreq = "+radioService.getCurrentFreq());
					//if the freq's value outof the cur_Type. example: curfreq is 10390 ,but cur_Type is AM, 520~1710
					if(radioService.getCurrentFreq() < RadioService.AM_LOW_FREQ || radioService.getCurrentFreq() > RadioService.AM_HIGH_FREQ){
						OUTSCOPE = OUTSCOPE_FLAG;
					}
					START = 96; END = 144;END_EMPTY = 144 - 1;

				}else if(radioService.getRadioType() == RadioService.RADIO_COLLECT){
					//when the type is RADIO_COLLECT ,the curfreq can be FM also can be AM too
					if(radioService.getCurrentFreq() < RadioService.FM_LOW_FREQ  || radioService.getCurrentFreq() > RadioService.FM_HIGH_FREQ){
						// make the current freq type int to String
						curfreq = Integer.toString(radioService.getCurrentFreq());
						focus_btn = FOCUS_BUTTON_ID + 96;
					}else if(radioService.getCurrentFreq() < RadioService.AM_LOW_FREQ || radioService.getCurrentFreq() > RadioService.AM_HIGH_FREQ){
						curfreq = Double.toString(radioService.getCurrentFreq()/100.0);
					}

					START = 96; END = 144;END_EMPTY = 144 - 1;
				}
				for (int i = START; i < END; i++) {
					if(DEBUG)Log.v(TAG,"("+i+")= "+radioService.getChannelItem(i).freq);
					if(OUTSCOPE == OUTSCOPE_FLAG){
						//if the freq's value outof the cur_Type. example: curfreq is 10390 ,but cur_Type is AM, 520~1710
						Toast.makeText(getApplicationContext(), R.string.invalid, Toast.LENGTH_SHORT).show();
						break;
					}else{
						if (radioService.getChannelItem(i).freq.equals("")) {
							//if freq is empty,save the i and assigned to empty
							empty = i;
							if(DEBUG)Log.v(TAG,"empty = " + empty);

							if(empty == END_EMPTY){
								//if emty in the end ,show a toast to tell user
								Toast.makeText(getApplicationContext(), R.string.nowhere, Toast.LENGTH_LONG).show();
							}

							//add the curfreq into the empty channel and refresh view
							ChannelItem item  ;
							item = radioService.getChannelItem(empty);

							/**************当前频率与焦点频率相同时,加入收藏栏的频率为焦点中文名
							 **************若不同时,则收藏栏显示频率值***********************/
							Log.v(TAG, "*******************CurrentFreq ="+radioService.getCurrentFreq()+" |item.freq="+radioService.getChannelItem(focus_btn).freq);
							if(!radioService.getChannelItem(focus_btn).freq.equals("")){
								if(radioService.getCurrentFreq() == Integer.parseInt(radioService.getChannelItem(focus_btn).freq.replaceAll("\\.", ""))*10
										||radioService.getCurrentFreq() == Integer.parseInt(radioService.getChannelItem(focus_btn).freq.replaceAll("\\.", ""))*1){
									item.freq = curfreq;
									item.name = radioService.getChannelItem(focus_btn).name;
									item.abridge = radioService.getChannelItem(focus_btn).abridge;//收藏栏中的频道,若在FM/AM中存在对应的中文名字,则把名称由频率值替换为中文名
									radioService.setChannelItem(empty,item);
								}else{
									item.freq = curfreq;
									item.name = "";
									item.abridge = "";
									radioService.setChannelItem(empty,item);
								}
								updateChannelList();
								updateFreqView();
								mButtonAddHeart.setBackground(getResources().getDrawable(R.drawable.collect_d));
								HEART_STATIC_FLAG = 1;
								Toast.makeText(getApplicationContext(), R.string.is_addHeart_toast, Toast.LENGTH_SHORT).show();
							}else {
								Toast.makeText(getApplicationContext(), R.string.not_effective_freq, Toast.LENGTH_SHORT).show();
							}

							/************************************************************/


							break;
						} else if(radioService.getChannelItem(i).freq.equals(curfreq)){
							//if curfreq == channel.freq, make a Toast
							sameFreq = i;
							Toast.makeText(getApplicationContext(), R.string.same_addHeart_toast, Toast.LENGTH_SHORT).show();
							if(DEBUG)Log.v(TAG,"sameFreq = " + sameFreq);
							break;
						}
					}
				}
			}else if (HEART_STATIC_FLAG == 1) {
				//全局变量为1,说明当前焦点频率在收藏栏中存在,再次点击爱心按钮,则变为取消收藏功能,且爱心变为空心
				int checkedId = 0;
				mButtonAddHeart.setBackground(getResources().getDrawable(R.drawable.collect_u));
				Toast.makeText(getApplicationContext(), R.string.clear_addHeart_toast, Toast.LENGTH_SHORT).show();
				HEART_STATIC_FLAG = 0;
				if (radioService.getRadioType() == RadioService.RADIO_FM1) {
					checkedId = FOCUS_BUTTON_ID;
				} else if (radioService.getRadioType() == RadioService.RADIO_COLLECT) {
					checkedId = FOCUS_BUTTON_ID + 96;
				} else if (radioService.getRadioType() == RadioService.RADIO_AM) {
					checkedId = FOCUS_BUTTON_ID + RadioService.RADIO_FM_COUNT;
				}
				ChannelItem item  ;
				item = radioService.getChannelItem(checkedId);
				for(int i=96; i<144; i++){
					if(radioService.getChannelItem(i).freq.equals(item.freq)){
						item = radioService.getChannelItem(i);
						item.freq = "";
						item.name = "";
						item.abridge = "";
						radioService.setChannelItem(i,item);
						updateChannelList();
						updateFreqView();
					}
				}
			}
		}else {
			Toast.makeText(getApplicationContext(), R.string.not_effective_freq, Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * @param curFreq
	 * 当前频率值与收藏栏的频率值作比较,若收藏栏存在当前频率值
	 * 则爱心Button变为红实心
	 */
	private void curFreq_Compare_To_Collect(int curFreq) {
		Log.v(TAG, "curFreq_Compare_To_Collect");
		//int curFreq = radioService.getCurrentFreq();
		Log.v(TAG, "curFreq_Compare_To_Collect-----curFreq="+curFreq);
		int collect_Freq = 0;
		mButtonAddHeart.setBackground(getResources().getDrawable(R.drawable.collect_u));
		HEART_STATIC_FLAG = 0;
		for(int i=96; i<144; i++){
			ChannelItem item  ;
			item = radioService.getChannelItem(i);
			if(item.freq.contains(".")){
				collect_Freq =Integer.parseInt(item.freq
						.replaceAll("\\.", "")) * 10;
			}else if(!item.freq.equals("")){
				collect_Freq = Integer.parseInt(item.freq);
			}

			if(collect_Freq == curFreq){
				HEART_STATIC_FLAG = 1;
				mButtonAddHeart.setBackground(getResources().getDrawable(R.drawable.collect_d));
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
		myIntentFilter.addAction("updateLayoutView");
		myIntentFilter.addAction("MediaStatus.BonovoRadio.Media_Broadcast_Close");
		return myIntentFilter;

	};

	private BroadcastReceiver myBroadcastReveiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
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
					float autoFreq = iCurfreq / 100f;
					if (mIdex >= CHANNEL_SIZE)
						mIdex = CHANNEL_SIZE - 1;
					item = radioService.getChannelItem(mIdex);
					item.freq = Float.toString(autoFreq);
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
				}else if (radioService.getRadioType() == RadioService.RADIO_COLLECT) {
					int nIdex;
					nIdex = mIdex + CHANNEL_SIZE*2;
					if (nIdex >= CHANNEL_SIZE * 3)
						nIdex = CHANNEL_SIZE * 3 - 1;
					if(radioService.getCurrentFreq() > RadioService.FM_LOW_FREQ && radioService.getCurrentFreq() < RadioService.FM_HIGH_FREQ){
						float autoFreq = iCurfreq / 100f;
						item = radioService.getChannelItem(nIdex);
						item.freq = Float.toString(autoFreq);
						item.abridge = "";
						item.name = "";
						radioService.setChannelItem(nIdex, item);
					}else if(radioService.getCurrentFreq() > RadioService.AM_LOW_FREQ && radioService.getCurrentFreq() < RadioService.AM_HIGH_FREQ){
						item = radioService.getChannelItem(nIdex);
						item.freq = Integer.toString(iCurfreq);
						item.abridge = "";
						item.name = "";
						radioService.setChannelItem(nIdex, item);
					}

					mHandler.sendEmptyMessage(UPDATE_CHANNEL_LIST);
					updateFreqView();
				}
				gone_Empty_ButtonView();
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
				if (radioService.getRadioType() == RadioInterface.RADIO_FM1
						|| radioService.getRadioType() == RadioInterface.RADIO_FM2) {
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
			}else if(intent.getAction().equals("Step-Search")){
				int stepCurfreq = intent.getIntExtra("step-curfreq", 0);
				curFreq_Compare_To_Collect(stepCurfreq);
			}else if(intent.getAction().equals("updateFreqView")){
				updateFreqView();
			}else if(intent.getAction().equals("updateLayoutView")){
                updateLayout(radioService.getLayout());
            }
			else if(intent.getAction().equals("MediaStatus.BonovoRadio.Media_Broadcast_Close"))
			{
				Log.v(TAG, "MediaStatus.BonovoRadio.Media_Broadcast_Close");
				AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
				ComponentName mRemoteControlClientReceiverComponent;
				mRemoteControlClientReceiverComponent = new ComponentName(
				                getPackageName(), MediaButtonIntentReceiver.class.getName());
				audioManager.unregisterMediaButtonEventReceiver(mRemoteControlClientReceiverComponent);
				radioService.stopService(new Intent("com.example.radio.RadioService"));

				finish();
			} else if (intent.getAction().equals("Radio.scanDelayUpdated")) {

			}
		}
	};

    public void updateLayout(int layout){
        if (layout == 1) {
            setContentView(R.layout.radio_player_layout_1);
			setupview();
        }
        else if (layout == 2) {
            setContentView(R.layout.radio_player_layout_2);
			setupview();
        }
		else if (layout == 3) {
			setContentView(R.layout.radio_player_layout_custom);
			setupview();

            topPanel = findViewById(R.id.topPanel);
            midPanel = findViewById(R.id.midPanel);
            botPanel = findViewById(R.id.bottomPanel);

            bmapTop = BitmapFactory.decodeResource(getResources(), R.drawable.top_bg_custom);
            bmapMid = BitmapFactory.decodeResource(getResources(), R.drawable.buttonbar_bg_custom);
            bmapBot = BitmapFactory.decodeResource(getResources(), R.drawable.bottom_bg_custom);

            topPanel.setBackground(radioService.changeColor(bmapTop));
            midPanel.setBackground(radioService.changeColor(bmapMid));
            botPanel.setBackground(radioService.changeColor(bmapBot));
		}
    }
}
