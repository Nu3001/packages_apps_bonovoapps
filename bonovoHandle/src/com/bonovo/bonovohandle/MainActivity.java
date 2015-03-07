package com.bonovo.bonovohandle;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener, OnSeekBarChangeListener, ServiceConnection{

	private static final String TAG = "MainActivity";
	private Button mGet_Button;
	private Button mSet_Button;
	private TextView mTextView;
	private TextView mDriverValue;
	private TextView mBringhtValue;
	private SeekBar mSeekBar;
	private Context mContext;
	
	private HandleService mHandleService;
	
	private Handler mHandler = new Handler(){
		int brightness;
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case R.id.mbutton_get:
				try {
		            brightness = Settings.System.getInt(mContext.getContentResolver(),
		                    Settings.System.SCREEN_BRIGHTNESS);
		        } catch (SettingNotFoundException snfe) {
		        }

				Log.d(TAG, "Brightness value is "+brightness);
				mTextView.setText(""+brightness);
				break;
			case R.id.mbutton_set:
				Log.d(TAG, "Set Brightness value is "+mSeekBar.getProgress());
				Settings.System.putInt(mContext.getContentResolver(),
	                    Settings.System.SCREEN_BRIGHTNESS, mSeekBar.getProgress());

				try {
		            IPowerManager power = IPowerManager.Stub.asInterface(
		                    ServiceManager.getService("power"));
		            if (power != null) {
		                //power.setBacklightBrightness(mSeekBar.getProgress());
		                power.setTemporaryScreenBrightnessSettingOverride(mSeekBar.getProgress());
		            }
		        } catch (RemoteException doe) {
		            
		        }
				break;
			}
		}
		
	};
	
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(intent.getAction().equals("android.intent.action.BONOVO_UPDATEBRIGHTNESS_KEY"))
			{
				Log.v(TAG, "report");	
				mDriverValue.setText(String.valueOf(mHandleService.getBrightness()));
			}

		}

	};
	
	private IntentFilter getIntentFilter(){
		IntentFilter myIntentFilter = new IntentFilter("android.intent.action.BONOVO_UPDATEBRIGHTNESS_KEY");
		return myIntentFilter;
	};
	
    @Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(myReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.registerReceiver(myReceiver, getIntentFilter());
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mTextView = (TextView)findViewById(R.id.mvalue);
        mBringhtValue = (TextView)findViewById(R.id.brightvalue);
        mDriverValue = (TextView)findViewById(R.id.driver_value);
        mSeekBar = (SeekBar)findViewById(R.id.set_value);
        mSeekBar.setOnSeekBarChangeListener(this);
        mGet_Button = (Button)findViewById(R.id.mbutton_get);
        mGet_Button.setOnClickListener(this);
        mSet_Button = (Button)findViewById(R.id.mbutton_set);
        mSet_Button.setOnClickListener(this);
        mContext = getApplicationContext();

		Intent intent_service = new Intent("com.bonovo.bonovohandle.HandleService");
        this.bindService(intent_service, this, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		Message msg = mHandler.obtainMessage(viewId);
		if(msg != null){
			mHandler.sendMessage(msg);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		Log.d(TAG, "onProgressChanged"+progress+"----"+seekBar.getProgress());
		mBringhtValue.setText(String.valueOf(progress));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		Log.d(TAG, "onStartTrackingTouch");
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		Log.d(TAG, "onStopTrackingTouch ");
	}

	@Override
	public void onServiceConnected(ComponentName arg0, IBinder service) {
		mHandleService = ((HandleService.ServiceBinder)service).getService();
		mDriverValue.setText(String.valueOf(mHandleService.getBrightness()));
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mHandleService = null;
	}

    
}
