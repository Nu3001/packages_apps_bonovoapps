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
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;
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
//	private View mDialMargin;
	private ImageButton mAnswerButton;
	private View mAnswerMargin;
	private ImageButton mEndCallButton;
	private View mEndCallMargin;
	private int mDialpadPressCount;
	private ViewStub mDialStub;
	private ViewStub mIncomingStub;
//	private View mDialView;
//	private View mIncomingView;
	private TextView mCallTime;
	private TextView mCallNumber;
	private Chronometer mTimer;
	private boolean mTimerIsCounting = false;
	private boolean mIsUserStarted = false;
	private static final int MSG_DIAL_HANG_UP = 0;
	private static final int MSG_DIAL_FINISH_ACTIVITY = 1;
	private static final long DELAY_TIME_HANG_UP = 2000;
	private static final long DELAY_TIME_FINISH = 1000;
	
	private AudioManager mAudioManger;
	private boolean mIsUseringAudio = false;

    private String getNameByNumber(Context context, String number){

        if(context == null)
            return null;

        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + number);
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{"display_name"}, null, null, null);
        String name = null;
        if(cursor.moveToFirst()){
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
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
						mCallTime.setText(R.string.description_phone_call_time);
						stopCallTimer();
						Message msg = mHandler.obtainMessage(MSG_DIAL_HANG_UP);
						mHandler.sendMessageDelayed(msg, DELAY_TIME_HANG_UP);
//						setIncomingView(false, false);
					}else if(BonovoBlueToothService.PhoneState.RINGING == phoneState){
						requestAudioFocus();
						setIncomingView(true, true);
						if(number != null) {
//							mDigits.setText(number);
                            String disp = getNameByNumber(mContext, number);
                            if(disp == null){
                                disp = number;
                            }
							mCallNumber.setText(disp);
							final Editable digits = mDigits.getText();
							digits.clear();
						}
						mCallTime.setText(R.string.description_phone_incoming);

					}else if(BonovoBlueToothService.PhoneState.DIALING == phoneState){
						requestAudioFocus();
						setIncomingView(true, false);
						if(number != null) {
//							mDigits.setText(number);
                            String disp = getNameByNumber(mContext, number);
                            if(disp == null){
                                disp = number;
                            }
							mCallNumber.setText(disp);
							final Editable digits = mDigits.getText();
							digits.clear();
						}
						mCallTime.setText(R.string.description_phone_dialing);
					}else if(BonovoBlueToothService.PhoneState.ACTIVE == phoneState){
						requestAudioFocus();
						setIncomingView(true, false);
						startCallTimer(myBlueToothService.getAnswerTime());
						mCallTime.setText(R.string.description_phone_in_call);
					}
				}
			}
		}
	};

	private IntentFilter getIntentFilter() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(BonovoBlueToothData.ACTION_PHONE_STATE_CHANGED);
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
				mCallTime.setText(R.string.description_phone_hang_up);
				setIncomingView(false, false);
				stopCallTimer();
				abandonAudioFocus();
			}else if(BonovoBlueToothService.PhoneState.RINGING == phoneState){
				setIncomingView(true, true);
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
			}else if(BonovoBlueToothService.PhoneState.DIALING == phoneState){
				setIncomingView(true, false);
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
			}else if(BonovoBlueToothService.PhoneState.ACTIVE == phoneState){
				setIncomingView(true, false);
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
				setIncomingView(false, false);
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
		
		mDigits = (EditText)findViewById(R.id.digits);
		mDelete = findViewById(R.id.deleteButton);
		if(mDelete != null){
			mDelete.setOnClickListener(this);
			mDelete.setOnLongClickListener(this);
		}
		
		mDialStub = (ViewStub)findViewById(R.id.dialViewStub);
//		mDialView = (View)findViewById(R.id.dialButtonContainer);
		mDialStub.inflate();
		mIncomingStub = (ViewStub)findViewById(R.id.incomingViewStub);
//		mIncomingView = (View)findViewById(R.id.incomingButtonContainer);
		mIncomingStub.inflate();
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
			setIncomingView(false, false);
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
				setIncomingView(false, false);
				stopCallTimer();
				abandonAudioFocus();
			}else if(BonovoBlueToothService.PhoneState.RINGING == phoneState){
				setIncomingView(true, true);
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
			}else if(BonovoBlueToothService.PhoneState.DIALING == phoneState){
				setIncomingView(true, false);
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
			}else if(BonovoBlueToothService.PhoneState.ACTIVE == phoneState){
				setIncomingView(true, false);
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
//			setIncomingView(false, false);
			if(myBlueToothService != null){
				if(BonovoBlueToothService.PhoneState.RINGING == myBlueToothService.getPhoneState()){
					myBlueToothService.BlueToothPhoneRejectCall();
				}else{
					myBlueToothService.BlueToothPhoneHangup();
				}
			}
			break;
		case R.id.answerButton:
//			mAnswerButton.setVisibility(View.GONE);
//			mAnswerMargin.setVisibility(View.GONE);
			if(myBlueToothService != null) {
				myBlueToothService.BlueToothPhoneAnswer();
				setIncomingView(true, false);
			}
			break;

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
	
	private boolean requestAudioFocus(){
		int result;
		if((!mIsUseringAudio)&& (mAudioManger != null)){
			result = mAudioManger.requestAudioFocus(this, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
			if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
				mIsUseringAudio = true;
			}
		}
		if(DEBUG) Log.d(TAG, "========== requestAudioFocus()  mIsUseringAudio:" + mIsUseringAudio);
		
		return mIsUseringAudio;
	}
	
	private boolean abandonAudioFocus(){
		if(mIsUseringAudio && (mAudioManger != null)){
			mAudioManger.abandonAudioFocus(null);
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
	
	private void setIncomingView(boolean visibility, boolean isAnswerShow){

		if(visibility){
			mIncomingStub.setVisibility(View.VISIBLE);
			mDialStub.setVisibility(View.GONE);
			if(isAnswerShow){
				if(mAnswerButton.getVisibility() != View.VISIBLE){
					mAnswerButton.setVisibility(View.VISIBLE);
				}
				if(mAnswerMargin.getVisibility() != View.VISIBLE) {
					mAnswerMargin.setVisibility(View.VISIBLE);
				}
			}else{
				if(mAnswerButton.getVisibility() != View.GONE){
					mAnswerButton.setVisibility(View.GONE);
				}
				if(mAnswerMargin.getVisibility() != View.GONE) {
					mAnswerMargin.setVisibility(View.GONE);
				}
			}
			if(mEndCallButton.getVisibility() != View.VISIBLE) {
				mEndCallButton.setVisibility(View.VISIBLE);
			}
			if(mEndCallMargin.getVisibility() != View.VISIBLE) {
				mEndCallMargin.setVisibility(View.VISIBLE);
			}
			if(mCallNumber.getVisibility() != View.VISIBLE){
				mCallNumber.setVisibility(View.VISIBLE);
			}
		}else{
			mIncomingStub.setVisibility(View.GONE);
			mDialStub.setVisibility(View.VISIBLE);
			mCallTime.setText("");
			mCallNumber.setText("");
			mTimer.setVisibility(View.GONE);
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
						setIncomingView(true, false);
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
//		mDialMargin = (View)findViewById(R.id.dialViewMargin);
		
		mAnswerButton = (ImageButton)findViewById(R.id.answerButton);
		if(mAnswerButton != null){
			mAnswerButton.setOnClickListener(this);
			mAnswerButton.setOnLongClickListener(this);
		}else{
			Log.e(TAG, " mAnswerButton is null");
		}
		mAnswerMargin = (View)findViewById(R.id.answerViewMargin);
		
		mEndCallButton = (ImageButton)findViewById(R.id.endCallButton);
		if(mEndCallButton != null){
			mEndCallButton.setOnClickListener(this);
			mEndCallButton.setOnLongClickListener(this);
		}else{
			Log.e(TAG, " mEndCallButton is null");
		}
		mEndCallMargin = (View)findViewById(R.id.endCallViewMargin);
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
