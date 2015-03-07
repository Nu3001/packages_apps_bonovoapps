package com.bonovo.bluetooth;

import com.bonovo.bluetooth.BonovoBlueToothService.BonovoBlueToothData;
import com.bonovo.bluetooth.BonovoBlueToothService.AudioLevel;
import com.bonovo.bluetooth.BonovoBlueToothReceiver;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.media.AudioManager;

public class BluetoothSettings extends Activity implements View.OnClickListener, View.OnLongClickListener{

	private final static String TAG = "BluetoothSettings";
	private final static boolean DEBUG = false;
    private boolean mIsSyncPhoneBook = false;
	private ViewStub mStubBtSettings = null;
	private ViewStub mStubBtMusic = null;
	private Button mBtnPhone = null;
	private Button mBtnMusic = null;
	private Button mBtnSettings = null;
	private Button mBtnSyncContacts = null;
	private ImageButton mIgeBtnVolUp = null;
	private ImageButton mIgeBtnVolDown = null;
	
	//private Button mBtnBtPower = null; 	//0718 删除
	private TextView mTvBtNameInfo = null;
	private TextView mTvBtName = null;
	private TextView mTvBtPinInfo = null;
	private TextView mTvBtPin = null;
	private TextView mTvBtStatus = null;
	private TextView mTvBtHFPStatus = null;
	private ImageView mIgeBtStatus = null;
	private TextView mBtnName = null;       //0825
	private TextView mBtnMusicName = null;  //0825
	
	private Button mBtnMusicPre = null;
	private Button mBtnMusicPlay = null;
	private Button mBtnMusicPause = null;
	private Button mBtnMusicStop = null;
	private Button mBtnMusicNext = null;
	private MySwitch mSwBtPower = null;      //0718 新加
	private MySwitch mSwMusic = null;	 //0820 修改
	private Context mContext = null;
	
	private static BonovoBlueToothService mBtService = null;
	private ProgressDialog mBluetoothContactsDialog;
	private static final int DIALOG_SYNC_CONTACTS = 1;
	
	private static final int MSG_SYNC_CONTACTS_COMPLETE = 1;
	private static final int MSG_UPDATA_BT_NAME = 2; // update name message id
	private static final int MSG_READ_BT_NAME = 3; // update name message id
	private static final int MSG_UPDATA_BT_PIN = 4;  // update pin message id
	private static final int MSG_READ_BT_PIN = 5;  // update pin message id
	private int mReadBtNameTime = 0;
	private int mReadBtPinTime = 0;
	private final static int MAX_READ_TIME = 5;
	private final static int DELAY_TIME_READ = 3 * 1000;
	private static boolean mDown = false;//keyEvent flag
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);
		mContext = this;
		
		mStubBtMusic = (ViewStub)findViewById(R.id.stubMusic);
		mStubBtMusic.inflate();
		mStubBtMusic.setVisibility(View.GONE);
		
		mStubBtSettings = (ViewStub)findViewById(R.id.stubSettings);
		mStubBtSettings.inflate();
		mStubBtSettings.setVisibility(View.VISIBLE);
		
		// bottom buttons
		mBtnPhone = (Button)findViewById(R.id.btnPhone);
		mBtnMusic = (Button)findViewById(R.id.btnMusic);
		mBtnSettings = (Button)findViewById(R.id.btnSettings);
		mBtnSyncContacts = (Button)findViewById(R.id.btnSyncContacts);
		mIgeBtnVolUp = (ImageButton)findViewById(R.id.btnVolAdd);
		mIgeBtnVolDown = (ImageButton)findViewById(R.id.btnVolDown);
		
		mBtnPhone.setOnClickListener(this);
		mBtnMusic.setOnClickListener(this);
		mBtnSettings.setOnClickListener(this);
		mBtnSyncContacts.setOnClickListener(this);
		mIgeBtnVolUp.setOnClickListener(this);
		mIgeBtnVolDown.setOnClickListener(this);
		
		//  bt settings's module
		//	mBtnBtPower = (Button)findViewById(R.id.btnBluetoothStatus);
		//	mBtnBtPower.setOnClickListener(this);
		mTvBtName = (TextView)findViewById(R.id.textView1);
		mTvBtNameInfo = (TextView)findViewById(R.id.textView2);
		mTvBtPin = (TextView)findViewById(R.id.textView3);
		mTvBtPinInfo = (TextView)findViewById(R.id.textView4);
		mTvBtStatus = (TextView)findViewById(R.id.textViewBlueToothStatus);
		mTvBtHFPStatus = (TextView)findViewById(R.id.textViewPhoneLinkStatus);
		mBtnName = (TextView)findViewById(R.id.textView5);      //0825
		mBtnMusicName = (TextView)findViewById(R.id.textView6);	//0825	
		
		mTvBtName.setOnLongClickListener(this);
		mTvBtNameInfo.setOnLongClickListener(this);
		mTvBtPin.setOnLongClickListener(this);
		mTvBtPinInfo.setOnLongClickListener(this);
		
		mIgeBtStatus = (ImageView)findViewById(R.id.imageViewBlueToothStatus);
		
		//bt music's module
		mBtnMusicPre = (Button)findViewById(R.id.buttonBluetoothMusicPre);
		mBtnMusicPlay = (Button)findViewById(R.id.buttonBluetoothMusicPlay);
		mBtnMusicPause = (Button)findViewById(R.id.buttonBluetoothMusicPause);
		mBtnMusicStop = (Button)findViewById(R.id.buttonBluetoothMusicStop);
		mBtnMusicNext = (Button)findViewById(R.id.buttonBluetoothMusicNext);
		
		mBtnMusicPre.setOnClickListener(this);
		mBtnMusicPlay.setOnClickListener(this);
		mBtnMusicPause.setOnClickListener(this);
		mBtnMusicStop.setOnClickListener(this);
		mBtnMusicNext.setOnClickListener(this);
		
		
		//-------------------------------------------------------------------------------------------------------
		//下面的代码是为了保持蓝牙界面风格的一致性而设计添加的；
		//将原来用Button表示的蓝牙开关改为用Switch表示；
		//Switch是一个可以在两种状态切换的开关控件；
		//通过实现CompoundButton.OnCheckedChangeListener接口，并实现其内部类的onCheckedChanged来监听状态变化；
		//然后再进行判断；
		// 1.当switch处于选中状态时，只有蓝牙开关状态mTvBtStatus不可见，其他均设置为可见；
		// 2.当switch处于未选中状态时，只有蓝牙开关状态mTvBtStatus可见，其他均设置为不可见。
		// 3.修改完善官方Switch控件（详见MySwitch内的代码）。增加了2项小功能：
		// 1）支持用Track背景图片的方式代替Texton Textoff等文字方式表现开关状态
		// 2）支持调整控制Switch的高度
		//  ——swxia. Date:20140825.
		// ------------------------------------------------------------------------------------------------------  
		
		mSwBtPower = (MySwitch)findViewById(R.id.swBtStatus);	
		mSwBtPower.setChecked(false);
		mSwBtPower.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				if(mBtService == null)
					return;

                if(mBtService.getBtSwitchStatus() != isChecked){
                    mBtService.setBtSwitchStatus(isChecked);
                }
				boolean powerStatus = mBtService.getBtSwitchStatus();
				boolean hfpStatus = mBtService.getBtHFPStatus();
				//	mSwBtPower.setText(powerStatus ? R.string.setting_button_bluetooth_status_closed : R.string.setting_button_bluetooth_status_open);						
				mTvBtStatus.setText(powerStatus ? R.string.bluetooth_status_opened : R.string.bluetooth_status_closed);
				mIgeBtStatus.setImageResource(powerStatus ? R.drawable.setting_bluetooth_opened : R.drawable.setting_bluetooth_close);
				mTvBtHFPStatus.setText(hfpStatus ? R.string.phone_link_status_opened : R.string.phone_link_status_closed);
				if(isChecked){
					//选中时所做的操作
					mTvBtName.setVisibility(View.VISIBLE);
					mTvBtPin.setVisibility(View.VISIBLE);
					mTvBtNameInfo.setText(mBtService.getBtName());
					mTvBtPinInfo.setText(mBtService.getBtPinCode());
					mTvBtNameInfo.setVisibility(View.VISIBLE);
					mTvBtPinInfo.setVisibility(View.VISIBLE);
					mSwMusic.setVisibility(View.VISIBLE);					
					mTvBtStatus.setVisibility(View.GONE);		//0722
					mTvBtHFPStatus.setVisibility(View.VISIBLE);//0722
					mBtnMusicName.setVisibility(View.VISIBLE);//0825
					
				}else{
					//未选中时所做的操作
					mTvBtName.setVisibility(View.GONE);
					mTvBtPin.setVisibility(View.GONE);
					mTvBtNameInfo.setVisibility(View.GONE);
					mTvBtPinInfo.setVisibility(View.GONE);
					mSwMusic.setVisibility(View.GONE);
					mTvBtStatus.setVisibility(View.VISIBLE);//0722
					mTvBtHFPStatus.setVisibility(View.GONE);//0722
					mBtnMusicName.setVisibility(View.GONE);//0825
				}
			}
		});
		
		
		mSwMusic = (MySwitch)findViewById(R.id.swBtMusic);
	    mSwMusic.setChecked(false);
        mSwMusic.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			
					
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				if(isChecked){
					mBtnMusic.setVisibility(View.VISIBLE);
					if(mBtService != null){
						mBtService.setMusicServiceEnable(true);
					}
				}else{
					mBtnMusic.setVisibility(View.GONE);
					if(mBtService != null){
						mBtService.setMusicServiceEnable(false);
					}
				}
			}
		});
		
		registerReceiver(mReceiver, getIntentFilter());

		Intent intent = new Intent();
		intent.setClassName("com.bonovo.bluetooth", "com.bonovo.bluetooth.BonovoBlueToothService");
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Intent intent = new Intent();
		intent.setClassName("com.bonovo.bluetooth", "com.bonovo.bluetooth.BonovoBlueToothService");
		unbindService(mServiceConnection);
		unregisterReceiver(mReceiver);
		AudioManager audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
		//娉ㄥ唽鎺ユ敹鐨凴eceiver
		ComponentName mRemoteControlClientReceiverComponent;
		mRemoteControlClientReceiverComponent = new ComponentName(
		                getPackageName(), BonovoBlueToothReceiver.class.getName());
		audioManager.unregisterMediaButtonEventReceiver(mRemoteControlClientReceiverComponent);
	}
	
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		AudioManager audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
		ComponentName mRemoteControlClientReceiverComponent;
		mRemoteControlClientReceiverComponent = new ComponentName(
				getPackageName(), BonovoBlueToothReceiver.class.getName());
		audioManager.registerMediaButtonEventReceiver(mRemoteControlClientReceiverComponent);
	}
	
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			mBtService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			mBtService = ((BonovoBlueToothService.ServiceBinder)service).getService();
			mSwMusic.setChecked(mBtService.getMusicServiceEnable());
			boolean powerStatus = mBtService.getBtSwitchStatus();
			boolean hfpStatus = mBtService.getBtHFPStatus();
            mSwBtPower.setChecked(powerStatus);
			//	mBtnBtPower.setText(powerStatus ? R.string.setting_button_bluetooth_status_closed : R.string.setting_button_bluetooth_status_open);
			mTvBtStatus.setText(powerStatus ? R.string.bluetooth_status_opened : R.string.bluetooth_status_closed);
			mIgeBtStatus.setImageResource(powerStatus ? R.drawable.setting_bluetooth_opened : R.drawable.setting_bluetooth_close);
			mTvBtHFPStatus.setText(hfpStatus ? R.string.phone_link_status_opened : R.string.phone_link_status_closed);
			mTvBtNameInfo.setText(mBtService.getBtName());
			mTvBtPinInfo.setText(mBtService.getBtPinCode());
			
			boolean musicStatus = mBtService.getMusicStatus();
			mBtnMusicPlay.setVisibility(musicStatus ? View.INVISIBLE : View.VISIBLE);
			mBtnMusicPause.setVisibility(musicStatus ? View.VISIBLE : View.INVISIBLE);
		}
	};

	private IntentFilter getIntentFilter(){
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BonovoBlueToothData.ACTION_DATA_IAIB_CHANGED);
		intentFilter.addAction(BonovoBlueToothData.ACTION_DATA_MAMB_CHANGED);
		intentFilter.addAction(BonovoBlueToothData.ACTION_SYNC_CONTACTS_COMPLETE);
        intentFilter.addAction(BonovoBlueToothData.ACTION_SYNC_CONTACTS_READ_COUNT);
        intentFilter.addAction(BonovoBlueToothData.ACTION_SYNC_CONTACTS_WRITE_DATABASE);
        intentFilter.addAction(BonovoBlueToothData.ACTION_SYNC_CONTACTS_TIMEOUT);
        intentFilter.addAction(BonovoBlueToothData.ACTION_SYNC_CONTACTS_NOT_SUPPORT);
        intentFilter.addAction(BonovoBlueToothData.ACTION_SEND_COMMANDER_ERROR);
		intentFilter.addAction(BonovoBlueToothData.ACTION_BT_NAME);
		intentFilter.addAction(BonovoBlueToothData.ACTION_BT_PINCODE);
		intentFilter.addAction("BlueTooth.Media_Broadcast_Next");
        intentFilter.addAction("BlueTooth.Media_Broadcast_Last");
        intentFilter.addAction("BlueTooth.Media_Broadcast_Play_Pause");
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
		intentFilter.addAction("android.intent.action.BONOVO_SLEEP_KEY");
		intentFilter.addAction("android.intent.action.BONOVO_WAKEUP_KEY");
		return intentFilter;
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(BonovoBlueToothData.ACTION_DATA_IAIB_CHANGED.equals(action)){
				if(mBtService != null){
					boolean hfpStatus = mBtService.getBtHFPStatus();
					mTvBtHFPStatus.setText(hfpStatus ? R.string.phone_link_status_opened : R.string.phone_link_status_closed);
				}
			}else if(BonovoBlueToothData.ACTION_DATA_MAMB_CHANGED.equals(action)){
                boolean musicStatus = false;
                if(mBtService != null){
				    musicStatus = mBtService.getMusicStatus();
			    }
				mBtnMusicPlay.setVisibility(musicStatus ? View.INVISIBLE : View.VISIBLE);
				mBtnMusicPause.setVisibility(musicStatus ? View.VISIBLE : View.INVISIBLE);
			}else if(BonovoBlueToothData.ACTION_SYNC_CONTACTS_COMPLETE.equals(action)){
                if(mBluetoothContactsDialog == null){
                    showDialog(DIALOG_SYNC_CONTACTS);
                }
                mBluetoothContactsDialog.setMessage(mContext.getString(R.string.description_sync_contacts_complete));
				mHandler.sendEmptyMessageDelayed(MSG_SYNC_CONTACTS_COMPLETE, DELAY_TIME_READ);
            }else if(BonovoBlueToothData.ACTION_SYNC_CONTACTS_READ_COUNT.equals(action)){
                if(mBluetoothContactsDialog == null){
                    showDialog(DIALOG_SYNC_CONTACTS);
                }
                int count = intent.getIntExtra(BonovoBlueToothData.KEY_SYNC_CONTACTS_COUNT, 0);
                String info = mContext.getString(R.string.description_reading_contacts_info) + " " + count;
                mBluetoothContactsDialog.setMessage(info);
            }else if(BonovoBlueToothData.ACTION_SYNC_CONTACTS_WRITE_DATABASE.equals(action)){
                if(mBluetoothContactsDialog == null){
                    showDialog(DIALOG_SYNC_CONTACTS);
                }
				int count = intent.getIntExtra(BonovoBlueToothData.KEY_SYNC_CONTACTS_COUNT, 0);
                mBluetoothContactsDialog.setMessage(mContext.getString(R.string.description_writing_contacts_info));
            }else if(BonovoBlueToothData.ACTION_SYNC_CONTACTS_TIMEOUT.equals(action)){
                if(mBluetoothContactsDialog == null){
                    showDialog(DIALOG_SYNC_CONTACTS);
                }
				int count = intent.getIntExtra(BonovoBlueToothData.KEY_SYNC_CONTACTS_COUNT, 0);
                String info = mContext.getString(R.string.description_sync_contacts_error) + count;
                mBluetoothContactsDialog.setMessage(info);
			}else if(BonovoBlueToothData.ACTION_SYNC_CONTACTS_NOT_SUPPORT.equals(action)){
				if(mBluetoothContactsDialog == null){
                    showDialog(DIALOG_SYNC_CONTACTS);
                }
                mBluetoothContactsDialog.setMessage(mContext.getString(R.string.description_sync_contacts_notsupport));
				mHandler.sendEmptyMessageDelayed(MSG_SYNC_CONTACTS_COMPLETE, DELAY_TIME_READ);
			}else if(BonovoBlueToothData.ACTION_SEND_COMMANDER_ERROR.equals(action)){
				if(DEBUG) Log.e(TAG, "send bluetooth commander error!!!");
            }else if(BonovoBlueToothData.ACTION_BT_NAME.equals(action)){
				String name = intent.getStringExtra(BonovoBlueToothService.BonovoBlueToothData.ACTION_INFO_BT_NAME);
				if(name != null && mTvBtNameInfo != null){
					mTvBtNameInfo.setText(name);
					if(mBtService != null && !name.equals(mBtService.getBtName())){
						if(mReadBtNameTime < MAX_READ_TIME){
							Message msgUpdateName = mHandler.obtainMessage(MSG_UPDATA_BT_NAME, mBtService.getBtName());
							mHandler.sendMessage(msgUpdateName);
							mReadBtNameTime++;
						}else{
							mReadBtNameTime = 0;
							Toast.makeText(mContext, R.string.description_updata_name_fail, Toast.LENGTH_SHORT).show();
						}
					}
				}
			} else if(BonovoBlueToothData.ACTION_BT_PINCODE.equals(action)){
				String pincode = intent.getStringExtra(BonovoBlueToothService.BonovoBlueToothData.ACTION_INFO_BT_PINCODE);
				if(pincode != null && mTvBtPinInfo != null){
					mTvBtPinInfo.setText(pincode);
					if(mBtService != null && !pincode.equals(mBtService.getBtPinCode())){
						if(mReadBtPinTime < MAX_READ_TIME){
							Message msgUpdateName = mHandler.obtainMessage(MSG_UPDATA_BT_PIN, mBtService.getBtPinCode());
							mHandler.sendMessage(msgUpdateName);
							mReadBtPinTime++;
						}else{
							mReadBtPinTime = 0;
							Toast.makeText(mContext, R.string.description_updata_pin_fail, Toast.LENGTH_SHORT).show();
						}
					}
				}
			}else if (intent.getAction().equals("BlueTooth.Media_Broadcast_Next")) {
				if(mBtService != null && mBtService.getBtSwitchStatus() && mBtService.getMusicServiceEnable()){
					mBtService.BlueToothMusicNext();
					if(DEBUG) Log.d(TAG, "btnNext is been pressed.");
				}else{
					Toast.makeText(mContext, R.string.description_music_disable, Toast.LENGTH_SHORT).show();
				}
			} else if (intent.getAction().equals("BlueTooth.Media_Broadcast_Last")) {
				if(mBtService != null && mBtService.getBtSwitchStatus() && mBtService.getMusicServiceEnable()){
					mBtService.BlueToothMusicPre();
					if(DEBUG) Log.d(TAG, "btnPre is been pressed.");
				}else{
					Toast.makeText(mContext, R.string.description_music_disable, Toast.LENGTH_SHORT).show();
				}
			
			} else if (intent.getAction().equals("BlueTooth.Media_Broadcast_Play_Pause")) {
				if(mBtService.getMusicStatus()){
					if(mBtService != null && mBtService.getMusicServiceEnable()){
						mBtService.BlueToothMusicPause();
                        if(DEBUG) Log.d(TAG, "btnPuse is been pressed.");
					}else{
                        Toast.makeText(mContext, R.string.description_music_disable, Toast.LENGTH_SHORT).show();
					}
					
				}else{
					if(mBtService != null && mBtService.getMusicServiceEnable()){
						mBtService.BlueToothMusicPlay();
						if(DEBUG) Log.d(TAG, "btnPlay is been pressed.");
					}else{
						Toast.makeText(mContext, R.string.description_music_disable, Toast.LENGTH_SHORT).show();
					}
				}
			}else if(action.equals("android.intent.action.BONOVO_SLEEP_KEY")
               || action.equals("android.intent.action.ACTION_SHUTDOWN")){
				mTvBtStatus.setText(R.string.bluetooth_status_closed);
    			mIgeBtStatus.setImageResource(R.drawable.setting_bluetooth_close);
    			mTvBtHFPStatus.setText(R.string.phone_link_status_closed);
                mBtnMusicPlay.setVisibility(View.VISIBLE);
			    mBtnMusicPause.setVisibility(View.INVISIBLE);
			}else if(action.equals("android.intent.action.BONOVO_WAKEUP_KEY")){

			}
		}
		
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth_settings, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		// the button of phone
		case R.id.btnPhone:{
			if((mBtService == null) || (!mBtService.getBtSwitchStatus())){
				Toast.makeText(mContext, R.string.description_phone_disable, Toast.LENGTH_SHORT).show();
				break;
			}
			
			if(DEBUG) Log.d(TAG, "btnPhone is been pressed.");
			Intent intent = new Intent(this, BonovoBluetoothHandfree.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
		}
		
		// the button of music
		case R.id.btnMusic:{
			if((mBtService == null) || (!mBtService.getBtSwitchStatus())){
				Toast.makeText(mContext, R.string.description_music_disable, Toast.LENGTH_SHORT).show();
				break;
			}
			if(mStubBtMusic != null){
				mStubBtMusic.setVisibility(View.VISIBLE);
				mBtnMusic.setVisibility(View.GONE);
				mBtnSettings.setVisibility(View.VISIBLE);
			}
			if(mStubBtSettings != null){
				mStubBtSettings.setVisibility(View.GONE);
			}
			break;
		}
		
		// the button of settings
		case R.id.btnSettings:{
			if(mStubBtSettings != null){
				mStubBtSettings.setVisibility(View.VISIBLE);
				mBtnSettings.setVisibility(View.GONE);
				mBtnMusic.setVisibility(View.VISIBLE);
			}
			if(mStubBtMusic != null){
				mStubBtMusic.setVisibility(View.GONE);
			}
			break;
		}
		
		// the button of sync contacts
		case R.id.btnSyncContacts:{
			if((mBtService == null) || (!mBtService.getBtSwitchStatus())){
				Toast.makeText(mContext, R.string.description_sync_contacts_disable, Toast.LENGTH_SHORT).show();
				break;
			}
			
			String syncInfo = getString(R.string.description_sync_select) + "\n" 
					+ getString(R.string.description_sync_sim) + "\n"
					+ getString(R.string.description_sync_phone);
			new AlertDialog.Builder(BluetoothSettings.this)
				.setTitle(R.string.title)
				.setMessage(syncInfo)
				.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
					
					@SuppressWarnings("deprecation")
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (mBtService.getBtSwitchStatus() && mBtService.getBtHFPStatus()) {
				
							showDialog(DIALOG_SYNC_CONTACTS);
							new Thread() {
								@Override
								public void run() {
								    mIsSyncPhoneBook = true;
									mBtService.SynchSimContacts();
								}
							}.start();
						} else {
							Toast.makeText(getApplicationContext(),
									R.string.description_sync_contacts_disable, Toast.LENGTH_SHORT)
									.show();
						}
						
					}
				})
				.setNeutralButton("Phone", new DialogInterface.OnClickListener() {
					
					@SuppressWarnings("deprecation")
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (mBtService.getBtSwitchStatus() && mBtService.getBtHFPStatus()) {
				
							showDialog(DIALOG_SYNC_CONTACTS);
							new Thread() {
								@Override
								public void run() {
								    mIsSyncPhoneBook = true;
									mBtService.SynchPhoneContacts();
								}
				
							}.start();
						} else {
							Toast.makeText(getApplicationContext(),
									R.string.description_sync_contacts_disable, Toast.LENGTH_SHORT)
									.show();
						}
					}
				})
				.create()
				.show();
			break;
		}
		
		// the button of volume up
		case R.id.btnVolAdd:{
			if((mBtService == null) || (!mBtService.getBtSwitchStatus())){
				Toast.makeText(mContext, R.string.description_volume_disable, Toast.LENGTH_SHORT).show();
				break;
			}
			if((mBtService != null) && mBtService.getBtSwitchStatus()){
				mBtService.BlueToothPhoneVolumeUp();
			}
			break;
		}
		
		// the button of volume down
		case R.id.btnVolDown:{
			if((mBtService == null) || (!mBtService.getBtSwitchStatus())){
				Toast.makeText(mContext, R.string.description_volume_disable, Toast.LENGTH_SHORT).show();
				break;
			}
			if((mBtService != null) && mBtService.getBtSwitchStatus()){
				mBtService.BlueToothPhoneVolumeDown();
			}
			break;
		}
			//-------------------------------------------------------------------------------------------------------
			//下面的Button代码是为了保持蓝牙界面风格的一致性而删除的。
			//下面Button实现的蓝牙开关控制功能已经用上面的Switch表示。
			//  ——swxia. Date:20140721.
			// ------------------------------------------------------------------------------------------------------  
//	    	case R.id.btnBluetoothStatus:{
//			mBtService.setBtSwitchStatus(!mBtService.getBtSwitchStatus());
//
//			boolean powerStatus = mBtService.getBtSwitchStatus();
//			boolean hfpStatus = mBtService.getBtHFPStatus();
//			mBtnBtPower.setText(powerStatus ? R.string.setting_button_bluetooth_status_closed : R.string.setting_button_bluetooth_status_open);
//			mTvBtStatus.setText(powerStatus ? R.string.bluetooth_status_opened : R.string.bluetooth_status_closed);
//			mIgeBtStatus.setImageResource(powerStatus ? R.drawable.setting_bluetooth_opened : R.drawable.setting_bluetooth_close);
//			mTvBtHFPStatus.setText(hfpStatus ? R.string.phone_link_status_opened : R.string.phone_link_status_closed);
//			mTvBtNameInfo.setText(mBtService.getBtName());
//			mTvBtPinInfo.setText(mBtService.getBtPinCode());
//
//			break;
//		}
    	case R.id.buttonBluetoothMusicPre:
    		if(mBtService != null && mBtService.getBtSwitchStatus()){
    			mBtService.BlueToothMusicPre();
    		}else{
    			Toast.makeText(mContext, R.string.description_music_disable, Toast.LENGTH_SHORT).show();
    		}
    		break;
    	case R.id.buttonBluetoothMusicPlay:
    		if(mBtService != null && mBtService.getBtSwitchStatus()){
    			mBtService.BlueToothMusicPlay();
    		}else{
    			Toast.makeText(mContext, R.string.description_music_disable, Toast.LENGTH_SHORT).show();
    		}
    		break;
    	case R.id.buttonBluetoothMusicPause:
    		if(mBtService != null && mBtService.getBtSwitchStatus()){
    			mBtService.BlueToothMusicPause();
    		}else{
    			Toast.makeText(mContext, R.string.description_music_disable, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.buttonBluetoothMusicStop:
			if(mBtService != null && mBtService.getBtSwitchStatus()){
				mBtService.BlueToothMusicStop();
			}else{
				Toast.makeText(mContext, R.string.description_music_disable, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.buttonBluetoothMusicNext:
			if(mBtService != null && mBtService.getBtSwitchStatus()){
				mBtService.BlueToothMusicNext();
			}else{
				Toast.makeText(mContext, R.string.description_music_disable, Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}


	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.textView1:
		case R.id.textView2:
		case R.id.textView3:
		case R.id.textView4:
			LayoutInflater inflater = getLayoutInflater();
			final View view = inflater.inflate(R.layout.dialog_bt_info_change, null);
			final EditText etBtName = (EditText)view.findViewById(R.id.etDialogBtName);
			final EditText etBtPin = (EditText)view.findViewById(R.id.etDialogBtPinCode);
			new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle(R.string.description_title_setting)
				.setView(view)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
						Message msgUpdateName = mHandler.obtainMessage(MSG_UPDATA_BT_NAME, etBtName.getText().toString());
						mHandler.sendMessage(msgUpdateName);
						
						Message msgUpdatePin = mHandler.obtainMessage(MSG_UPDATA_BT_PIN, etBtPin.getText().toString());
						mHandler.sendMessage(msgUpdatePin);
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
			etBtName.setText(mTvBtNameInfo.getText());
			etBtName.setSelection(etBtName.getText().length());
			etBtPin.setText(mTvBtPinInfo.getText());
			etBtPin.setSelection(etBtPin.getText().length());
			break;
		default:
			break;
		}
		return false;
	}
	
	private Handler mHandler = new Handler() {
		@SuppressWarnings("deprecation")
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
            case MSG_SYNC_CONTACTS_COMPLETE:
                mIsSyncPhoneBook = false;
				if (mBluetoothContactsDialog != null) {
					removeDialog(DIALOG_SYNC_CONTACTS);
                    mBluetoothContactsDialog = null;
				}
				break;
			case MSG_UPDATA_BT_NAME:
				String newName = (String)msg.obj;
				if((newName != null) && (!newName.equals("")) && (mBtService != null)){
					mBtService.setBtName(newName);
                    if(mBtService.getBtSwitchStatus()){
                        mBtService.BlueToothSetOrCheckName(newName);
                        mHandler.sendEmptyMessageDelayed(MSG_READ_BT_NAME, DELAY_TIME_READ);
                    }else{
                        mTvBtNameInfo.setText(mBtService.getBtName());
                    }
				}
				break;
			case MSG_READ_BT_NAME:
				if(mBtService != null){
					mBtService.BlueToothSetOrCheckName(null);
				}
				break;
			case MSG_UPDATA_BT_PIN:
				String newPin = (String)msg.obj;
				if((newPin != null) && (!newPin.equals("")) && (mBtService != null)){
					mBtService.setBtPinCod(newPin);
                    if(mBtService.getBtSwitchStatus()){
                        mBtService.BlueToothSetOrCheckPin(newPin);
                        mHandler.sendEmptyMessageDelayed(MSG_READ_BT_PIN, DELAY_TIME_READ);
                    }else{
                        mTvBtPinInfo.setText(mBtService.getBtPinCode());
                    }
				}
				break;
			case MSG_READ_BT_PIN:
				if(mBtService != null){
					mBtService.BlueToothSetOrCheckPin(null);
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case DIALOG_SYNC_CONTACTS:
			mBluetoothContactsDialog = new ProgressDialog(this);
			mBluetoothContactsDialog
					.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mBluetoothContactsDialog.setTitle(getResources().getString(
					R.string.app_name));
			mBluetoothContactsDialog.setMessage(mContext.getString(R.string.description_sync_contacts_wait_info));
			mBluetoothContactsDialog.setIndeterminate(false);

			return mBluetoothContactsDialog;
		}
		return null;
	}
}
