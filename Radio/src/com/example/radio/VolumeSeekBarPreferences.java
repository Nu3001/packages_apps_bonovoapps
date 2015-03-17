package com.example.radio;

import android.preference.DialogPreference;
import android.widget.SeekBar;
import android.app.Dialog;
import android.os.Handler;
import android.widget.ImageView;
import android.content.Context;
import android.widget.TextView;
import android.util.AttributeSet;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.Window;

public class VolumeSeekBarPreferences extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	private static final String ATTR_DEFAULT_VALUE = "defaultValue";
	private static final String ATTR_MAX_VALUE = "maxValue";
	private static final String ATTR_MIN_VALUE = "minValue";
	private static final String PREFERENCE_NS = "http://schemas.android.com/apk/res/com.example.radio";
	private VolumeSeekBarPreferences.seekBarCallBack back;
	private Dialog dialog;
	private Handler handler;
	private ImageView imageView;
	private Context mContext;
	private int mCurrentValue;
	private int mDefaultValue;
	private int mMaxValue;
	private int mMinValue;
	private SeekBar mSeekBar;
	private TextView mValueText;
	private final int REMOVE_DIALOG_TIME = 2000;
	Thread thread;

	public static interface seekBarCallBack
	{


		public abstract int getSeekBarVolume();


		public abstract void setSeekBarVolume(int i);

	}
	
	public VolumeSeekBarPreferences(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mMinValue = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/com.example.radio", "minValue", 0);
		mMaxValue = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/com.example.radio", "maxValue", 100);
		mDefaultValue = attrs.getAttributeIntValue("http://schemas.android.com/apk/res/android", "defaultValue", 50);
	}
	
	protected View onCreateDialogView() {
		mCurrentValue = getPersistedInt(mDefaultValue);
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService("layout_inflater");
		View view = inflater.inflate(R.layout.custom_seekbar_volume, null);
		mSeekBar = (SeekBar)view.findViewById(R.id.seekbar1);
		mSeekBar.setMax((mMaxValue - mMinValue));
		mSeekBar.setProgress(back.getSeekBarVolume());
		mSeekBar.setOnSeekBarChangeListener(this);
		mValueText = (TextView)view.findViewById(R.id.text3);
		mValueText.setText(Integer.toString(mCurrentValue));
		imageView = (ImageView)view.findViewById(R.id.image);
		if(back.getSeekBarVolume() == 0) {
			imageView.setBackgroundResource(R.drawable.ic_volume_off);
		} else {
			imageView.setBackgroundResource(R.drawable.ic_volume);
		}
		handler = new Handler();
		handler.removeCallbacks(thread);
		handler.postDelayed(thread, REMOVE_DIALOG_TIME);
		return view;
	}
	
	protected void showDialog(Bundle state) {
		Context context = getContext();
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
		View contentView = onCreateDialogView();
		if(contentView != null) {
			onBindDialogView(contentView);
			mBuilder.setView(contentView);
		} else {
			mBuilder.setMessage("No Message");
		}
		onPrepareDialogBuilder(mBuilder);
		dialog = mBuilder.create();
		if(state != null) {
			dialog.onRestoreInstanceState(state);
		}
		dialog.setOnDismissListener(this);
		dialog.getWindow().addFlags(0x10000000);
		dialog.setCancelable(false);
		dialog.show();
	}
	
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		mCurrentValue = (mMinValue + progress);
		back.setSeekBarVolume(mCurrentValue);
		if(mCurrentValue == 0) {
			imageView.setBackgroundResource(R.drawable.ic_volume_off);
		} else {
			imageView.setBackgroundResource(R.drawable.ic_volume);
		}
	}
	
	public void onStartTrackingTouch(SeekBar seekBar) {
		if(dialog != null) {
			handler.removeCallbacks(thread);
		}
	}
	
	public void onStopTrackingTouch(SeekBar seekBar) {
		if(dialog != null) {
			handler.removeCallbacks(thread);
			handler.postDelayed(thread, REMOVE_DIALOG_TIME);
		}
	}
	Thread thread = (new Runnable() {
		
		public void run() {
			dialog.dismiss();
		}
	});
	
	public void setSeekbarListener(VolumeSeekBarPreferences.seekBarCallBack barCallBack) {
		back = barCallBack;
	}
}
