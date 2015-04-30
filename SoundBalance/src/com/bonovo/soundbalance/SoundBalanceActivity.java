package com.bonovo.soundbalance;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class SoundBalanceActivity extends Activity implements OnSeekBarChangeListener{

//	private final static String TAG = "SoundBalance";
	private final static String LOUT1_VOLUME = "LOUT1_VOLUME";
	private final static String ROUT1_VOLUME = "ROUT1_VOLUME";
	private final static String LOUT2_VOLUME = "LOUT2_VOLUME";
	private final static String ROUT2_VOLUME = "ROUT2_VOLUME";
	private final static String SOUND_BALANCE_CHANNEL = "channel";
	private final static String SOUND_BALANCE_VOLUME = "volume";
	private final static int CHANNEL_LOUT1 = 0;
	private final static int CHANNEL_ROUT1 = 1;
	private final static int CHANNEL_LOUT2 = 2;
	private final static int CHANNEL_ROUT2 = 3;
	private SeekBar mFLSeekBar;
	private SeekBar mFRSeekBar;
	private SeekBar mRLSeekBar;
	private SeekBar mRRSeekBar;
	private TextView mFLTextView;
	private TextView mFRTextView;
	private TextView mRLTextView;
	private TextView mRRTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_balance);
		
		mFLTextView = (TextView)findViewById(R.id.textFL_max);
		mFRTextView = (TextView)findViewById(R.id.textFR_max);
		mRLTextView = (TextView)findViewById(R.id.textRL_max);
		mRRTextView = (TextView)findViewById(R.id.textRR_max);
		
		mFLSeekBar = (SeekBar)findViewById(R.id.seekBarFL);
		mFRSeekBar = (SeekBar)findViewById(R.id.seekBarFR);
		mRLSeekBar = (SeekBar)findViewById(R.id.seekBarRL);
		mRRSeekBar = (SeekBar)findViewById(R.id.seekBarRR);
		
		mFLSeekBar.setMax(100);
		mFRSeekBar.setMax(100);
		mRLSeekBar.setMax(100);
		mRRSeekBar.setMax(100);
		
		mFLSeekBar.setProgress(100);
		mFRSeekBar.setProgress(100);
		mRLSeekBar.setProgress(100);
		mRRSeekBar.setProgress(100);
		
		mFLSeekBar.setOnSeekBarChangeListener(this);
		mFRSeekBar.setOnSeekBarChangeListener(this);
		mRLSeekBar.setOnSeekBarChangeListener(this);
		mRRSeekBar.setOnSeekBarChangeListener(this);
		
		registerReceiver(mBroadcastReceiver, getIntentFilter());
		Intent intent = new Intent("android.intent.action.BONOVO_GET_SOUND_BALANCE");
		sendBroadcast(intent);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		int channel = -1;
		switch(seekBar.getId()){
		case R.id.seekBarFL:
			channel = CHANNEL_LOUT1;
			mFLTextView.setText(progress+"%");
			break;
		case R.id.seekBarFR:
			channel = CHANNEL_ROUT1;
			mFRTextView.setText(progress+"%");
			break;
		case R.id.seekBarRL:
			channel = CHANNEL_LOUT2;
			mRLTextView.setText(progress+"%");
			break;
		case R.id.seekBarRR:
			channel = CHANNEL_ROUT2;
			mRRTextView.setText(progress+"%");
			break;
		default:
			break;
		}
		Intent intent = new Intent("android.intent.action.BONOVO_SET_SOUND_BALANCE");
		intent.putExtra(SOUND_BALANCE_CHANNEL, channel);
		intent.putExtra(SOUND_BALANCE_VOLUME, progress);
		sendBroadcast(intent);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	

	private IntentFilter getIntentFilter() {
		IntentFilter intentFilter = new IntentFilter("android.intent.action.BONOVO_SOUND_BALANCE");
		return intentFilter;
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals("android.intent.action.BONOVO_SOUND_BALANCE")){
				int volFL = intent.getIntExtra(LOUT1_VOLUME, -1);
				int volFR = intent.getIntExtra(ROUT1_VOLUME, -1);
				int volRL = intent.getIntExtra(LOUT2_VOLUME, -1);
				int volRR = intent.getIntExtra(ROUT2_VOLUME, -1);
				if(volFL >= 0 && volFL <= 100){
					mFLSeekBar.setProgress(volFL);
				}
				if(volFR >= 0 && volFR <= 100){
					mFRSeekBar.setProgress(volFR);
				}
				if(volRL >= 0 && volRL <= 100){
					mRLSeekBar.setProgress(volRL);
				}
				if(volRR >= 0 && volRR <= 100){
					mRRSeekBar.setProgress(volRR);
				}
			}
		}
	};
}
