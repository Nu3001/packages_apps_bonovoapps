package com.example.bonovomcu;

import com.example.bonovomcu.McuDialogFragment.CallbackMcu;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ViewFlipper;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

public class BonovoMcuActivity extends Activity implements OnClickListener,
		ServiceConnection,CallbackMcu {

	private final static String TAG = "McuActivity";
	private static final boolean DEBUG = false;

	private McuServicer mServicer;
	private ProgressDialog mProgressDialog;
	private FragmentManager fragmentManager;
	private FragmentTransaction transaction;
	private static final int DIALOG_Update = 1;

	private ViewFlipper mViewFilpper;
	private float startX;
	private int TOUCHDIS = 200; // Fingers sliding distance
	private Animation in_lefttoright; // Animation effects
	private Animation out_lefttoright;
	private Animation in_righttoleft;
	private Animation out_righttoleft;
	private Button switchBtnleft;
	private Button switchBtnRight;

	private Button mButton1; // 检查更新按钮
	private TextView mtextView1; // MCU版本号文本框
	private TextView mtextView2; // 系统版本号文本框
	private TextView mtextView3; // Android版本号文本框
	private CheckBox muteCheckBox; // 倒车静音按钮
	private CheckBox brightCheckBox; // 大灯背光调节按钮
	private CheckBox cameraCheckBox; // 倒车摄像头按钮
	private CheckBox volumeCheckBox; // 自动音量按钮
	private SeekBar seekBarBrigthness; // 背光拖动条
	private SeekBar seekBarVolume; // 音量拖动条

	public static boolean isMuteCheck; // 倒车静音标志
	public static boolean isCheckLight; // 大灯背光调节标志
	public static boolean isCheckCamera; // 大灯背光调节标志
	public static boolean isCheckVolume; // 自动音量标志
	public static int progessBrigthness; // 保存大灯背光调节滑动条数值
	public static int progessVolume; // 保存大灯背光调节滑动条数值

	private int NO_LIGTHNESS = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (DEBUG)
			Log.v(TAG, "BonovoMcuActivity onCreate()!!!");

		in_lefttoright = AnimationUtils.loadAnimation(this,
				R.anim.enter_lefttoright); // Load the anim folder directory of
											// animation effects
		out_lefttoright = AnimationUtils.loadAnimation(this,
				R.anim.out_lefttoright);

		in_righttoleft = AnimationUtils.loadAnimation(this,
				R.anim.enter_righttoleft);
		out_righttoleft = AnimationUtils.loadAnimation(this,
				R.anim.out_righttoleft);
		mViewFilpper = (ViewFlipper) findViewById(R.id.viewFlipper);

		Intent mcuIntent = new Intent("com.example.McuServicer");
		this.bindService(mcuIntent, this, BIND_AUTO_CREATE);
		setview();
		//add fragmentMCU
		fragmentManager = getFragmentManager();
	}

	private void setview() {
		mButton1 = (Button) findViewById(R.id.button1);
		mButton1.setOnClickListener(this);
		switchBtnleft = (Button) findViewById(R.id.button2);
		switchBtnleft.setOnClickListener(this);
		switchBtnRight = (Button) findViewById(R.id.button3);
		switchBtnRight.setOnClickListener(this);

		// init SeekBar
		seekBarBrigthness = (SeekBar) findViewById(R.id.seekBarLight);
		seekBarBrigthness.setMax(80);
		// acquire the mProgess
		SharedPreferences sharedPreferences = getSharedPreferences(
				"MUTE_STATE", 0);
		progessBrigthness = sharedPreferences.getInt("Progress", 0);

		seekBarBrigthness.setProgress(progessBrigthness);
		seekBarBrigthness
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						SharedPreferences setting4 = getSharedPreferences(
								"MUTE_STATE", 0);
						progessBrigthness = setting4.getInt("Progress", 0);
						Log.d(TAG, "stop progress is" + progessBrigthness);
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						// TODO Auto-generated method stub
						// Toast.makeText(BonovoMcuActivity.this,
						// "myu ProgressChanged is "+progress,
						// Toast.LENGTH_SHORT).show();
						SharedPreferences setting = getSharedPreferences(
								"MUTE_STATE", 0);
						setting.edit().putInt("Progress", progress).commit();

						if (isCheckLight == true /* && mServicer.lightState == 1 */) {
							mServicer.lowBrightness(progress + 10);
						}
					}
				});// end about seekBarBrigthness

		muteCheckBox = (CheckBox) findViewById(R.id.checkBox1);
		// save the muteCheckBox state in SharedPreferences
		SharedPreferences setting5 = getSharedPreferences("MUTE_STATE", 0);
		isMuteCheck = setting5.getBoolean("Check", true);

		muteCheckBox.setChecked(isMuteCheck);
		muteCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					isMuteCheck = isChecked;
					SharedPreferences setting = getSharedPreferences(
							"MUTE_STATE", 0);
					setting.edit().putBoolean("Check", isChecked).commit();
					mServicer.setAsternMute(true);
				} else {
					isMuteCheck = isChecked;
					SharedPreferences setting = getSharedPreferences(
							"MUTE_STATE", 0);
					setting.edit().putBoolean("Check", isChecked).commit();
					mServicer.setAsternMute(false);
				}
			}
		});// end about muteCheckBox

		brightCheckBox = (CheckBox) findViewById(R.id.checkBox2);
		// save the brightCheckBox state in SharedPreferences
		SharedPreferences setting2 = getSharedPreferences("MUTE_STATE", 0);
		isCheckLight = setting2.getBoolean("Check2", false);

		brightCheckBox.setChecked(isCheckLight);
		if (isCheckLight) {
			seekBarBrigthness.setVisibility(View.VISIBLE);
		} else {
			seekBarBrigthness.setVisibility(View.GONE);
		}
		brightCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						if (isChecked) {
							isCheckLight = isChecked;
							SharedPreferences setting = getSharedPreferences(
									"MUTE_STATE", 0);
							setting.edit().putBoolean("Check2", isChecked)
									.commit();
							seekBarBrigthness.setVisibility(View.VISIBLE);
							mServicer.lowBrightness(progessBrigthness + 10);
						} else {
							isCheckLight = isChecked;
							int mSysBrightness = -1;
							try {
								mSysBrightness = Settings.System.getInt(
										getContentResolver(),
										Settings.System.SCREEN_BRIGHTNESS);// acquire
																			// system
																			// brigthness
								Log.d(TAG,
										"$%$%$%$%$%System sysBrightness&*&*&*&*&*&*& = "
												+ mSysBrightness);
							} catch (SettingNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							SharedPreferences setting = getSharedPreferences(
									"MUTE_STATE", 0);
							setting.edit().putBoolean("Check2", isChecked)
									.commit();
							seekBarBrigthness.setVisibility(View.GONE);
							// remove by myu 2014-7-25:
							// mServicer.setBrightness(mSysBrightness);
							mServicer.lowBrightness(0);
						}
					}
				});// end about brightCheckBox

		cameraCheckBox = (CheckBox) findViewById(R.id.checkBox3);
		// save the cameraCheckBox state in SharedPreferences
		SharedPreferences setting3 = getSharedPreferences("MUTE_STATE", 0);
		isCheckCamera = setting3.getBoolean("Check3", true);

		cameraCheckBox.setChecked(isCheckCamera);
		cameraCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						if (isChecked) {
							isCheckCamera = isChecked;
							SharedPreferences setting = getSharedPreferences(
									"MUTE_STATE", 0);
							setting.edit().putBoolean("Check3", isChecked)
									.commit();
							mServicer.setRearviewCamera(true);
						} else {
							isCheckCamera = isChecked;
							SharedPreferences setting = getSharedPreferences(
									"MUTE_STATE", 0);
							setting.edit().putBoolean("Check3", isChecked)
									.commit();
							mServicer.setRearviewCamera(false);
						}
					}
				});// end about cameraCheckBox

		seekBarVolume = (SeekBar) findViewById(R.id.seekBarAutoVolume);
		seekBarVolume.setMax(40);

		progessVolume = sharedPreferences.getInt("ProgressVolume", 0);
		seekBarVolume.setProgress(progessVolume);
		seekBarVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				SharedPreferences setting = getSharedPreferences("MUTE_STATE",
						0);
				setting.edit().putInt("ProgressVolume", progress).commit();

				 if(isCheckVolume == true /*&& mServicer.lightState == 1*/){
					 mServicer.volumePercent(progress + 10);
				 }
			}
		});// end about seekBarVolume

		volumeCheckBox = (CheckBox) findViewById(R.id.checkBoxvolume);
		isCheckVolume = sharedPreferences.getBoolean("checkVolume", false);

		volumeCheckBox.setChecked(isCheckVolume);
		if (isCheckVolume) {
			seekBarVolume.setVisibility(View.VISIBLE);
		} else {
			seekBarVolume.setVisibility(View.GONE);
		}
		volumeCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						if (isChecked) {
							isCheckVolume = isChecked;
							SharedPreferences sharedPreferences = getSharedPreferences(
									"MUTE_STATE", 0);
							sharedPreferences.edit()
									.putBoolean("checkVolume", isChecked)
									.commit();
							progessVolume = sharedPreferences.getInt("ProgressVolume", 0);
							seekBarVolume.setVisibility(View.VISIBLE);
							Log.v(TAG,"progessVolume is " + progessVolume);
							mServicer.volumePercent(progessVolume+10);
						} else {
							isCheckVolume = isChecked;
							SharedPreferences sharedPreferences = getSharedPreferences(
									"MUTE_STATE", 0);
							sharedPreferences.edit()
									.putBoolean("checkVolume", isChecked)
									.commit();
							seekBarVolume.setVisibility(View.GONE);
							mServicer.volumePercent(0);
						}
					}
				});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			startX = event.getX();
			if (false) {
				Log.d(TAG, "startX = " + startX);
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			float endX = event.getX();
			if (false) {
				Log.d(TAG, "endX = " + endX);
			}
			int distance = (int) endX - (int) startX;
			if (false) {
				Log.d(TAG, "distance = " + distance);
			}
			if (distance > TOUCHDIS) {
				mViewFilpper.setInAnimation(in_lefttoright);
				mViewFilpper.setOutAnimation(out_lefttoright);
				mViewFilpper.showNext(); // to second page
			} else if (distance < -TOUCHDIS) {
				mViewFilpper.setInAnimation(in_righttoleft);
				mViewFilpper.setOutAnimation(out_righttoleft);
				mViewFilpper.showPrevious();// return first page
			}
			return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		Message msg = mMcuHandler.obtainMessage(id);
		if (mMcuHandler != null) {
			mMcuHandler.sendMessage(msg);
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler mMcuHandler = new Handler() {
		@SuppressWarnings("deprecation")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.id.button1:
				if (DEBUG)
					Log.v(TAG, "button1 is worked!!!");
				if (mServicer.checkSDCard() && mServicer.checkFile()) {
//					Intent its = new Intent(BonovoMcuActivity.this,
//							DialogActivity.class);
//					its.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					BonovoMcuActivity.this.startActivity(its);
					McuDialogFragment mcuDialogFragment = new McuDialogFragment();
					transaction = fragmentManager.beginTransaction();
					transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);	//Standard of animation
					mcuDialogFragment.show(transaction, "df");
				} else {
					Toast.makeText(getApplicationContext(), R.string.loga,
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.button2:
				mViewFilpper.setInAnimation(in_righttoleft);
				mViewFilpper.setOutAnimation(out_righttoleft);
				mViewFilpper.showPrevious();
				break;
			case R.id.button3:
				mViewFilpper.setInAnimation(in_lefttoright);
				mViewFilpper.setOutAnimation(out_lefttoright);
				mViewFilpper.showNext();
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		if (DEBUG)
			Log.v(TAG, "onServiceConnected()");
		// TODO Auto-generated method stub
		mServicer = ((McuServicer.ServiceBinder) service).getServicer();
		mtextView1 = (TextView) findViewById(R.id.versionView);
		mtextView1.setText("V" + Integer.toString(mServicer.version()) + "版本");
		mtextView2 = (TextView) findViewById(R.id.systemView);
		mtextView2.setText(SystemProperties.get("ro.rksdk.version",
				Build.UNKNOWN));
		mtextView3 = (TextView) findViewById(R.id.androidView);
		mtextView3.setText(android.os.Build.VERSION.RELEASE);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		mServicer = null;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case DIALOG_Update:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setMessage("Please wait for a moment...");
			mProgressDialog.setIndeterminate(false);

			return mProgressDialog;
		}
		return null;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mServicer = null;
		this.unbindService(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public boolean checkSDCard() {
		// TODO Auto-generated method stub
		return mServicer.checkSDCard();
	}

	@Override
	public boolean checkFile() {
		// TODO Auto-generated method stub
		return mServicer.checkFile();
	}

	@Override
	public void cpyfile() {
		// TODO Auto-generated method stub
		mServicer.cpyfile();
	}

	@Override
	public boolean checkMcu() {
		// TODO Auto-generated method stub
		return mServicer.checkMcu();
	}

	@Override
	public int wipeMcuAPP() {
		// TODO Auto-generated method stub
		return mServicer.wipeMcuAPP();
	}

	@Override
	public boolean checkdBuffer() {
		// TODO Auto-generated method stub
		return mServicer.checkdBuffer();
	}

	@Override
	public void delMcuFile() {
		// TODO Auto-generated method stub
		mServicer.delMcuFile();
	}

	@Override
	public void rebootMcu() {
		// TODO Auto-generated method stub
		mServicer.rebootMcu();
	}

	@Override
	public int getLoopNum() {
		// TODO Auto-generated method stub
		return mServicer.loop;
	}

}
