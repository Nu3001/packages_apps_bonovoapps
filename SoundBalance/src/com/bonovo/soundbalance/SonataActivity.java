package com.bonovo.soundbalance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.Activity;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.os.Bundle;
import android.content.IntentFilter;
import android.view.View;
import android.util.Log;

public class SonataActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
    public static final String EXTRA_EQ_BASS = "eq_bass";
    public static final String EXTRA_EQ_MID = "eq_mid";
    public static final String EXTRA_EQ_TREBLE = "eq_treble";
    public static final String EXTRA_VOLUME_FRONT_AND_REAR = "front_rear";
    public static final String EXTRA_VOLUME_LEFT_AND_RIGHT = "left_right";
    private static final String TAG = "SonataActivity";
    public static int bal;
    public static int bass;
    public static int fad;
    private SeekBar mBassSeekBar;
    private TextView mBassTextView;
    private SeekBar mFRSeekBar;
    private SeekBar mLRSeekBar;
    private SeekBar mMidSeekBar;
    private TextView mMidTextView;
    private Button mREButton;
    private SeekBar mTreSeekBar;
    private TextView mTreTextView;
    public static int mid;
    private SharedPreferences preferences;
    public static int tre;
	
	public class S8VolumeBalanceRecevier extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			SharedPreferences sp = context.getSharedPreferences("s8_balance_volume", 0x1);
			SonataActivity.fad = sp.getInt("s8_balance_volume_fad", 10);
			SonataActivity.bal = sp.getInt("s8_balance_volume_bal", 10);
			SonataActivity.mid = sp.getInt("s8_balance_volume_mid", 10);
			SonataActivity.bass = sp.getInt("s8_balance_volume_bas", 10);
			SonataActivity.tre = sp.getInt("s8_balance_volume_tre", 10);
			
			Intent intentLR = new Intent("com.android.internal.car.can.action.ACTION_BALANCE_LEFT_AND_RIGHT");
			intentLR.putExtra("left_right", SonataActivity.bal);
			context.sendBroadcast(intentLR);
			
			Intent intentFR = new Intent("com.android.internal.car.can.action.ACTION_BALANCE_FRONT_AND_REAR");
			intentFR.putExtra("front_rear", SonataActivity.fad);
			context.sendBroadcast(intentFR);
			
			Intent intentBASS = new Intent("com.android.internal.car.can.action.ACTION_EQ_BASS");
			intentBASS.putExtra("eq_bass", SonataActivity.bass);
			context.sendBroadcast(intentBASS);
			
			Intent intentMID = new Intent("com.android.internal.car.can.action.ACTION_EQ_MID");
			intentMID.putExtra("eq_mid", SonataActivity.mid);
			context.sendBroadcast(intentMID);
			
			Intent intentTRE = new Intent("com.android.internal.car.can.action.ACTION_EQ_TREBLE");
			intentTRE.putExtra("eq_treble", SonataActivity.tre);
			context.sendBroadcast(intentTRE);
		}
	}
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sonata_layout);
		
        IntentFilter intentFilter = new IntentFilter("com.android.internal.car.can.action.RECEIVED");
        intentFilter.addCategory("com.android.internal.car.can.Sonata8");
        registerReceiver(broadcastReceiver, intentFilter);
		
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
        mREButton.setOnClickListener(new View.OnClickListener(this) {
            
            public void onClick(View v) {
                mLRSeekBar.setProgress(20);
                mFRSeekBar.setProgress(20);
                mBassSeekBar.setProgress(20);
                mMidSeekBar.setProgress(20);
                mTreSeekBar.setProgress(20);
            }
        });
    }
    
	@Override
    protected void onNewIntent(Intent intent) {
        Bundle bundle = intent.getBundleExtra("sonata8_bundle");
        if(bundle == null) {
            Log.v(TAG, "bundle is null");
        } else {
            int vfr = bundle.getInt(EXTRA_VOLUME_FRONT_AND_REAR, 5);
            int vlr = bundle.getInt(EXTRA_VOLUME_LEFT_AND_RIGHT, 5);
            int eb = bundle.getInt(EXTRA_EQ_BASS, 5);
            int em = bundle.getInt(EXTRA_EQ_MID, 5);
            int et = bundle.getInt(EXTRA_EQ_TREBLE, 5);
            Log.v(TAG, "vfr= " + vfr + " vlr= " + vlr + " eb= " + eb + " em= " + em + " et= " + et);
            mLRSeekBar.setProgress(vfr);
            mFRSeekBar.setProgress(vlr);
            mBassSeekBar.setProgress(eb);
            mMidSeekBar.setProgress(em);
            mTreSeekBar.setProgress(et);
        }
        super.onNewIntent(intent);
    }
    
	@Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch(seekBar.getId()) {
            case R.id.seekBar_LR:
            {
                Intent intentLR = new Intent("com.android.internal.car.can.action.ACTION_BALANCE_LEFT_AND_RIGHT");
                intentLR.putExtra("left_right", Math.abs((progress - 20)));
                preferences = getSharedPreferences("s8_balance_volume", 0x1);
                preferences.edit().putInt("s8_balance_volume_bal", Math.abs((progress - 20))).commit();
                sendBroadcast(intentLR);
                return;
            }
            case R.id.seekBar_FR:
            {
                Intent intentFR = new Intent("com.android.internal.car.can.action.ACTION_BALANCE_FRONT_AND_REAR");
                intentFR.putExtra("front_rear", Math.abs((progress - 20)));
                preferences = getSharedPreferences("s8_balance_volume", 0x1);
                preferences.edit().putInt("s8_balance_volume_fad", Math.abs((progress - 20))).commit();
                sendBroadcast(intentFR);
                return;
            }
            case R.id.seekBar_bass:
            {
                mBassTextView.setText(String.valueOf((progress - 10)));
                Intent intentBASS = new Intent("com.android.internal.car.can.action.ACTION_EQ_BASS");
                intentBASS.putExtra("eq_bass", progress);
                preferences = getSharedPreferences("s8_balance_volume", 0x1);
                preferences.edit().putInt("s8_balance_volume_bas", progress).commit();
                sendBroadcast(intentBASS);
                return;
            }
            case R.id.seekBar_mid:
            {
                mMidTextView.setText(String.valueOf((progress - 10)));
                Intent intentMID = new Intent("com.android.internal.car.can.action.ACTION_EQ_MID");
                intentMID.putExtra("eq_mid", progress);
                preferences = getSharedPreferences("s8_balance_volume", 0x1);
                preferences.edit().putInt("s8_balance_volume_mid", progress).commit();
                sendBroadcast(intentMID);
                return;
            }
            case R.id.seekBar_treble:
            {
                mTreTextView.setText(String.valueOf((progress - 10)));
                Intent intentTRE = new Intent("com.android.internal.car.can.action.ACTION_EQ_TREBLE");
                intentTRE.putExtra("eq_treble", progress);
                preferences = getSharedPreferences("s8_balance_volume", 0x1);
                preferences.edit().putInt("s8_balance_volume_tre", progress).commit();
                sendBroadcast(intentTRE);
                break;
            }
        }
    }
    
	@Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    
	@Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
    
	@Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
    
    private void readShareInfo() {
        SharedPreferences sp = getSharedPreferences("s8_balance_volume", 0x1);
        fad = sp.getInt("s8_balance_volume_fad", 10);
        bal = sp.getInt("s8_balance_volume_bal", 10);
        mid = sp.getInt("s8_balance_volume_mid", 10);
        bass = sp.getInt("s8_balance_volume_bas", 10);
        tre = sp.getInt("s8_balance_volume_tre", 10);
    }
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            
		@Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "=====mSoundBalanceReceiver=====");
            if(intent.getAction().equals("com.android.internal.car.can.action.RECEIVED")) {
                Log.v(TAG, "can be into recevier");
                Bundle bundle = intent.getBundleExtra("sonata8_bundle");
                if(bundle == null) {
                    Log.v(TAG, "bundle is null");
                    return;
                }
                int vfr = bundle.getInt("volume_front_rear", 5);
                int vlr = bundle.getInt("volume_left_right", 5);
                int eb = bundle.getInt("eq_bass", 5);
                int em = bundle.getInt("eq_mid", 5);
                int et = bundle.getInt("eq_treble", 5);
                Log.v(TAG, "(222)vfr= " + vfr + " vlr= " + vlr + " eb= " + eb + " em= " + em + " et= " + et);
                mLRSeekBar.setProgress(vfr);
                mFRSeekBar.setProgress(vlr);
                mBassSeekBar.setProgress(eb);
                mMidSeekBar.setProgress(em);
                mTreSeekBar.setProgress(et);
            }
        }
    };
}
