package com.bonovo.keyeditor;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class KeyEditorActivity extends Activity 
	implements OnClickListener, ServiceConnection {
	
	public static final int KEY_HOMEPAGE = 172; // 0 - KEY_HOMEPAGE
	public static final int KEY_BACK = 158; // 1 - KEY_BACK
	public static final int KEY_0 = 11; // 2 - KEY_0
	public static final int KEY_1 = 2; // 3 - KEY_1
	public static final int KEY_2 = 3; // 4 - KEY_2
	public static final int KEY_3 = 4; // 5 - KEY_3
	public static final int KEY_4 = 5; // 6 - KEY_4
	public static final int KEY_5 = 6; // 7 - KEY_5
	public static final int KEY_6 = 7; // 8 - KEY_6
	public static final int KEY_7 = 8; // 9 - KEY_7
	public static final int KEY_8 = 9; // 10 - KEY_8
	public static final int KEY_9 = 10; // 11 - KEY_9
	public static final int KEY_STAR = 522; // 12 - KEY_STAR
	public static final int KEY_POUND = 523; // 13 - KEY_POUND
	public static final int KEY_UP = 103; // 14 - KEY_UP
	public static final int KEY_DOWN = 108; // 15 - KEY_DOWN
	public static final int KEY_LEFT = 105; // 16 - KEY_LEFT
	public static final int KEY_RIGHT = 106; // 17 - KEY_RIGHT
	public static final int KEY_SELECT = 353; // 18 - KEY_SELECT
												// [KEY_BONOVO_DPAD_CENTER]
	public static final int KEY_VOLUMEUP = 115; // 19 - KEY_VOLUMEUP
	public static final int KEY_VOLUMEDOWN = 114; // 20 - KEY_VOLUMEDOWN
	public static final int KEY_POWER = 116; // 21 - KEY_POWER
	public static final int KEY_WWW = 150; // 22 - KEY_WWW
	public static final int KEY_MAIL = 155; // 23 - KEY_MAIL
	public static final int KEY_COMPOSE = 127; // 24 - KEY_COMPOSE (menu) ---
												// also changed to 139(KEY_MENU)
	public static final int KEY_SEARCH = 217; // 25 - KEY_SEARCH
	public static final int KEY_PLAYPAUSE = 164; // 26 - KEY_PLAYPAUSE
	public static final int KEY_STOP = 128; // 27 - KEY_STOP
	public static final int KEY_NEXTSONG = 163; // 28 - KEY_NEXTSONG
	public static final int KEY_PREVIOUSSONG = 165; // 29 - KEY_PREVIOUSSONG
	public static final int KEY_REWIND = 168; // 30 - KEY_REWIND
	public static final int KEY_FASTFORWARD = 208; // 31 - KEY_FASTFORWARD
	public static final int KEY_PAGEUP = 104; // 32 - KEY_PAGEUP
	public static final int KEY_PAGEDOWN = 109; // 33 - KEY_PAGEDOWN
	public static final int KEY_MUTE = 113; // 34 - KEY_MUTE
	public static final int KEY_CALENDAR = 397; // 35 - KEY_CALENDAR
	public static final int KEY_CONFIG = 171; // 36 - KEY_CONFIG (music)
	public static final int KEY_CALC = 140; // 37 - KEY_CALC (calculator)
	public static final int KEY_RADIO = 0x250; // 38 - KEY_RADIO
	public static final int KEY_BONOVO_UPDATETIME = 0x220;
	public static final int KEY_BONOVO_UPDATEBRIGHTNESS = 0x221;
	public static final int KEY_BONOVO_POWEROFF = 0x222;
	public static final int KEY_BONOVO_UPDATEVOLUME = 0x223;
	public static final int KEY_BONOVO_UPDATESYSVOL = 0x224;

	public static final int KEY_BONOVO_VOLUME_ADD = 0x225;
	public static final int KEY_BONOVO_VOLUME_SUB = 0x226;
	public static final int KEY_BONOVO_SLEEP = 0x227;
	public static final int KEY_BONOVO_WAKEUP = 0x228;
	public static final int KEY_BONOVO_NAVI = 0x229;
	public static final int KEY_BONOVO_SYSTEM_MUTE = 0x230;
	public static final int KEY_BONOVO_CUSTOM_IR_BUTTON = 0x231;
	public static final int KEY_BONOVO_EQ = 0x232;
	public static final int KEY_BONOVO_DVD = 0x233;
	public static final int KEY_BONOVO_3G = 0x234;
	public static final int KEY_BONOVO_RECODER = 0x235;
	public static final int KEY_BONOVO_CMMB_TV = 0x236;
	public static final int KEY_BONOVO_CMMB_RADIO = 0x237;
	public static final int KEY_BONOVO_CAMERA = 0x238;

	public static final int KEY_BONOVO_BT = 0x240;
	public static final int KEY_BONOVO_BT_ANSWER = 0x241;
	public static final int KEY_BONOVO_BT_HANG_UP = 0x242;
	public static final int KEY_BONOVO_BT_ANSWER_HANG = 0x243;
	public static final int KEY_BONOVO_BT_SWITCH_AUDIO = 0x244;

	public static final int KEY_BONOVO_RADIO = 0x250;
	public static final int KEY_BONOVO_RADIO_TURNUP = 0x251;
	public static final int KEY_BONOVO_RADIO_TURNDOWN = 0x252;
	public static final int KEY_BONOVO_SWITCH_FMAM = 0x253;

	public static final int KEY_BONOVO_MIN_VALUE = KEY_BONOVO_UPDATETIME;
	public static final int KEY_BONOVO_MAX_VALUE = KEY_BONOVO_SWITCH_FMAM;
	

	private static Context mContext;
	private TextView mTvPanel = null;
	private TextView mTvIr = null;
	private TextView mTvExt = null;
	
	private static boolean DEBUG = false;
	private static final String TAG = "KeyEditorActivity";
	private static final int TAB_PANEL = 0;
	private static final int TAB_IR = 1;
	private static final int TAB_EXTERNAL = 2;
	private static final int DEF_OFFSET = 40;
	private static final int MAX_OFFSET = 200;
	private static final int MIN_OFFSET = 20;
	private int mCurrTab = -1;
	
	public static final int[] mFuncCodeArr = {
		172, 158,  11,   2,   3,   4,   5,   6,   7,   8,
	      9,  10, 522, 523, 103, 108, 105, 106, 353, 115,  // DPAD_CENTER from 547 to 353
	    114, 116, 150, 155, 127, 217, 164, 128, 163, 165,
	    168, 208, 104, 109, 113, 397, 171, 140, KEY_RADIO,
	    KEY_BONOVO_NAVI, // 39 - KEY_BONOVO_NAVI
		KEY_BONOVO_DVD,               // 40  0x233
		KEY_BONOVO_3G,                // 41  0x234
		KEY_BONOVO_CAMERA,            // 42  0x238
		KEY_BONOVO_BT,                // 43  0x240
		KEY_BONOVO_BT_ANSWER,         // 44  0x241
		KEY_BONOVO_BT_HANG_UP,        // 45  0x242
		KEY_BONOVO_BT_ANSWER_HANG,    // 46  0x243
		KEY_BONOVO_BT_SWITCH_AUDIO,   // 47  0x244
		KEY_BONOVO_RADIO_TURNUP,      // 48  0x251
		KEY_BONOVO_RADIO_TURNDOWN,    // 49  0x252
		KEY_BONOVO_SWITCH_FMAM        // 50  0x253
	};
	private static final int NUM_PAIRS = mFuncCodeArr.length;
	private static Button[] mBtns;
	private boolean mIsBtnPress = false;
	private Button mPressBtn = null;
	private String mPressBtnText = null;
	private KeyService mService = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_key_editor);
		try {
			getWindow().addFlags(WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null));
		}catch (NoSuchFieldException e) {
			// Ignore since this field won't exist in most versions of Android
		}catch (IllegalAccessException e) {
			Log.w("feelyou.info", "Could not access FLAG_NEEDS_MENU_KEY in addLegacyOverflowButton()", e);
		}

		mContext = this;
		
		mTvPanel = (TextView)findViewById(R.id.tvPanel);
		mTvIr = (TextView)findViewById(R.id.tvIr);
		mTvExt = (TextView)findViewById(R.id.tvExt);
		mTvPanel.setOnClickListener(this);
		mTvIr.setOnClickListener(this);
		mTvExt.setOnClickListener(this);
		setCurrTab(TAB_EXTERNAL);
		
		Intent intent = new Intent(this, KeyService.class);
		bindService(intent, this, Context.BIND_AUTO_CREATE);
		setButtonOnClick();
    	if(DEBUG) Log.d(TAG, "=========onCreate=============");
		this.registerReceiver(mReceiver, getIntentFilter());
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mReceiver);
		unbindService(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		cancelCustomMode();
		if(mService != null){
			mService.exportDefaultFile();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.key_editor, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);
		menu.clear();
		switch(mCurrTab){
		case TAB_PANEL:
			menu.add(Menu.NONE, Menu.FIRST+1, 2, R.string.menu_set_accuracy).setIcon(android.R.drawable.ic_menu_compass);
			menu.add(Menu.NONE, Menu.FIRST+2, 3, R.string.clean_config).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			menu.add(Menu.NONE, Menu.FIRST+3, 1, R.string.read_accuracy).setIcon(android.R.drawable.ic_menu_view);
			break;
		case TAB_IR:
			menu.add(Menu.NONE, Menu.FIRST+2, 3, R.string.clean_config).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			break;
		case TAB_EXTERNAL:
			menu.add(Menu.NONE, Menu.FIRST+1, 2, R.string.menu_set_accuracy).setIcon(android.R.drawable.ic_menu_compass);
			menu.add(Menu.NONE, Menu.FIRST+2, 3, R.string.clean_config).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			menu.add(Menu.NONE, Menu.FIRST+3, 1, R.string.read_accuracy).setIcon(android.R.drawable.ic_menu_view);
			break;
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		MappingTable table = null;
		if(mService == null){
			Log.e(TAG, "KeyService is null.");
			return false;
		}
		switch(mCurrTab){
		case TAB_PANEL:
			table = mService.mPanelTable;
			break;
		case TAB_IR:
			table = mService.mIrTable;
			break;
		case TAB_EXTERNAL:
			table = mService.mExtTable;
			break;
		default:
			Log.e(TAG, "The current tab is invalid.");
			return false;
		}
		
		switch(item.getItemId()){
		case Menu.FIRST+1:
			if(mCurrTab == TAB_IR){
				Toast.makeText(mContext, R.string.error_set_ir_accuracy, Toast.LENGTH_SHORT).show();
			}else{
				createSettingDialog(table.getTableOffset());
			}
			break;
		case Menu.FIRST+2:
			final MappingTable currTable = table;
			new Builder(mContext).setTitle(R.string.clear_btns_title)
				.setMessage(R.string.clear_btns_info)
				.setPositiveButton(R.string.new_dialog_ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if(currTable != null){
							mService.clearKeys(currTable.getTableType());
							updateView(currTable,mCurrTab);
						}
					}
				})
				.setNegativeButton(R.string.new_dialog_cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				}).create().show();
			break;
		case Menu.FIRST+3:
			String infoAccuracy = getResources().getString(R.string.accuracy_info) + " " + mService.readOffset(table.getTableType());
			Toast.makeText(mContext, infoAccuracy, Toast.LENGTH_SHORT).show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		super.onOptionsMenuClosed(menu);
	}

	private AlertDialog createSettingDialog(int accuracy){
		View v = View.inflate(mContext, R.layout.settings, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(v);
		AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getWindow().setGravity(Gravity.CENTER);
		
		final SeekBar seekbarAccuracy = (SeekBar)dialog.getWindow().findViewById(R.id.seekBarAccuracy);
		if(mCurrTab == TAB_EXTERNAL||mCurrTab == TAB_PANEL){
			seekbarAccuracy.setMax(MAX_OFFSET - MIN_OFFSET);
			seekbarAccuracy.setProgress(accuracy - MIN_OFFSET);
		}else{
			seekbarAccuracy.setMax(MAX_OFFSET);
			seekbarAccuracy.setProgress(accuracy);
		}
		final TextView tvAccuracy = (TextView)dialog.getWindow().findViewById(R.id.tvAccuracy);
		tvAccuracy.setText(String.valueOf(accuracy));
		
		seekbarAccuracy.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				if(mService == null) return;
				switch(mCurrTab){
				case TAB_PANEL:
					mService.mPanelTable.setTableOffset(seekBar.getProgress()+ MIN_OFFSET);
					mService.writeOffset(TAB_PANEL, seekBar.getProgress()+ MIN_OFFSET);
					break;
				case TAB_IR:
					//mService.mIrTable.setTableOffset(seekBar.getProgress());
					//mService.writeOffset(TAB_IR, seekBar.getProgress());
					break;
				case TAB_EXTERNAL:
					mService.mExtTable.setTableOffset(seekBar.getProgress() + MIN_OFFSET);
					mService.writeOffset(TAB_EXTERNAL, seekBar.getProgress() + MIN_OFFSET);
					break;
				default:
					Log.e(TAG, "Current Tab is invalid.");
					return;
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if(mCurrTab == TAB_EXTERNAL||mCurrTab == TAB_PANEL){
					tvAccuracy.setText(String.valueOf(progress + MIN_OFFSET));
				}else{
					tvAccuracy.setText(String.valueOf(progress));
				}
			}
		});
		return dialog;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.tvPanel:
			setCurrTab(TAB_PANEL);
			break;
		case R.id.tvIr:
			setCurrTab(TAB_IR);
			break;
		case R.id.tvExt:
			setCurrTab(TAB_EXTERNAL);
			break;
		default:
			
			break;
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		mService = ((KeyService.ServerBinder)service).getService();
		if(mService == null){
			return;
		}
		int[][] initKeyMap = new int[NUM_PAIRS][2];
		for(int i=0; i<NUM_PAIRS; i++){
			initKeyMap[i][0] = mFuncCodeArr[i];
			initKeyMap[i][1] = -1;
		}
		if(mService.mPanelTable == null){
			mService.mPanelTable = new MappingTable(mService.KEY_TYPE_PANEL, NUM_PAIRS, 0, initKeyMap, DEF_OFFSET);
			mService.writeConfig(mService.mPanelTable);
		}
		if(mService.mIrTable == null){
			mService.mIrTable = new MappingTable(mService.KEY_TYPE_IR, NUM_PAIRS, 0, initKeyMap, 0);
			mService.writeConfig(mService.mIrTable);
		}
		if(mService.mExtTable == null){
			mService.mExtTable = new MappingTable(mService.KEY_TYPE_EXTERNAL, NUM_PAIRS, 0, initKeyMap, DEF_OFFSET);
			mService.writeConfig(mService.mExtTable);
		}
		
		switch(mCurrTab){
		case TAB_PANEL:
			updateView(mService.mPanelTable,mCurrTab);
			break;
		case TAB_IR:
			updateView(mService.mIrTable,mCurrTab);
			break;
		case TAB_EXTERNAL:
			updateView(mService.mExtTable,mCurrTab);
			break;
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		mService = null;
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(mService == null){
				if(DEBUG) Log.d(TAG, "============ onReceive  service in null.");
				return;
			}
			if (intent.getAction().equals("android.intent.action.BONOVO_CUSTOM_IR_BUTTON")) {
				if(DEBUG) Log.d(TAG, "get BONOVO_CUSTOM_IR_BUTTON event.");
				
				OriginalKey originalKey = null;
				if(!mIsBtnPress){
					mService.setMode(true);
				}
				if ((originalKey = mService.readOriginalKey()) == null) {
					Log.e(TAG, "can't get data from driver. nativeReadKey failed.");
					mService.setMode(false);
					return;
				}
				if(!mIsBtnPress){
					mService.setMode(false);
				}
				
				MappingTable table = null;
				switch(mCurrTab){
				case TAB_PANEL:
					table = mService.mPanelTable;
					break;
				case TAB_IR:
					table = mService.mIrTable;
					break;
				case TAB_EXTERNAL:
					table = mService.mExtTable;
					break;
				default:
					Log.e(TAG, "Current Tab is invalid.");
					return;
				}
				if(isSameType(originalKey.getScanCode())){
					if(mIsBtnPress /*&& (table.getTableId() == originalKey.getAddrCode())*/){
						mHandler.removeMessages(0);
						int idxInTable = table.searchByPhysCode(originalKey.getScanCode());
						int idxInArray = getIndexInArray(mPressBtn);
						if(idxInArray < 0){
							Log.e(TAG, "Can't find the button which had been pressed.");
							return;
						}
						if((idxInTable >= 0) && (idxInTable != idxInArray)){
							table.setPhysCode(idxInTable, -1);
							int err = 0;
							if((err = mService.writeKey(table, idxInTable)) < 0){
								Log.e(TAG, "111111 write key to kernel failed. error:" + err);
							} else {
								mBtns[idxInTable].setText(R.string.undef);
							}
						}
						
						table.setPhysCode(idxInArray, originalKey.getScanCode());
						if(mService.writeKey(table, idxInArray) < 0){
							Log.e(TAG, "222222 write key to kernel failed.");
							mPressBtn.setText(mPressBtnText);
						} else {
							mPressBtn.setText(Integer.toString(table.getPhysCode(idxInArray)));
						}
						mIsBtnPress = false;
						mPressBtn = null;
						mService.setMode(false);
					}else{
						if(DEBUG) Log.d(TAG, "==== address code is different. table's address Code:" + table.getTableId()
								+ "  original key's address code:" + originalKey.getAddrCode());
					}
				}else{
					if(DEBUG) Log.d(TAG, "=== type is different. table type:" + table.getTableType() + "    curr type:" + mCurrTab);
					if(DEBUG) Log.d(TAG, "==== originalKey.getAddrCode:" + originalKey.getAddrCode() 
							+ "   originalKey.getScanCode:" + originalKey.getScanCode());
				}
			}
		}
	};

	private IntentFilter getIntentFilter() {
		IntentFilter myIntentFilter = new IntentFilter(
				"android.intent.action.BONOVO_CUSTOM_IR_BUTTON");
		return myIntentFilter;
	}
	
	private View.OnClickListener mBtnOnClkLstner = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (mIsBtnPress) {
				if (mPressBtn != null) {
					mPressBtn.setText(mPressBtnText);
					mHandler.removeMessages(0);

					if (mPressBtn.equals((Button) v)) {
						mIsBtnPress = false;
						mPressBtn = null;
						if (mService.setMode(false) < 0) {
							Log.e(TAG, "In onClickListener set mode false failed.");
						}
					} else {
						mPressBtn = (Button) v;
						mPressBtnText = mPressBtn.getText().toString();
						mPressBtn.setText(R.string.press_btn);						
						Message msg = mHandler.obtainMessage(0, mPressBtn);
						mHandler.sendMessageDelayed(msg, 10000);
					}
				} else {
					Button tempBtn = (Button)v;
					mPressBtnText = tempBtn.getText().toString();
				}
			} else {
				int err = -1;
				mPressBtn = (Button) v;
				mPressBtnText = mPressBtn.getText().toString();
				err = mService.setMode(true);
				if (err == 1) {
					// synchronized (mPressBtn) {
					mPressBtn.setText(R.string.press_btn);
					mIsBtnPress = true;
					// }
					Message msg = mHandler.obtainMessage(0, mPressBtn);
					mHandler.sendMessageDelayed(msg, 10000);
				} else {
					Log.e(TAG, "Set Custom Mode failed.");
				}
			}
		}
	};
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Button btn = (Button) msg.obj;
			if(btn == null){
				return;
			}
			btn.setText(mPressBtnText);

			mIsBtnPress = false;
			mPressBtn = null;
			if (mService.setMode(false) < 0) {
				Log.e(TAG, "In mHandler set mode false failed.");
			}
		}
	};
	
	public boolean isSameType(int keyCode){
		boolean ret = false;
		
		switch((keyCode>>16)&0x00FF){
		case 0:
		case 1:
		case 2:
			if(mCurrTab == TAB_PANEL)
				ret = true;
			break;
		case 3:
			if(mCurrTab == TAB_IR)
				ret = true;
			break;
		case 4:
		case 5:
		case 6:
			if(mCurrTab == TAB_EXTERNAL)
				ret = true;
			break;
		default:
			break;
		}
		return ret;
	}
	
	private void setCurrTab(int tab){
		if(mCurrTab == tab){
			return;
		}
		mCurrTab = tab;
		switch(tab){
		case TAB_PANEL:
			mTvPanel.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.panel_select), null, null);
			mTvPanel.setTextColor(Color.rgb(25, 25, 235));
			mTvIr.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ir_normal), null, null);
			mTvIr.setTextColor(Color.rgb(0, 0, 0));
			mTvExt.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.external_normal), null, null);
			mTvExt.setTextColor(Color.rgb(0, 0, 0));
			if(mBtns != null){
				mBtns[21].setEnabled(true);
			}
			if(mService != null){
				updateView(mService.mPanelTable,tab);
			}
			break;
		case TAB_IR:
			mTvPanel.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.panel_normal), null, null);
			mTvPanel.setTextColor(Color.rgb(0, 0, 0));
			mTvIr.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ir_select), null, null);
			mTvIr.setTextColor(Color.rgb(25, 25, 235));
			mTvExt.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.external_normal), null, null);
			mTvExt.setTextColor(Color.rgb(0, 0, 0));
			if(mBtns != null){
				mBtns[21].setEnabled(false);
			}
			if(mService != null){
				updateView(mService.mIrTable,tab);
			}
			break;
		case TAB_EXTERNAL:
			mTvPanel.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.panel_normal), null, null);
			mTvPanel.setTextColor(Color.rgb(0, 0, 0));
			mTvIr.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ir_normal), null, null);
			mTvIr.setTextColor(Color.rgb(0, 0, 0));
			mTvExt.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.external_select), null, null);
			mTvExt.setTextColor(Color.rgb(25, 25, 235));
			if(mBtns != null){
				mBtns[21].setEnabled(false);
			}
			if(mService != null){
				updateView(mService.mExtTable,tab);
			}
			break;
		default:
			break;
		}
		
		cancelCustomMode();
	}
	private void cancelCustomMode(){
		if(mIsBtnPress){
			mHandler.removeMessages(0);
			Message msg = mHandler.obtainMessage(0, mPressBtn);
			mHandler.sendMessage(msg);
		}
	}
	
	private void setButtonOnClick(){
		mBtns = new Button[NUM_PAIRS];
		mBtns[0] = (Button)findViewById(R.id.btnHome); // 172,
		mBtns[1] = (Button)findViewById(R.id.btnBack); // 158,
		mBtns[2] = (Button)findViewById(R.id.btnKey0); // 11,
		mBtns[3] = (Button)findViewById(R.id.btnKey1); // 2,
		mBtns[4] = (Button)findViewById(R.id.btnKey2); // 3,
		mBtns[5] = (Button)findViewById(R.id.btnKey3); // 4,
		mBtns[6] = (Button)findViewById(R.id.btnKey4); // 5,
		mBtns[7] = (Button)findViewById(R.id.btnKey5); // 6, 
		mBtns[8] = (Button)findViewById(R.id.btnKey6); // 7,
		mBtns[9] = (Button)findViewById(R.id.btnKey7); // 8,
		mBtns[10] = (Button)findViewById(R.id.btnKey8); // 9,
		mBtns[11] = (Button)findViewById(R.id.btnKey9); // 10,
		mBtns[12] = (Button)findViewById(R.id.btnKeyStar); // 522,
		mBtns[13] = (Button)findViewById(R.id.btnKeyPound); // 523
		mBtns[14] = (Button)findViewById(R.id.btnKeyUp); // 103
		mBtns[15] = (Button)findViewById(R.id.btnKeyDown); // 108
		mBtns[16] = (Button)findViewById(R.id.btnKeyLeft); // 105
		mBtns[17] = (Button)findViewById(R.id.btnKeyRight); // 106
		mBtns[18] = (Button)findViewById(R.id.btnKeyEnter); // 353
		mBtns[19] = (Button)findViewById(R.id.btnVolumeUp); // 115
		mBtns[20] = (Button)findViewById(R.id.btnVolumeDown); // 114
		mBtns[21] = (Button)findViewById(R.id.btnPower); // 116
		mBtns[22] = (Button)findViewById(R.id.btnKeyExplorer); // 150
		mBtns[23] = (Button)findViewById(R.id.btnKeyEnvelope); // 155
		mBtns[24] = (Button)findViewById(R.id.btnMenu); // 127
		mBtns[25] = (Button)findViewById(R.id.btnSearch); // 217
		mBtns[26] = (Button)findViewById(R.id.btnMediaPlayPause); // 164
		mBtns[27] = (Button)findViewById(R.id.btnMediaStop); // 128
		mBtns[28] = (Button)findViewById(R.id.btnMediaNext); // 163
		mBtns[29] = (Button)findViewById(R.id.btnMediaPrev); // 165
		mBtns[30] = (Button)findViewById(R.id.btnMediaRewind); // 168
		mBtns[31] = (Button)findViewById(R.id.btnMediaFastForward); // 208
		mBtns[32] = (Button)findViewById(R.id.btnPageUp); // 104
		mBtns[33] = (Button)findViewById(R.id.btnPageDown); // 109
		mBtns[34] = (Button)findViewById(R.id.btnMute); // 113
		mBtns[35] = (Button)findViewById(R.id.btnKeyCalendar); //397
		mBtns[36] = (Button)findViewById(R.id.btnKeyMusic); // 171
		mBtns[37] = (Button)findViewById(R.id.btnKeyCalculator); // 140
		mBtns[38] = (Button)findViewById(R.id.btnKeyRadio); // RADIO
		mBtns[39] = (Button)findViewById(R.id.btnKeyNavi); // NAVI
		mBtns[40] = (Button)findViewById(R.id.btnDvd); // KEY_BONOVO_DVD
		mBtns[41] = (Button)findViewById(R.id.btn3G);  // KEY_BONOVO_3G
		mBtns[42] = (Button)findViewById(R.id.btnCamera); // KEY_BONOVO_CAMERA
		mBtns[43] = (Button)findViewById(R.id.btnBt); // KEY_BONOVO_BT
		mBtns[44] = (Button)findViewById(R.id.btnBtAnswer); // KEY_BONOVO_BT_ANSWER
		mBtns[45] = (Button)findViewById(R.id.btnBtHangUp); // KEY_BONOVO_BT_HANG_UP
		mBtns[46] = (Button)findViewById(R.id.btnBtAnswerHang); // KEY_BONOVO_BT_ANSWER_HANG
		mBtns[47] = (Button)findViewById(R.id.btnBtSwitchAudio); // KEY_BONOVO_BT_SWITCH_AUDIO
		mBtns[48] = (Button)findViewById(R.id.btnRadioTurnUp); // KEY_BONOVO_RADIO_TURNUP
		mBtns[49] = (Button)findViewById(R.id.btnRadioTurnDown); // KEY_BONOVO_RADIO_TURNDOWN
		mBtns[50] = (Button)findViewById(R.id.btnRadioFmAm); // KEY_BONOVO_SWITCH_FMAM

		for(int i=0; i < NUM_PAIRS; i++){
			mBtns[i].setOnClickListener(mBtnOnClkLstner);
			if(i==21){
				mBtns[21].setEnabled(false);
			}
		}
	}
	
	private int getIndexInArray(View v){
		int idx = -1;
		for(int i=0; i < NUM_PAIRS; i++){
			if(mBtns[i].equals(v)){
				idx = i;
				break;
			}
		}
		return idx;
	}
	
	private void updateView(MappingTable table, int tab){
		if(table == null){
			return;
		}
		
		if(DEBUG) Log.d(TAG, "====== type:" + table.getTableType());
		for(int i=0; i<NUM_PAIRS; i++){
			if(DEBUG) Log.d(TAG, "Key[" + i + "]  FuncCode:" + table.getFuncCode(i) + "  PhysCode:" + table.getPhysCode(i));
			if(table.getPhysCode(i) == -1){
				mBtns[i].setText(R.string.undef);
				if(i==21){
					switch(tab){
					case TAB_PANEL:
						
						break;
					case TAB_IR:
						mBtns[21].setText(R.string.dose_not_support);
						break;
					case TAB_EXTERNAL:
						mBtns[21].setText(R.string.dose_not_support);
						break;
					}
				}
				
			}else{
				mBtns[i].setText(Integer.toString(table.getPhysCode(i)));
			}
		}
	}
}
