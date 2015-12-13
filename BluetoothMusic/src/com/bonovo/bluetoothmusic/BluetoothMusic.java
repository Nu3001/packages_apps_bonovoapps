package com.bonovo.bluetoothmusic;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BluetoothMusic extends Activity implements View.OnClickListener{

	private final static String TAG = "BluetoothMusic";
	private final static boolean DEBUG = false;

	private String a2dpTrackName = "";
	private String a2dpArtist = "";
	private String a2dpAlbum = "";
	private TextView mTvA2DPTrackName = null;
	private TextView mTvA2DPArtist = null;
	private TextView mTvA2DPAlbum = null;
	private Button mBtnMusicPre = null;
	private Button mBtnMusicPlay = null;
	private Button mBtnMusicPause = null;
	private Button mBtnMusicStop = null;
	private Button mBtnMusicNext = null;
	private Context mContext = null;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_music);
		mContext = this;
				
		mTvA2DPTrackName = (TextView)findViewById(R.id.txtTrackName);
		mTvA2DPArtist = (TextView)findViewById(R.id.txtArtistName);
		mTvA2DPAlbum = (TextView)findViewById(R.id.txtAlbumName);
				
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
						
		registerReceiver(mReceiver, getIntentFilter());
		
		requestTrackInfo();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private IntentFilter getIntentFilter(){
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.intent.action.DATA_IAIB_CHANGED");
		intentFilter.addAction("android.intent.action.DATA_MAMB_CHANGED");
        intentFilter.addAction("android.intent.action.SEND_COMMANDER_ERROR");
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
		intentFilter.addAction("android.intent.action.BONOVO_SLEEP_KEY");
		intentFilter.addAction("android.intent.action.BONOVO_WAKEUP_KEY");
		intentFilter.addAction("android.intent.action.A2DP_TRACK_CHANGED");
		return intentFilter;
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(action.equals("android.intent.action.DATA_IAIB_CHANGED")){
				// Connection status changed
			}else if(action.equals("android.intent.action.DATA_MAMB_CHANGED")){
                boolean musicStatus = false;
                musicStatus = intent.getBooleanExtra("a2dp_status", false);
                
                mBtnMusicPlay.setVisibility(musicStatus ? View.GONE : View.VISIBLE);
				mBtnMusicPause.setVisibility(musicStatus ? View.VISIBLE : View.GONE);
			}else if(action.equals("android.intent.action.SEND_COMMANDER_ERROR")){
				if(DEBUG) Log.e(TAG, "send bluetooth commander error!!!");
			}else if(action.equals("android.intent.action.BONOVO_SLEEP_KEY")
               || action.equals("android.intent.action.ACTION_SHUTDOWN")){
                mBtnMusicPlay.setVisibility(View.VISIBLE);
			    mBtnMusicPause.setVisibility(View.GONE);
			}else if(action.equals("android.intent.action.A2DP_TRACK_CHANGED")){
				a2dpArtist = intent.getStringExtra("Artist");
				a2dpAlbum = intent.getStringExtra("Album");
				a2dpTrackName = intent.getStringExtra("Title");
				
				updateTrackInfo();
			}
		}	
	};

	void requestTrackInfo() {
		// The service monitors for this and will reload and resend the info when it arrives
		Intent i = new Intent("android.intent.action.BONOVO_REQUEST_TRACKINFO_REFRESH");
		mContext.sendBroadcast(i, null);
	}
	
	void updateTrackInfo() {
		mTvA2DPTrackName.setText(a2dpTrackName);
		mTvA2DPArtist.setText(a2dpArtist);
		mTvA2DPAlbum.setText(a2dpAlbum);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
    	case R.id.buttonBluetoothMusicPre:{
    		Intent i = new Intent("android.intent.action.BONOVO_BTMUSIC_PREVTRACK");
			mContext.sendBroadcast(i, null);
    	}
    		break;
    	case R.id.buttonBluetoothMusicPlay:{
    		Intent i = new Intent("android.intent.action.BONOVO_BTMUSIC_PLAY");
			mContext.sendBroadcast(i, null);
			
			updateTrackInfo();
    	}
    		break;
    	case R.id.buttonBluetoothMusicPause:{
    		Intent i = new Intent("android.intent.action.BONOVO_BTMUSIC_PAUSE");
			mContext.sendBroadcast(i, null);
    	}
			break;
		case R.id.buttonBluetoothMusicStop:{
    		Intent i = new Intent("android.intent.action.BONOVO_BTMUSIC_STOP");
			mContext.sendBroadcast(i, null);
    	}
			break;
		case R.id.buttonBluetoothMusicNext:{
    		Intent i = new Intent("android.intent.action.BONOVO_BTMUSIC_NEXTTRACK");
			mContext.sendBroadcast(i, null);
    	}
			break;
		default:
			break;
		}
	}
}
