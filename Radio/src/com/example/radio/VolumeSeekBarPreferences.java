package com.example.radio;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class VolumeSeekBarPreferences extends DialogPreference implements
		OnSeekBarChangeListener {
	private static final String PREFERENCE_NS = "http://schemas.android.com/apk/res/com.example.radio";
	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

	private static final String ATTR_DEFAULT_VALUE = "defaultValue";
	private static final String ATTR_MIN_VALUE = "minValue";
	private static final String ATTR_MAX_VALUE = "maxValue";
	private final int REMOVE_DIALOG_TIME = 2000;

	private int mMinValue;
	private int mMaxValue;
	private int mDefaultValue;
	private int mCurrentValue;

	private SeekBar mSeekBar;
	private TextView mValueText;
	private ImageView imageView;
	private Context mContext;
	private seekBarCallBack back;
	private Handler handler;
	private Dialog dialog;

	public VolumeSeekBarPreferences(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		mMinValue = attrs
				.getAttributeIntValue(PREFERENCE_NS, ATTR_MIN_VALUE, 0);
		mMaxValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MAX_VALUE,
				100);
		mDefaultValue = attrs.getAttributeIntValue(ANDROID_NS,
				ATTR_DEFAULT_VALUE, 50);
	}

	@Override
	protected View onCreateDialogView() {
		// TODO Auto-generated method stub
		// Get current value from settings
		mCurrentValue = getPersistedInt(mDefaultValue);

		// Inflate layout
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.custom_seekbar_volume, null);

		// // Put minimum and maximum
		// ((TextView) view.findViewById(R.id.text1)).setText(Integer
		// .toString(mMinValue));
		// ((TextView) view.findViewById(R.id.text2)).setText(Integer
		// .toString(mMaxValue));

		// Setup SeekBar
		mSeekBar = (SeekBar) view.findViewById(R.id.seekbar1);
		mSeekBar.setMax(mMaxValue - mMinValue);
		mSeekBar.setProgress(back.getSeekBarVolume());
		mSeekBar.setOnSeekBarChangeListener(this);

		// Put current value
		// Now is Gone!!!
		mValueText = (TextView) view.findViewById(R.id.text3);
		mValueText.setText(Integer.toString(mCurrentValue));

		imageView = (ImageView) view.findViewById(R.id.image);
		if (back.getSeekBarVolume() == 0) {
			imageView.setBackgroundResource(R.drawable.ic_volume_off);
		} else {
			imageView.setBackgroundResource(R.drawable.ic_volume);
		}

		handler = new Handler();
		handler.removeCallbacks(thread);
		handler.postDelayed(thread, REMOVE_DIALOG_TIME);

		return view;
	}

	@Override
	protected void showDialog(Bundle state) {
		// TODO Auto-generated method stub
		Context context = getContext();

		AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
		// AlertDialog.Builder mBuilder = new AlertDialog.Builder(context)
		// .setTitle("音量调节");

		View contentView = onCreateDialogView();
		if (contentView != null) {
			onBindDialogView(contentView);
			mBuilder.setView(contentView);
		} else {
			mBuilder.setMessage("No Message");
		}

		onPrepareDialogBuilder(mBuilder);

		// Create the dialog
		dialog = mBuilder.create();
		if (state != null) {
			dialog.onRestoreInstanceState(state);
		}
		dialog.setOnDismissListener(this);
		dialog.getWindow().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		mCurrentValue = progress + mMinValue;
		back.setSeekBarVolume(mCurrentValue);
		// mValueText.setText(Integer.toString(mCurrentValue)+"%");
		if (mCurrentValue == 0) {
			imageView.setBackgroundResource(R.drawable.ic_volume_off);
		} else {
			imageView.setBackgroundResource(R.drawable.ic_volume);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		if (dialog != null) {
			handler.removeCallbacks(thread);
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		if (dialog != null) {
			handler.removeCallbacks(thread);
			handler.postDelayed(thread, REMOVE_DIALOG_TIME);
		}

	}
	
	/*
	 * 通过handle发送延迟处理信息关闭dialog
	 */
	Thread thread = new Thread(new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			dialog.dismiss();
		}
	});


	public interface seekBarCallBack {
		public void setSeekBarVolume(int volume);

		public int getSeekBarVolume();
	}

	public void setSeekbarListener(seekBarCallBack barCallBack) {
		back = barCallBack;
	}
}
