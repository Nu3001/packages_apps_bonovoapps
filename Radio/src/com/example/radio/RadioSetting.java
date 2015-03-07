package com.example.radio;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

public class RadioSetting extends Activity implements OnClickListener ,ServiceConnection{
	
	private final static String TAG = "RadioSetting";
	private static final boolean DEBUG = true;
	private static boolean mChecked = false;
	
	private ImageButton mImportButton;
	private ToggleButton mRemoteButton;
	private RadioGroup mRadioGroup;
	private RadioButton checkJapanButton;
	private RadioButton checkChinaButton;
	private RadioButton checkEuropeButton;
	private SeekBar mSeekBar;
	private boolean remoteCheck;
	
	private RadioService mSerivce2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radio_setting_layout);
		setupView();
		
		Intent settingIntent = new Intent("com.example.RadioService");
		this.bindService(settingIntent, this, BIND_AUTO_CREATE);
	}
	
	private void setupView(){
		mImportButton = (ImageButton) findViewById(R.id.btnimport);
		mImportButton.setOnClickListener(this);
		mRemoteButton = (ToggleButton) findViewById(R.id.remote);
		SharedPreferences preferences = getSharedPreferences("CHECKED", 0);
		remoteCheck = preferences.getBoolean("onoff", true);
		
		mRemoteButton.setChecked(remoteCheck);
		
		mRemoteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						if (isChecked) {
							mChecked = isChecked;
							SharedPreferences flagdata = getSharedPreferences(
									"CHECKED", 0);
							flagdata.edit().putBoolean("onoff", mChecked)
									.commit();

							if (DEBUG)
								Log.v(TAG, "open open open !!@@");
							mSerivce2.setRemote(1);
						} else {

							mChecked = isChecked;
							SharedPreferences flagdata = getSharedPreferences(
									"CHECKED", 0);
							flagdata.edit().putBoolean("onoff", mChecked)
									.commit();

							if (DEBUG)
								Log.v(TAG, "close close close !!@@");
							mSerivce2.setRemote(0);
						}
					}
		});
		setSeekBarView();
		setRadioGroup();
		
	}
	
	/*****建立SeekBar控件及事件监听****/
	private void setSeekBarView(){
		mSeekBar = (SeekBar) findViewById(R.id.seek);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl());
	}
	
	private class OnSeekBarChangeListenerImpl implements SeekBar.OnSeekBarChangeListener{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			// TODO Auto-generated method stub
			mSerivce2.setVolume(progress);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/*****建立RadioGroup控件及事件监听****/
	private void setRadioGroup(){
		mRadioGroup = (RadioGroup) findViewById(R.id.radiogroup);
		checkJapanButton = (RadioButton) findViewById(R.id.checkjapan);
		checkChinaButton = (RadioButton) findViewById(R.id.checkchina);
		checkEuropeButton = (RadioButton) findViewById(R.id.checkeurope);
		
		mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId == checkJapanButton.getId()){
					if (DEBUG) Log.v(TAG, "checkJapanButton is checked!!!");
					Toast.makeText(getApplicationContext(), R.string.unrealized, Toast.LENGTH_SHORT).show();
				}else if(checkedId == checkChinaButton.getId()){
					if (DEBUG) Log.v(TAG, "checkChinaButton is checked!!!");
				}else if(checkedId == checkEuropeButton.getId()){
					if (DEBUG) Log.v(TAG, "checkEuropeButton is checked!!!");
					Toast.makeText(getApplicationContext(), R.string.unrealized, Toast.LENGTH_SHORT).show();
				}
			}
		});		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id =v.getId();
		if(mSettingHandler != null ) {
			mSettingHandler.sendEmptyMessage(id);
		}
	}
	
	private Handler mSettingHandler = new Handler(){
		public void handleMessage(final Message msg){
			switch (msg.what){
			case R.id.btnimport:
				if (DEBUG) Log.v(TAG, "btnimportbtn is checked!!!");
				Intent importintent = new Intent(RadioSetting.this, RadioImportActivity.class);
				RadioSetting.this.startActivity(importintent);
				break;
//			case R.id.remote:
//				if (DEBUG) Log.v(TAG, "remotebtn is checked!!!");
//				Toast.makeText(getApplicationContext(), "此功能暂未实现", Toast.LENGTH_SHORT).show();
//				break;
			}
		}
	};

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		mSerivce2 = ((RadioService.ServiceBinder)service).getService();
		mSeekBar.setProgress(mSerivce2.getVolume());
		
//		SharedPreferences flagdata = getSharedPreferences(
//				"CHECKED", 0);
//		mChecked = flagdata.getBoolean("onoff", false);
//		
//		if(mChecked){
//			mRemoteButton.setChecked(true);
//			//mSerivce2.setRemote(1);
//		}else {
//			mRemoteButton.setChecked(false);
//			mSerivce2.setRemote(0);
//		}
		
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		mSerivce2 = null;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mSerivce2 = null;
		this.unbindService(this);
	}

}
