package com.bonovo.soundbalance;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

public class SonataActivity extends Activity implements OnSeekBarChangeListener{

	private final static String TAG = "SonataActivity";
	private SeekBar mLRSeekBar;
	private SeekBar mFRSeekBar;
	private SeekBar mBassSeekBar;
	private SeekBar mMidSeekBar;
	private SeekBar mTreSeekBar;
	private TextView mBassTextView;
	private TextView mMidTextView;
	private TextView mTreTextView;
	public static final String EXTRA_VOLUME_LEFT_AND_RIGHT = "left_right";
	public static final String EXTRA_VOLUME_FRONT_AND_REAR = "front_rear";
    public static final String EXTRA_EQ_BASS = "eq_bass";
    public static final String EXTRA_EQ_MID = "eq_mid";
    public static final String EXTRA_EQ_TREBLE = "eq_treble";
    public static int fad;
    public static int bal;
    public static int bass;
    public static int mid;
    public static int tre;
    
    private SharedPreferences preferences;
    
    private Button mREButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sonata_layout);
		
		IntentFilter intentFilter = new IntentFilter("com.android.internal.car.can.action.RECEIVED");
		intentFilter.addCategory("com.android.internal.car.can.Sonata8");
		this.registerReceiver(broadcastReceiver, intentFilter);
		
//		Intent intent = new Intent("com.android.internal.car.can.action.ACTION_S8_READINFO");
//		sendBroadcast(intent);
		
		mLRSeekBar = (SeekBar)findViewById(R.id.seekBar_LR);
		mFRSeekBar = (SeekBar)findViewById(R.id.seekBar_FR);
		mBassSeekBar = (SeekBar)findViewById(R.id.seekBar_bass);
		mMidSeekBar = (SeekBar)findViewById(R.id.seekBar_mid);
		mTreSeekBar = (SeekBar)findViewById(R.id.seekBar_treble);
		mBassTextView = (TextView)findViewById(R.id.text_bass_value);
		mMidTextView = (TextView)findViewById(R.id.text_mid_value);
		mTreTextView = (TextView)findViewById(R.id.text_treble_value);
		
		readShareInfo();
		
		mLRSeekBar.setMax(20);
		mFRSeekBar.setMax(20);
		mBassSeekBar.setMax(20);
		mMidSeekBar.setMax(20);
		mTreSeekBar.setMax(20);
		
		mLRSeekBar.setProgress(bal);
		mFRSeekBar.setProgress(fad);
		mBassSeekBar.setProgress(bass);
		mMidSeekBar.setProgress(mid);
		mTreSeekBar.setProgress(tre);
		
		mLRSeekBar.setOnSeekBarChangeListener(this);
		mFRSeekBar.setOnSeekBarChangeListener(this);
		mBassSeekBar.setOnSeekBarChangeListener(this);
		mMidSeekBar.setOnSeekBarChangeListener(this);
		mTreSeekBar.setOnSeekBarChangeListener(this);
		
		mREButton = (Button)findViewById(R.id.reset);
		mREButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mLRSeekBar.setProgress(10);
				mFRSeekBar.setProgress(10);
				mBassSeekBar.setProgress(10);
				mMidSeekBar.setProgress(10);
				mTreSeekBar.setProgress(10);
			}
		});
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Bundle bundle = intent.getBundleExtra("sonata8_bundle");
		if (bundle == null) {
			Log.v(TAG, "bundle is null");
		} else {
			int vfr = bundle.getInt("volume_front_rear", 5);
			int vlr = bundle.getInt("volume_left_right", 5);
			int eb = bundle.getInt("eq_bass", 5);
			int em = bundle.getInt("eq_mid", 5);
			int et = bundle.getInt("eq_treble", 5);
			Log.v(TAG, "vfr= " + vfr + " vlr= " + vlr + " eb= " + eb + " em= " + em
					+ " et= " + et);
			mLRSeekBar.setProgress(vfr);
			mFRSeekBar.setProgress(vlr);
			mBassSeekBar.setProgress(eb);
			mMidSeekBar.setProgress(em);
			mTreSeekBar.setProgress(et);
		}
		super.onNewIntent(intent);
	}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			switch(seekBar.getId()){
			case R.id.seekBar_LR:
				Intent intentLR = new Intent("com.android.internal.car.can.action.ACTION_BALANCE_LEFT_AND_RIGHT");
				intentLR.putExtra(EXTRA_VOLUME_LEFT_AND_RIGHT, Math.abs(progress-20));
				preferences = getSharedPreferences("s8_balance_volume", Context.MODE_WORLD_READABLE);
				preferences.edit()
				  .putInt("s8_balance_volume_bal", Math.abs(progress-20))
				  .commit(); 
				
				sendBroadcast(intentLR);
				break;
			case R.id.seekBar_FR:
				Intent intentFR = new Intent("com.android.internal.car.can.action.ACTION_BALANCE_FRONT_AND_REAR");
				intentFR.putExtra(EXTRA_VOLUME_FRONT_AND_REAR, Math.abs(progress-20));
				preferences = getSharedPreferences("s8_balance_volume", Context.MODE_WORLD_READABLE);
				preferences.edit()
				  .putInt("s8_balance_volume_fad", Math.abs(progress-20))
				  .commit(); 
				
				sendBroadcast(intentFR);
				break;
			case R.id.seekBar_bass:
				mBassTextView.setText(String.valueOf(progress-10));
				Intent intentBASS = new Intent("com.android.internal.car.can.action.ACTION_EQ_BASS");
				intentBASS.putExtra(EXTRA_EQ_BASS, progress);
				preferences = getSharedPreferences("s8_balance_volume", Context.MODE_WORLD_READABLE);
				preferences.edit()
				  .putInt("s8_balance_volume_bas", progress)
				  .commit(); 
				
				sendBroadcast(intentBASS);
				break;
			case R.id.seekBar_mid:
				mMidTextView.setText(String.valueOf(progress-10));
				Intent intentMID = new Intent("com.android.internal.car.can.action.ACTION_EQ_MID");
				intentMID.putExtra(EXTRA_EQ_MID, progress);
				preferences = getSharedPreferences("s8_balance_volume", Context.MODE_WORLD_READABLE);
				preferences.edit()
				  .putInt("s8_balance_volume_mid", progress)
				  .commit(); 
				
				sendBroadcast(intentMID);
				break;
			case R.id.seekBar_treble:
				mTreTextView.setText(String.valueOf(progress-10));
				Intent intentTRE = new Intent("com.android.internal.car.can.action.ACTION_EQ_TREBLE");
				intentTRE.putExtra(EXTRA_EQ_TREBLE, progress);
				preferences = getSharedPreferences("s8_balance_volume", Context.MODE_WORLD_READABLE);
				preferences.edit()
				  .putInt("s8_balance_volume_tre", progress)
				  .commit(); 
				
				sendBroadcast(intentTRE);
				break;
			default:
				break;
			}
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}
		
		private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				Log.d(TAG, "=====mSoundBalanceReceiver=====");
				if(intent.getAction().equals("com.android.internal.car.can.action.RECEIVED")){
					Log.v(TAG, "can be into recevier");
					Bundle bundle = intent.getBundleExtra("sonata8_bundle");
					if (bundle == null) {
						Log.v(TAG, "bundle is null");
					} else {
						int vfr = bundle.getInt("volume_front_rear", 5);
						int vlr = bundle.getInt("volume_left_right", 5);
						int eb = bundle.getInt("eq_bass", 5);
						int em = bundle.getInt("eq_mid", 5);
						int et = bundle.getInt("eq_treble", 5);
						Log.v(TAG, "(222)vfr= " + vfr + " vlr= " + vlr + " eb= " + eb + " em= " + em
								+ " et= " + et);
						mLRSeekBar.setProgress(vfr);
						mFRSeekBar.setProgress(vlr);
						mBassSeekBar.setProgress(eb);
						mMidSeekBar.setProgress(em);
						mTreSeekBar.setProgress(et);
					}
				}
			}
		};
		
		protected void onDestroy() {
			super.onDestroy();
			unregisterReceiver(broadcastReceiver);
		};
		
		public static class S8VolumeBalanceRecevier extends BroadcastReceiver{

			@Override
			public void onReceive(Context context, Intent arg1) {
				// TODO Auto-generated method stub
				SharedPreferences sp = context.getSharedPreferences("s8_balance_volume", MODE_WORLD_READABLE);
				fad = sp.getInt("s8_balance_volume_fad", 10);
				bal = sp.getInt("s8_balance_volume_bal", 10);
				mid = sp.getInt("s8_balance_volume_mid", 10);
				bass = sp.getInt("s8_balance_volume_bas", 10);
				tre = sp.getInt("s8_balance_volume_tre", 10);
				
				Intent intentLR = new Intent("com.android.internal.car.can.action.ACTION_BALANCE_LEFT_AND_RIGHT");
				intentLR.putExtra(EXTRA_VOLUME_LEFT_AND_RIGHT, bal);
				context.sendBroadcast(intentLR);
				
				Intent intentFR = new Intent("com.android.internal.car.can.action.ACTION_BALANCE_FRONT_AND_REAR");
				intentFR.putExtra(EXTRA_VOLUME_FRONT_AND_REAR, fad);
				context.sendBroadcast(intentFR);
				
				Intent intentBASS = new Intent("com.android.internal.car.can.action.ACTION_EQ_BASS");
				intentBASS.putExtra(EXTRA_EQ_BASS, bass);
				context.sendBroadcast(intentBASS);
				
				Intent intentMID = new Intent("com.android.internal.car.can.action.ACTION_EQ_MID");
				intentMID.putExtra(EXTRA_EQ_MID, mid);
				context.sendBroadcast(intentMID);
				
				Intent intentTRE = new Intent("com.android.internal.car.can.action.ACTION_EQ_TREBLE");
				intentTRE.putExtra(EXTRA_EQ_TREBLE, tre);
				context.sendBroadcast(intentTRE);
			}
			
		}
		
		private void readShareInfo(){
			SharedPreferences sp = getSharedPreferences("s8_balance_volume", MODE_WORLD_READABLE);
			fad = sp.getInt("s8_balance_volume_fad", 10);
			bal = sp.getInt("s8_balance_volume_bal", 10);
			mid = sp.getInt("s8_balance_volume_mid", 10);
			bass = sp.getInt("s8_balance_volume_bas", 10);
			tre = sp.getInt("s8_balance_volume_tre", 10);
		}

}
