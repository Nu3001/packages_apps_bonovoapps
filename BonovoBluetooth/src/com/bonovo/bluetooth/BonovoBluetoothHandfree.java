package com.bonovo.bluetooth;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.net.Uri;
import android.database.Cursor;

import com.bonovo.bluetooth.BonovoBlueToothService.BonovoBlueToothData;
import com.bonovo.bluetooth.dialpad.DialpadImageButton;

public class BonovoBluetoothHandfree extends Activity
	implements View.OnClickListener, View.OnLongClickListener,
	DialpadImageButton.OnPressedListener, AudioManager.OnAudioFocusChangeListener {

	private final boolean DEBUG = false;
	private final String TAG = "BonovoBluetoothHandfree";
	public Context mContext;
	
	private static final int TONE_LENGTH_INFINITE = -1;
	private static final int TONE_LENGTH_MS = 150;
	private boolean mDTMFToneEnabled = true;
	private static final String EMPTY_NUMBER = "";
	private String mLastNumberDialed = EMPTY_NUMBER;
	private String mProhibitedPhoneNumberRegexp;
	private static final int DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_DTMF;
	private static final int TONE_RELATIVE_VOLUME = 80;
	private final Object mToneGeneratorLock = new Object();
	private ToneGenerator mToneGenerator;
	private EditText mDigits;
	private View mDelete;
	private ImageButton mDialButton;
	private ImageButton mAnswerButton;
	private View mLeftPanel;
	private ImageView mContactPhoto;
	private ImageButton mEndCallButton;
	private ImageButton mBackToCallButton;
	private ImageButton mDialpadViewButton;
	private ImageButton mMicMuteButton;
	private ImageButton mConferenceButton;
	private int mDialpadPressCount;
	private ViewStub mDialStub;
	private ViewStub mIncomingStub;
	private TextView mCallTime;
	private TextView mCallNumber;
	private View mCallWaitingContainer;
	private Button mRejectCallWaiting;
	private Button mHoldAndSwitchToCallWaiting;
	private Button mEndAndSwitchToCallWaiting;
	private TextView mCallWaitingNumber;
	private Chronometer mTimer;
	
	private boolean mTimerIsCounting = false;
	private boolean mIsUserStarted = false;
	
	private static final int MSG_DIAL_HANG_UP = 0;
	private static final int MSG_DIAL_FINISH_ACTIVITY = 1;
	private static final long DELAY_TIME_HANG_UP = 2000;
	private static final long DELAY_TIME_FINISH = 1000;
	
	private AudioManager mAudioManger;
	private boolean mIsUseringAudio = false;

	enum phoneLayouts {
	       PHONE_DIALPAD, 
	       PHONE_INCALL,
	       PHONE_RINGING_IN,
	       PHONE_RINGING_OUT
	}
	
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		synchronized public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BonovoBlueToothData.ACTION_PHONE_STATE_CHANGED.equals(action)) {
				String number = intent.getStringExtra(BonovoBlueToothService.BonovoBlueToothData.PHONE_NUMBER);
				if(DEBUG) Log.d(TAG, "=========== BroadcastReceiver number:" + number);
				if(number != null){
					byte[] temp = number.getBytes();
					int i = 0;
					for(i=0; i<number.length(); i++){
						if((temp[i] == '\0') || (temp[i] == '\r') || (temp[i] == '\n')){
							break;
						}
					}
					number = number.substring(0, i);
				}
				if(myBlueToothService != null){
					BonovoBlueToothService.PhoneState phoneState = myBlueToothService.getPhoneState();
					if(DEBUG) Log.d(TAG, "=========== BroadcastReceiver phoneState:" + phoneState);
					if(BonovoBlueToothService.PhoneState.IDLE == phoneState 
							|| BonovoBlueToothService.PhoneState.OFFHOOK == phoneState){
						abandonAudioFocus();
						mCallWaitingContainer.setVisibility(View.GONE);
						mCallNumber.setTextSize(R.dimen.call_number_text_size);
						
						mCallTime.setText(R.string.description_phone_call_time);
						stopCallTimer();
						
						Message msg = mHandler.obtainMessage(MSG_DIAL_HANG_UP);
						mHandler.sendMessageDelayed(msg, DELAY_TIME_HANG_UP);
//						setIncomingView(false, false);
						
					}else if(BonovoBlueToothService.PhoneState.RINGING == phoneState){
						requestAudioFocus();
						
						// If we've switched calls quickly, a hangup message may still be pending.
						//   If so, remove it so the new call display doesn't get removed.
						mHandler.removeMessages(MSG_DIAL_HANG_UP);
						mHandler.removeMessages(MSG_DIAL_FINISH_ACTIVITY);
						
						setView(phoneLayouts.PHONE_RINGING_IN);
						
						if(number != null) {
//							mDigits.setText(number);
                            String disp = getNameByNumber(mContext, number);
                            if(disp == null){
                                disp = number;
                            }
							mCallNumber.setText(disp);
							
							final Editable digits = mDigits.getText();
							digits.clear();
							setContactPhoto(mContext, number);
						}
						
						mCallTime.setText(R.string.description_phone_incoming);

					}else if(BonovoBlueToothService.PhoneState.DIALING == phoneState){
						requestAudioFocus();
						
						// If we've switched calls quickly, a hangup message may still be pending.
						//   If so, remove it so the new call display doesn't get removed.
						mHandler.removeMessages(MSG_DIAL_HANG_UP);
						mHandler.removeMessages(MSG_DIAL_FINISH_ACTIVITY);
						
						setView(phoneLayouts.PHONE_RINGING_OUT);
						
						if(number != null) {
//							mDigits.setText(number);
                            String disp = getNameByNumber(mContext, number);
                            if(disp == null){
                                disp = number;
                            }
							mCallNumber.setText(disp);
							setContactPhoto(mContext, number);
							
							final Editable digits = mDigits.getText();
							digits.clear();
						}
						
						mCallTime.setText(R.string.description_phone_dialing);
						
					}else if(BonovoBlueToothService.PhoneState.ACTIVE == phoneState){
						requestAudioFocus();
						
						// If we've switched calls quickly, a hangup message may still be pending.
						//   If so, remove it so the new call display doesn't get removed.
						mHandler.removeMessages(MSG_DIAL_HANG_UP);
						mHandler.removeMessages(MSG_DIAL_FINISH_ACTIVITY);
						
						setView(phoneLayouts.PHONE_INCALL);
						startCallTimer(myBlueToothService.getAnswerTime());
						mCallTime.setText(R.string.description_phone_in_call);
					}
				}
			}else if(BonovoBlueToothData.ACTION_PHONE_NEW_CALL_WAITING.equals(action)){
				// A new inactive call is ringing
				String number = intent.getStringExtra(BonovoBlueToothService.BonovoBlueToothData.PHONE_NUMBER);
				if(number != null){
					byte[] temp = number.getBytes();
					int i = 0;
					for(i=0; i<number.length(); i++){
						if((temp[i] == '\0') || (temp[i] == '\r') || (temp[i] == '\n')){
							break;
						}
					}
					number = number.substring(0, i);
				}
				// Show the call waiting panel
				setView(phoneLayouts.PHONE_INCALL);
				mCallWaitingContainer.setVisibility(View.VISIBLE);
				mCallWaitingNumber.setText(number);
				
				mRejectCallWaiting.setText(R.string.call_waiting_reject);
				mRejectCallWaiting.setVisibility(View.VISIBLE);
				
				mHoldAndSwitchToCallWaiting.setText(R.string.call_waiting_holdAndAccept);
				mHoldAndSwitchToCallWaiting.setVisibility(View.VISIBLE);
				
				mEndAndSwitchToCallWaiting.setVisibility(View.GONE);
				mConferenceButton.setVisibility(View.VISIBLE);
				
			}else if(BonovoBlueToothData.ACTION_PHONE_HUNG_UP_INACTIVE.equals(action)){
				// Inactive call is gone
				mCallWaitingContainer.setVisibility(View.GONE);
				mConferenceButton.setVisibility(View.GONE);
				
			}else if(BonovoBlueToothData.ACTION_PHONE_CONFERENCE_CALL.equals(action)){
				// Our calls have merged and we now have two people on one call
				mCallNumber.setText(mCallNumber.getText() + "\n" + mCallWaitingNumber.getText());
				mCallNumber.setTextSize(R.dimen.call_number_conference_text_size);
				
				setContactPhoto(mContext, "0");	 // More than one person in call, default the contact photo
				
				mCallWaitingContainer.setVisibility(View.GONE);
				mConferenceButton.setVisibility(View.GONE);
				
			}else if(BonovoBlueToothData.ACTION_PHONE_HELD_ACTIVE_SWITCHED_TO_CALL_WAITING.equals(action)){
				// Our inactive call is now active and our original call is the inactive one
				mCallWaitingContainer.setVisibility(View.VISIBLE);
				
				String inActiveCall = (String) mCallWaitingNumber.getText();
				String activeCall = (String) mCallNumber.getText();
				mCallNumber.setText(inActiveCall);
				mCallWaitingNumber.setText(activeCall);
				
				setContactPhoto(mContext, inActiveCall);
				
				mRejectCallWaiting.setText(R.string.call_waiting_end_held);
				mRejectCallWaiting.setVisibility(View.VISIBLE);
				
				mHoldAndSwitchToCallWaiting.setText(R.string.call_waiting_swap_to_held);
				mHoldAndSwitchToCallWaiting.setVisibility(View.VISIBLE);
				
				mEndAndSwitchToCallWaiting.setVisibility(View.GONE);
				mConferenceButton.setVisibility(View.VISIBLE);
				
			}else if(BonovoBlueToothData.ACTION_PHONE_HUNG_UP_ACTIVE_SWITCHED_TO_CALL_WAITING.equals(action)){
				// Our inactive call is now active and our original call is gone
				mCallNumber.setText(mCallWaitingNumber.getText());
				mCallWaitingContainer.setVisibility(View.GONE);
				mConferenceButton.setVisibility(View.GONE);
			}
		}
	};

	private IntentFilter getIntentFilter() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(BonovoBlueToothData.ACTION_PHONE_STATE_CHANGED);
		myIntentFilter.addAction(BonovoBlueToothData.ACTION_PHONE_NEW_CALL_WAITING);
		myIntentFilter.addAction(BonovoBlueToothData.ACTION_PHONE_CONFERENCE_CALL);
		myIntentFilter.addAction(BonovoBlueToothData.ACTION_PHONE_HELD_ACTIVE_SWITCHED_TO_CALL_WAITING);
		myIntentFilter.addAction(BonovoBlueToothData.ACTION_PHONE_HUNG_UP_ACTIVE_SWITCHED_TO_CALL_WAITING);
		myIntentFilter.addAction(BonovoBlueToothData.ACTION_PHONE_HUNG_UP_INACTIVE);
		myIntentFilter.addAction(BonovoBlueToothData.ACTION_PHONE_BATTERY_LEVEL_CHANGED);
		myIntentFilter.addAction(BonovoBlueToothData.ACTION_PHONE_NETWORK_NAME_CHANGED);
		myIntentFilter.addAction(BonovoBlueToothData.ACTION_PHONE_SIGNAL_LEVEL_CHANGED);
		return myIntentFilter;
	};
	
	private static BonovoBlueToothService myBlueToothService = null;
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			if(DEBUG) Log.d(TAG, "onServiceConnected");
			myBlueToothService = ((BonovoBlueToothService.ServiceBinder) service).getService();
			
			BonovoBlueToothService.PhoneState phoneState = myBlueToothService.getPhoneState();
			if(DEBUG) Log.d(TAG, "=========== ServiceConnection phoneState:" + phoneState);
			
			if(BonovoBlueToothService.PhoneState.IDLE == phoneState 
					|| BonovoBlueToothService.PhoneState.OFFHOOK == phoneState){
				abandonAudioFocus();
				stopCallTimer();
				mCallTime.setText(R.string.description_phone_hang_up);
				setView(phoneLayouts.PHONE_DIALPAD);
				
			}else if(BonovoBlueToothService.PhoneState.RINGING == phoneState){
				setView(phoneLayouts.PHONE_RINGING_IN);
				mCallTime.setText(R.string.description_phone_incoming);
                String number = myBlueToothService.getCurrentNumber();
                String disp = getNameByNumber(mContext, number);
                
                if(disp == null){
                    disp = number;
                }
                
				mCallNumber.setText(disp);
				final Editable digits = mDigits.getText();
				digits.clear();
				requestAudioFocus();
				setContactPhoto(mContext, number);
				
			}else if(BonovoBlueToothService.PhoneState.DIALING == phoneState){
				setView(phoneLayouts.PHONE_RINGING_OUT);
				mCallTime.setText(R.string.description_phone_dialing);
                String number = myBlueToothService.getCurrentNumber();
                String disp = getNameByNumber(mContext, number);
                
                if(disp == null){
                    disp = number;
                }
                
				mCallNumber.setText(disp);
//				mDigits.setText(myBlueToothService.getCurrentNumber());
				final Editable digits = mDigits.getText();
				digits.clear();
				requestAudioFocus();
				setContactPhoto(mContext, number);
				
			}else if(BonovoBlueToothService.PhoneState.ACTIVE == phoneState){
				setView(phoneLayouts.PHONE_INCALL);
				startCallTimer(myBlueToothService.getAnswerTime());
				mCallTime.setText(R.string.description_phone_in_call);
                String number = myBlueToothService.getCurrentNumber();
                String disp = getNameByNumber(mContext, number);
                
                if(disp == null){
                    disp = number;
                }
                
				mCallNumber.setText(disp);
//				mDigits.setText(myBlueToothService.getCurrentNumber());
				final Editable digits = mDigits.getText();
				digits.clear();
				requestAudioFocus();
				setContactPhoto(mContext, number);

			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onServiceDisconnected");
			myBlueToothService = null;
		}

	};
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int what = msg.what;
			
			switch (what) {
			case MSG_DIAL_HANG_UP:{
				if(mCallTime != null){
					mCallTime.setText(R.string.description_phone_hang_up);
					
					mDigits.setText(mCallNumber.getText());
					mDigits.setSelection(mDigits.getText().length());
				}
				abandonAudioFocus();
				setView(phoneLayouts.PHONE_DIALPAD);
				mHandler.sendEmptyMessageDelayed(MSG_DIAL_FINISH_ACTIVITY, DELAY_TIME_FINISH);
			}
				break;
			
			case MSG_DIAL_FINISH_ACTIVITY:
				if(!mIsUserStarted){
					mHandler.removeMessages(MSG_DIAL_HANG_UP);
					mHandler.removeMessages(MSG_DIAL_FINISH_ACTIVITY);
					finish();
				}
				break;

			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bonovo_bluetooth_handfree);
		
		mAudioManger = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
		mContext = this;
		
		mDialStub = (ViewStub)findViewById(R.id.dialViewStub);
		mDialStub.inflate();
		
		mIncomingStub = (ViewStub)findViewById(R.id.incomingViewStub);
		mIncomingStub.inflate();

		mLeftPanel = (View)findViewById(R.id.left_panel);
		mDigits = (EditText)findViewById(R.id.digits);
		mDelete = findViewById(R.id.deleteButton);
		
		if(mDelete != null){
			mDelete.setOnClickListener(this);
			mDelete.setOnLongClickListener(this);
		}	
								
		mContactPhoto = (ImageView)findViewById(R.id.contactPhoto);
		
		mCallWaitingContainer = findViewById(R.id.callWaitingContainer);
		mRejectCallWaiting = (Button)findViewById(R.id.CWRejectButton);
		if(mRejectCallWaiting != null){
			mRejectCallWaiting.setOnClickListener(this);
		}	
		mEndAndSwitchToCallWaiting = (Button)findViewById(R.id.CWEndAndSwitch);
		if(mEndAndSwitchToCallWaiting != null){
			mEndAndSwitchToCallWaiting.setOnClickListener(this);
		}
		mHoldAndSwitchToCallWaiting = (Button)findViewById(R.id.CWHoldAndSwitch);
		if(mHoldAndSwitchToCallWaiting != null){
			mHoldAndSwitchToCallWaiting.setOnClickListener(this);
		}	
		mCallWaitingNumber = (TextView)findViewById(R.id.CallWaitingNumber); 
		
		setupKeypad();
		
		if(DEBUG) Log.d(TAG, "============ onCreate()");
		Bundle extras = getIntent().getExtras();
		String number = null;
		
		if(extras != null) {
			if(DEBUG) Log.d(TAG, "============ onCreate() extras is not null");
			number = extras.getString(BonovoBlueToothService.BonovoBlueToothData.PHONE_NUMBER);
			mIsUserStarted = false;
		}else{
			mIsUserStarted = true;
		}
		
		if(DEBUG) Log.d(TAG, "BonovoBluetoothHandfree onCreate() number:" + number);
//		if(number != null){
//			if(number != null){
//				setIncomingView(true, true);
//				mDigits.setText(number);
//			}
//		}else{
		setView(phoneLayouts.PHONE_DIALPAD);
//		}
		
		mProhibitedPhoneNumberRegexp = getResources().getString(R.string.config_prohibited_phone_number_regexp);
		Intent intent = new Intent();
		intent.setClassName("com.bonovo.bluetooth", "com.bonovo.bluetooth.BonovoBlueToothService");
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		registerReceiver(myReceiver, getIntentFilter());
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindService(conn);
		unregisterReceiver(myReceiver);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		stopTone();
		mDialpadPressCount = 0;
		synchronized (mToneGeneratorLock) {
			if(mToneGenerator != null) {
				mToneGenerator.release();
				mToneGenerator = null;
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mDTMFToneEnabled = Settings.System.getInt(getContentResolver(), 
				Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
		synchronized (mToneGeneratorLock) {
			if(mToneGenerator == null){
				try{
					mToneGenerator = new ToneGenerator(DIAL_TONE_STREAM_TYPE, TONE_RELATIVE_VOLUME);
				} catch (RuntimeException e){
					Log.w(TAG, "Exception caught while creating local tone generator: " + e);
					mToneGenerator = null;
				}
			}
		}
		mDialpadPressCount = 0;
		
        if(myBlueToothService != null){
            BonovoBlueToothService.PhoneState phoneState = myBlueToothService.getPhoneState();
			if(BonovoBlueToothService.PhoneState.IDLE == phoneState 
					|| BonovoBlueToothService.PhoneState.OFFHOOK == phoneState){
				mCallTime.setText(R.string.description_phone_hang_up);
				setView(phoneLayouts.PHONE_DIALPAD);
				stopCallTimer();
				abandonAudioFocus();
				
			}else if(BonovoBlueToothService.PhoneState.RINGING == phoneState){
				setView(phoneLayouts.PHONE_RINGING_IN);
				mCallTime.setText(R.string.description_phone_incoming);
                String number = myBlueToothService.getCurrentNumber();
                String disp = getNameByNumber(mContext, number);
                
                if(disp == null){
                    disp = number;
                }
                
				mCallNumber.setText(disp);
//				mDigits.setText(myBlueToothService.getCurrentNumber());
				final Editable digits = mDigits.getText();
				digits.clear();
				requestAudioFocus();
				setContactPhoto(mContext, number);
				
			}else if(BonovoBlueToothService.PhoneState.DIALING == phoneState){
				setView(phoneLayouts.PHONE_RINGING_OUT);
				mCallTime.setText(R.string.description_phone_dialing);
                String number = myBlueToothService.getCurrentNumber();
                String disp = getNameByNumber(mContext, number);
                
                if(disp == null){
                    disp = number;
                }
                
				mCallNumber.setText(disp);
//				mDigits.setText(myBlueToothService.getCurrentNumber());
				final Editable digits = mDigits.getText();
				digits.clear();
				requestAudioFocus();
				setContactPhoto(mContext, number);
				
			}else if(BonovoBlueToothService.PhoneState.ACTIVE == phoneState){
				setView(phoneLayouts.PHONE_INCALL);
				startCallTimer(myBlueToothService.getAnswerTime());
				mCallTime.setText(R.string.description_phone_in_call);
                String number = myBlueToothService.getCurrentNumber();
                String disp = getNameByNumber(mContext, number);
                
                if(disp == null){
                    disp = number;
                }
                
				mCallNumber.setText(disp);
//				mDigits.setText(myBlueToothService.getCurrentNumber());
				final Editable digits = mDigits.getText();
				digits.clear();
				requestAudioFocus();
				setContactPhoto(mContext, number);
			}
        }
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(DEBUG) Log.d(TAG, "======= onClick()");
		switch (v.getId()) {
		case R.id.deleteButton:
			keyPressed(KeyEvent.KEYCODE_DEL);
			break;
			
		case R.id.digits:
			if(!isDigitsEmpty()){
				mDigits.setCursorVisible(true);
			}
			break;
			
		case R.id.dialButton:
			dialButtonPressed();
			break;
			
		case R.id.endCallButton:
			if(myBlueToothService != null){
				if(BonovoBlueToothService.PhoneState.RINGING == myBlueToothService.getPhoneState()){
					myBlueToothService.BlueToothPhoneRejectCall();
				}else{
					myBlueToothService.BlueToothPhoneHangup();
				}
			}
			break;
			
		case R.id.answerButton:
			if(myBlueToothService != null) {
				myBlueToothService.BlueToothPhoneAnswer();
				setView(phoneLayouts.PHONE_INCALL);
			}
			break;

		case R.id.backToCallButton:
			// Switch back from dial pad to active call
			setView(phoneLayouts.PHONE_INCALL);
			break;
			
		case R.id.DialpadViewButton:
			// Force switch view from incall to dialpad, normally because the caller
			//   needs to send DTMF codes (press 1 for x, press 2 for y...)
			setView(phoneLayouts.PHONE_DIALPAD);
			break;
			
		case R.id.MuteMicButton:
			// Toggles the microphone off and on
			
			// Get state of microphone before requesting switch
			Boolean currMuteState = myBlueToothService.BlueToothMicrophoneState();
			myBlueToothService.BlueToothPhoneMute();		
		 
			// Record new state of Microphone
			currMuteState = !currMuteState;
			if(currMuteState == true) {
				// Microphone is currently off
				mMicMuteButton.setImageResource(R.drawable.ic_dial_call_muted);
			}else{
				// Microphone is currently on
				mMicMuteButton.setImageResource(R.drawable.ic_dial_call_unmuted);
			}
					
			break;
		case R.id.ConferenceButton:
			// merge calls into a conference call
			if(myBlueToothService != null){
				myBlueToothService.BlueToothPhoneConferenceCalls();
			}
			break;
		case R.id.CWRejectButton:
			// Reject Call Waiting
			if(myBlueToothService != null){
				myBlueToothService.BlueToothPhoneRejectWaitingCall();
			}
			break;
		case R.id.CWEndAndSwitch:
			// Switch to call waiting and end current call
			if(myBlueToothService != null){
				myBlueToothService.BlueToothPhoneEndAndSwitchToWaitingCall();
			}

		case R.id.CWHoldAndSwitch:
			// Switch to call waiting and put current call on hold
			if(myBlueToothService != null){
				myBlueToothService.BlueToothPhoneHoldAndSwitchToWaitingCall();
			}

		default:
			Log.wtf(TAG, "Unexpected onClick() event from:" + v);
			break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		final Editable digits = mDigits.getText();
		final int id = v.getId();
		switch(id){
		case R.id.deleteButton:
			digits.clear();
			return true;
			
		case R.id.one:
			return false;
			
		case R.id.zero:
			removePreviousDigitIfPossible();
			keyPressed(KeyEvent.KEYCODE_PLUS);
			stopTone();
			if(mDialpadPressCount > 0) mDialpadPressCount--;
			return true;
			
		case R.id.digits:
			mDigits.setCursorVisible(true);
			return false;
			
		case R.id.dialButton:
			if(isDigitsEmpty()){
				handleDialButtonClickWithEmptyDigits();
				return true;
			}else{
				return false;
			}
		}
		return false;
	}

	@Override
	public void onPressed(View view, boolean pressed) {
		// TODO Auto-generated method stub
		if(DEBUG) Log.d(TAG, "====== onPressed");
		if(pressed) {
			switch (view.getId()) {
			case R.id.one:
				keyPressed(KeyEvent.KEYCODE_1);
				break;
				
			case R.id.two:
				keyPressed(KeyEvent.KEYCODE_2);
				break;
				
			case R.id.three:
				keyPressed(KeyEvent.KEYCODE_3);
				break;
				
			case R.id.four:
				keyPressed(KeyEvent.KEYCODE_4);
				break;
				
			case R.id.five:
				keyPressed(KeyEvent.KEYCODE_5);
				break;
				
			case R.id.six:
				keyPressed(KeyEvent.KEYCODE_6);
				break;
				
			case R.id.seven:
				keyPressed(KeyEvent.KEYCODE_7);
				break;
				
			case R.id.eight:
				keyPressed(KeyEvent.KEYCODE_8);
				break;
				
			case R.id.nine:
				keyPressed(KeyEvent.KEYCODE_9);
				break;
				
			case R.id.zero:
				keyPressed(KeyEvent.KEYCODE_0);
				break;
				
			case R.id.star:
				keyPressed(KeyEvent.KEYCODE_STAR);
				break;
				
			case R.id.pound:
				keyPressed(KeyEvent.KEYCODE_POUND);
				break;

			default:
				if(DEBUG) Log.d(TAG, "Unexpected onTouch(ACTION_DOWN) event from: " + view);
				break;
			}
			mDialpadPressCount++;
		} else {
			view.jumpDrawablesToCurrentState();
			mDialpadPressCount--;
			
			if(mDialpadPressCount < 0){
				if(DEBUG) Log.d(TAG, "mKeyPressCount became negative.");
				stopTone();
				mDialpadPressCount = 0;
				
			} else if(mDialpadPressCount == 0){
				stopTone();
			}
		}
	}
	
	private void setContactPhoto(Context context, String number){
        if(context == null)
	        return;
               
        if(mContactPhoto == null) {
        	Log.d(TAG, "setContactPhoto: mContactPhoto is null.");
        	return;
        }

		Uri contactPhotoUri = getContactPhotoUriByNumber(context, number);

		if (contactPhotoUri == null) {
			Log.d(TAG, "setContactPhoto: number: " + number + " no photo set.");

			// Show the default image
			mContactPhoto.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_contact_picture_holo_light));
		}else{
			Log.d(TAG, "setContactPhoto: number: " + number + " got photo uri: " + contactPhotoUri.toString());

			// Show the contact image
			ContentResolver resolver = context.getContentResolver();
			try {
				Bitmap contactBitmap = BitmapFactory.decodeStream(resolver.openInputStream(contactPhotoUri));
				mContactPhoto.setImageBitmap(getCircleBitmap(contactBitmap));		
			}catch(java.io.IOException ioe){
				// show the default image
				mContactPhoto.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_contact_picture_holo_light));
			}
		}
	}
	
	private String getContactIdFromPhoneNumber(Context context, String phoneNumber) {
        if(context == null)
            return null;
        
	    Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
	        Uri.encode(phoneNumber));
	    Cursor cursor = context.getContentResolver().query(uri,
	        new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup._ID },
	        null, null, null);

	    String contactId = "";

	    if (cursor.moveToFirst()) {
	        do {
	        contactId = cursor.getString(cursor
	            .getColumnIndex(PhoneLookup._ID));
	        } while (cursor.moveToNext());
	    }

	    return contactId;
	  }
	
    private String getNameByNumber(Context context, String number){

        if(context == null)
            return null;

        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + number);
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{Contacts.DISPLAY_NAME}, null, null, null);
        String name = null;
        if(cursor.moveToFirst()){
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }
    
    private Uri getContactPhotoUriByNumber(Context context, String number){

        if(context == null)
            return null;
        
        try {
        	String id = getContactIdFromPhoneNumber(context, number);
        	Uri photo = ContentUris.withAppendedId( ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
        	photo = Uri.withAppendedPath( photo, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY );       
        
        	return photo;
        } catch (Throwable e) {
        	return null;
        }
    }
    
	
	private boolean requestAudioFocus(){
		int result;
		if((!mIsUseringAudio)&& (mAudioManger != null)){
			result = mAudioManger.requestAudioFocus(this, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
				mIsUseringAudio = true;
			}
		}
		
		if(DEBUG) Log.d(TAG, "========== requestAudioFocus()  mIsUseringAudio:" + mIsUseringAudio);
		
		return mIsUseringAudio;
	}
	
	private boolean abandonAudioFocus(){
		if(mIsUseringAudio && (mAudioManger != null)){
			mAudioManger.abandonAudioFocus(this);
			mIsUseringAudio = false;
		}
		
		if(DEBUG) Log.d(TAG, "========== abandonAudioFocus()  mIsUseringAudio:" + mIsUseringAudio);
		return !mIsUseringAudio;
	}
	
	private void startCallTimer(long baseTime){
		if((mTimer != null) && (baseTime >= 0) && !mTimerIsCounting){
			mTimer.setVisibility(View.VISIBLE);
			mTimer.setBase(baseTime);
			mTimer.start();
			mTimerIsCounting = true;
		}
	}
	
	private void stopCallTimer(){
		if((mTimer != null) && mTimerIsCounting){
			mTimer.stop();
			mTimerIsCounting = false;
		}
	}
	
	private void setView(phoneLayouts requestedLayout){

		switch (requestedLayout) {
		case PHONE_DIALPAD:
			mLeftPanel.setVisibility(View.VISIBLE);
			mIncomingStub.setVisibility(View.GONE);
											
			mAnswerButton.setVisibility(View.GONE);
			mEndCallButton.setVisibility(View.GONE);
			mCallNumber.setVisibility(View.GONE);
			
			// Dial pad can show to both dial a number and to send DTMF codes during an active call. 
			//  Therefore it has two different states to handle
			if(myBlueToothService != null){
				if(BonovoBlueToothService.PhoneState.ACTIVE == myBlueToothService.getPhoneState()){
					mBackToCallButton.setVisibility(View.VISIBLE);
					mDialStub.setVisibility(View.GONE);
				}else{
					mBackToCallButton.setVisibility(View.GONE);
					mDialStub.setVisibility(View.VISIBLE);
				
					mCallTime.setText("");
					mCallNumber.setText("");
					mTimer.setVisibility(View.GONE);
				}
			}else{
				mBackToCallButton.setVisibility(View.GONE);
				mDialStub.setVisibility(View.VISIBLE);
			
				mCallTime.setText("");
				mCallNumber.setText("");
				mTimer.setVisibility(View.GONE);
			}
			
			break;
			
		case PHONE_INCALL:
			mIncomingStub.setVisibility(View.VISIBLE);
			mDialStub.setVisibility(View.GONE);
			mLeftPanel.setVisibility(View.GONE);
			
			mAnswerButton.setVisibility(View.GONE);
			mEndCallButton.setVisibility(View.VISIBLE);
			mCallNumber.setVisibility(View.VISIBLE);
			mDialpadViewButton.setVisibility(View.VISIBLE);
			mMicMuteButton.setVisibility(View.VISIBLE);
			
			mTimer.setVisibility(View.VISIBLE);
			break;
			
		case PHONE_RINGING_IN:
			mIncomingStub.setVisibility(View.VISIBLE);
			mDialStub.setVisibility(View.GONE);
			mLeftPanel.setVisibility(View.GONE);
			
			mAnswerButton.setVisibility(View.VISIBLE);
			mEndCallButton.setVisibility(View.VISIBLE);
			mCallNumber.setVisibility(View.VISIBLE);
			mDialpadViewButton.setVisibility(View.GONE);
			mMicMuteButton.setVisibility(View.GONE);
			mConferenceButton.setVisibility(View.GONE);
			
			mTimer.setVisibility(View.GONE);
			break;
			
		case PHONE_RINGING_OUT:
			mIncomingStub.setVisibility(View.VISIBLE);
			mLeftPanel.setVisibility(View.GONE);
			mDialStub.setVisibility(View.GONE);
			
			mAnswerButton.setVisibility(View.GONE);
			mEndCallButton.setVisibility(View.VISIBLE);
			mCallNumber.setVisibility(View.VISIBLE);
			mDialpadViewButton.setVisibility(View.GONE);
			mMicMuteButton.setVisibility(View.GONE);
			mConferenceButton.setVisibility(View.GONE);
			
			mTimer.setVisibility(View.GONE);
			break;
			
		}
	}
	
	private void removePreviousDigitIfPossible(){
//		final Editable editable = mDigits.getText();
		final int currentPosition = mDigits.getSelectionStart();
		
		if(currentPosition > 0){
			mDigits.setSelection(currentPosition);
			mDigits.getText().delete(currentPosition - 1, currentPosition);
		}
	}
	
	private boolean isDigitsEmpty(){
		return mDigits.length() == 0;
	}
	
	private void dialButtonPressed() {
		if(isDigitsEmpty()) {
			handleDialButtonClickWithEmptyDigits();
		} else {
			final String number = mDigits.getText().toString();
			if(number != null
					&& !TextUtils.isEmpty(mProhibitedPhoneNumberRegexp)
					&& number.matches(mProhibitedPhoneNumberRegexp)){
			}else{
				// dial number
				if(myBlueToothService != null){
					if(myBlueToothService.getBtHFPStatus()){
						myBlueToothService.BlueToothPhoneDial(number);
						setView(phoneLayouts.PHONE_RINGING_OUT);
					} else {
						Toast toast = Toast.makeText(this, R.string.description_dial_info, Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
//						Log.e(TAG, "Bluetooth HFP is disconnect");
					}
//					Intent intent = new Intent(this, BonovoBluetoothInCallScreen.class);
//					intent.putExtra("NUMBER", number);
//					startActivity(intent);
//					this.finish();
				}
			}
		}
	}
	
	private void handleDialButtonClickWithEmptyDigits() {
		if(myBlueToothService != null && myBlueToothService.getBtHFPStatus()){
			if(!TextUtils.isEmpty(mLastNumberDialed)){
				mDigits.setText(mLastNumberDialed);
				mDigits.setSelection(mDigits.getText().length());
			}else{
				playTone(ToneGenerator.TONE_PROP_NACK);
			}
		}
	}
	
	private void setupKeypad(){
		int[] buttonIds = new int[] { R.id.one, R.id.two, R.id.three,
			R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight,
			R.id.nine, R.id.zero, R.id.star, R.id.pound};
		for(int id : buttonIds){
			((DialpadImageButton)findViewById(id)).setOnPressedListener(this);
		}
		findViewById(R.id.one).setOnLongClickListener(this);
		findViewById(R.id.zero).setOnLongClickListener(this);
		
		mDialButton = (ImageButton)findViewById(R.id.dialButton);
		if(mDialButton != null){
			mDialButton.setOnClickListener(this);
			mDialButton.setOnLongClickListener(this);
		}else{
			Log.e(TAG, "mDialButton is null");
		}
		
		mAnswerButton = (ImageButton)findViewById(R.id.answerButton);
		if(mAnswerButton != null){
			mAnswerButton.setOnClickListener(this);
			mAnswerButton.setOnLongClickListener(this);
		}else{
			Log.e(TAG, " mAnswerButton is null");
		}
		
		mEndCallButton = (ImageButton)findViewById(R.id.endCallButton);
		if(mEndCallButton != null){
			mEndCallButton.setOnClickListener(this);
			mEndCallButton.setOnLongClickListener(this);
		}else{
			Log.e(TAG, " mEndCallButton is null");
		}
		
		mDialpadViewButton = (ImageButton)findViewById(R.id.DialpadViewButton);
		if(mDialpadViewButton != null){
			mDialpadViewButton.setOnClickListener(this);
			mDialpadViewButton.setOnLongClickListener(this);
		}else{
			Log.e(TAG, " mDialpadViewButton is null");
		}

		
		mBackToCallButton = (ImageButton)findViewById(R.id.backToCallButton);
		if(mBackToCallButton != null){
			mBackToCallButton.setOnClickListener(this);
			mBackToCallButton.setOnLongClickListener(this);
		}else{
			Log.e(TAG, " mBackToCallButton is null");
		}

		mMicMuteButton = (ImageButton)findViewById(R.id.MuteMicButton);
		if(mMicMuteButton != null){
			mMicMuteButton.setOnClickListener(this);
			mMicMuteButton.setOnLongClickListener(this);
		}else{
			Log.e(TAG, " mMicMuteButton is null");
		}

		mConferenceButton = (ImageButton)findViewById(R.id.ConferenceButton);
		if(mConferenceButton != null){
			mConferenceButton.setOnClickListener(this);
		}else{
			Log.e(TAG, " mConferenceButton is null");
		}
		
		mCallTime = (TextView)findViewById(R.id.tvTelephoneInfo);
		mCallTime.setText("");
		
		// show call time
		mTimer = (Chronometer)findViewById(R.id.timer);
		
		// show phone number when in call
		mCallNumber = (TextView)findViewById(R.id.tvTelephoneNumber);
	}
	
	private void keyPressed(int keyCode) {
		String dtmfNumber = "";
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                playTone(ToneGenerator.TONE_DTMF_1, TONE_LENGTH_INFINITE);
                dtmfNumber = "1";
                break;
            case KeyEvent.KEYCODE_2:
                playTone(ToneGenerator.TONE_DTMF_2, TONE_LENGTH_INFINITE);
                dtmfNumber = "2";
                break;
            case KeyEvent.KEYCODE_3:
                playTone(ToneGenerator.TONE_DTMF_3, TONE_LENGTH_INFINITE);
                dtmfNumber = "3";
                break;
            case KeyEvent.KEYCODE_4:
                playTone(ToneGenerator.TONE_DTMF_4, TONE_LENGTH_INFINITE);
                dtmfNumber = "4";
                break;
            case KeyEvent.KEYCODE_5:
                playTone(ToneGenerator.TONE_DTMF_5, TONE_LENGTH_INFINITE);
                dtmfNumber = "5";
                break;
            case KeyEvent.KEYCODE_6:
                playTone(ToneGenerator.TONE_DTMF_6, TONE_LENGTH_INFINITE);
                dtmfNumber = "6";
                break;
            case KeyEvent.KEYCODE_7:
                playTone(ToneGenerator.TONE_DTMF_7, TONE_LENGTH_INFINITE);
                dtmfNumber = "7";
                break;
            case KeyEvent.KEYCODE_8:
                playTone(ToneGenerator.TONE_DTMF_8, TONE_LENGTH_INFINITE);
                dtmfNumber = "8";
                break;
            case KeyEvent.KEYCODE_9:
                playTone(ToneGenerator.TONE_DTMF_9, TONE_LENGTH_INFINITE);
                dtmfNumber = "9";
                break;
            case KeyEvent.KEYCODE_0:
                playTone(ToneGenerator.TONE_DTMF_0, TONE_LENGTH_INFINITE);
                dtmfNumber = "0";
                break;
            case KeyEvent.KEYCODE_POUND:
                playTone(ToneGenerator.TONE_DTMF_P, TONE_LENGTH_INFINITE);
                dtmfNumber = "#";
                break;
            case KeyEvent.KEYCODE_STAR:
                playTone(ToneGenerator.TONE_DTMF_S, TONE_LENGTH_INFINITE);
                dtmfNumber = "*";
                break;
            default:
                break;
        }

        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mDigits.onKeyDown(keyCode, event);

        // If the cursor is at the end of the text we hide it.
        final int length = mDigits.length();
        if (length == mDigits.getSelectionStart() && length == mDigits.getSelectionEnd()) {
            mDigits.setCursorVisible(false);
        }
        
        if(myBlueToothService != null){
        	BonovoBlueToothService.PhoneState phoneState = myBlueToothService.getPhoneState();
			if((BonovoBlueToothService.PhoneState.ACTIVE == phoneState)&&(dtmfNumber.length()>0)){
				myBlueToothService.BlueToothPhoneDTMF(dtmfNumber);
			}
        }
    }
	
	/**
     * Plays the specified tone for TONE_LENGTH_MS milliseconds.
     */
    private void playTone(int tone) {
        playTone(tone, TONE_LENGTH_MS);
    }

    /**
     * Play the specified tone for the specified milliseconds
     *
     * The tone is played locally, using the audio stream for phone calls.
     * Tones are played only if the "Audible touch tones" user preference
     * is checked, and are NOT played if the device is in silent mode.
     *
     * The tone length can be -1, meaning "keep playing the tone." If the caller does so, it should
     * call stopTone() afterward.
     *
     * @param tone a tone code from {@link ToneGenerator}
     * @param durationMs tone length.
     */
    private void playTone(int tone, int durationMs) {
        // if local tone playback is disabled, just return.
        if (!mDTMFToneEnabled) {
        	if(DEBUG) Log.d(TAG, "====== playTone() 00");
            return;
        }

        // Also do nothing if the phone is in silent mode.
        // We need to re-check the ringer mode for *every* playTone()
        // call, rather than keeping a local flag that's updated in
        // onResume(), since it's possible to toggle silent mode without
        // leaving the current activity (via the ENDCALL-longpress menu.)
        AudioManager audioManager =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
            || (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
            return;
        }

        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                Log.w(TAG, "playTone: mToneGenerator == null, tone: " + tone);
                return;
            }
            // Start the new tone (will stop any playing tone)
            mToneGenerator.startTone(tone, durationMs);
        }
    }

    /**
     * Stop the tone if it is played.
     */
    private void stopTone() {
        // if local tone playback is disabled, just return.
        if (!mDTMFToneEnabled) {
            return;
        }
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                Log.w(TAG, "stopTone: mToneGenerator == null");
                return;
            }
            mToneGenerator.stopTone();
        }
    }
    
    private Bitmap getCircleBitmap(Bitmap bitmap) {
    	 final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    	 final Canvas canvas = new Canvas(output);

    	 final int color = Color.RED;
    	 final Paint paint = new Paint();
    	 final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    	 final RectF rectF = new RectF(rect);

    	 paint.setAntiAlias(true);
    	 canvas.drawARGB(0, 0, 0, 0);
    	 paint.setColor(color);
    	 canvas.drawOval(rectF, paint);

    	 paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
    	 canvas.drawBitmap(bitmap, rect, rect, paint);

    	 bitmap.recycle();

    	 return output;
    	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		// TODO Auto-generated method stub
		if(DEBUG) Log.d(TAG, "onAudioFocusChange focusChange: " + focusChange);
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			break;
		case AudioManager.AUDIOFOCUS_LOSS:
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			break;

		default:
			break;
		}
	}
}
